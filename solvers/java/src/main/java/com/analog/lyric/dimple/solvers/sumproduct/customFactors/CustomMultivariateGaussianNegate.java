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

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

public class CustomMultivariateGaussianNegate extends MultivariateGaussianFactorBase
{
	public CustomMultivariateGaussianNegate(Factor factor)
	{
		super(factor);
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		int inPortNum = 1 - outPortNum;		// Exactly two ports
		
		MultivariateNormalParameters inputMsg = _inputMsgs[inPortNum];
		MultivariateNormalParameters outMsg = _outputMsgs[outPortNum];

		double[] mean = inputMsg.getMean();
		for (int i = 0; i < mean.length; i++)
			mean[i] = -mean[i];		// Negate the mean vector
		outMsg.setMeanAndCovariance(mean, inputMsg.getCovariance());
	}
	
	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		for (int i = 0, end = factor.getSiblingCount(); i < end; i++)
		{
			Variable v = factor.getSibling(i);
			Domain domain = v.getDomain();
			
			// Must be unbounded multivariate real
			if (!domain.isRealJoint() || domain.isBounded())
			{
				return false;
			}
		}
		return true;
	}

}
