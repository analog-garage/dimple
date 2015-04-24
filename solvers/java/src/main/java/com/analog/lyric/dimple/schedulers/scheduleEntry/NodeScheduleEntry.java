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
import com.analog.lyric.dimple.model.variables.Variable;



/**
 * @author jeffb
 * 
 *         A schedule entry that contains an entire node. The update method
 *         updates the entire node (all edges).
 */
public class NodeScheduleEntry implements IScheduleEntry
{
	private final INode _node;
	
	public NodeScheduleEntry(INode node)
	{
		_node = node;
	}
	
	public INode getNode()
	{
		return _node;
	}
	
	@Override
	public @Nullable IScheduleEntry copy(Map<Object,Object> old2newObjs, boolean copyToRoot)
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
			INode newNode = (INode)old2newObjs.get(node);
			return new NodeScheduleEntry(newNode);
		
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @return parent of {@link #getNode() node}.
	 */
	@Override
	public @Nullable FactorGraph getParentGraph()
	{
		return _node.getParentGraph();
	}

	@Override
	public Iterable<? extends INode> getNodes()
	{
		return Collections.singletonList(_node);
	}
	
	@Override
	public String toString()
	{
		return String.format("[NodeScheduleEntry %s]", getNode().getName());
	}
	
	@Override
	public Type type()
	{
		return Type.NODE;
	}
}
