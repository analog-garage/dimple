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

package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.google.common.collect.Iterables;


/**
 * <p>
 * A schedule entry that contains a sub-schedule.
 * <p>
 * If you want to simply refer to the
 * <p>
 * @deprecated use {@link SubgraphScheduleEntry} instead.
 */
@Deprecated
public class SubScheduleEntry implements IScheduleEntry
{
	private final ISchedule _subschedule;
	
	public SubScheduleEntry(ISchedule subschedule)
	{
		_subschedule = subschedule;
	}
	
	public ISchedule getSchedule()
	{
		return _subschedule;
	}
	
	@Override
	public IScheduleEntry copy(Map<Object,Object> old2newObjs, boolean copyToRoot)
	{
		FactorGraph newGraph = (FactorGraph)old2newObjs.get(this.getSchedule().getFactorGraph());
		return new SubScheduleEntry(newGraph.getSchedule());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @return {@linkplain ISchedule#getFactorGraph() graph} of {@linkplain #getSubschedule() subschedule}.
	 */
	@Override
	public @Nullable FactorGraph getParentGraph()
	{
		return _subschedule.getFactorGraph();
	}
	
	@Override
	public Iterable<? extends INode> getNodes()
	{
		List<INode> nodes = new ArrayList<>();
		for (IScheduleEntry subentry : _subschedule)
		{
			Iterables.addAll(nodes, subentry.getNodes());
		}
		return nodes;
	}

	/**
	 * @return the subschedule
	 */
	public ISchedule getSubschedule()
	{
		return _subschedule;
	}

	@Override
	public Type type()
	{
		return Type.SUBSCHEDULE;
	}
}
