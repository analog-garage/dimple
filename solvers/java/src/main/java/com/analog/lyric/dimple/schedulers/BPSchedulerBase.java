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

import com.analog.lyric.dimple.options.BPOptions;

/**
 * Base class for schedulers intended to be used with BP solvers.
 * <p>
 * These schedulers are expected to produce schedules that consist
 * primarily of edge and node schedule entries for passing messages
 * around the graph.
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class BPSchedulerBase extends SchedulerBase
{
	private static final long serialVersionUID = 1L;

	/*--------------------
	 * IScheduler methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Subclasses of {@link BPSchedulerBase} will include {@link BPOptions#scheduler}.
	 */
	@Override
	public List<SchedulerOptionKey> applicableSchedulerOptions()
	{
		return Collections.singletonList(BPOptions.scheduler);
	}
}
