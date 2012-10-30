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
import com.analog.lyric.dimple.model.DimpleException;


/**
 * @author jeffb
 * 
 *         Parameterized HMM transition factor, which corresponds to p(y | x, A),
 *         where A is a matrix of transition probabilities that are
 *         parameterized as energy values (-log of unnormalized probabilities).
 *         The transition matrix is organized such that columns correspond to
 *         the output distribution for each input state. That is, the transition
 *         matrix multiplies on the left.
 * 
 *         Representing A as described, the conjugate prior for A is such that
 *         each entry of the A matrix is independently distributed according to
 *         a negative exp-Gamma distribution, all with a common Beta parameter.
 *         Depending on the solver, it may or may not be necessary to use a
 *         conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 *         The variables in the argument list are ordered as follows:
 * 
 *         y: Discrete output variable (MUST be zero-based integer values) // TODO: remove this restriction
 *         x: Discrete input variable (MUST be zero-based integer values) // TODO: remove this restriction
 *         A: Matrix of transition energy values
 */
public class ParameterizedHMMTransition extends FactorFunction
{
	protected int _dimension;
	protected double[][] _A;
	
	public ParameterizedHMMTransition(int dimension)
	{
		super("ParameterizedHMMTransition");
		_dimension = dimension;
		_A = new double[_dimension][_dimension];
	}
	
    @Override
	public double evalEnergy(Object ... input)
    {
    	if (input.length != _dimension*_dimension + 2)
    		throw new DimpleException("Incorrect number of arguments.");
    	
    	int index = 0;
    	
    	Object yIn = input[index++];
    	int y = (yIn instanceof Double) ? (int)Math.round((Double)yIn) : (Integer)yIn;
    	
    	Object xIn = input[index++];
    	int x = (xIn instanceof Double) ? (int)Math.round((Double)xIn) : (Integer)xIn;

    	for (int col = 0; col < _dimension; col++)		// Matrix is scanned by columns
    		for (int row = 0; row < _dimension; row++)
    			_A[row][col] = (Double)input[index++];
    	
    	double sum = 0;
    	for (int row = 0; row < _dimension; row++)
    		sum += Math.exp(-_A[row][x]);
    	
    	return _A[y][x] + Math.log(sum);
	}
    
    
}
