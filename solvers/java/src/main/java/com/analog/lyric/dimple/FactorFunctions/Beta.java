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
 * Gamma distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Alpha: Alpha parameter of the Gamma distribution (non-negative)
 * 2) Beta: Beta parameter of the Gamma distribution (non-negative)
 * 3) Gamma distributed real variable
 * 
 * Alpha and Beta parameters may optionally be specified as constants in the constructor.
 * In this case, they are not included in the list of arguments.
 * 
 */
public class Beta extends FactorFunction
{
	double _alpha;
	double _beta;
	boolean _alphaConstant = false;
	boolean _betaConstant = false;

	public Beta() {super();}
	public Beta(double alpha, double beta)
	{
		this();
		_alpha = alpha;
		_alphaConstant = true;
		_beta = beta;
		_betaConstant = true;
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
			if (_beta < 0) throw new DimpleException("Negative alpha parameter. Domain must be restricted to non-negative values.");
		}
		double x = FactorFunctionUtilities.toDouble(arguments[index++]);	// Third input is the Gamma distributed variable

		if (x < 0 || x > 1)
			return Double.POSITIVE_INFINITY;
		else if (_alpha == 1 && _beta == 1)
			return 0;
		else if (_alpha == 1)
			return org.apache.commons.math.special.Beta.logBeta(_alpha, _beta) - (_beta - 1) * Math.log(1 - x);
		else if (_beta == 1)
			return org.apache.commons.math.special.Beta.logBeta(_alpha, _beta) - (_alpha - 1) * Math.log(x);
		else
			return org.apache.commons.math.special.Beta.logBeta(_alpha, _beta) - (_alpha - 1) * Math.log(x) - (_beta - 1) * Math.log(1 - x);
	}

}
