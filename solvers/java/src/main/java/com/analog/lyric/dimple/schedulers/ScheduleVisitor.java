/*******************************************************************************
 *   Copyright 2014 Analog Devices, Inc.
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

import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;
import com.analog.lyric.util.misc.Internal;

/**
 * Forwards a schedule's node update and update edge calls to a {@link IScheduledActivity}.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class ScheduleVisitor
{
	private ScheduleVisitor()
	{
	}

	public static void visit(ISchedule schedule, IScheduledActivity scheduledActivity)
	{
		for (IScheduleEntry scheduleEntry : schedule)
		{
			if (scheduleEntry instanceof BlockScheduleEntry)
			{
				// Block updates are ignored.
			}
			if (scheduleEntry instanceof EdgeScheduleEntry)
			{
				EdgeScheduleEntry edgeScheduleEntry = (EdgeScheduleEntry) scheduleEntry;
				int portNum = edgeScheduleEntry.getPortNum();
				scheduledActivity.updateEdge(edgeScheduleEntry.getNode(), portNum);
			}
			if (scheduleEntry instanceof NodeScheduleEntry)
			{
				NodeScheduleEntry nodeScheduleEntry = (NodeScheduleEntry) scheduleEntry;
				scheduledActivity.update(nodeScheduleEntry.getNode());
			}
			if (scheduleEntry instanceof SubScheduleEntry)
			{
				SubScheduleEntry subScheduleEntry = (SubScheduleEntry) scheduleEntry;
				visit(subScheduleEntry.getSchedule(), scheduledActivity);
			}
		}
	}
}
