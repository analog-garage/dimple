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
 * Alpha: Alpha parameter of the underlying Gamma distribution (non-negative)
 * Beta: Beta parameter of the underlying Gamma distribution (non-negative)
 * x: NegativeExpGamma distributed variable
 * 
 * Alpha and Beta parameters may optionally be specified as constants in the constructor.
 * In this case, they are not included in the list of arguments.
 * 
 */
public class NegativeExpGamma extends FactorFunction
{
	double _alpha;
	double _beta;
	boolean _alphaConstant = false;
	boolean _betaConstant = false;
	int _directedToIndex = 2;

	public NegativeExpGamma() {super();}
	public NegativeExpGamma(double alpha, double beta)
	{
		this();
		_alpha = alpha;
		_alphaConstant = true;
		_beta = beta;
		_betaConstant = true;
		_directedToIndex = 0;
    	if (_alpha < 0) throw new DimpleException("Negative alpha parameter. This must be a non-negative value.");
    	if (_beta < 0) throw new DimpleException("Negative beta parameter. This must be a non-negative value.");
	}
	
    @Override
	public double evalEnergy(Object... arguments)
    {
    	int index = 0;
    	if (!_alphaConstant)
    	{
    		_alpha = FactorFunctionUtilities.toDouble(arguments[index++]);	// First input is alpha parameter (must be non-negative)
    		if (_alpha < 0) throw new DimpleException("Negative alpha parameter. Domain must be restricted to non-negative values.");
    	}
    	if (!_betaConstant)
    	{
    		_beta = FactorFunctionUtilities.toDouble(arguments[index++]);	// Second input is beta parameter (must be non-negative)
    		if (_beta < 0) throw new DimpleException("Negative beta parameter. Domain must be restricted to non-negative values.");
    	}
    	double x = FactorFunctionUtilities.toDouble(arguments[index++]);	// Third input is the NegativeExpGamma distributed variable
    	
    	return x * (_alpha - 1) + _beta * Math.exp(-x) - _alpha * Math.log(_beta) + org.apache.commons.math.special.Gamma.logGamma(_alpha);
	}
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices() {return new int[]{_directedToIndex};}
}
