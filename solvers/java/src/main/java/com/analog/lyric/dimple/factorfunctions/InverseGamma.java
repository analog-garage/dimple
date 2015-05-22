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
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.factorfunctions.core.IParametricFactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.UnaryFactorFunction;
import com.analog.lyric.dimple.model.values.Value;


/**
 * Inverse Gamma distribution. The variables in the argument list are ordered as follows:
 * <p>
 * <ol>
 * <li>Alpha: Alpha parameter of the Inverse Gamma distribution (non-negative)
 * <li>Beta: Beta parameter of the Inverse Gamma distribution (non-negative)
 * <li>... an arbitrary number of real variables
 * </ol>
 * Alpha and Beta parameters may optionally be specified as constants in the constructor.
 * In this case, they are not included in the list of arguments.
 * 
 */
public class InverseGamma extends UnaryFactorFunction implements IParametricFactorFunction
{
	private static final long serialVersionUID = 1L;

	protected double _alpha;
	protected double _beta;
	protected double _alphaPlusOne;
	protected double _logGammaAlphaMinusAlphaLogBeta;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 2;

	/*--------------
	 * Construction
	 */
	
	public InverseGamma() {super((String)null);}
	public InverseGamma(double alpha, double beta)
	{
		this();
		_alpha = alpha;
		_beta = beta;
		_alphaPlusOne = _alpha + 1;
		_logGammaAlphaMinusAlphaLogBeta = org.apache.commons.math3.special.Gamma.logGamma(_alpha) - _alpha * Math.log(_beta);
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	if (_alpha <= 0) throw new DimpleException("Non-positive alpha parameter. This must be a positive value.");
    	if (_beta <= 0) throw new DimpleException("Non-positive beta parameter. This must be a positive value.");
	}
	
	/**
	 * Constructs inverse gamma distribution with fixed alpha and beta parameters.
	 * @param parameters May specify either or both of the alpha and beta parameters using "alpha" and
	 * "beta" keys respectively. Values will otherwise default to 1.0.
	 * @since 0.07
	 */
	public InverseGamma(Map<String,Object> parameters)
	{
		this((double)getOrDefault(parameters, "alpha", 1.0), (double)getOrDefault(parameters, "beta", 1.0));
	}
	
	protected InverseGamma(InverseGamma other)
	{
		super(other);
		_alpha = other._alpha;
		_beta = other._beta;
		_alphaPlusOne = other._alphaPlusOne;
		_logGammaAlphaMinusAlphaLogBeta = other._logGammaAlphaMinusAlphaLogBeta;
		_parametersConstant = other._parametersConstant;
		_firstDirectedToIndex = other._firstDirectedToIndex;
	}
	
	@Override
	public InverseGamma clone()
	{
		return new InverseGamma(this);
	}
	
	/*----------------
	 * IDatum methods
	 */
	
	@Override
	public boolean objectEquals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof InverseGamma)
		{
			InverseGamma that = (InverseGamma)other;
			return _parametersConstant == that._parametersConstant &&
				_alpha == that._alpha &&
				_beta == that._beta &&
				_firstDirectedToIndex == that._firstDirectedToIndex;
		}
		
		return false;
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
    		_alpha = arguments[index++].getDouble();			// First input is alpha parameter (must be non-negative)
    		if (_alpha <= 0) return Double.POSITIVE_INFINITY;
    		_beta = arguments[index++].getDouble();				// Second input is beta parameter (must be non-negative)
    		if (_beta <= 0) return Double.POSITIVE_INFINITY;
    		_alphaPlusOne = _alpha + 1;
    		_logGammaAlphaMinusAlphaLogBeta = org.apache.commons.math3.special.Gamma.logGamma(_alpha) - _alpha * Math.log(_beta);
    	}
    	final int length = arguments.length;
    	final int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		final double x = arguments[index].getDouble();		// Remaining inputs are Inverse Gamma variables
        	if (x <= 0)
        		return Double.POSITIVE_INFINITY;
        	else
        		sum += _beta/x + _alphaPlusOne * Math.log(x);
    	}
		return sum + N * _logGammaAlphaMinusAlphaLogBeta;
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
    public boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }
}
