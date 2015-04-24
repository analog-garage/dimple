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

import java.util.List;
import java.util.Map;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.options.IOptionValue;

/**
 * @author jeffb
 * 
 *         Any scheduler must implement this interface.
 * 
 *         The createSchedule method takes a factor-graph as input, and returns
 *         a schedule of any type.
 */
public interface IScheduler extends IOptionValue
{
	/**
	 * Create schedule appropriate for given solver graph.
	 * @since 0.08
	 */
	public ISchedule createSchedule(ISolverFactorGraph solverGraph);
	
	/**
	 * Create schedule appropriate for given graph.
	 */
	public ISchedule createSchedule(FactorGraph graph);
	
	/**
	 * Returns an immutable list of scheduler option keys for which the scheduler is intended to be used.
	 * <p>
	 * For instance, a scheduler that produces edge schedule entries appropriate for message
	 * passing might return a list containing {@linkplain com.analog.lyric.dimple.options.BPOptions#scheduler
	 * BPOptions.scheduler}
	 * <p>
	 * This list is used by the {@link FactorGraph#setScheduler} method to set the corresponding options
	 * listed by the scheduler.
	 * @since 0.08
	 */
	public List<SchedulerOptionKey> applicableSchedulerOptions();
	
	public IScheduler copy(Map<Object,Object> old2NewMap, boolean copyToRoot);
	
	/**
	 * True if this is a custom scheduler.
	 * <p>
	 * Schedules produced by custom schedulers are subject to validation during graph initialization.
	 * <p>
	 * @since 0.08
	 */
	public boolean isCustomScheduler();
	
	/**
	 * Indicates this is a default scheduler for one or more {@link SchedulerOptionKey}.
	 * @since 0.08
	 */
	public boolean isDefaultScheduler();
	
	/**
	 * Validates whether scheduler can be used with given graph.
	 * <p>
	 * Most schedulers can be applied to any graph and will not do anything.
	 * <p>
	 * @since 0.08
	 * @throws ScheduleValidationException if scheduler cannot be used with {@code graph}.
	 */
	public void validateForGraph(FactorGraph graph);
}
