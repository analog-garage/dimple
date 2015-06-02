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

import static com.analog.lyric.math.Utilities.*;

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
 * <p>
 * Representing A as described, the conjugate prior for A is such that
 * each entry of the A matrix is independently distributed according to
 * a negative exp-Gamma distribution, all with a common Beta parameter.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * <p>
 * The variables in the argument list are ordered as follows:
 * <ol>
 * <li>y: Discrete output variable
 * <li>x: Discrete input variable
 * <li>...: The entries of the transition matrix flattened out in column major order (the standard
 * multidimensional array order used by MATLAB).
 * </ol>
 */
public class DiscreteTransitionEnergyParameters extends FactorFunction
{
	/*-------
	 * State
	 */
	
	private final static int NUM_DATA_ARGUMENTS = 2;

	protected final int _yDimension;
	protected final int _xDimension;

	/*--------------
	 * Construction
	 */
	
	public DiscreteTransitionEnergyParameters(int dimension) {this(dimension, dimension);}	// Square transition matrix
	public DiscreteTransitionEnergyParameters(int yDimension, int xDimension)
	{
		super();
		_yDimension = yDimension;
		_xDimension = xDimension;
	}
	
	/*------------------------
	 * FactorFunction methods
	 */
	
    @Override
	public final double evalEnergy(Value[] arguments)
    {
    	if (arguments.length != _xDimension*_yDimension + NUM_DATA_ARGUMENTS)
    		throw new DimpleException("Incorrect number of arguments.");
    	
    	final int y = arguments[0].getIndexOrInt();				// First argument is y (output variable)
    	final int x = arguments[1].getIndexOrInt();				// Second argument is x (input variable)

    	// Beginning of the column for the given value of x (matrix is scanned by columns)
    	final int colStartIndex = x * _yDimension + NUM_DATA_ARGUMENTS;
    	
       	double sum = 0;
    	for (int index = colStartIndex + _yDimension; --index>=colStartIndex;)
    	{
    		sum += energyToWeight(arguments[index].getDouble());
    	}
    	
    	return arguments[colStartIndex + y].getDouble() + Math.log(sum);
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
