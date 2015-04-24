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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;


/**
 * Base class for BP schedulers that maintains no local state.
 * <p>
 * This class overrides {@link #hashCode()} and {@link #equals(Object)} to
 * treat all instances of the same class as equal.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class StatelessBPScheduler extends BPSchedulerBase
{
	private static final long serialVersionUID = 1L;

	/*----------------
	 * Object methods
	 */
	@Override
	public boolean equals(@Nullable Object obj)
	{
		return obj != null && obj.getClass() == getClass();
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode();
	}
	
	/*----------------------
	 * IOptionValue methods
	 */
	
	@Override
	public boolean isMutable()
	{
		return false;
	}
	
	/*--------------------
	 * IScheduler methods
	 */
	
	@Override
	public IScheduler copy(Map<Object,Object> old2NewMap, boolean copyToRoot)
	{
		return this;
	}
}
