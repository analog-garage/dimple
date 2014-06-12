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
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;

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
public class SequentialScheduler implements IScheduler
{
	@SuppressWarnings("all")
	protected Class _subGraphSchedulerClass = SequentialScheduler.class;


	@Override
	@SuppressWarnings("unchecked")
	public ISchedule createSchedule(FactorGraph g)
	{
		FixedSchedule schedule = new FixedSchedule();


		// Update all owned functions
		for (Factor f : g.getNonGraphFactorsTop())
		{
			// For each function, update variable edges connecting to that function
			for (INode n : f.getSiblings())
			{
				schedule.add(new EdgeScheduleEntry(n,n.getPortNum(f)));
			}

			// Then update the function
			schedule.add(new NodeScheduleEntry(f));
		}

		// Update all sub-graphs
		for (FactorGraph sg : g.getNestedGraphs())
		{
			// For each sub-graph, update the variable edges connecting to that sub-graph
			for (INode p : sg.getSiblings())
			{
				schedule.add(new EdgeScheduleEntry(p,p.getPortNum(sg)));
			}

			// Then update the sub-graph
			final IScheduler scheduler =sg.getExplicitlySetScheduler();
			if (scheduler != null)	// If there's a scheduler associated with the sub-graph, use that and re-create the sub-graph schedule
			{
				ISchedule tmp = scheduler.createSchedule(sg);
				tmp.attach(sg);
				schedule.add(tmp);
			}
			else										// Otherwise, create a new schedule for the sub-graph too (sequential by default)
			{
				ISchedule tmp =null;
				try
				{
					tmp =((IScheduler)_subGraphSchedulerClass.getConstructor().newInstance()).createSchedule(sg);
				}
				catch (Exception e)
				{
					throw new DimpleException("could not create scheduler class");
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
