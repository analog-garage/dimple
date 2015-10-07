/*******************************************************************************
*   Copyright 2012-2015 Analog Devices, Inc.
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
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.google.common.collect.Iterables;


public class CustomGaussianNegate extends GaussianFactorBase
{
	public CustomGaussianNegate(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		
		if (factor.getSiblingCount() != 2)
			throw new SolverFactorCreationException("Factor must have exactly two connected varaibles");

		assertUnboundedReal(factor);
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		int inPortNum = 1 - outPortNum;		// Exactly two ports
		
		NormalParameters inputMsg = getSiblingEdgeState(inPortNum).varToFactorMsg;
		NormalParameters outMsg = getSiblingEdgeState(outPortNum).factorToVarMsg;

		outMsg.setMean(-inputMsg.getMean());	// Negate the mean
		outMsg.setPrecision(inputMsg.getPrecision());
	}


	
	/**
	 * Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	 * @deprecated as of release 0.08
	 */
	@Deprecated
	public static boolean isFactorCompatible(Factor factor)
	{
		return Iterables.all(factor.getSiblings(), VariablePredicates.isUnboundedReal());
	}
}
