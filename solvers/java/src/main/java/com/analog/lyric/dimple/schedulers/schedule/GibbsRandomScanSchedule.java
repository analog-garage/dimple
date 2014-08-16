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

package com.analog.lyric.dimple.schedulers.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.GibbsSequentialScanScheduler;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.math.DimpleRandomGenerator;
import org.eclipse.jdt.annotation.NonNull;

/**
 * @author jeffb
 * 
 *         This is a dynamic schedule, which updates only one variable that is
 *         randomly chosen. This allows one iteration to correspond to exactly
 *         one variable update. In the Gibbs solver, factors do not need to be
 *         explicitly updated in the schedule.
 * 
 *         WARNING: This schedule DOES NOT respect any existing sub-graph
 *         scheduler associations. That is, if any sub-graph already has an
 *         associated scheduler, that scheduler is ignored in creating this
 *         schedule. I believe this is a necessary limitation for Gibbs sampling
 *         to operate properly.
 * 
 */
public class GibbsRandomScanSchedule extends ScheduleBase implements IGibbsSchedule
{
	protected FixedSchedule _scheduleEntryPool;

	
	@SuppressWarnings("null")
	public GibbsRandomScanSchedule(FactorGraph factorGraph)
	{
		_factorGraph = factorGraph;
		initialize();
	}
	
	@Override
	public void attach(FactorGraph factorGraph)
	{
		super.attach(factorGraph);
		initialize();
	}

	protected void initialize()
	{
		// Create a pool of schedule entries that will be randomly chosen from
		// By default, this is the same set of nodes in a sequential-scan schedule
		_scheduleEntryPool = (FixedSchedule)new GibbsSequentialScanScheduler().createSchedule(getFactorGraph());
	}

	@Override
	public @NonNull FactorGraph getFactorGraph()
	{
		return Objects.requireNonNull(_factorGraph);
	}

	@Override
	public Iterator<IScheduleEntry> iterator()
	{
		// Choose an entry in the list of schedule entries uniformly at random
		// Note: the DimpleRandomGenerator is used here so that if a fixed seed is set in the solver, then the schedule will also be repeatable
		int entryIndex = DimpleRandomGenerator.rand.nextInt(_scheduleEntryPool.size());
		
		// Create a single schedule entry that includes all of the selected variable
		ArrayList<IScheduleEntry> updateList = new ArrayList<IScheduleEntry>();
		updateList.add(_scheduleEntryPool.get(entryIndex));
		
		return updateList.iterator();
	}

	
	// Add a block schedule entry, which will replace individual variable updates included in the block
	@Override
	public void addBlockScheduleEntry(BlockScheduleEntry blockScheduleEntry)
	{
		_scheduleEntryPool.addBlockScheduleEntry(blockScheduleEntry);
	}

	// Indicate the number of entries in the schedule entry pool
	@Override
	public int size()
	{
		return _scheduleEntryPool.size();
	}

}
