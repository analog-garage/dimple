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

package com.analog.lyric.dimple.data;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;

/**
 * Enumerates classes of {@link IDatum} representation.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see IDatum#representationType()
 */
public enum DataRepresentationType
{
	/**
	 * A {@link FactorFunction} object used to represent a probability distribution
	 * over a single variable's values.
	 */
	FUNCTION,
	
	/**
	 * A {@link IParameterizedMessage} object used to represent a probability distribution
	 * over a single variable's values.
	 */
	MESSAGE,
	
	/**
	 * A {@link Value} object used to represent a single variable value.
	 */
	VALUE;
}
