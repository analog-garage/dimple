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

package com.analog.lyric.dimple.schedulers.schedule;

import com.analog.lyric.dimple.exceptions.DimpleException;

/**
 * Indicates a problem with a Dimple schedule.
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class ScheduleValidationException extends DimpleException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs with given message formated by {@link String#format}
	 * @since 0.08
	 */
	public ScheduleValidationException(String format, Object ... args)
	{
		super(format, args);
	}

	/**
	 * Wraps given {@code cause} with message formatted by {@link String#format}.
	 * @since 0.08
	 */
	public ScheduleValidationException(Throwable cause, String format, Object ... args)
	{
		super(cause, format, args);
	}
}
