/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.factorfunctions.core.DeterministicRealUnaryFactorFunction;


/**
 * Deterministic exp function. This is a deterministic directed factor
 * (if smoothing is not enabled).
 * 
 * Optional smoothing may be applied, by providing a smoothing value in
 * the constructor. If smoothing is enabled, the distribution is
 * smoothed by exp(-difference^2/smoothing), where difference is the
 * distance between the output value and the deterministic output value
 * for the corresponding inputs.
 * 
 * The variables are ordered as follows in the argument list:
 * 
 * 1) Output (exp of input)
 * 2) Input (double or integer)
 * 
 */
public class Exp extends DeterministicRealUnaryFactorFunction
{
	protected final double myFunction(double in)
	{
		return Math.exp(in);
	}
}
