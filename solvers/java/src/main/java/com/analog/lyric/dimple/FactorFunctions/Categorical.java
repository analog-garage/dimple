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

package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;


/**
 * Parameterized categorical distribution, which corresponds to p(x | alpha),
 * where alpha is a vector of probabilities that are parameterized as energy values
 * (-log of unnormalized probabilities).
 * 
 * Representing alpha as described, the conjugate prior for alpha is such that
 * each entry of alpha is independently distributed according to
 * a negative exp-Gamma distribution, all with a common Beta parameter.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * x: Discrete output variable (MUST be zero-based integer values) 	// TODO: remove this restriction
 * alpha: Vector of energy values (-log of unnormalized probabilities)
 * 
 */
public class Categorical extends FactorFunction
{
	protected int _dimension;
	protected double[] _alpha;
	
	public Categorical(int dimension)
	{
		super("Categorical");
		_dimension = dimension;
		_alpha = new double[dimension];
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	if (arguments.length != _dimension + 1)
    		throw new DimpleException("Incorrect number of arguments.");
    	
    	int index = 0;
    	
    	int x = FactorFunctionUtilities.toInteger(arguments[index++]);			// First argument is y (output variable)

    	for (int i = 0; i < _dimension; i++)
    		_alpha[i] = FactorFunctionUtilities.toDouble(arguments[index++]);
    	
    	double sum = 0;															// Normalize
    	for (int i = 0; i < _dimension; i++)
    		sum += Math.exp(-_alpha[i]);
    	
    	return _alpha[x] + Math.log(sum);
	}
    
}
