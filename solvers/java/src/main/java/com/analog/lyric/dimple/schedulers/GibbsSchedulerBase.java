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

package com.analog.lyric.dimple.schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.schedulers.schedule.IGibbsSchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * Base implementation class for {@link IGibbsScheduler}s.
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class GibbsSchedulerBase extends SchedulerBase implements IGibbsScheduler
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	final protected ArrayList<BlockScheduleEntry> _blockEntries;
	
	/*--------------
	 * Construction
	 */
	
	protected GibbsSchedulerBase()
	{
		_blockEntries = new ArrayList<>(0);
	}
	
	protected GibbsSchedulerBase(GibbsSchedulerBase other, Map<Object,Object> old2new, boolean copyToRoot)
	{
		_blockEntries = new ArrayList<>(other._blockEntries.size());
		for (BlockScheduleEntry blockEntry : other._blockEntries)
		{
			_blockEntries.add(blockEntry.copy(old2new, copyToRoot));
		}
	}
	
	@Override
	public IGibbsSchedule createSchedule(ISolverFactorGraph solverGraph)
	{
		return createSchedule(solverGraph.getModelObject());
	}
	
	/*----------------------
	 * IOptionValue methods
	 */

	/**
	 * This scheduler is mutable.
	 * @see #addBlockScheduleEntry(BlockScheduleEntry)
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
	public List<? extends SchedulerOptionKey> applicableSchedulerOptions()
	{
		return Collections.singletonList(GibbsOptions.scheduler);
	}
	
	/*-------------------------
	 * IGibbsScheduler methods
	 */
	
	@Deprecated
	@Override
	public void addBlockScheduleEntry(BlockScheduleEntry blockScheduleEntry)
	{
		_blockEntries.add(blockScheduleEntry);
	}
	
	@Override
	public void addBlockWithReplacement(IBlockUpdater blockUpdater, VariableBlock block)
	{
		addBlockScheduleEntry(new BlockScheduleEntry(blockUpdater, block));
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	/**
	 * Adds block entries from scheduler to specified schedule and returns it.
	 * @since 0.08
	 */
	protected <Schedule extends IGibbsSchedule> Schedule addBlockEntries(Schedule schedule)
	{
		for (BlockScheduleEntry entry : _blockEntries)
		{
			schedule.addBlockScheduleEntry(entry);
		}
		return schedule;
	}
}
