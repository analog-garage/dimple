/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.model.core;

import static java.util.Objects.*;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.MapList;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

public abstract class Node extends DimpleOptionHolder implements INode
{
	/*-----------
	 * Constants
	 */
	
    /**
     * {@link #_topologicalFlags} value used by {@link #isMarked()}
     */
	private static final int MARKED = 0x10000000;
	
	/**
	 * {@link #_topologicalFlags} value used by {@link #wasVisited()}
	 */
	private static final int VISITED = 0x20000000;
	
	/**
	 * Flags that are reserved for use by this class and should not be
	 * used by subclasses when invoking {@link #setFlags(int)} or {@link #clearFlags()}.
	 */
	protected static final int RESERVED = 0xFF000000;
	
	/*-------
	 * State
	 */
	
	private int _id;
	protected @Nullable String _name;
	private @Nullable FactorGraph _parentGraph;
	
	/**
	 * Identifies the edges that connect to this node.
	 * <p>
	 * Contains integer indexes into the parent graph's edge list.
	 */
	private final IntArrayList _siblingEdges = new IntArrayList();
	
	/**
	 * Temporary flags that can be used to mark the node during the execution of various algorithms
	 * or to mark non-static attributes of the node.
	 * <p>
	 * The flags are automatically cleared by {@link #initialize()}.
	 */
	protected int _flags;
	
	// FIXME - eliminate this if possible
	/**
	 * Reverse mapping of sibling to its index. Created lazily as needed.
	 */
	private @Nullable HashMap<INode,Integer> _siblingToIndex = null;

	private class SiblingEdgeStateIterator extends UnmodifiableIterator<EdgeState>
	{
		private final int _size = getSiblingCount();
		private int _index;
		
		@Override
		public boolean hasNext()
		{
			return _index < _size;
		}
		
		@Override
		public EdgeState next()
		{
			return getSiblingEdgeState(_index++);
		}
	}
	
	private class SiblingEdgeStateIterable extends AbstractCollection<EdgeState>
	{
		@Override
		public Iterator<EdgeState> iterator()
		{
			return new SiblingEdgeStateIterator();
		}

		@Override
		public int size()
		{
			return getSiblingCount();
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	@Internal
	protected Node(int id)
	{
		_id = id;
	}
	
	protected Node(Node other)
	{
		super(other);
		_id = other._id | NodeId.LOCAL_ID_INDEX_MAX;
		_name = other._name;
	}
	
	/*----------------------------
	 * IDimpleEventSource methods
	 */
	
	@Override
	public @Nullable FactorGraph getContainingGraph()
	{
		return _parentGraph;
	}
	
	@Override
	public @Nullable IDimpleEventSource getEventParent()
	{
		return _parentGraph;
	}
	
	@Override
	public String getEventSourceName()
	{
		// FIXME - determine what this should be
		return toString();
	}

	@Override
	public Node getModelEventSource()
	{
		return this;
	}
	
    @Override
    public void notifyListenerChanged()
    {
    	clearFlags(getEventMask());
    }

    /*----------------------------
     * IDimpleEventSource methods
     */
	/*---------------
	 * INode methods
	 */
	
    // FIXME - give this a better name! e.g. getReverseSiblingEdgeNumber
	@Override
	public int getReverseSiblingNumber(int index)
	{
		final EdgeState edge = getSiblingEdgeState(index);
		return isVariable() ? edge._factorToVariableEdgeNumber : edge._variableToFactorEdgeNumber;
	}
	
	@Override
	public @Nullable Factor asFactor() { return null; }
	@Override
	public @Nullable FactorGraph asFactorGraph() { return null; }
	@Override
	public @Nullable Variable asVariable() { return null;}
	
	@Override
	public boolean isFactor() { return false; }
	@Override
	public boolean isFactorGraph() { return false; }
	@Override
	public boolean isVariable() { return false; }

	/**
	 * Returns newly allocated list of ancestor graphs from the root down to the parent of this node.
	 */
	public List<FactorGraph> getAncestors()
	{
		LinkedList<FactorGraph> ancestors = new LinkedList<FactorGraph>();
		
		FactorGraph ancestor = this.getParentGraph();
		
		while (ancestor != null)
		{
			ancestors.addFirst(ancestor);
			ancestor = ancestor.getParentGraph();
		}
		
		return ancestors;
	}
	
	@Override
	public @Nullable FactorGraph getAncestorAtHeight(int height)
	{
		FactorGraph ancestor = this.getParentGraph();
		
		while (height-- > 0 && ancestor != null)
		{
			ancestor = ancestor.getParentGraph();
		}
		
		return ancestor;
	}
	
	/**
	 * Returns the closest common ancestor graph containing both this node and {@code other}
	 * or null if there isn't one.
	 * 
	 * @param other is another node with which to compare.
	 * @param uncommonAncestors if non-null, then any ancestors that are not in common will
	 * be added to this list in order from top to bottom.
	 * 
	 * @see #getCommonAncestor(Node)
	 */
	public @Nullable FactorGraph getCommonAncestor(Node other, @Nullable List<FactorGraph> uncommonAncestors)
	{
		// First try some common special cases to avoid computation of full path to the root.
		FactorGraph thisParent = getParentGraph();
		FactorGraph otherParent = other.getParentGraph();

		if (thisParent == otherParent)
		{
			return thisParent;
		}
		
		if (thisParent == null || otherParent == null)
		{
			return null;
		}

		if (this == otherParent)
		{
			return otherParent;
		}
		
		if (other == thisParent)
		{
			return thisParent;
		}
		
		Iterator<FactorGraph> theseAncestors = getAncestors().iterator();
		Iterator<FactorGraph> otherAncestors = other.getAncestors().iterator();
		
		FactorGraph ancestor = null;
		
		while (theseAncestors.hasNext() && otherAncestors.hasNext())
		{
			FactorGraph thisAncestor = theseAncestors.next();
			FactorGraph otherAncestor = otherAncestors.next();
			if (thisAncestor == otherAncestor)
			{
				ancestor = thisAncestor;
			}
			else
			{
				if (uncommonAncestors != null)
				{
					// Add remaining ancestors to set, if provided
					uncommonAncestors.add(thisAncestor);
					Iterators.addAll(uncommonAncestors, theseAncestors);
					uncommonAncestors.add(otherAncestor);
					Iterators.addAll(uncommonAncestors, otherAncestors);
				}
				break;
			}
		}
		
		return ancestor;
	}
	
	/**
	 * Returns the closest common ancestor graph containing both this node and {@code other}
	 * or null if there isn't one.
	 * 
	 * @see #getCommonAncestor(Node, List)
	 */
	public @Nullable FactorGraph getCommonAncestor(Node other)
	{
		return getCommonAncestor(other, null);
	}
	
	@Override
	public INode getConnectedNodeFlat(int portNum)
	{
		return getSibling(portNum);
	}

	
	@Override
	public List<? extends INode> getSiblings()
	{
		return new AbstractList<INode>()
		{
			@Override
			public Node get(int index)
			{
				return getSibling(index);
			}

			@Override
			public int size()
			{
				return getSiblingCount();
			}
		};
	}
	
	@Override
	public int getSiblingCount()
	{
		return _siblingEdges.size();
	}
	
	@Override
	public Node getSibling(int i)
	{
		return getSiblingEdgeState(i).getSibling(this);
	}

	@Override
	public IMapList<INode> getConnectedNodes()
	{
		return getConnectedNodesFlat();
	}
	
	@Override
	public INode getConnectedNode(int relativeDepth, int portNum)
	{
		if (relativeDepth < 0)
			relativeDepth = 0;
		
		int myDepth = getDepth();
		
		//int desiredDepth = siblingDepth - relativeDepth;
		
		int desiredDepth = myDepth+relativeDepth;
		
		//Avoid overflow
		if (desiredDepth < 0)
			desiredDepth = Integer.MAX_VALUE;
		
		INode node = getSibling(portNum);
		
		// TODO: Instead of computing depths, which is O(depth), could we instead
		// just look for matching parent. For example, if relativedDepth is zero
		// can we just walk through the sibling node's parents until we find a match
		// for the parent of the node for this side of the connection?
		
		for (int depth = node.getDepth(); depth > desiredDepth; --depth)
		{
			node = requireNonNull(node.getParentGraph());
		}

		return Objects.requireNonNull(node);
	}

	@Override
	public ArrayList<INode> getConnectedNodeAndParents(int index)
	{
		ArrayList<INode> retval = new ArrayList<INode>();
		
		INode n = getSibling(index);
		
		while (n != null)
		{
			retval.add(n);
			n = n.getParentGraph();
		}
		
		return retval;
	}

	
	@Override
	public IMapList<INode> getConnectedNodes(int relativeNestingDepth)
	{
    	MapList<INode> list = new MapList<INode>();

		for (int i = 0, end = getSiblingCount(); i < end; i++)
		{
			list.add(getConnectedNode(relativeNestingDepth,i));
		}
    	
		return list;
	}
	
	@Override
	public int getDepth()
	{
		int depth = 0;
		
		for (FactorGraph parent = this.getParentGraph(); parent != null; parent = parent.getParentGraph())
		{
			++depth;
		}
		
		return depth;
	}

	@Override
	public int getDepthBelowAncestor(FactorGraph ancestor)
	{
		int depth = 0;

		for (FactorGraph parent = this.getParentGraph(); parent != null; parent = parent.getParentGraph())
		{
			if (parent == ancestor)
			{
				return depth;
			}
			++depth;
		}

		return -depth - 1;
	}
	
	@Override
	public IMapList<INode> getConnectedNodesFlat()
	{
		return getConnectedNodes(Integer.MAX_VALUE);
	}
	
	@Override
	public IMapList<INode> getConnectedNodesTop()
	{
		return getConnectedNodes(0);
	}
	
	protected void setParentGraph(@Nullable FactorGraph parentGraph)
	{
		// TODO: combine with adding to owned list?
		_parentGraph = parentGraph;
	}
	
	/**
	 * Returns the graph that immediately contains this node, or null if node does not belong to any graph
	 * or this is the root graph.
	 * <p>
	 * @see #requireParentGraph()
	 */
	@Override
	public @Nullable FactorGraph getParentGraph()
	{
		return _parentGraph;
	}
	
	/**
	 * Returns the outermost graph containing this node. Returns the node itself if it is the root graph.
	 */
	@Override
	public @Nullable FactorGraph getRootGraph()
	{
		FactorGraph parent = _parentGraph;
		return parent != null ? parent.getRootGraph() : null;
	}
	
	@Override
	public Collection<Port> getPorts()
	{
		final int size = _siblingEdges.size();
		ArrayList<Port> ports = new ArrayList<Port>(size);
		for (int i = 0; i < size; i++ )
			ports.add(getPort(i));
		return ports;
	}
	
	@Override
	public boolean hasParentGraph()
	{
		return _parentGraph != null;
	}
	
	@Deprecated
	@Override
	public long getId()
	{
		return getGlobalId();
	}
	
	void setLocalId(int id)
	{
		_id = id;
	}
	
	/**
	 * A local identifier that uniquely identifies the node within its containing graph.
	 * <p>
	 * This id can be used to retrieve the node from its graph using {@link FactorGraph#getNodeByLocalId(int)}.
	 * <p>
	 * @since 0.08
	 * @see #getGlobalId()
	 * @see NodeId
	 */
	@Override
	public int getLocalId()
	{
		return _id;
	}
	
	/**
	 * A global identifier that uniquely identifies the node within the containing Dimple environment.
	 * <p>
	 * This id can be used to retrieve the node from its graph using {@link FactorGraph#getNodeByGlobalId(long)}
	 * or from the environment using {@link DimpleEnvironment#getNodeByGlobalId}.
	 * <p>
	 * @since 0.08
	 * @see #getLocalId()
	 * @see NodeId
	 */
	@Override
	public long getGlobalId()
	{
		final FactorGraph parent = _parentGraph;
		return NodeId.globalIdFromParts(parent != null ? parent.getGraphId() : 0, _id);
	}
	
	@Override
	public long getGraphTreeId()
	{
		final FactorGraph parent = _parentGraph;
		return NodeId.graphTreeIdFromParts(parent != null ? parent.getGraphTreeIndex() : 0, _id);
	}
	
	@Override
	public void setName(@Nullable String name)
	{
		// TODO restrict name to valid Java identifier
		if(name != null && name.contains("."))
		{
			throw new DimpleException("ERROR '.' is not a valid character in names");
		}
		final FactorGraph parentGraph = _parentGraph;
		if(parentGraph != null)
		{
			parentGraph.setChildNameInternal(this, name);
		}

		this._name = name;
	}
	
	
	@Override
	public void setLabel(@Nullable String name)
	{
		if (name != null)
		{
			setOption(DimpleOptions.label, name);
		}
		else
		{
			unsetOption(DimpleOptions.label);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * For regular nodes implicitly generated name will be computed
	 * by {@link NodeId#defaultNameForLocalId(int)} using the
	 * value of {@link #getId()}.
	 */
	@Override
	public String getName()
	{
		String name = _name;
		return name != null ? name : NodeId.defaultNameForLocalId(_id);
	}
	
    @Override
	public UUID getUUID()
	{
    	return NodeId.makeUUID(getEnvironment().getEnvId(), getGlobalId());
	}
    
    abstract public String getClassLabel();
    
	@Override
	public String getQualifiedName()
	{
		StringBuilder sb = new StringBuilder();
		buildQualifiedName(sb);
		return sb.toString();
	}
	
	protected void buildQualifiedName(StringBuilder sb)
	{
		final FactorGraph parent = getParentGraph();
		if (parent != null)
		{
			parent.buildQualifiedName(sb);
			sb.append('.');
		}
		sb.append(getName());
	}

	@Override
	public String getLabel()
	{
		String name = getOption(DimpleOptions.label);
		if (name == null)
		{
			name = getName();
		}
		return name;
	}
	
	@Override
	public String getQualifiedLabel()
	{
		String s = getLabel();
		final FactorGraph parentGraph = _parentGraph;
		if (parentGraph != null)
		{
			s = parentGraph.getQualifiedLabel() + "." + s;
		}
		return s;
	}
	@Override
	public @Nullable String getExplicitName()
	{
		return _name;
	}
	@Override
	public String toString()
	{
		return getLabel();
	}
	
	@Override
	public final int findSibling(INode node)
	{
		return findSibling(node, 0);
	}
	
	@Override
	public final int findSibling(INode node, int start)
	{
		for (int i = start, n = getSiblingCount(); i < n; ++i)
		{
			if (node == getSibling(i))
			{
				return i;
			}
		}
		
		return -1;
	}
	
	@Override
	@Deprecated
	public final int getPortNum(INode node)
	{
		int port = getPortNumNoThrow(node);
		if (port < 0)
		{
			throw new DimpleException("Nodes are not connected: " + this + " and " + node);
		}
		return port;
	}
	
	@Override
	public void initialize()
	{
		clearFlags();
	}
	
	@Deprecated
	@Override
	public void updateEdge(INode other)
	{
		int num = findSibling(other);
		updateEdge(num);
	}

	
	@Override
	public final boolean isConnected(INode node)
	{
		return getPortNumNoThrow(node) >= 0;
	}

	/*--------------
	 * Node methods
	 */
	
	/**
	 * Gets representation of i'th edge.
	 * <p>
	 * Note that unlike {@link #getSiblingEdgeState(int)}, this returns a temporary object
	 * that fully describes the edge. To avoid excess allocation do not use this method within
	 * inner loops.
	 * <p>
     * @param i should be between 0 (inclusive) and {@link #getSiblingCount()} (exclusive)
     * @since 0.08
     * @throws IndexOutOfBoundsException if {@code i} is not in range.
	 * @since 0.08
	 * @see #getSiblingEdgeState(int)
	 */
	@Override
	public Edge getSiblingEdge(int i)
	{
		return new Edge(requireNonNull(_parentGraph), getSiblingEdgeState(i));
	}
	
	/**
	 * Returns the graph index of the i'th edge connected to this node.
	 * @param i is a number in the range from 0 to {@link #getSiblingCount()} - 1.
	 * @return non-negative index in parent graph for the sibling edge, which can be used with
	 * {@link FactorGraph#getGraphEdgeState(int)} to retrieve the edge state.
	 * @since 0.08
	 */
	public final int getSiblingEdgeIndex(int i)
	{
		return _siblingEdges.get(i);
	}
	
    /**
     * Get state for i'th edge.
     * <p>
     * @param i should be between 0 (inclusive) and {@link #getSiblingCount()} (exclusive)
     * @since 0.08
     * @throws IndexOutOfBoundsException if {@code i} is not in range.
     */
	@SuppressWarnings("null")
	@Override
	public EdgeState getSiblingEdgeState(int i)
	{
		return requireParentGraph().getGraphEdgeState(_siblingEdges.get(i));
	}
	
	/**
	 * A view of the sibling edge state objects connected to this node.
	 * @since 0.08
	 */
	public Collection<EdgeState> getSiblingEdgeState()
	{
		return new SiblingEdgeStateIterable();
	}
	
	/**
	 * Returns the index of the edge state
	 * @param edge an edge attached to this node
	 * @return the index of the edge or -1 if edge is not currently attached.
	 * @since 0.08
	 */
	public int indexOfSiblingEdgeState(EdgeState edge)
	{
		return isVariable() ? edge._variableToFactorEdgeNumber : edge._factorToVariableEdgeNumber;
	}
	
	/**
	 * Returns parent {@link FactorGraph} or throws an exception if none.
	 * @since 0.08
	 * @throws IllegalStateException if {@link #getParentGraph()} is null.
	 */
	public FactorGraph requireParentGraph()
	{
		final FactorGraph parent = _parentGraph;
		
		if (parent != null)
		{
			return parent;
		}
		
		throw new IllegalStateException(String.format("Node '%s' does not belong to a graph.", this));
	}
	
	/*------------------
	 * Internal methods
	 */
	
    @Override
	@Internal
    public void clearMarked()
    {
    	clearFlags(MARKED);
    }
	
    @Override
	@Internal
    public void clearVisited()
    {
    	clearFlags(VISITED);
    }
	
    @Override
	@Internal
    public final boolean isMarked()
    {
    	return isFlagSet(MARKED);
    }
    
    @Override
	@Internal
    public final boolean wasVisited()
    {
    	return isFlagSet(VISITED);
    }
    
    @Override
	@Internal
    public final void setMarked()
    {
    	setFlags(MARKED);
    }
    
    @Override
	@Internal
    public final void setVisited()
    {
    	setFlags(VISITED);
    }
    
	/*-------------------
	 * Protected methods
	 */

    @Internal
	protected void addEdge(Factor factor, Variable variable)
	{
		requireNonNull(_parentGraph).addEdge(factor, variable);
	}
	
    @Internal
	protected void addSiblingEdgeState(EdgeState edge)
	{
		final int i = _siblingEdges.size();
		final HashMap<INode,Integer> siblingToIndex = _siblingToIndex;
		if (siblingToIndex != null)
		{
			siblingToIndex.put(edge.getSibling(this), i);
		}
		if (isVariable())
		{
			edge._variableToFactorEdgeNumber = i;
		}
		else
		{
			edge._factorToVariableEdgeNumber = i;
		}
		_siblingEdges.add(edge.edgeIndex(this));

		notifyConnectionsChanged();
	}
	
	@Internal
	protected void clearSiblingEdgeState()
	{
		_siblingEdges.clear();
		if (isVariable())
		{
			for (int j = _siblingEdges.size(); --j >= 0;)
			{
				getSiblingEdgeState(j)._variableToFactorEdgeNumber = -1;
			}
		}
		else
		{
			for (int j = _siblingEdges.size(); --j >= 0;)
			{
				getSiblingEdgeState(j)._factorToVariableEdgeNumber = -1;
			}
		}
		_siblingToIndex = null;
		notifyConnectionsChanged();
	}
	
	@Internal
	protected void removeSiblingEdge(EdgeState edge)
	{
		requireNonNull(_parentGraph).removeSiblingEdge(edge);
	}
	
	@Internal
	protected void removeSiblingEdgeState(EdgeState edge)
	{
		if (isVariable())
		{
			final int i = edge._variableToFactorEdgeNumber;
			_siblingEdges.remove(i);
			edge._variableToFactorEdgeNumber = -1;
			for (int j = _siblingEdges.size(); --j >= i;)
			{
				getSiblingEdgeState(j)._variableToFactorEdgeNumber = j;
			}
		}
		else
		{
			final int i = edge._factorToVariableEdgeNumber;
			_siblingEdges.remove(i);
			edge._factorToVariableEdgeNumber = -1;
			for (int j = _siblingEdges.size(); --j >= i;)
			{
				getSiblingEdgeState(j)._factorToVariableEdgeNumber = j;
			}
		}
		_siblingToIndex = null;
		notifyConnectionsChanged();
	}
	
	@Internal
	protected void replaceSiblingEdgeState(EdgeState oldEdge, EdgeState newEdge)
	{
		assert(isFactor());
		final int i = oldEdge._factorToVariableEdgeNumber;
		oldEdge._factorToVariableEdgeNumber = -1;
		_siblingEdges.set(i, newEdge.edgeIndex(this));
		newEdge._factorToVariableEdgeNumber = i;
		final HashMap<INode,Integer> siblingToIndex = _siblingToIndex;
		if (siblingToIndex != null)
		{
			final FactorGraph fg = requireParentGraph();
			siblingToIndex.remove(oldEdge.getVariable(fg));
			siblingToIndex.put(newEdge.getVariable(fg), i);
		}
		notifyConnectionsChanged();
	}
	
	/**
	 * Clear all flag values. Invoked automatically by {@link #initialize()}.
	 */
	protected void clearFlags()
	{
		_flags = 0;
	}
	
	/**
	 * Clear flags in given mask.
	 * <p>
	 * Subclasses should not use bits in the {@link #RESERVED} mask.
	 */
	protected void clearFlags(int mask)
	{
		_flags = BitSetUtil.clearMask(_flags, mask);
	}
	
	/**
	 * Return mask of flag bits that are used to determine whether to
	 * generate events. This is used by {@link #notifyListenerChanged()}
	 * to clear the specified flag bits. It is assumed that the value of
	 * all zeros indicates that the object needs to recompute its flags
	 * based on the listener.
	 * <p>
	 * The default implementation returns zero.
	 *
	 * @since 0.06
	 */
	protected int getEventMask()
	{
		return 0;
	}

	/**
	 * True if all of the bits in {@code mask} are set in the flags.
	 * <p>
	 * Subclasses should not use bits in the {@link #RESERVED} mask.
	 */
	protected boolean isFlagSet(int mask)
	{
		return BitSetUtil.isMaskSet(_flags, mask);
	}

	/**
	 * Invoked when a change is made to the siblings list.
	 * Subclasses may override this to clear cached state that
	 * was computed from the siblings. The default implementation
	 * does nothing.
	 * 
	 * @since 0.07
	 */
	protected void notifyConnectionsChanged()
	{
	}
	
	protected final void raiseEvent(@Nullable DimpleEvent event)
	{
		if (event != null)
		{
			final IDimpleEventListener listener = getEventListener();
			final boolean handled = listener != null && listener.raiseEvent(event);
			if (!handled)
			{
				// Listener configuration probably changed. Reconfigure source to
				// prevent further event creation.
				notifyListenerChanged();
			}
		}
	}
	
	/**
	 * Sets all of the bits in {@code mask} in the flags.
	 * <p>
	 * Subclasses should not use bits in the {@link #RESERVED} mask.
	 */
	protected final void setFlags(int mask)
	{
		_flags = BitSetUtil.setMask(_flags, mask);
	}
	
	/**
	 * Sets bits of flag specified by {@code mask} to {@code value}.
	 */
	@Internal
	protected final void setFlagValue(int mask, int value)
	{
		_flags = BitSetUtil.setMaskedValue(_flags, mask, value);
	}

	/*-----------------
	 * Private methods
	 */

	private int getPortNumNoThrow(INode node)
	{
		int nSiblings = _siblingEdges.size();
		
		HashMap<INode,Integer> siblingToIndex = _siblingToIndex;
		
		if (siblingToIndex == null && nSiblings > 10)
		{
			siblingToIndex = _siblingToIndex = new HashMap<>(nSiblings);
			for (int i = 0; i < nSiblings; ++i)
			{
				siblingToIndex.put(getSibling(i), i);
			}
		}
		
		if (siblingToIndex != null)
		{
			Integer index = siblingToIndex.get(node);
			return index != null ? index : -1;
		}
		else
		{
			for (int i = 0; i < _siblingEdges.size(); i++)
			{
				// FIXME: isConnected walks up parent chain. I don't think that is what
				// we want.
				if (isConnected(node,i))
					return i;
			}
		}
		return -1;
	}
	
	private boolean isConnected(INode node, int portIndex)
	{
		INode other = requireNonNull(getSibling(portIndex));
		
		if (other == node)
			return true;
		
		// FIXME: this logic does not seem right to me. This will return true if 'node' refers to a mutual
		// parent graph of the current node and the sibling. Is that what we want? Shouldn't we instead
		// have the caller of this method walk up this node's parent graphs and see if any of them are
		// a direct sibling of 'node'?
		
		for (FactorGraph parent; (parent = other.getParentGraph()) != null; other = parent)
		{
			if (parent == node)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/*--------------------
	 * Deprecated methods
	 */
	
	@Deprecated
	@Override
	public int getSiblingPortIndex(int siblingNumber)
	{
		return getReverseSiblingNumber(siblingNumber);
	}
}
