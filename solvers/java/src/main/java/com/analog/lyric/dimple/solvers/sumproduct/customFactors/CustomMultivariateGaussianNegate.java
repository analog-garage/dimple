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
import com.analog.lyric.dimple.model.variables.VariablePredicates;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.google.common.collect.Iterables;

public class CustomMultivariateGaussianNegate extends MultivariateGaussianFactorBase
{
	public CustomMultivariateGaussianNegate(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		assertUnboundedRealJoint(factor);
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		int inPortNum = 1 - outPortNum;		// Exactly two ports
		
		MultivariateNormalParameters inputMsg = getSiblingEdgeState(inPortNum).varToFactorMsg;
		MultivariateNormalParameters outMsg = getSiblingEdgeState(outPortNum).factorToVarMsg;

		if (inputMsg.isNull())
		{
			outMsg.setNull();
		}
		else
		{
			outMsg.set(inputMsg);
			outMsg.negateMean();
		}
	}
	
	
	/**
	 * Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	 * @deprecated as of release 0.08
	 */
	@Deprecated
	public static boolean isFactorCompatible(Factor factor)
	{
		return Iterables.all(factor.getSiblings(), VariablePredicates.isUnboundedRealJoint());
	}

}
