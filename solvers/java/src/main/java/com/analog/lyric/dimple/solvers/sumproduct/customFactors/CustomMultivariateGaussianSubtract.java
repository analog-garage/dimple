/*******************************************************************************
*   Copyright 2012-2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.VariableBase;

// Same as CustomMultivariateGaussianSum, except the second edge is treated as the output instead of the first
public class CustomMultivariateGaussianSubtract extends CustomMultivariateGaussianSum
{
	public CustomMultivariateGaussianSubtract(Factor factor)
	{
		super(factor);
		_sumIndex = 1;	// Port that is the sum of all the others
	}
	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		for (int i = 0, end = factor.getSiblingCount(); i < end; i++)
		{
			VariableBase v = factor.getSibling(i);
			
			// Must be real
			if (v.getDomain().isDiscrete())
				return false;
			
			// Must be multivariate
			if (v instanceof Real)
				return false;
			
			// Must be unbounded
			if (v.getDomain().asRealJoint().isBounded())
				return false;
		}
		return true;
	}
}
