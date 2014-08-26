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

import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;

/**
 * Forwards factor updates, and filters others.
 * 
 * @since 0.07
 * @author jking
 */
public final class ScheduleVisitorFactorFilter implements IScheduledActivity
{
	private final IScheduledActivity _scheduledActivity;
	
	public ScheduleVisitorFactorFilter(IScheduledActivity scheduledActivity)
	{
		_scheduledActivity = scheduledActivity;
	}
	
	public static IScheduledActivity create(IScheduledActivity scheduledActivity)
	{
		return new ScheduleVisitorFactorFilter(scheduledActivity);
	}
	
	@Override
	public void updateEdge(INode node, int portNum)
	{
		Factor factor = node.asFactor();
		if (factor != null)
		{
			_scheduledActivity.updateEdge(factor, portNum);
		}
	}

	@Override
	public void update(INode node)
	{
		Factor factor = node.asFactor();
		if (factor != null)
		{
			_scheduledActivity.update(factor);
		}
	}
}
