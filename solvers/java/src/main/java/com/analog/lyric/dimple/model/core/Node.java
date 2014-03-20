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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import cern.colt.map.OpenIntIntHashMap;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.math.Utilities;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;
import com.analog.lyric.util.misc.MapList;
import com.google.common.collect.Iterators;

public abstract class Node implements INode, Cloneable
{
	/*-----------
	 * Constants
	 */
	
    /**
     * {@link #_topologicalFlags} value used by {@link #isMarked()}
     */
	private static final byte MARKED = 0x01;
	
	/**
	 * {@link #_topologicalFlags} value used by {@link #wasVisited()}
	 */
	private static final byte VISITED = 0x02;
	
	/**
	 * Flags that are reserved for use by this class and should not be
	 * used by subclasses when invoking {@link #setFlags(int)} or {@link #clearFlags()}.
	 */
	protected static final int RESERVED = 0xFFFFFF03;
	
	/*-------
	 * State
	 */
	
	private int _id;
	private UUID _UUID;
	protected String _name;
	protected String _label;
	private FactorGraph _parentGraph;
	
	private List<INode> _siblings;
	
	/**
	 * Temporary flags that can be used to mark the node during the execution of various algorithms
	 * or to mark non-static attributes of the node.
	 * <p>
	 * The flags are automatically cleared by {@link #initialize()}.
	 */
	private byte _flags;
	
	/**
	 * Maps sibling index to the inverse index from the corresponding sibling
	 * back to this node. Indices are stored by adding one, so that the value zero
	 * can be used to indicate values that have not been initialized.
	 */
	private int [] _siblingIndices = ArrayUtil.EMPTY_INT_ARRAY;
	
	/**
	 * Reverse mapping of sibling id to its index plus one. Created lazily as needed.
	 */
	private OpenIntIntHashMap _siblingToIndex = null;

	/*--------------
	 * Construction
	 */
	
	public Node()
	{
		init(NodeId.getNext(),
			 NodeId.getNextUUID(),
			 null,
			 null,
			 null,
			 new ArrayList<INode>());
	}
	public Node(int id)
	{
		init(id,
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
		init(id,
			 UUID,
			 name,
			 null,
			 null,
			 new ArrayList<INode>());
	}
	public Node(int id,
			 UUID UUID,
			 String name,
			 String label,
			 FactorGraph parentGraph,
			 ArrayList<INode> nodes)
	{
		init(id,
			 UUID,
			 name,
			 label,
			 parentGraph,
			 nodes);
	}
		
	public void init(int id,
					 UUID UUID,
					 String name,
					 String label,
					 FactorGraph parentGraph,
					 ArrayList<INode> siblings)
	{
		_id = id;
		_UUID = UUID;
		_name = name;
		_label = label;
		_siblings = siblings;
		_parentGraph = parentGraph;
	}

	@Override
	public Node clone()
	{
		/*******
		 * NOTE: Any derived class that defines instance variables that are
		 * objects (rather than primitive types) must implement clone(), which
		 * must first call super.clone(), and then deep-copy those instance
		 * variables to the clone.
		 *******/
		Node n = null;
		try {n = (Node)(super.clone());}
		catch (CloneNotSupportedException e) {e.printStackTrace();}
		
		n._siblings = new ArrayList<INode>();	// Clear the ports in the clone
		n._id = NodeId.getNext();
		n._UUID = NodeId.getNextUUID();
		n._parentGraph = null;
		n._name = _name;
		if (_label != null)
			n._label = _label;
		
		return n;
	}
	
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
	public Factor asFactor() { return null; }
	@Override
	public FactorGraph asFactorGraph() { return null; }
	@Override
	public VariableBase asVariable() { return null;}
	
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
	public FactorGraph getAncestorAtHeight(int height)
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
	public FactorGraph getCommonAncestor(Node other, List<FactorGraph> uncommonAncestors)
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
	public FactorGraph getCommonAncestor(Node other)
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
			node = node.getParentGraph();
		}

		return node;
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
		if (_siblingToIndex != null)
		{
			_siblingToIndex.put(node.getId(), _siblings.size());
		}
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
	}
	
	@Override
	public void disconnect(INode node)
	{
		disconnect(getPortNum(node));
	}

	@Override
	public void setParentGraph(FactorGraph parentGraph)
	{
		_parentGraph = parentGraph;
	}
	
	/**
	 * Returns the graph that immediately contains this node, or null if node does not belong to any graph
	 * or this is the root graph.
	 */
	@Override
	public FactorGraph getParentGraph()
	{
		return _parentGraph;
	}
	
	/**
	 * Returns the outermost graph containing this node. Returns the node itself if it is the root graph.
	 */
	@Override
	public FactorGraph getRootGraph()
	{
		FactorGraph root = _parentGraph;
		boolean more = root != null;
		while(more)
		{
			FactorGraph temp = root.getParentGraph();
			if(temp != null)
			{
				root = temp;
			}
			else
			{
				more = false;
			}
		}
		return root;
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
	public void setName(String name)
	{
		if(name != null && name.contains("."))
		{
			throw new DimpleException("ERROR '.' is not a valid character in names");
		}
		if(_parentGraph != null)
		{
			_parentGraph.setChildName(this, name);
		}

		this._name = name;
	}
	
	
	@Override
	public void setLabel(String name)
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
		if(_parentGraph != null)
		{
			_parentGraph.setChildUUID(this, newUUID);
		}

		_UUID = newUUID;
	}
    
    abstract public String getClassLabel();
    
	@Override
	public String getQualifiedName()
	{
		String s = getName();
		if(s != null && getParentGraph() != null)
		{
			s = getParentGraph().getQualifiedName() + "." + s;
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
		if(s != null && getParentGraph() != null)
		{
			s = _parentGraph.getQualifiedLabel() + "." + s;
		}
		return s;
	}
	@Override
	public String getExplicitName()
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
		_flags = (byte) BitSetUtil.clearMask(_flags, mask);
	}
	
	protected void clearSiblings()
	{
		_siblings.clear();
		_siblingIndices = ArrayUtil.EMPTY_INT_ARRAY;
		_siblingToIndex = null;
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
			if (_siblingToIndex != null)
			{
				_siblingToIndex.removeKey(oldNode.getId());
				_siblingToIndex.put(newNode.getId(), index + 1);
			}
		}
	}
	
	/**
	 * Sets all of the bits in {@code mask} in the flags.
	 * <p>
	 * Subclasses should not use bits in the {@link #RESERVED} mask.
	 */
	protected void setFlags(int mask)
	{
		_flags = (byte) BitSetUtil.setMask(_flags, mask);
	}
	
	/*-----------------
	 * Private methods
	 */

	private int getPortNumNoThrow(INode node)
	{
		int nSiblings = _siblings.size();
		
		if (_siblingToIndex == null && nSiblings > 10)
		{
			_siblingToIndex = new OpenIntIntHashMap(nSiblings);
			for (int i = 0; i < nSiblings; ++i)
			{
				_siblingToIndex.put(_siblings.get(i).getId(), i + 1);
			}
		}
		
		if (_siblingToIndex != null)
		{
			return _siblingToIndex.get(node.getId()) - 1;
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
		INode other = _siblings.get(portIndex);
		
		if (other == node)
			return true;
		
		// FIXME: this logic does not seem right to me. This will return true if 'node' refers to a mutual
		// parent graph of the current node and the sibling. Is that what we want? Shouldn't we instead
		// have the caller of this method walk up this node's parent graphs and see if any of them are
		// a direct sibling of 'node'?
		
		while (other.getParentGraph() != null)
		{
			other = other.getParentGraph();
			
			if (other == node)
				return true;
		}
		
		return false;
	}
	
}
