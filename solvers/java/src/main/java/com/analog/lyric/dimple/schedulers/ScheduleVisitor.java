/*******************************************************************************
 *   Copyright 2014 Analog Devices, Inc.
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

import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubgraphScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.Internal;

/**
 * Forwards a schedule's node update and update edge calls to a {@link IScheduledActivity}.
 * 
 * @since 0.07
 * @author jking
 * @category internal
 */
@SuppressWarnings("deprecation")
@Internal
public final class ScheduleVisitor
{
	private ScheduleVisitor()
	{
	}

	@SuppressWarnings("deprecation") // for SUBSCHEDULE
	public static void visit(ISchedule schedule, ISolverFactorGraph solverGraph, IScheduledActivity scheduledActivity)
	{
		for (IScheduleEntry scheduleEntry : schedule)
		{
			switch (scheduleEntry.type())
			{
			case VARIABLE_BLOCK:
			{
				BlockScheduleEntry blockEntry = (BlockScheduleEntry)scheduleEntry;
				for (Variable var : blockEntry.getBlock())
				{
					scheduledActivity.update(var);
				}
				break;
			}
			case EDGE:
			{
				EdgeScheduleEntry edgeScheduleEntry = (EdgeScheduleEntry) scheduleEntry;
				int portNum = edgeScheduleEntry.getPortNum();
				scheduledActivity.updateEdge(edgeScheduleEntry.getNode(), portNum);
				break;
			}
			case NODE:
			{
				NodeScheduleEntry nodeScheduleEntry = (NodeScheduleEntry) scheduleEntry;
				scheduledActivity.update(nodeScheduleEntry.getNode());
				break;
			}
			case SUBGRAPH:
			{
				SubgraphScheduleEntry subgraphEntry = (SubgraphScheduleEntry)scheduleEntry;
				ISolverFactorGraph ssubgraph = solverGraph.getSolverSubgraph(subgraphEntry.getSubgraph());
				visit(ssubgraph.getSchedule(), ssubgraph, scheduledActivity);
				break;
			}
			case SUBSCHEDULE:
			{
				SubScheduleEntry subScheduleEntry = (SubScheduleEntry) scheduleEntry;
				visit(subScheduleEntry.getSchedule(), solverGraph, scheduledActivity);
				break;
			}
			case CUSTOM:
				// ignore
				break;
			}
		}
	}
}
