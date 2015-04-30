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

package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.Map;

import com.analog.lyric.dimple.solvers.interfaces.ISolverVariableBlock;


/**
 * Interface for block variable updaters.
 * <p>
 * Block updaters are used by {@link BlockScheduleEntry}s to perform block updates of
 * solver variables.
 * <p>
 * @since 0.06
 * @author jeffb
 */
public interface IBlockUpdater
{
	/**
	 * Update variables in solver block.
	 * @return true if updater was successful. If false, then the caller is expected to handle the
	 * update (typically by simply doing individual variable updates).
	 * @since 0.08
	 */
	public boolean update(ISolverVariableBlock sblock);

	/**
	 * Return a clone of this object.
	 * <p>
	 * This is used when copying updaters in {@link BlockScheduleEntry}s from a template graph to its instantiation.
	 * <p>
	 * @param old2newobjs contains a mapping from old to new objects that can be used by the copy
	 * operation if applicable.
	 * @since 0.08
	 */
	public IBlockUpdater copy(Map<Object,Object> old2newobjs);
}
