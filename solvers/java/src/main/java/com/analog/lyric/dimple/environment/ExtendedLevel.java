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

package com.analog.lyric.dimple.environment;

import java.util.logging.Level;

/**
 * Defines additional logging levels.
 * <p>
 * This complements the standard set of logging levels provided by {@link Level}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class ExtendedLevel extends Level
{
	private static final long serialVersionUID = 1L;

	/**
	 * Log level with severity between the standard {@link Level#WARNING} and {@link Level#SEVERE} levels.
	 */
	public static final ExtendedLevel ERROR =
		new ExtendedLevel("ERROR", (Level.WARNING.intValue() + Level.SEVERE.intValue()) / 2);

	/**
	 * @param name
	 * @param value
	 * @since 0.07
	 */
	protected ExtendedLevel(String name, int value)
	{
		super(name, value);
	}

	/**
	 * Makes sure that deserialization produces canonical instance.
	 */
	private Object readResolve()
	{
		switch (getName())
		{
		case "ERROR":
			return ERROR;
		}
		
		return this;
	}
}
