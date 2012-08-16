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

package com.analog.lyric.dimple.model;

import java.util.ArrayList;
import java.util.UUID;

import com.analog.lyric.util.misc.MapList;

public abstract class Node implements INode, Cloneable
{
	private int _id;
	private UUID _UUID;
	protected String _name;
	protected String _label;
	private FactorGraph _parentGraph;
	protected ArrayList<Port> _ports;

	public Node()
	{
		init(NodeId.getNext(),
			 java.util.UUID.randomUUID(),
			 null,
			 null,
			 null,
			 new ArrayList<Port>());
	}
	public Node(int id)
	{
		init(id,
			 java.util.UUID.randomUUID(),
			 null,
			 null,
			 null,
			 new ArrayList<Port>());
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
			 new ArrayList<Port>());
	}
	public Node(int id,
			 UUID UUID,
			 String name,
			 String label,
			 FactorGraph parentGraph,
			 ArrayList<Port> ports)
	{
		init(id,
			 UUID,
			 name,
			 label,
			 parentGraph,
			 ports);
	}
		
	public void init(int id,
					 UUID UUID,
					 String name,
					 String label,
					 FactorGraph parentGraph,
					 ArrayList<Port> ports)
	{
		_id = id;
		_UUID = UUID;
		_name = name;
		_label = label;
		_ports = ports;
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
		
		n._ports = new ArrayList<Port>();	// Clear the ports in the clone
		n._id = NodeId.getNext();
		n._UUID = java.util.UUID.randomUUID();
		n._parentGraph = null;
		n._name = _name;
		
		return n;
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
	
	@Override
public ArrayList<Port> getPorts()
	{
		return _ports;
	}

	@Override
	public MapList<INode> getConnectedNodes()
	{
		return getConnectedNodesFlat();
	}

	@Override
	public MapList<INode> getConnectedNodes(int relativeNestingDepth)
	{
    	MapList<INode> list = new MapList<INode>();

		for (int i = 0; i < getPorts().size(); i++)
		{
	    	list.add(getPorts().get(i).getConnectedNode(relativeNestingDepth));
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
	public MapList<INode> getConnectedNodesFlat()
	{
		return getConnectedNodes(Integer.MAX_VALUE);
	}
	
	@Override
	public MapList<INode> getConnectedNodesTop()
	{
		return getConnectedNodes(0);
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
	public boolean hasParentGraph()
	{
		return _parentGraph != null;
	}
	
	public abstract double getEnergy() ;
	
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
		for (int i = 0; i < _ports.size(); i++)
		{
			if (_ports.get(i).isConnected(node))
				return i;
		}
		throw new DimpleException("Nodes are not connected: " + this + " and " + node);
	}
}
