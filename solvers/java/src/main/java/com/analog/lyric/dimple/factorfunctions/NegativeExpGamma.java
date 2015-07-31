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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.GammaParameters;


/**
 * NegativeExpGamma distribution, which is a distribution over a
 * variable whose negative exponential is Gamma distributed. That is,
 * this is the negative log of a Gamma distributed variable.
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * 1) Alpha: Alpha parameter of the underlying Gamma distribution (non-negative)
 * 2) Beta: Beta parameter of the underlying Gamma distribution (non-negative)
 * 3...) An arbitrary number of real variables

 * Alpha and Beta parameters may optionally be specified as constants in the constructor.
 * In this case, they are not included in the list of arguments.
 * 
 */
public class NegativeExpGamma extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;

	protected GammaParameters _parameters;
	protected boolean _parametersConstant;
	protected int _firstDirectedToIndex ;

	/*--------------
	 * Construction
	 */
	
	private NegativeExpGamma(GammaParameters parameters, int index)
	{
		super((String)null);
		_parameters = parameters;
		_parametersConstant = index == 0;
		_firstDirectedToIndex = index;
	}
	
	public NegativeExpGamma()
	{
		this(new GammaParameters(), 2);
	}

	/**
	 * @since 0.08
	 */
	public NegativeExpGamma(GammaParameters parameters)
	{
		this(parameters, 0);
	}
	
	public NegativeExpGamma(double alpha, double beta)
	{
		this(new GammaParameters(alpha - 1, beta));
	}
	
	protected NegativeExpGamma(NegativeExpGamma other)
	{
		super(other);
		_parameters = other._parameters.clone();
		_firstDirectedToIndex = other._firstDirectedToIndex;
		_parametersConstant = other._parametersConstant;
	}
	
	@Override
	public NegativeExpGamma clone()
	{
		return new NegativeExpGamma(this);
	}
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof NegativeExpGamma)
		{
			NegativeExpGamma that = (NegativeExpGamma)other;
			return _parametersConstant == that._parametersConstant &&
				_parameters.objectEquals(that._parameters) &&
				_firstDirectedToIndex == that._firstDirectedToIndex;
		}
		
		return false;
	}
	
    @Override
	public final double evalEnergy(Value[] arguments)
    {
    	int index = 0;
    	
    	double alphaMinusOne = _parameters.getAlphaMinusOne();
    	double beta = _parameters.getBeta();
    	
    	if (!_parametersConstant)
    	{
    		double alpha = arguments[index++].getDouble(); // First input is alpha parameter (must be non-negative)
    		if (alpha <= 0)
    			return Double.POSITIVE_INFINITY;
    		beta = arguments[index++].getDouble(); // Second input is beta parameter (must be non-negative)
    		if (beta <= 0)
    			return Double.POSITIVE_INFINITY;
    		_parameters.setAlpha(alpha);
    		_parameters.setBeta(beta);
    		alphaMinusOne = alpha - 1;
    	}
    	
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	
    	// The standard Gamma normalization can be reused:
    	double sum = N * -_parameters.getNormalizationEnergy();

    	for (; index < length; index++)
    	{
    		final double x = arguments[index].getDouble();		// Remaining inputs are NegativeExpGamma variables
        	sum += x * alphaMinusOne + Math.exp(-x) * beta;
    	}
    	
    	return sum;
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
    		parameters.put("alpha", _parameters.getAlpha());
    		parameters.put("beta", _parameters.getBeta());
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
    			return _parameters.getAlpha();
    		case "beta":
    			return _parameters.getBeta();
    		}
    	}
    	return null;
    }
    
    @Override
    public GammaParameters getParameterizedMessage()
    {
    	return _parameters;
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
    	return _parameters.getAlphaMinusOne();
    }
    public final double getBeta()
    {
    	return _parameters.getBeta();
    }
}
