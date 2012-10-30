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

import org.apache.commons.math.special.Gamma;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;


/**
 * @author jeffb
 * 
 *         NegativeExpGamma distribution, which is a distribution over a
 *         variable whose negative exponential is Gamma distributed. That is,
 *         this is the negative log of a Gamma distributed variable.
 *         
 *         The variables in the argument list are ordered as follows:
 *         
 *         alpha: Alpha parameter of the underlying Gamma distribution (non-negative)
 *         beta: Beta parameter of the underlying Gamma distribution (non-negative)
 *         x: NegativeExpGamma distributed variable
 */
public class NegativeExpGamma extends FactorFunction
{
	double _alpha;
	double _beta;
	boolean _alphaSpecified = false;
	boolean _betaSpecified = false;
	
	public NegativeExpGamma() {super("NegativeExpGamma");}
	public NegativeExpGamma(double alpha, double beta)
	{
		this();
		_alpha = alpha;
		_alphaSpecified = true;
		_beta = beta;
		_betaSpecified = true;
	}
	
    @Override
	public double evalEnergy(Object ... input)
    {
    	int index = 0;
    	if (!_alphaSpecified)
    		_alpha = (Double)input[index++];		// First input is alpha parameter (must be non-negative)
    	if (!_betaSpecified)
    		_beta = (Double)input[index++];			// Second input is beta parameter (must be non-negative)
    	double x = (Double)input[index++];			// Third input is the NegativeExpGamma distributed variable
    	if (_alpha < 0) throw new DimpleException("Negative alpha parameter. Domain must be restricted to non-negative values.");
    	if (_beta < 0) throw new DimpleException("Negative beta parameter. Domain must be restricted to non-negative values.");
    	
    	return x * (_alpha - 1) + _beta * Math.exp(-x) - _alpha * Math.log(_beta) + Gamma.logGamma(_alpha);
	}
    
    
}
