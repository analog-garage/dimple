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

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Map;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Variable;
import org.eclipse.jdt.annotation.Nullable;



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
	
	@Override
	public void update()
	{
		_node.update();
	}

	public INode getNode()
	{
		return _node;
	}
	
	@Override
	public @Nullable IScheduleEntry copy(Map<Node,Node> old2newObjs)
	{
		return copy(old2newObjs, false);
	}
	@Override
	public @Nullable IScheduleEntry copyToRoot(Map<Node,Node> old2newObjs)
	{
		return copy(old2newObjs, true);
	}
	
	public @Nullable IScheduleEntry copy(Map<Node,Node> old2newObjs, boolean copyToRoot)
	{
		boolean skip = false;
		boolean isBoundaryVariable = false;
		
		final INode node = getNode();
		if (node instanceof Variable)
		{
			final FactorGraph fg = requireNonNull(node.getParentGraph());
			isBoundaryVariable = fg.getBoundaryVariables().contains(node);
			
			if(copyToRoot)
			{
				skip = isBoundaryVariable && fg.hasParentGraph();
			}
			else
			{
				skip = isBoundaryVariable;
			}
		}
		
		if (!skip)
		{
			INode newNode = old2newObjs.get(node);
			return new NodeScheduleEntry(newNode);
		
		}
		return null;
	}

	@Override
	public Iterable<Port> getPorts()
	{
		ArrayList<Port> ports = new ArrayList<Port>();
		
		//Add each port of this node to the list.
		for (int index = 0, end = _node.getSiblingCount(); index < end; index++)
		{
			ports.add(new Port(_node,index));
		}
		return ports;
	}

	@Override
	public String toString()
	{
		return String.format("IScheduleEntry [%s]"
				,getNode().getName());
	}
}
