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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.options.IOptionValue;


/**
 * @author jeffb
 * 
 *         Any schedule produced by a scheduler must implement this interface. A
 *         schedule is required to implement the Iterable interface, with each
 *         iteration, returning a sequence of ScheduleEntry objects.
 */
public interface ISchedule extends Iterable<IScheduleEntry>, IOptionValue
{
	
	/*
	 * This method is called when setSchedule is called on the FactorGraph.
	 */
	@Deprecated
	public void attach(FactorGraph factorGraph) ;
	
	public @Nullable FactorGraph getFactorGraph();
	
	/**
	 * The scheduler that created this schedule, if any.
	 * @since 0.08
	 */
	public @Nullable IScheduler getScheduler();
	
	/**
	 * Override the {@link #getScheduler scheduler} attribute.
	 * <p>
	 * This should be used by schedulers that use another scheduler to do their work.
	 */
	public void setScheduler(IScheduler scheduler);
	
	/**
	 * True if this is a custom schedule.
	 * <p>
	 * Custom schedule's are subject to validation upon graph initialization.
	 * <p>
	 * Currently only true for {@link FixedSchedule}s produced manually or by a
	 * {@linkplain IScheduler#isCustomScheduler() custom scheduler}.
	 * @since 0.08
	 */
	public boolean isCustom();
	
	public boolean isUpToDateForSolver(ISolverFactorGraph sgraph);
	
	/**
	 * A simple counter that is incremented if the schedule changes.
	 * <p>
	 * For dynamic schedules, this number should only be incremented if the
	 * the parameters of the schedule changes.
	 */
	public long scheduleVersion();
	
	/**
	 * The {@linkplain FactorGraph#structureVersion() structure version} of the {@linkplain #getFactorGraph graph}
	 * when schedule was last updated.
	 * <p>
	 * @since 0.08
	 */
	public long structureVersion();
}
