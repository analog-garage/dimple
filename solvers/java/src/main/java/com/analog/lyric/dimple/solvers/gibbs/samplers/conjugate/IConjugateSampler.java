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

package com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate;

import java.util.List;

import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;

/**
 * Base interface for conjugate samplers
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public interface IConjugateSampler extends ISampler
{

	IParameterizedMessage createParameterMessage();

	/**
	 * Note: previous version of this method used Ports instead of edges and a single input object.
	 * @since 0.08
	 */
	void aggregateParameters(IParameterizedMessage aggregateParameters,
		ISolverEdgeState[] edges,
		List<? extends IDatum> inputs);
}
