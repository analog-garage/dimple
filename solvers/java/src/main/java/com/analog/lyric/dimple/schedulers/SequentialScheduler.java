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

package com.analog.lyric.dimple.schedulers;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;

/**
 * @author jeffb
 *
 *         This class generates a sequential schedule on a graph. This schedule
 *         updates each factor sequentially. Prior to updating each factor, the
 *         edge from each neighboring variable to that factor is updated.
 *
 *         This scheduler respects any schedulers already assigned to
 *         sub-graphs. That is, if a sub-graph already has a scheduler
 *         associated with it, that scheduler will be used for that sub-graph
 *         instead of this one.
 *
 *         Implements setSubGraphScheduler method, which allows modifying the
 *         default scheduler used for sub-graphs. This is useful in cases where
 *         this scheduler is used as a fall-back to a different scheduler.
 */
public class SequentialScheduler extends BPSchedulerBase
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	protected Class<? extends IScheduler> _subGraphSchedulerClass = SequentialScheduler.class;

	/*--------------
	 * Construction
	 */
	
	public SequentialScheduler()
	{
	}
	
	SequentialScheduler(SequentialScheduler other)
	{
		_subGraphSchedulerClass = other._subGraphSchedulerClass;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public int hashCode()
	{
		return getClass().hashCode() + 13 * _subGraphSchedulerClass.hashCode();
	}
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		return obj instanceof SequentialScheduler &&
			((SequentialScheduler)obj)._subGraphSchedulerClass == _subGraphSchedulerClass;
	}
	
	/*----------------------
	 * IOptionValue methods
	 */

	/**
	 * This type of scheduler is mutable.
	 * @see #setSubGraphScheduler(Class)
	 */
	@Override
	public boolean isMutable()
	{
		return true;
	}
	
	/*--------------------
	 * IScheduler methods
	 */
	
	@Override
	public IScheduler copy(Map<Object, Object> old2NewMap, boolean copyToRoot)
	{
		return new SequentialScheduler(this);
	}
	
	@Override
	public ISchedule createSchedule(ISolverFactorGraph solverGraph)
	{
		return createSchedule(solverGraph.getModelObject(), solverGraph.getSolverMapping());
	}
	
	@Override
	public ISchedule createSchedule(FactorGraph graph)
	{
		return createSchedule(graph, null);
	}
	
	private ISchedule createSchedule(FactorGraph g, @Nullable SolverNodeMapping solverMap)
	{
		FixedSchedule schedule = new FixedSchedule(this, g);

		// Update all owned functions
		for (Factor f : g.getNonGraphFactorsTop())
		{
			// For each function, update variable edges connecting to that function
			for (int i = 0, end = f.getSiblingCount(); i < end; ++i)
			{
				final EdgeState edge = f.getSiblingEdgeState(i);
				schedule.add(new EdgeScheduleEntry(edge.getVariable(g), edge.getVariableToFactorEdgeNumber()));
			}

			// Then update the function
			schedule.add(new NodeScheduleEntry(f));
		}

		// Update all sub-graphs
		for (FactorGraph sg : g.getOwnedGraphs())
		{
			// For each sub-graph, update the variable edges connecting to that sub-graph
			for (int i = 0, end = sg.getSiblingCount(); i < end; ++i)
			{
				final EdgeState edge = sg.getSiblingEdgeState(i);
				schedule.add(new EdgeScheduleEntry(edge.getVariable(g), edge.getVariableToFactorEdgeNumber()));
			}

			// Then update the sub-graph
			IScheduler subscheduler = null;
			
			if (solverMap != null)
			{
				ISolverFactorGraph ssg = solverMap.getSolverGraphOrNull(sg);
				if (ssg != null)
				{
					subscheduler = ssg.getLocalOption(BPOptions.scheduler);
				}
			}
			
			if (subscheduler == null)
			{
				subscheduler = sg.getLocalOption(BPOptions.scheduler);
			}
			
			if (subscheduler == null)
			{
				// If scheduler has not been set on the solver or model subgraph, then use the
				// subgraph scheduler.
				subscheduler = SchedulerBase.instantiateClass(_subGraphSchedulerClass);
			}
			
			schedule.add(sg);
		}

		return schedule;
	}

	@SuppressWarnings("all")
	public void setSubGraphScheduler(Class<? extends IScheduler> schedulerClass)
	{
		_subGraphSchedulerClass = SchedulerBase.validateClass(schedulerClass);
	}

}
