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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

public class CustomMultivariateGaussianProduct extends MultivariateGaussianFactorBase
{
	private double [][] _constant;
	
	public CustomMultivariateGaussianProduct(Factor factor)
	{
		super(factor);
		
		// Make sure this is of the form a = b*c where either b or c is a constant.
		if (factor.getSiblingCount() != 2)
			throw new DimpleException("factor must be of form a = b*c where b is a constant matrix");
		
		// TODO: alternatively, one of the ports could be a discrete variable with a single domain element
		
		FactorFunction ff = factor.getFactorFunction();
		if (ff.getConstantCount() != 1)
			throw new DimpleException("expected one constant");
		
		double[][] constant;
		if (ff.getConstants()[0] instanceof double[])
			constant = new double[][] {(double[])ff.getConstants()[0]};
		else
			constant = (double[][])ff.getConstants()[0];
		
		if (!ff.isConstantIndex(1))
			throw new DimpleException("Expect matrix to be second arg");
		
		
		VariableBase a = _factor.getSibling(0);
		VariableBase b = _factor.getSibling(1);
		
		if (a.getDomain().isDiscrete() || b.getDomain().isDiscrete())
			throw new DimpleException("Variables must be reals");
		
		_constant = constant;
		
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		MutlivariateGaussianMatrixProduct matMult = new MutlivariateGaussianMatrixProduct(_constant);
		
		char direction;
		if (outPortNum == 0)
			direction = 'F';
		else
			direction = 'R';
		
		MultivariateNormalParameters outMsg = _outputMsgs[outPortNum];
		MultivariateNormalParameters inMsg = _inputMsgs[1-outPortNum];
		
		matMult.ComputeMsg(inMsg, outMsg, direction);
	}

}
