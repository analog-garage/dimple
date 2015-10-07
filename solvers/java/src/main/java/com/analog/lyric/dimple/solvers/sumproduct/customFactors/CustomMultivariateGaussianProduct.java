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

import static java.util.Objects.*;

import com.analog.lyric.dimple.model.domains.RealJointDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SolverFactorCreationException;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;

public class CustomMultivariateGaussianProduct extends MultivariateGaussianFactorBase
{
	private double [][] _constant;
	
	public CustomMultivariateGaussianProduct(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		
		// Make sure this is of the form a = b*c where either b or c is a constant.
		if (factor.getSiblingCount() != 2)
			throw new SolverFactorCreationException("factor must be of form a = b*c where b is a constant matrix");
		
		// TODO: alternatively, one of the ports could be a discrete variable with a single domain element
		
		if (factor.getConstantCount() != 1)
			throw new SolverFactorCreationException("expected one constant");
		
		if (!factor.hasConstantAtIndex(1))
			throw new SolverFactorCreationException("Expect matrix to be second arg");
		
		Object constantObj = requireNonNull(factor.getConstantValues().get(0).getObject());
		
		if (!(constantObj instanceof double[][]))
		{
			throw new SolverFactorCreationException("Constant not a double[][] matrix");
		}
		
		final double[][] constant = (double[][])constantObj;
		
		assertUnboundedRealJoint(factor);
		
		final int yDim = factor.getSibling(0).getDomain().getDimensions();
		final int xDim = factor.getSibling(1).getDomain().getDimensions();
		
		if (constant.length != yDim || constant[0].length != xDim)
		{
			throw new SolverFactorCreationException("Constant matrix does not have expected dimensions");
		}
		
		_constant = constant;
		
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		MutlivariateGaussianMatrixProduct matMult = new MutlivariateGaussianMatrixProduct(_constant);
		
		char direction;
		if (outPortNum == 0)
			direction = 'F';
		else
			direction = 'R';
		
		MultivariateNormalParameters outMsg = getSiblingEdgeState(outPortNum).factorToVarMsg;
		MultivariateNormalParameters inMsg = getSiblingEdgeState(1-outPortNum).varToFactorMsg;
		
		matMult.ComputeMsg(inMsg, outMsg, direction);
	}
	
	
	/**
	 * Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	 * @deprecated as of release 0.08
	 */
	@Deprecated
	public static boolean isFactorCompatible(Factor factor)
	{
		// Must be of the form form y = A*x where either A is a constant matrix.
		if (factor.getSiblingCount() != 2)
			return false;

		// Must have exactly one constant
		if (factor.getConstantCount() != 1)
			return false;
		
		Variable y = factor.getSibling(0);
		Variable x = factor.getSibling(1);

		RealJointDomain yDomain = y.getDomain().asRealJoint();
		RealJointDomain xDomain = x.getDomain().asRealJoint();
		
		// Variables must be unbounded multivariate reals
		if (yDomain == null || xDomain == null || yDomain.isBounded() || xDomain.isBounded())
		{
			return false;
		}
		
		// Constant must be a matrix of the proper size
		int yDimension = yDomain.getDimensions();
		int xDimension = xDomain.getDimensions();
		Object constant = factor.getConstantValues().get(0).getObject();
		if (!(constant instanceof double[][]))
			return false;
		double[][] dConstant = (double[][])constant;
		if (dConstant.length != yDimension || dConstant[0].length != xDimension)
			return false;

		return true;
	}


}
