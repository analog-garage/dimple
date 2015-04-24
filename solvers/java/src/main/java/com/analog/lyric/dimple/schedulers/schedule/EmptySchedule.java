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

package com.analog.lyric.dimple.schedulers.schedule;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.EmptyScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.google.common.collect.Iterators;

/**
 * A canonical schedule with no entries.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public enum EmptySchedule implements ISchedule
{
	INSTANCE;

	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public Iterator<IScheduleEntry> iterator()
	{
		return Iterators.emptyIterator();
	}
	
	/*----------------------
	 * IOptionValue methods
	 */

	@Override
	public boolean isMutable()
	{
		return false;
	}

	@Override
	public String toString()
	{
		return super.toString();
	}
	
	/*-------------------
	 * ISchedule methods
	 */
	
	@Override
	public void attach(FactorGraph factorGraph)
	{
	}

	@Override
	public @Nullable FactorGraph getFactorGraph()
	{
		return null;
	}

	@Override
	public EmptyScheduler getScheduler()
	{
		return EmptyScheduler.INSTANCE;
	}

	@Override
	public void setScheduler(IScheduler scheduler)
	{
	}
	
	@Override
	public boolean isCustom()
	{
		return false;
	}
	
	@Override
	public boolean isUpToDateForSolver(ISolverFactorGraph sgraph)
	{
		return true;
	}

	@Override
	public long scheduleVersion()
	{
		return 0;
	}

	@Override
	public long structureVersion()
	{
		return 0;
	}
}
