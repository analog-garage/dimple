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

import static com.analog.lyric.dimple.environment.DimpleEnvironment.*;

import java.util.Iterator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.GibbsSequentialScanScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.google.common.collect.Iterators;

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
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	protected FixedSchedule _scheduleEntryPool;

	/*--------------
	 * Construction
	 */
	
	public GibbsRandomScanSchedule(FactorGraph factorGraph)
	{
		this(null, factorGraph);
	}

	@SuppressWarnings("null")
	public GibbsRandomScanSchedule(@Nullable IScheduler scheduler, FactorGraph factorGraph)
	{
		super(scheduler, factorGraph);
		initialize();
	}
	
	/*-------------------
	 * ISchedule methods
	 */
	
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
		++_version;
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
		// Note: the DimpleRandomGenerator is used here so that if a fixed seed is set in the solver,
		// then the schedule will also be repeatable
		int entryIndex = activeRandom().nextInt(_scheduleEntryPool.size());
		
		// Create a single schedule entry that includes all of the selected variable
		return Iterators.singletonIterator(_scheduleEntryPool.get(entryIndex));
	}

	
	// Add a block schedule entry, which will replace individual variable updates included in the block
	@Override
	public void addBlockScheduleEntry(BlockScheduleEntry blockScheduleEntry)
	{
		_scheduleEntryPool.addBlockScheduleEntry(blockScheduleEntry);
	}

	@Override
	public final long scheduleVersion()
	{
		return _scheduleEntryPool.scheduleVersion();
	}
	
	// Indicate the number of entries in the schedule entry pool
	@Override
	public int size()
	{
		return _scheduleEntryPool.size();
	}
}
