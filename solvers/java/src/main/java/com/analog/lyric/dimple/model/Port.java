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


public class Port
{
	private INode _parent;
	private Port _sibling;
	private Object _inputMsg;
	private int _portId;
	
	public Port(INode parent,Port sibling, int id)
	{
		_parent = parent;
		_sibling = sibling;
		if (_sibling != null)
		{
			sibling._sibling = this;
		}
		_inputMsg = null;
		_portId = id;
	}
	public Port(INode parent, int id)
	{
		this(parent,null,id);
	}
	
	public void setId(int id)
	{
		_portId = id;
	}

	public void setInputMsg(Object value)
	{
		_inputMsg = value;
	}
	public Object getInputMsg()
	{
		return _inputMsg;
	}
	public void setOutputMsg(Object value)
	{
		_sibling.setInputMsg(value);
	}
	public Object getOutputMsg()
	{
		return _sibling.getInputMsg();
	}
	
	public boolean isConnected(INode node)
	{
		INode other = _sibling.getParent();
		
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
	
	public ArrayList<INode> getConnectedNodes()
	{
		ArrayList<INode> retval = new ArrayList<INode>();
		
		INode n = _sibling.getParent();
		
		while (n != null)
		{
			retval.add(n);
			n = n.getParentGraph();
		}
		
		return retval;
	}

	public INode getConnectedNode()
	{
		return getConnectedNodeFlat();
	}

	public INode getConnectedNode(int relativeDepth)
	{
		if (relativeDepth < 0)
			relativeDepth = 0;
		
		int myDepth = _parent.getDepth();
		
		//int desiredDepth = siblingDepth - relativeDepth;
		
		int desiredDepth = myDepth+relativeDepth;
		
		//Avoid overflow
		if (desiredDepth < 0)
			desiredDepth = Integer.MAX_VALUE;
		
		INode node = _sibling.getParent();
		
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
	
	public INode getConnectedNodeFlat()
	{
		return _sibling.getParent();
	}
	
	public INode getConnectedNodeTop()
	{
		return getConnectedNode(0);
	}
	
	public INode getParent()
	{
		return _parent;
	}
	public Port getSibling()
	{
		return _sibling;
	}
	public int getId()
	{
		return _portId;
	}
	
	public String getName()
	{
		return getParent().getLabel() + "_to_" + getConnectedNodeFlat().getLabel();
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
	
	public void setSibling(Port sibling)
	{
		_sibling = sibling;
	}
}
