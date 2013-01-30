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

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;


/**
 * @author jeffb
 * 
 *         This class generates a sequential schedule on a graph specifically
 *         suited for the Gibbs solver. This schedule updates each variable
 *         sequentially. Prior to updating each variable, the edge from each
 *         neighboring factor to that variable is updated.
 * 
 *         WARNING: This schedule DOES NOT respect any existing sub-graph
 *         scheduler associations. That is, if any sub-graph already has an
 *         associated scheduler, that scheduler is ignored in creating this
 *         schedule. I believe this is a necessary limitation for Gibbs sampling
 *         to operate properly.
 */
public class GibbsSequentialScanScheduler implements IScheduler
{

	public ISchedule createSchedule(FactorGraph g) 
	{
		FixedSchedule schedule = new FixedSchedule();


		// Update all owned variables
		for (VariableBase v : g.getVariablesFlat())
		{
			// For each variable, update factor edges connecting to that variable, then update the variable
			// Do this as a single sub-graph schedule entry so that it will be done all at once
			FixedSchedule subSchedule = new FixedSchedule();
			for (INode p : v.getSiblings())
			{
				subSchedule.add(new EdgeScheduleEntry(p,p.getPortNum(v)));
			}

			// Then update the variable
			subSchedule.add(new NodeScheduleEntry(v));
			
			// Add the sub-schedule to the outer schedule
			schedule.add(new SubScheduleEntry(subSchedule));
		}

		return schedule;
	}

}
