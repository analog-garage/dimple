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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Parameterized discrete transition factor, which corresponds to p(y | x, A),
 * where A is a matrix of transition probabilities that are
 * parameterized as energy values (-log of unnormalized probabilities).
 * The transition matrix is organized such that columns correspond to
 * the output distribution for each input state. That is, the transition
 * matrix multiplies on the left.  The domain of x and y do not need to be
 * the same.
 * 
 * Representing A as described, the conjugate prior for A is such that
 * each entry of the A matrix is independently distributed according to
 * a negative exp-Gamma distribution, all with a common Beta parameter.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * y: Discrete output variable (MUST be zero-based integer values) 	// TODO: remove this restriction
 * x: Discrete input variable (MUST be zero-based integer values) 	// TODO: remove this restriction
 * A: Matrix of transition energy values
 * 
 */
public class DiscreteTransitionEnergyParameters extends FactorFunction
{
	protected int _yDimension;
	protected int _xDimension;
	protected double[] _Acol;
	private final static int NUM_DATA_ARGUMENTS = 2;

	public DiscreteTransitionEnergyParameters(int dimension) {this(dimension, dimension);}	// Square transition matrix
	public DiscreteTransitionEnergyParameters(int yDimension, int xDimension)
	{
		super();
		_yDimension = yDimension;
		_xDimension = xDimension;
		_Acol = new double[yDimension];
	}
	
    @Override
	public final double evalEnergy(Value[] arguments)
    {
    	if (arguments.length != _xDimension*_yDimension + NUM_DATA_ARGUMENTS)
    		throw new DimpleException("Incorrect number of arguments.");
    	
    	final int y = arguments[0].getInt();				// First argument is y (output variable)
    	final int x = arguments[1].getInt();				// Second argument is x (input variable)

    	int index = x * _yDimension + NUM_DATA_ARGUMENTS;	// Beginning of the column for the given value of x (matrix is scanned by columns)
    	for (int row = 0; row < _yDimension; row++)
    		_Acol[row] = arguments[index++].getDouble();
    	
    	double sum = 0;										// Normalize over column selected by the value of x
    	for (int row = 0; row < _yDimension; row++)
    		sum += Math.exp(-_Acol[row]);
    	
    	return _Acol[y] + Math.log(sum);
	}
    
    
    // Factor-specific methods
    public final int getXDimension()
    {
    	return _xDimension;
    }
    public final int getYDimension()
    {
    	return _yDimension;
    }
    public final int getNumParameters()
    {
    	return _xDimension * _yDimension;
    }
}
