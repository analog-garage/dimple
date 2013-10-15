/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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
import java.util.UUID;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.MapList;

public abstract class Node implements INode, Cloneable
{
	private int _id;
	private UUID _UUID;
	protected String _name;
	protected String _label;
	private FactorGraph _parentGraph;
	protected ArrayList<INode> _siblings;
	private int [] _siblingIndices = new int[0];

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
	
	public int getSiblingPortIndex(int index)
	{
		if (_siblingIndices.length <= index)
		{		
			_siblingIndices = Arrays.copyOf(_siblingIndices, index+1);
			_siblingIndices[index] = _siblings.get(index).getPortNum(this);
		}
		
		return _siblingIndices[index];
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
	
	public INode getConnectedNodeFlat(int portNum)
	{
		return _siblings.get(portNum);
	}

	
	@Override
	public ArrayList<INode> getSiblings()
	{
		return _siblings;
	}

	@Override
	public IMapList<INode> getConnectedNodes()
	{
		return getConnectedNodesFlat();
	}
	
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

		for (int i = 0; i < getSiblings().size(); i++)
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
	}

	@Override
	public void setParentGraph(FactorGraph parentGraph) 
	{
		_parentGraph = parentGraph;
	}
	@Override
	public FactorGraph getParentGraph()
	{
		return _parentGraph;
	}
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
	public ArrayList<Port> getPorts()
	{
		ArrayList<Port> ports = new ArrayList<Port>();
		for (int i = 0; i < _siblings.size(); i++ )
			ports.add(new Port(this,i));
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
	public int getPortNum(INode node) 
	{
		for (int i = 0; i < _siblings.size(); i++)
		{
			if (isConnected(node,i))
				return i;
		}
		throw new DimpleException("Nodes are not connected: " + this + " and " + node);
	}
	
	@Override
	public void updateEdge(INode other)
	{
		int num = getPortNum(other);
		updateEdge(num);
	}

	
	public boolean isConnected(INode node)
	{
		for (int i = 0; i < _siblings.size(); i++)
		{
			if (isConnected(node,i))
				return true;
		}
		return false;
		
	}
	
	@Override
	public boolean isConnected(INode node, int portIndex)
	{
		INode other = _siblings.get(portIndex);
		
		if (other == node)
			return true;
		
		while (other.getParentGraph() != null)
		{
			other = other.getParentGraph();
			
			if (other == node)
				return true;
		}
		
		return false;
	}

}
