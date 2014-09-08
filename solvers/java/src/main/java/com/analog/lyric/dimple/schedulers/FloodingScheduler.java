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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;

/**
 * @author jeffb
 * 
 *         The class generates a flooding schedule on a graph.
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
public class FloodingScheduler implements IScheduler
{
	@SuppressWarnings("all")
	protected Class _subGraphSchedulerClass = FloodingScheduler.class;

	
	@Override
	@SuppressWarnings("unchecked")
	public ISchedule createSchedule(FactorGraph factorGraph)
	{
		FixedSchedule schedule = new FixedSchedule();

		// Update all the variables
		for (Variable v : factorGraph.getVariablesTop())
			schedule.add(new NodeScheduleEntry(v));
		
		// Include boundary variables only if there's no parent to do it
		if (!factorGraph.hasParentGraph())
			for (Variable v : factorGraph.getBoundaryVariables())
				schedule.add(new NodeScheduleEntry(v));
		
		// Update all the function nodes
		for (Factor f : factorGraph.getNonGraphFactorsTop())
			schedule.add(new NodeScheduleEntry(f));
		
		// Update all the sub-graphs
		for (FactorGraph sg : factorGraph.getNestedGraphs())
		{
			final IScheduler scheduler = sg.getExplicitlySetScheduler();
			if (scheduler != null)	// If there's a scheduler associated with the sub-graph, use that and re-create the sub-graph schedule
			{
				ISchedule tmp = scheduler.createSchedule(sg);
				tmp.attach(sg);
				schedule.add(tmp);
			}
			else										// Otherwise, create a new schedule for the sub-graph too (flooding by default)
			{
				ISchedule tmp=null;
				try {
					tmp = ((IScheduler)_subGraphSchedulerClass.getConstructor().newInstance()).createSchedule(sg);
				} catch (Exception e) {
					throw new DimpleException("could not instantiate scheduler class");
				}
				tmp.attach(sg);
				schedule.add(tmp);
			}
		}

		return schedule;
	}
	
	
	@SuppressWarnings("all")
	public void setSubGraphScheduler(Class schedulerClass)
	{
		_subGraphSchedulerClass = schedulerClass;
	}

}
