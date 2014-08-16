/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import org.eclipse.jdt.annotation.Nullable;

public interface IRealConjugateSampler extends ISampler
{
	public double nextSample(Port[] ports, @Nullable FactorFunction input);
	public IParameterizedMessage createParameterMessage();
	public void aggregateParameters(IParameterizedMessage aggregateParameters, Port[] ports,
		@Nullable FactorFunction input);
}
