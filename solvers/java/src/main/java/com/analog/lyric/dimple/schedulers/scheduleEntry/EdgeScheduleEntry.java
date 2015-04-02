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

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;



/**
 * @author jeffb
 * 
 *         A schedule entry that contains a single edge. The update method
 *         updates just one edge of the specified node.
 */
public class EdgeScheduleEntry implements IScheduleEntry
{
	private final INode _node;
	private final int _portNum;
	
	public EdgeScheduleEntry(INode node, int portNum)
	{
		_node = node;
		_portNum = portNum;
	}
	
	/**
	 * @deprecated this method cannot correctly handle multiple connections between the same
	 * factor and variable. Instead use {@link #EdgeScheduleEntry(INode, int)} method.
	 */
	@Deprecated
	public EdgeScheduleEntry(INode node, INode other)
	{
		_node = node;
		_portNum = node.findSibling(other);
	}

	@Override
	public void update(SolverNodeMapping solvers)
	{
		solvers.getSolverNode(_node).updateEdge(_portNum);
	}
	
	@Override
	public void update()
	{
		_node.updateEdge(_portNum);
	}
	
	public INode getNode()
	{
		return _node;
	}
	
	public int getPortNum()
	{
		return _portNum;
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
			isBoundaryVariable = fg.isBoundaryVariable((Variable)node);
			
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
			return new EdgeScheduleEntry(newNode,this.getPortNum());
		
		}
		return null;
	}
	
	@Override
	public Iterable<Port> getPorts()
	{
		return Collections.singleton(_node.getPort(_portNum));
	}
	
	@Override
	public String toString()
	{
		return String.format("IScheduleEntry [%s] -> %d -> [%s]"
				,getNode().getLabel()
				,getPortNum()
				,getNode().getSibling(getPortNum()).getLabel());
	}
	
	@Override
	public Type type()
	{
		return Type.EDGE;
	}
}
