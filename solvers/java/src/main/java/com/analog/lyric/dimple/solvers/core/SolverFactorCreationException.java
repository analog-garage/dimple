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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.exceptions.DimpleException;

/**
 * Indicates failure to create a solver factor.
 * <p>
 * Thrown by {@link ISolverFactorCreator#create} to indicate that solver factor cannot be created,
 * typically because some additional constraints required by a custom factor implementation have not
 * been met. While this exception will most often be automatically caught and handled by solver's factor
 * creation methods, the exception may be logged and therefore its message should provide sufficient information
 * for users to determine why a solver factor was not created.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class SolverFactorCreationException extends DimpleException
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param format
	 * @param args
	 * @since 0.08
	 */
	public SolverFactorCreationException(String format, Object ... args)
	{
		super(format, args);
	}

	public SolverFactorCreationException(Throwable ex, String format, Object ... args)
	{
		super(ex, format, args);
	}
}
