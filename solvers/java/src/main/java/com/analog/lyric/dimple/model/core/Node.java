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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.map.OpenIntIntHashMap;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.math.Utilities;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.MapList;
import com.google.common.collect.Iterators;

public abstract class Node extends DimpleOptionHolder implements INode, Cloneable
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
	private UUID _UUID;
	protected @Nullable String _name;
	protected @Nullable String _label;
	private @Nullable FactorGraph _parentGraph;
	
	private List<INode> _siblings;
	
	/**
	 * Temporary flags that can be used to mark the node during the execution of various algorithms
	 * or to mark non-static attributes of the node.
	 * <p>
	 * The flags are automatically cleared by {@link #initialize()}.
	 */
	protected int _flags;
	
	/**
	 * Maps sibling index to the inverse index from the corresponding sibling
	 * back to this node. Indices are stored by adding one, so that the value zero
	 * can be used to indicate values that have not been initialized.
	 */
	private int [] _siblingIndices = ArrayUtil.EMPTY_INT_ARRAY;
	
	/**
	 * Reverse mapping of sibling id to its index plus one. Created lazily as needed.
	 */
	private @Nullable OpenIntIntHashMap _siblingToIndex = null;

	/*--------------
	 * Construction
	 */
	
	public Node()
	{
		this(NodeId.getNext(),
			 NodeId.getNextUUID(),
			 null,
			 null,
			 null,
			 new ArrayList<INode>());
	}
	public Node(int id)
	{
		this(id,
				NodeId.getNextUUID(),
			 null,
			 null,
			 null,
			 new ArrayList<INode>());
	}
	public Node(int id,
				 UUID UUID,
				 String name)
	{
		this(id,
			 UUID,
			 name,
			 null,
			 null,
			 new ArrayList<INode>());
	}
	public Node(int id,
			 UUID UUID,
			 @Nullable String name,
			 @Nullable String label,
			 @Nullable FactorGraph parentGraph,
			 ArrayList<INode> siblings)
	{
		_id = id;
		_UUID = UUID;
		_name = name;
		_label = label;
		_siblings = siblings;
		_parentGraph = parentGraph;
	}
		
	public void init(int id,
					 UUID UUID,
					 @Nullable String name,
					 @Nullable String label,
					 @Nullable FactorGraph parentGraph,
					 ArrayList<INode> siblings)
	{
		_id = id;
		_UUID = UUID;
		_name = name;
		_label = label;
		_siblings = siblings;
		_parentGraph = parentGraph;
		notifyConnectionsChanged();
	}

	@Override
	public @NonNull Node clone()
	{
		/*******
		 * NOTE: Any derived class that defines instance variables that are
		 * objects (rather than primitive types) must implement clone(), which
		 * must first call super.clone(), and then deep-copy those instance
		 * variables to the clone.
		 *******/
		try
		{
			final Node n = (Node)(super.clone());
		
			n._siblings = new ArrayList<INode>();	// Clear the ports in the clone
			n._id = NodeId.getNext();
			n._UUID = NodeId.getNextUUID();
			n._parentGraph = null;
			n._name = _name;
			if (_label != null)
				n._label = _label;

			return n;
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException(e); // should never happen
		}
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
	
	@Override
	public int getSiblingPortIndex(int index)
	{
		if (_siblingIndices.length <= index)
		{
			// Round up to next power of two no larger than the sibling size to avoid
			// thrashing if this is called for each index in order.
			int newSize = Math.min(_siblings.size(), Utilities.nextPow2(index + 1));
			_siblingIndices = Arrays.copyOf(_siblingIndices, newSize);
		}
		
		// Zero values in _siblingIndices indicates not yet initialized, thus the offset by one.
		
		int reverseIndex = _siblingIndices[index] - 1;
		
		INode sibling = _siblings.get(index);
		if (reverseIndex < 0 || sibling.getSibling(reverseIndex) != this)
		{
			// Update reverse index if it was not yet initialized or it points to the wrong node,
			// which can happen if nodes were removed.
			reverseIndex = sibling.getPortNum(this);
			_siblingIndices[index] = reverseIndex + 1;
		}
		
		return reverseIndex;
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
		return _siblings.get(portNum);
	}

	
	@Override
	public List<? extends INode> getSiblings()
	{
		return Collections.unmodifiableList(_siblings);
	}
	
	@Override
	public int getSiblingCount()
	{
		return _siblings.size();
	}
	
	@Override
	public INode getSibling(int i)
	{
		return _siblings.get(i);
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
		
		INode node = _siblings.get(portNum);
		
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
		
		INode n = _siblings.get(index);
		
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

	@Override
	public void connect(INode node)
	{
		_siblings.add(node);
		final OpenIntIntHashMap siblingToIndex = _siblingToIndex;
		if (siblingToIndex != null)
		{
			siblingToIndex.put(node.getId(), _siblings.size());
		}
		notifyConnectionsChanged();
	}
	
	@Override
	public void disconnect(int portNum)
	{
		_siblings.remove(portNum);
		_siblingToIndex = null;
		if (portNum < _siblingIndices.length)
		{
			_siblingIndices = ArrayUtil.EMPTY_INT_ARRAY;
		}
		notifyConnectionsChanged();
	}
	
	@Override
	public void disconnect(INode node)
	{
		disconnect(getPortNum(node));
	}

	@Override
	public void setParentGraph(@Nullable FactorGraph parentGraph)
	{
		_parentGraph = parentGraph;
	}
	
	/**
	 * Returns the graph that immediately contains this node, or null if node does not belong to any graph
	 * or this is the root graph.
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
	public Port getPort(int i)
	{
		return new Port(this, i);
	}
	
	@Override
	public ArrayList<Port> getPorts()
	{
		final int size = _siblings.size();
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
	
	
	@Override
	public int getId()
	{
		return _id;
	}
	
	@Override
	public void setName(@Nullable String name)
	{
		if(name != null && name.contains("."))
		{
			throw new DimpleException("ERROR '.' is not a valid character in names");
		}
		final FactorGraph parentGraph = _parentGraph;
		if(parentGraph != null)
		{
			parentGraph.setChildName(this, name);
		}

		this._name = name;
	}
	
	
	@Override
	public void setLabel(@Nullable String name)
	{
		_label = name;
	}

	@Override
	public String getName()
	{
		String name = _name;
		if(name == null)
		{
			name = _UUID.toString();
		}
		return name;
	}
    @Override
	public UUID getUUID()
	{
		return _UUID;
	}
	@Override
	public void setUUID(UUID newUUID)
	{
		final FactorGraph parentGraph = _parentGraph;
		if (parentGraph != null)
		{
			parentGraph.setChildUUID(this, newUUID);
		}

		_UUID = newUUID;
	}
    
    abstract public String getClassLabel();
    
	@Override
	public String getQualifiedName()
	{
		String s = getName();
		final FactorGraph parent = getParentGraph();
		if (parent != null)
		{
			s = parent.getQualifiedName() + "." + s;
		}
		return s;
	}

	@Override
	public String getLabel()
	{
		String name = _label;
		if(name == null)
		{
			name = _name;
			if(name == null)
			{
				name = String.format("%s_%d_%s"
						,getClassLabel()
						,getId()
						,_UUID.toString().substring(0, 8));
			}
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
	
	@Override
	public void updateEdge(INode other)
	{
		int num = getPortNum(other);
		updateEdge(num);
	}

	
	@Override
	public final boolean isConnected(INode node)
	{
		return getPortNumNoThrow(node) >= 0;
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
	
	protected void clearSiblings()
	{
		_siblings.clear();
		_siblingIndices = ArrayUtil.EMPTY_INT_ARRAY;
		_siblingToIndex = null;
		notifyConnectionsChanged();
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
	
	protected void replaceSibling(INode oldNode, INode newNode)
	{
		if (oldNode != newNode)
		{
			int index = getPortNum(oldNode);
			_siblings.set(index, newNode);
			if (_siblingIndices.length > index)
			{
				_siblingIndices[index] = 0;
			}
			final OpenIntIntHashMap siblingToIndex = _siblingToIndex;
			if (siblingToIndex != null)
			{
				siblingToIndex.removeKey(oldNode.getId());
				siblingToIndex.put(newNode.getId(), index + 1);
			}
			notifyConnectionsChanged();
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
		int nSiblings = _siblings.size();
		
		OpenIntIntHashMap siblingToIndex = _siblingToIndex;
		
		if (siblingToIndex == null && nSiblings > 10)
		{
			siblingToIndex = _siblingToIndex = new OpenIntIntHashMap(nSiblings);
			for (int i = 0; i < nSiblings; ++i)
			{
				siblingToIndex.put(_siblings.get(i).getId(), i + 1);
			}
		}
		
		if (siblingToIndex != null)
		{
			return siblingToIndex.get(node.getId()) - 1;
		}
		else
		{
			for (int i = 0; i < _siblings.size(); i++)
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
		INode other = requireNonNull(_siblings.get(portIndex));
		
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
	
}
