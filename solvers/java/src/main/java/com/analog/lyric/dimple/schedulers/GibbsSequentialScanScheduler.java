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
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import org.eclipse.jdt.annotation.Nullable;


/**
 * @author jeffb
 * 
 *         This class generates a sequential schedule on a graph specifically
 *         suited for the Gibbs solver. This schedule updates each variable
 *         sequentially.  In the Gibbs solver, factors do not need to be explicitly
 *         updated in the schedule.
 * 
 *         WARNING: This schedule DOES NOT respect any existing sub-graph
 *         scheduler associations. That is, if any sub-graph already has an
 *         associated scheduler, that scheduler is ignored in creating this
 *         schedule. I believe this is a necessary limitation for Gibbs sampling
 *         to operate properly.
 */
public class GibbsSequentialScanScheduler implements IGibbsScheduler
{
	@Nullable FixedSchedule _schedule;
	
	@Override
	public ISchedule createSchedule(FactorGraph g)
	{
		FixedSchedule schedule = _schedule = new FixedSchedule();

		// Update all owned variables
		for (Variable v : g.getVariablesFlat())
			schedule.add(new NodeScheduleEntry(v));

		return schedule;
	}

	
	// Add a block schedule entry, which will replace individual variable updates included in the block
	@Override
	public void addBlockScheduleEntry(BlockScheduleEntry blockScheduleEntry)
	{
		FixedSchedule schedule = _schedule;
		if (schedule == null)
			throw new DimpleException("Schedule must be created before adding a block schedule entry.");
		
		schedule.addBlockScheduleEntry(blockScheduleEntry);
	}
}
