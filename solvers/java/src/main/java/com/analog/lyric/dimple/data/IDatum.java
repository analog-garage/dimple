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

import java.io.Serializable;

import com.analog.lyric.collect.IEquals;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;


/**
 * Abstract interface for different representations of data associated with a Dimple {@link Variable}
 * <p>
 * Every {@code IDatum} object is typically associated with a particular {@link Variable} instance, through
 * a {@link DataLayerBase} mapping. {@code IDatum} objects typically do not contain any direct references to
 * their variable.
 * <p>
 * There are three main classes of data (which are enumerated by {@link DataRepresentationType}):
 * <ul>
 * <li>{@link Value} instances representing observed or sampled values.
 * <li>{@link IParameterizedMessage} objects representing a probability distribution used for input
 * distributions or output beliefs.
 * <li>{@link IUnaryFactorFunction} objects that can take a single argument of the variable's type, used to
 * represent input distribution.
 * </ul>
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public interface IDatum extends IEquals, Cloneable, Serializable
{
	public IDatum clone();
	
	/**
	 * Identifies representation used by this object.
	 * @since 0.08
	 * @see DataRepresentationType
	 */
	public DataRepresentationType representationType();
}
