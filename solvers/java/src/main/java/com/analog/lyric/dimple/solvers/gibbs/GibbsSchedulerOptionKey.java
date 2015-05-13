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

package com.analog.lyric.dimple.solvers.gibbs;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.schedulers.IGibbsScheduler;
import com.analog.lyric.dimple.schedulers.SchedulerOptionKey;
import com.analog.lyric.dimple.schedulers.validator.ScheduleValidatorOptionKey;
import com.analog.lyric.options.IOptionHolder;
import com.analog.lyric.options.OptionValidationException;

/**
 * Option key type for Gibbs schedulers.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see SchedulerOptionKey
 */
public class GibbsSchedulerOptionKey extends SchedulerOptionKey
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	GibbsSchedulerOptionKey(Class<? extends IGibbsScheduler> defaultSchedulerClass,
		ScheduleValidatorOptionKey validatorKey)
	{
		super(GibbsOptions.class, "scheduler", defaultSchedulerClass, validatorKey);
	}
	
	/*--------------------
	 * IOptionKey methods
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends IGibbsScheduler> defaultClass()
	{
		return (Class<? extends IGibbsScheduler>)super.defaultClass();
	}
	
	@Override
	public @Nullable IGibbsScheduler get(IOptionHolder holder)
	{
		return (IGibbsScheduler)super.get(holder);
	}

	@Override
	public IGibbsScheduler getOrDefault(IOptionHolder holder)
	{
		return (IGibbsScheduler)super.getOrDefault(holder);
	}
	
	@Override
	public Class<IGibbsScheduler> type()
	{
		return IGibbsScheduler.class;
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	protected Class<? extends IGibbsScheduler> validateClass(Class<?> schedulerClass)
	{
		super.validateClass(schedulerClass);
		
		try
		{
			return schedulerClass.asSubclass(IGibbsScheduler.class);
		}
		catch (ClassCastException ex)
		{
			throw new OptionValidationException("Not valid for option '%s': %s", this, ex.getMessage());
		}
	}
}
