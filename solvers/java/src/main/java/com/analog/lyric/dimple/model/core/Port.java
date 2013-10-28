/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

public class Port
{
	public INode node;
	public int index;
	public Port(INode node, int index)
	{
		this.node = node;
		this.index = index;
	}
	
	@Override
	public int hashCode()
	{
		return node.hashCode()+index;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Port)
		{
			Port p = (Port)obj;
			return p.node == this.node && p.index == this.index;
		}
		return false;
	}
	
	@Override
	public String toString()
	{
		return node.toString() + " index: " + index;
	}
	
	public INode getConnectedNode()
	{
		return node.getSibling(index);
	}

	public void setInputMsgValues(Object obj)
	{
		node.getSolver().setInputMsgValues(index, obj);
	}

	public void setOutputMsgValues(Object obj)
	{
		node.getSolver().setOutputMsgValues(index,obj);
	}
	
	public Object getInputMsg()
	{
		return node.getSolver().getInputMsg(index);
	}
	public Object getOutputMsg()
	{
		return node.getSolver().getOutputMsg(index);
	}
	public INode getSibling()
	{
		return node.getSibling(index);
	}
	public Port getSiblingPort()
	{
		return new Port(getSibling(),node.getSiblingPortIndex(index));
	}
}
