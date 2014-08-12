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

package com.analog.lyric.options;

/**
 * Indicates invalid option value.
 * <p>
 * Throw by implementations of {@link IOptionKey#validate}.
 * @since 0.07
 * @author Christopher Barber
 */
public class OptionValidationException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct exception with given message.
	 * @see String#format
	 * @since 0.07
	 */
	public OptionValidationException(String format, Object ... args)
	{
		super(String.format(format, args));
	}

	/**
	 * Construct exception given message and cause.
	 * @param cause another exception that resulted in this error.
	 * @param format is the string message format
	 * @param args string format arguments
	 * @see String#format
	 * @since 0.07
	 */
	public OptionValidationException(Throwable cause, String format, Object ... args)
	{
		super(String.format(format, args), cause);
	}
}
