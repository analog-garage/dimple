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

import org.apache.commons.math3.special.Gamma;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;


/**
 * Factor for an exchangeable set of Dirichlet distributed variables
 * associated with a variable or fixed parameter vector. The variables are
 * ordered as follows in the argument list:
 * 
 * 1) Parameters (non-negative RealJoint variable).
 * 2...) An arbitrary number of RealJoint variables, each one a Dirichlet distributed random variable.
 * 
 * The parameters may optionally be specified as constants in the constructor.
 * In this case, the parameters are not included in the list of arguments.
 */
public class Dirichlet extends FactorFunction
{
	private int _dimension;
	private double[] _alpha;
	private double _logBetaAlpha;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;
	private static final double SIMPLEX_THRESHOLD = 1e-12;
	

	public Dirichlet()		// Variable parameters
	{
		super();
		_parametersConstant = false;
		_firstDirectedToIndex = 1;	// Parameter vector is an array (one RealJoint variable)
	}
	public Dirichlet(double[] alpha)	// Constant parameters
	{
		super();
		_dimension = alpha.length;
		_alpha = alpha.clone();
		_logBetaAlpha = logBeta(_alpha);
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	for (int i = 0; i < _dimension; i++)
    		if (_alpha[i] < 0) throw new DimpleException("Negative alpha parameter. Domain must be restricted to non-negative values.");
	}
	
    @Override
	public double evalEnergy(Object ... arguments)
	{
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_alpha = (double[])arguments[index++];		// First variable is array of parameter values
    		_logBetaAlpha = logBeta(_alpha);
    		_dimension = _alpha.length;
    		for (int i = 0; i < _dimension; i++)
    			if (_alpha[i] < 0) throw new DimpleException("Negative parameter value. Domain must be restricted to non-negative values.");
    	}

    	double sum = 0;
    	int length = arguments.length;
    	int N = length - index;			// Number of non-parameter variables
    	for (; index < length; index++)
    	{
    		double[] x = (double[])arguments[index];	// Remaining inputs are Dirichlet distributed random variable vectors
    		if (x.length != _dimension)
	    		throw new DimpleException("Dimension of variable does not equal to the dimension of the parameter vector.");
    		double xSum = 0;
    		for (int i = 0; i < _dimension; i++)
    		{
    			if (x[i] < 0)
    				return Double.POSITIVE_INFINITY;
    			else
    				sum -= (_alpha[i]-1) * Math.log(x[i]);	// -log(x_i ^ (a_i-1))
    			xSum += x[i];
    		}
    		
    		if (!almostEqual(xSum, 1, SIMPLEX_THRESHOLD * _dimension))	// Values must be on the probability simplex
    			return Double.POSITIVE_INFINITY;
    	}

    	return sum + N * _logBetaAlpha;
	}
    
    
    private final double logBeta(double[] alpha)
    {
    	int dimension = alpha.length;
    	double sumAlpha = 0;
    	for (int i = 0; i < dimension; i++)
    		sumAlpha += alpha[i];
    	
    	double sumLogGamma = 0;
    	for (int i = 0; i < dimension; i++)
    		sumLogGamma += Gamma.logGamma(alpha[i]);
    	
    	return sumLogGamma - Gamma.logGamma(sumAlpha);
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
    public final double[] getParameters()
    {
    	return _alpha;
    }
    public final int getDimension()
    {
    	return _dimension;
    }
}
