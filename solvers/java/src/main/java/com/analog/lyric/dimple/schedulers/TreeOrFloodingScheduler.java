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

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;


/**
 * @author jeffb
 * 
 *         If this graph is a tree, or any of it's sub-graphs are trees, this
 *         class generates a tree-schedule. Otherwise it generates a flooding
 *         schedule on the portion of the graph that is not a tree.
 * 
 *         This scheduler respects any schedulers already assigned to
 *         sub-graphs. That is, if a sub-graph already has a scheduler
 *         associated with it, that scheduler will be used for that sub-graph
 *         instead of this one.
 */
public class TreeOrFloodingScheduler extends TreeSchedulerAbstract
{
	@Override
	protected ISchedule createNonTreeSchedule(FactorGraph g) 
	{
		FloodingScheduler floodingScheduler = new FloodingScheduler();
		floodingScheduler.setSubGraphScheduler(TreeOrFloodingScheduler.class);
		
		return floodingScheduler.createSchedule(g);
	}

}
