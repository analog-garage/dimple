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

package com.analog.lyric.dimple.schedulers.validator;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;

/**
 * Schedule validator that verifies that nodes in schedule entries are in schedule's graph
 * <p>
 * Ensures that all of a schedule entry's {@linkplain IScheduleEntry#getNodes() nodes} are
 * in the graph tree rooted at the schedule's {@linkplain ISchedule#getFactorGraph() graph}.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class InGraphScheduleValidator extends ScheduleValidator
{
	@Override
	public void validateNext(IScheduleEntry entry) throws ScheduleValidationException
	{
		final ISchedule schedule = _schedule;
		if (schedule != null)
		{
			final FactorGraph scheduleGraph = schedule.getFactorGraph();
			if (scheduleGraph != null)
			{
				final FactorGraph entryParent = entry.getParentGraph();

				if (entryParent != scheduleGraph && !scheduleGraph.isAncestorOf(entryParent))
				{
					throw new ScheduleValidationException("%s contains node that is not in graph %s",
						entry, scheduleGraph);
				}
			}
		}
	}
}
