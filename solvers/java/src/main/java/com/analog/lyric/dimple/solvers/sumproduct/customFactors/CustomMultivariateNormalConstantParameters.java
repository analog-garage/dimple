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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariablePredicates;
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.google.common.collect.Iterables;


public class CustomMultivariateNormalConstantParameters extends MultivariateGaussianFactorBase
{
	public CustomMultivariateNormalConstantParameters(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		assertUnboundedRealJoint(factor);
		if (!((MultivariateNormal)factor.getFactorFunction()).hasConstantParameters())
		{
			throw new SolverFactorCreationException("MultivariateNormal function does not have constant parameters");
		}
	}

	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		// For constant parameters, output message will never change
	}


	@Override
	public void initialize()
	{
		super.initialize();
		
		// Pre-compute output message
		FactorFunction factorFunction = _model.getFactorFunction();
		MultivariateNormal specificFactorFunction = (MultivariateNormal)factorFunction;

		MultivariateNormalParameters outputMessage;
		if (specificFactorFunction.hasConstantParameters())
			outputMessage = specificFactorFunction.getParameters();
		else
			throw new DimpleException("Normal factor must have constant parameters");
		
		// Can set the output messages now since it will never change
		for (int edge = 0, n = getSiblingCount(); edge < n; edge++)
			getSiblingEdgeState(edge).factorToVarMsg.set(outputMessage);
	}
	
	
	/**
	 * Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	 * @deprecated as of release 0.08
	 */
	@Deprecated
	public static boolean isFactorCompatible(Factor factor)
	{
		FactorFunction factorFunction = factor.getFactorFunction();
		MultivariateNormal specificFactorFunction = (MultivariateNormal)factorFunction;
		
		if (!specificFactorFunction.hasConstantParameters())
			return false;
		
		return Iterables.all(factor.getSiblings(), VariablePredicates.isUnboundedRealJoint());
	}


}
