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
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.DimpleException;


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
public class NegativeExpGamma extends FactorFunction
{
	double _alpha;
	double _beta;
	double _alphaMinusOne;
	double _logGammaAlphaMinusAlphaLogBeta;
	boolean _parametersConstant = false;
	int _firstDirectedToIndex = 2;

	public NegativeExpGamma() {super();}
	public NegativeExpGamma(double alpha, double beta)
	{
		this();
		_alpha = alpha;
		_beta = beta;
		_alphaMinusOne = _alpha - 1;
		_logGammaAlphaMinusAlphaLogBeta = org.apache.commons.math.special.Gamma.logGamma(_alpha) - _alpha * Math.log(_beta);
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
    	if (_alpha < 0) throw new DimpleException("Negative alpha parameter. This must be a non-negative value.");
    	if (_beta < 0) throw new DimpleException("Negative beta parameter. This must be a non-negative value.");
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	if (!_parametersConstant)
    	{
    		_alpha = FactorFunctionUtilities.toDouble(arguments[index++]);	// First input is alpha parameter (must be non-negative)
    		_beta = FactorFunctionUtilities.toDouble(arguments[index++]);	// Second input is beta parameter (must be non-negative)
    		_alphaMinusOne = _alpha - 1;
    		_logGammaAlphaMinusAlphaLogBeta = org.apache.commons.math.special.Gamma.logGamma(_alpha) - _alpha * Math.log(_beta);
    		if (_alpha < 0) throw new DimpleException("Negative alpha parameter. Domain must be restricted to non-negative values.");
    		if (_beta < 0) throw new DimpleException("Negative beta parameter. Domain must be restricted to non-negative values.");
    	}
    	int length = arguments.length;
    	int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	for (; index < length; index++)
    	{
    		double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are NegativeExpGamma variables
        	sum += x * _alphaMinusOne + Math.exp(-x) * _beta;
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
}
