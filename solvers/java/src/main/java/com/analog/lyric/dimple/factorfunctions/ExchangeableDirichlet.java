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

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Factor for an exchangeable set of Dirichlet distributed variables
 * associated with a variable or fixed parameter vector. In this version,
 * all of the parameter values are common and specified as a single real
 * value.  The variables are ordered as follows in the argument list:
 * 
 * 1) Parameter (non-negative Real variable).
 * 2...) An arbitrary number of RealJoint variables, each one a Dirichlet distributed random variable.
 * 
 * The dimension of the Dirichlet variable must be specified in the constructor.
 * 
 * The parameter may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 * 
 * @since 0.05
 */
public class ExchangeableDirichlet extends FactorFunction
{
	private int _dimension;
	private double _alpha;
	private double _logBetaAlpha;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;
	private static final double SIMPLEX_THRESHOLD = 1e-12;
	

	public ExchangeableDirichlet(int dimension)		// Variable parameter
	{
		super();
		_dimension = dimension;
		_parametersConstant = false;
		_firstDirectedToIndex = 1;
	}
	public ExchangeableDirichlet(int dimension, double alpha)	// Constant parameter
	{
		super();
		_dimension = dimension;
		_alpha = alpha;
		_logBetaAlpha = logBeta(_alpha);
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
		if (_alpha <= 0) throw new DimpleException("Non-positive alpha parameter. Domain must be restricted to positive values.");
	}
	

    @Override
    public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_alpha = arguments[index++].getDouble();		// First variable is parameter value
    		if (_alpha <= 0)
    			return Double.POSITIVE_INFINITY;
    		_logBetaAlpha = logBeta(_alpha);
    	}

    	double sum = 0;
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	for (; index < length; index++)
    	{
    		final double[] x = arguments[index].getDoubleArray();	// Remaining inputs are Dirichlet distributed random variable vectors
    		if (x.length != _dimension)
	    		throw new DimpleException("Dimension of variable does not equal to the dimension of the parameter vector.");
    		double xSum = 0;
    		for (int i = 0; i < _dimension; i++)
    		{
    			final double xi = x[i];
    			if (xi <= 0)
    				return Double.POSITIVE_INFINITY;
    			else
    				sum -= Math.log(xi);	// -log(x_i ^ (a_i-1))
    			xSum += xi;
    		}
    		
    		if (!almostEqual(xSum, 1, SIMPLEX_THRESHOLD * _dimension))	// Values must be on the probability simplex
    			return Double.POSITIVE_INFINITY;
    	}

    	return sum * (_alpha - 1) + N * _logBetaAlpha;
    }
    
    private final double logBeta(double alpha)
    {
    	return Gamma.logGamma(alpha) * _dimension - Gamma.logGamma(alpha * _dimension);
    }
    
    private final boolean almostEqual(double a, double b, double threshold)
    {
    	return (a >= b ? a - b : b - a) < threshold;
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
    public final boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }
    public final double getAlphaMinusOne()
    {
    	return _alpha - 1;
    }
    public final double[] getAlphaMinusOneArray()	// Get parameters as if they were separate
    {
    	double[] parameterArray = new double[_dimension];
    	Arrays.fill(parameterArray, _alpha - 1);
    	return parameterArray;
    }
    public final int getDimension()
    {
    	return _dimension;
    }
}
