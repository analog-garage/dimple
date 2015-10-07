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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariablePredicates;
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.google.common.collect.Iterables;



public class CustomGaussianProduct extends GaussianFactorBase
{

	private double _constant;
	private final static int PRODUCT_INDEX = 0;
	private final static int MULTIPLICAND_INDEX = 1;
	
	public CustomGaussianProduct(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		
		// Make sure this is of the form a = b*c where either b or c is a non-zero constant.
		if (factor.getSiblingCount() != 2)
			throw new SolverFactorCreationException("Factor must be of form a = b*c where b or c is a constant");
		
		if (factor.getConstantCount() != 1)
			throw new SolverFactorCreationException("Expected one constant");
		_constant = factor.getConstantValues().get(0).getDouble();
		if (_constant == 0)
			throw new SolverFactorCreationException("Constant of 0 not supported");
		
		assertUnboundedReal(factor);
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		if (outPortNum == PRODUCT_INDEX)
			updateProduct();
		else
			updateMultiplicand();
	}

	private void updateProduct()
	{
		NormalParameters outMsg = getSiblingEdgeState(PRODUCT_INDEX).factorToVarMsg;
		NormalParameters inMsg = getSiblingEdgeState(MULTIPLICAND_INDEX).varToFactorMsg;
		
		// Up = C*Uv
		outMsg.setMean(inMsg.getMean() * _constant);
		
		// SigmaP = C*sigmav
		outMsg.setStandardDeviation(inMsg.getStandardDeviation() * _constant);
	}

	private void updateMultiplicand()
	{
		NormalParameters outMsg = getSiblingEdgeState(MULTIPLICAND_INDEX).factorToVarMsg;
		NormalParameters inMsg = getSiblingEdgeState(PRODUCT_INDEX).varToFactorMsg;
		
		// Uv = Up/C
		outMsg.setMean(inMsg.getMean() / _constant);
		
		// Sigmav = sigmaP/c
		outMsg.setStandardDeviation(inMsg.getStandardDeviation() / Math.abs(_constant));
	}
	
	
	/**
	 * Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	 * @deprecated as of release 0.08
	 */
	@Deprecated
	public static boolean isFactorCompatible(Factor factor)
	{
		// Must be of the form form a = b*c where either b or c is a constant.
		if (factor.getSiblingCount() != 2)
			return false;

		// Must have exactly one constant
		if (factor.getConstantCount() != 1)
			return false;
		
		// Variables must be real and univariate
		if (!Iterables.all(factor.getSiblings(), VariablePredicates.isUnboundedReal()))
			return false;
		
		// Constant must be non-zero
		double constant = factor.getConstantValues().get(0).getDouble();
		if (constant == 0)
			return false;

		return true;
	}

}
