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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Gamma distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Alpha: Alpha parameter of the Gamma distribution (non-negative)
 * 2) Beta: Beta parameter of the Gamma distribution (non-negative)
 * 3...) An arbitrary number of real variables
 * 
 * Alpha and Beta parameters may optionally be specified as constants in the constructor.
 * In this case, they are not included in the list of arguments.
 * 
 */
public class Gamma extends FactorFunction implements IParametricFactorFunction
{
	protected double _alpha;
	protected double _beta;
	protected double _alphaMinusOne;
	protected double _logBeta;
	protected double _logGammaAlphaMinusAlphaLogBeta;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 2;

	/*--------------
	 * Construction
	 */
	
	public Gamma() {super();}
	public Gamma(double alpha, double beta)
	{
		this();
		_alpha = alpha;
		_beta = beta;
		_alphaMinusOne = _alpha - 1;
		_logBeta = Math.log(_beta);
		_logGammaAlphaMinusAlphaLogBeta = org.apache.commons.math3.special.Gamma.logGamma(_alpha) - _alpha * _logBeta;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	if (_alpha <= 0) throw new DimpleException("Non-positive alpha parameter. This must be a positive value.");
    	if (_beta <= 0) throw new DimpleException("Non-positive beta parameter. This must be a positive value.");
	}
	
	/**
	 * Construct from specified parameters
	 * @param parameters the following values are supported
	 * <ul>
	 * <li>alpha (default 1.0)
	 * <li>beta (default 1.0)
	 * </ul>
	 * @since 0.07
	 */
	public Gamma(Map<String,Object> parameters)
	{
		this((double)getOrDefault(parameters, "alpha", 1.0), (double)getOrDefault(parameters, "beta", 1.0));
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
    		_alpha = arguments[index++].getDouble();	// First input is alpha parameter (must be non-negative)
    		if (_alpha <= 0) return Double.POSITIVE_INFINITY;
    		_beta = arguments[index++].getDouble();	// Second input is beta parameter (must be non-negative)
    		if (_beta <= 0) return Double.POSITIVE_INFINITY;
    		_logBeta = Math.log(_beta);
    	}
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	if (_alpha == 1)
    	{
    		for (; index < length; index++)
    		{
    			double x = arguments[index].getDouble();				// Remaining inputs are Gamma variables
    			if (x < 0)
    				return Double.POSITIVE_INFINITY;
    			else
    				sum += x;
    		}
    		return sum * _beta - N * _logBeta;
    	}
    	else
    	{
    		if (!_parametersConstant)
    		{
        		_alphaMinusOne = _alpha - 1;
        		_logGammaAlphaMinusAlphaLogBeta = org.apache.commons.math3.special.Gamma.logGamma(_alpha) - _alpha * _logBeta;
    		}
        	for (; index < length; index++)
        	{
        		final double x = arguments[index].getDouble();				// Remaining inputs are Gamma variables
            	if (x < 0)
            		return Double.POSITIVE_INFINITY;
            	else
            		sum += x * _beta - Math.log(x) * _alphaMinusOne;
        	}
        	return sum + N * _logGammaAlphaMinusAlphaLogBeta;
    	}
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
    		parameters.put("alpha", _alpha);
    		parameters.put("beta", _beta);
    		return 2;
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
    			return _alpha;
    		case "beta":
    			return _beta;
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
    
    public final double getAlphaMinusOne()
    {
    	return _alphaMinusOne;
    }
    public final double getBeta()
    {
    	return _beta;
    }
}
