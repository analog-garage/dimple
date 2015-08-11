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

package com.analog.lyric.dimple.model.variables;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.values.Value;

/**
 * Simple interface for mapping variables to values.
 * @since 0.08
 * @author Christopher Barber
 */
public interface IVariableToValue
{
	// Note: do not add more methods to this interface unless they have
	// default values (which requires Java 8) so that lambda expressions can
	// be used in place of this interface.
	
	/**
	 * Returns object containing value for given variable, if any.
	 * @since 0.08
	 */
	public @Nullable Value varToValue(Variable var);
}
