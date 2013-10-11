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

package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.VariableBase;



/**
 * @author jeffb
 * 
 *         A schedule entry that contains an entire node. The update method
 *         updates the entire node (all edges).
 */
public class NodeScheduleEntry implements IScheduleEntry
{
	private INode _node;
	
	public NodeScheduleEntry(INode node)
	{
		_node = node;
	}
	
	public void update() 
	{
		_node.update();
	}

	public INode getNode()
	{
		return _node;
	}
	
	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, false);
	}
	public IScheduleEntry copyToRoot(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, true);
	}
	
	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs, boolean copyToRoot)
	{
		boolean skip = false;
		boolean isBoundaryVariable = false;
		
		
		if (this.getNode() instanceof VariableBase)
		{
			isBoundaryVariable = this.getNode().getParentGraph().getBoundaryVariables().contains((VariableBase)this.getNode());
			
			if(copyToRoot)
			{
				skip = isBoundaryVariable && 
				   this.getNode().getParentGraph().hasParentGraph();
			}
			else
			{
				skip = isBoundaryVariable;
			}
		}
		
		if (!skip)
		{
			INode newNode = (INode)old2newObjs.get(this.getNode());
			return new NodeScheduleEntry(newNode);
		
		}
		return null;
	}

	@Override
	public Iterable<Port> getPorts() 
	{
		ArrayList<Port> ports = new ArrayList<Port>();
		
		//Add each port of this node to the list.
		for (int index = 0; index < _node.getSiblings().size(); index++)
		{
			ports.add(new Port(_node,index));
		}
		return ports;
	}

	public String toString()
	{
		return String.format("IScheduleEntry [%s]"
				,getNode().getName());
	}
}
