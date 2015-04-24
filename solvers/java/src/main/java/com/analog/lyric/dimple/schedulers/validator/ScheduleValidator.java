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

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;

/**
 * Base class for schedule validation.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public abstract class ScheduleValidator
{
	/*-------
	 * State
	 */
	
	protected @Nullable ISchedule _schedule;
	
	/*--------------
	 * Construction
	 */
	
	protected ScheduleValidator()
	{
	}
	
	/*---------------------------
	 * ScheduleValidator methods
	 */
	
	/**
	 * Schedule currently being validated, if any.
	 * @since 0.08
	 */
	public @Nullable ISchedule schedule()
	{
		return _schedule;
	}
	
	/**
	 * Start validation of specified {@code schedule}.
	 * <p>
	 * Default method simply records {@code schedule} in a field.
	 * <p>
	 * @param schedule
	 * @throws ScheduleValidationException
	 * @since 0.08
	 */
	public void start(ISchedule schedule) throws ScheduleValidationException
	{
		_schedule = schedule;
	}
	
	/**
	 * Validate next entry produced by {@linkplain #schedule() schedule}.
	 * <p>
	 * This method is used for incremental validation of schedules as they are produced.
	 * <p>
	 * @param entry
	 * @throws ScheduleValidationException
	 * @since 0.08
	 * @see #validate
	 */
	public abstract void validateNext(IScheduleEntry entry) throws ScheduleValidationException;
	
	/**
	 * Completes validation of currently started {@linkplain #schedule() schedule}.
	 * <p>
	 * Assumes that all of the entries produced by the schedule have been passed to
	 * {@link #validateNext}. The default implementation simply clears the schedule field.
	 * <p>
	 * @throws ScheduleValidationException
	 * @since 0.08
	 * @see #start
	 */
	public void finish() throws ScheduleValidationException
	{
		_schedule = null;
	}
	
	/**
	 * Validates the schedule.
	 * <p>
	 * The default implementation invokes {@link #validateNext(IScheduleEntry)} on the entries produced by the
	 * schedule's {@linkplain ISchedule#iterator iterator}
	 * <p>
	 * @param schedule
	 * @throws ScheduleValidationException
	 * @since 0.08
	 */
	public void validate(ISchedule schedule) throws ScheduleValidationException
	{
		start(schedule);
		for (IScheduleEntry entry : schedule)
		{
			validateNext(entry);
		}
		finish();
	}
}
