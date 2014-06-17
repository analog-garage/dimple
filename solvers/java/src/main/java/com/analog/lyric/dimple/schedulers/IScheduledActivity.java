/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.schedulers;

import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.util.misc.Internal;

/**
 * Represents an activity driven by a schedule.
 * 
 * @see ScheduleVisitor#visit(com.analog.lyric.dimple.schedulers.schedule.ISchedule, IScheduledActivity)
 * @since 0.07
 * @author jking
 */
@Internal
public interface IScheduledActivity
{
	/**
	 * Updates an out-bound edge on a factor.
	 * 
	 * @param node The node whose out-bound edge is to be updated.
	 * @param portNum Which edge to update.
	 * @since 0.07
	 */
	public abstract void updateEdge(INode node, int portNum);

	/**
	 * Updates all of a factor's out-bound edges.
	 * 
	 * @param node The node to update.
	 * @since 0.07
	 */
	public abstract void update(INode node);
}
