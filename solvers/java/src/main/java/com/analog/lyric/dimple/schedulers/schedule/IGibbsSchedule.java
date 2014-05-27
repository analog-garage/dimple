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

package com.analog.lyric.dimple.schedulers.schedule;

import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;

/**
 * 
 * @since 0.06
 * @author jeffb
 */
public interface IGibbsSchedule extends ISchedule
{
	public void addBlockScheduleEntry(BlockScheduleEntry blockScheduleEntry);	// Add a block schedule entry and replace individual node entries
	public int size();	// Number of entries in the schedule considered one Gibbs scan
}
