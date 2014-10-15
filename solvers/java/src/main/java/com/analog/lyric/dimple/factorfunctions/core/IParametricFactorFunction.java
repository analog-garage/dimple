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

package com.analog.lyric.dimple.factorfunctions.core;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Parametric factor function supporting generic parameter lookup.
 * <p>
 * Non-abstract factor functions that implement this interface are expected to provide a constructor
 * that takes a single argument of type {@code Map<String,Object>} that specifies the function's initial
 * parameter values.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public interface IParametricFactorFunction extends IFactorFunction
{
	/**
	 * Copies the current parameter values in provided {@code parameters} map.
	 * <p>
	 * This will do nothing if {@link #hasConstantParameters()} is false.
	 * <p>
	 * @param parameters is a map into which the entries will be written. Existing entries may be overwritten
	 * if they match an added parameter name, but will otherwise be left alone.
	 * @return the number of entries that were added to the map.
	 * @since 0.07
	 */
	public int copyParametersInto(Map<String,Object> parameters);

	/**
	 * Returns the value of the specified parameter.
	 * <p>
	 * @param parameterName identifies the parameter. Some factors may provide more than one way to retrieve a
	 * parameter.
	 * @return the parameter value or null if there is no such parameter with the given {@code parameterName}
	 * or if {@link #hasConstantParameters()} is false.
	 * @since 0.07
	 */
	public @Nullable Object getParameter(String parameterName);
	
	/**
	 * Indicates that the function's parameters are stored as constants within the factor function
	 * itself.
	 * <p>
	 * If false, then the parameters will instead need to be provided as variables attached to the factor.
	 * See the documentation for the specific function for details as to how the variables should be ordered,
	 * but most often parameter variables come first.
	 * <p>
	 * Not that when true the parameter is only "constant" in the sense that it is not represented as
	 * a variable in the graph. Some factor functions may still allow the value of the parameter to be
	 * changed after construction.
	 * <p>
	 * @since 0.07
	 */
	public boolean hasConstantParameters();
}

