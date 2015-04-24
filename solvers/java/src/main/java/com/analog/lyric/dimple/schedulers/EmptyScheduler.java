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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.EmptySchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * Scheduler that produces canonical empty schedule.
 * <p>
 * Use this for solvers that do not use a schedule (e.g. LP).
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public enum EmptyScheduler implements IScheduler
{
	INSTANCE;

	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return "[EmptyScheduler]";
	}

	/*----------------------
	 * IOptionValue methods
	 */
	
	@Override
	public boolean isMutable()
	{
		return false;
	}
	
	/*--------------------
	 * IScheduler methods
	 */
	
	@Override
	public IScheduler copy(Map<Object, Object> old2NewMap, boolean copyToRoot)
	{
		return this;
	}
	
	@Override
	public EmptySchedule createSchedule(FactorGraph g)
	{
		return EmptySchedule.INSTANCE;
	}

	@Override
	public ISchedule createSchedule(ISolverFactorGraph solverGraph)
	{
		return EmptySchedule.INSTANCE;
	}

	@Override
	public List<SchedulerOptionKey> applicableSchedulerOptions()
	{
		return Collections.emptyList();
	}

	@Override
	public boolean isCustomScheduler()
	{
		return false;
	}
	
	@Override
	public boolean isDefaultScheduler()
	{
		return false;
	}
	
	@Override
	public void validateForGraph(FactorGraph graph)
	{
	}
}
