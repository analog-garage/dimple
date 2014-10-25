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

import java.util.Map;

import org.apache.commons.math3.special.Gamma;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.model.values.Value;


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
public class Dirichlet extends FactorFunction implements IParametricFactorFunction
{
	private int _dimension;
	private double[] _alphaMinusOne;
	private double _logBetaAlpha;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;
	private static final double SIMPLEX_THRESHOLD = 1e-12;
	
	/*--------------
	 * Construction
	 */
	
	public Dirichlet()		// Variable parameters
	{
		super();
		_alphaMinusOne = ArrayUtil.EMPTY_DOUBLE_ARRAY;
		_parametersConstant = false;
		_firstDirectedToIndex = 1;	// Parameter vector is an array (one RealJoint variable)
	}
	public Dirichlet(double[] alpha)	// Constant parameters
	{
		super();
		_dimension = alpha.length;
		_alphaMinusOne = new double[_dimension];
		_logBetaAlpha = logBeta(alpha);
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	for (int i = 0; i < _dimension; i++)
    	{
    		if (alpha[i] <= 0) throw new DimpleException("Non-positive alpha parameter. Domain must be restricted to positive values.");
    		_alphaMinusOne[i] = alpha[i] - 1;
    	}
	}
	
	/**
	 * Construct from specified parameters
	 * @param parameters the following values are supported:
	 * <ul>
	 * <li>alpha (default 1.0)
	 * <li>beta (default 1.0)
	 * </ul>
	 * @since 0.07
	 */
	public Dirichlet(Map<String,Object> parameters)
	{
		this((double[])require(parameters, "alpha", "alphas"));
	}
	
	/*------------------------
	 * FactorFunction methods
	 */
	
    @Override
	public final double evalEnergy(Value[] arguments)
	{
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		final double[] alpha = arguments[index++].getDoubleArray();		// First variable is array of parameter values
    		_dimension = alpha.length;
    		_alphaMinusOne = new double[_dimension];
    		for (int i = 0; i < _dimension; i++)
    		{
    			final double alphai = alpha[i];
    			if (alphai <= 0)
    				return Double.POSITIVE_INFINITY;
        		_alphaMinusOne[i] = alphai - 1;
    		}
    		_logBetaAlpha = logBeta(alpha);
    	}

    	double sum = 0;
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	for (; index < length; index++)
    	{
    		final double[] x = arguments[index].getDoubleArray();			// Remaining inputs are Dirichlet distributed random variable vectors
    		if (x.length != _dimension)
	    		throw new DimpleException("Dimension of variable does not equal to the dimension of the parameter vector.");
    		double xSum = 0;
    		for (int i = 0; i < _dimension; i++)
    		{
    			final double xi = x[i];
    			if (xi <= 0)
    				return Double.POSITIVE_INFINITY;
    			else
    				sum -= (_alphaMinusOne[i]) * Math.log(xi);	// -log(x_i ^ (a_i-1))
    			xSum += xi;
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

    /*-----------------------------------
     * IParametricFactorFunction methods
     */
    
    @Override
    public int copyParametersInto(Map<String, Object> parameters)
    {
    	if (_parametersConstant)
    	{
    		parameters.put("alpha", getParameter("alpha"));
    		return 1;
    	}
    	return 0;
    }
    
    @Override
    public @Nullable Object getParameter(String parameterName)
    {
    	if (_parametersConstant)
    	{
    		switch (parameterName)
    		{
    		case "alpha":
    		case "alphas":
    			final double[] alpha = _alphaMinusOne.clone();
    			for (int i = alpha.length; --i>=0; )
    			{
    				++alpha[i];
    			}
    			return alpha;
    		}
    	}
    	return null;
    }
    
    @Override
	public final boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }

    /*-------------------------
    * Factor-specific methods
    */
    
    public final double[] getAlphaMinusOneArray()
    {
    	return _alphaMinusOne;
    }
    public final int getDimension()
    {
    	return _dimension;
    }
}
