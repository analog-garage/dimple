/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class SubgraphScheduleEntry implements IScheduleEntry
{
	/*-------
	 * State
	 */
	
	private final FactorGraph _subgraph;
	
	/*--------------
	 * Construction
	 */
	
	public SubgraphScheduleEntry(FactorGraph subgraph)
	{
		_subgraph = subgraph;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.format("[SubgraphScheduleEntry %s]", _subgraph);
	}
	
	/*------------------------
	 * IScheduleEntry methods
	 */
	
	@Override
	public @Nullable IScheduleEntry copy(Map<Object, Object> old2newObjs, boolean copyToRoot)
	{
		FactorGraph subgraph = (FactorGraph)old2newObjs.get(_subgraph);
		if (subgraph != null)
		{
			return new SubgraphScheduleEntry(subgraph);
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @return parent graph of {@link #getSubgraph() subgraph}.
	 * @since 0.08
	 */
	@Override
	public @Nullable FactorGraph getParentGraph()
	{
		return _subgraph.getParentGraph();
	}
	
	@Override
	public Iterable<? extends INode> getNodes()
	{
		return Collections.singletonList(_subgraph);
	}
	
	@Override
	public Type type()
	{
		return IScheduleEntry.Type.SUBGRAPH;
	}

	/*-----------------------
	 * SubGraphEntry methods
	 */
	
	public final FactorGraph getSubgraph()
	{
		return _subgraph;
	}
	
	/**
	 * Returns schedule for corresponding solver subgraph
	 * <p>
	 * @param solverGraph should be a solver graph for the parent of the subgraph
	 * @since 0.08
	 */
	public final ISchedule getSubgraphSchedule(ISolverFactorGraph solverGraph)
	{
		return solverGraph.getSolverSubgraph(_subgraph).getSchedule();
	}
}
