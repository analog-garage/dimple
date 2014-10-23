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
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Parameterized multinomial distribution, which corresponds to p(x | N, alpha),
 * where x is a vector of discrete count variables, N is the total count across all categories, and
 * where alpha is a vector of real probability parameters (not necessarily normalized).
 * <p>
 * The domain of each x variable must be a zero-based integer with maximum value N (domain 0 through N).
 * If N is a variable rather than a constant, then the domain of each x must be range from 0 through
 * the maximum value in the domain of N.
 * <p>
 * Representing alpha as described, the conjugate prior for alpha is such that
 * each entry of alpha is independently distributed according to
 * a negative exp-Gamma distribution, all with a common Beta parameter.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * <p>
 * The variables in the argument list are ordered as follows:
 * 
 * <ol>
 * <li> N: Parameter indicating the total count
 * <li> dimension+1) Alpha: A number of Real variables containing energy values, where
 * 	  the number of variables must equal the specified dimension
 * <li> dimension+2...2*dimension+1) x: A number of discrete count variable, where the number of variables
 *    must equal the specified dimension
 * </ol>
 * 
 * N parameter may optionally be specified as a constant in the constructor.
 * In this case, N is not included in the list of arguments. *
 * <p>
 * @since 0.06
 */
public class MultinomialEnergyParameters extends FactorFunction
{
	private int _dimension;
	protected int _N;
	protected double _negativeLogFactorialN;
	protected boolean _NParameterConstant = false;
	private double[] _alpha;
	private int _firstDirectedToIndex;

	public MultinomialEnergyParameters(int dimension)				// Variable N
	{
		super();
		_dimension = dimension;
		_NParameterConstant = false;
		_firstDirectedToIndex = dimension + 1;
		_alpha = new double[dimension];
	}
	public MultinomialEnergyParameters(int dimension, int N)		// Fixed N
	{
		super();
		_dimension = dimension;
		_N = N;
    	if (_N < 0) throw new DimpleException("N must be a non-negative value.");
		_negativeLogFactorialN = -org.apache.commons.math3.special.Gamma.logGamma(_N + 1);
		_NParameterConstant = true;
		_firstDirectedToIndex = dimension;
		_alpha = new double[dimension];
	}

	
    @Override
	public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	if (!_NParameterConstant)
    	{
    		_N = arguments[index++].getInt();				// First argument is N parameter
    		if (_N < 0) return Double.POSITIVE_INFINITY;
    		_negativeLogFactorialN = -org.apache.commons.math3.special.Gamma.logGamma(_N + 1);
    	}
    	
    	for (int i = 0; i < _dimension; i++)
    		_alpha[i] = arguments[index++].getDouble();		// Next _dimension arguments are vector of Alpha parameters
    	
    	if (arguments.length - index != _dimension)
    		throw new DimpleException("Number of count variables must equal the dimension of the parameter vector.");


    	int countSum = 0;
    	double parameterSum = 0;
    	double sum = _negativeLogFactorialN;
    	for (int i = 0; i < _dimension; i++)
    	{
    		final double alphai = _alpha[i];
    		parameterSum += Math.exp(-alphai);

    		final int x = arguments[index++].getInt();		// Remaining arguments are discrete count variables
    		if (x < 0)
    			return Double.POSITIVE_INFINITY;
    		countSum += x;
    		
    		sum += x * alphai + org.apache.commons.math3.special.Gamma.logGamma(x + 1);
    	}
    	if (countSum != _N)
			return Double.POSITIVE_INFINITY;
    	
    	final double energy = sum + _N * Math.log(parameterSum);
    	if (energy != energy)	// Faster isNaN
    		return Double.POSITIVE_INFINITY;

    	return energy;
	}
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices(int numEdges)
	{
    	// All edges except the parameter edges (if present) are directed-to edges
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}
    
    
    // Factor-specific methods
    public final int getDimension()
    {
    	return _dimension;
    }
    public final boolean hasConstantNParameter()
    {
    	return _NParameterConstant;
    }
    public final int getN()
    {
    	return _N;
    }
}
