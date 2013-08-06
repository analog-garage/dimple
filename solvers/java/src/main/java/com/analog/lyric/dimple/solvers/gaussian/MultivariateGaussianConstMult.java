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

package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;

public class MultivariateGaussianConstMult extends MultivariateFactorBase 
{
	private double [][] _constant;
	
	public MultivariateGaussianConstMult(Factor factor)  
	{
		super(factor);
		
		//Make sure this is of the form a = b*c where either b or c is a constant.
		if (factor.getSiblings().size() != 2)
			throw new DimpleException("factor must be of form a = b*c where b is a constant matrix");
		
		//TODO: alternatively, one of the ports could be a discrete variable with a single domain
		//      element
		
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)factor.getFactorFunction();
		if (ff.getConstants().length != 1)
			throw new DimpleException("expected one constant");
		
		double [][] constant; 
		if (ff.getConstants()[0] instanceof double [])
		{
			constant = new double[][] {(double[])ff.getConstants()[0]};
		}
		else
		{
			constant = (double[][])ff.getConstants()[0];
		}
		
		if (ff.getConstantIndices()[0] != 1)
			throw new DimpleException("Expect matrix to be second arg");
		
		
		VariableBase a = (VariableBase)_factor.getSiblings().get(0);
		VariableBase b = (VariableBase)_factor.getSiblings().get(1);
		
		if (a.getDomain().isDiscrete() || b.getDomain().isDiscrete())
			throw new DimpleException("Variables must be reals");
		
		_constant = constant;
		
	}

	@Override
	public void updateEdge(int outPortNum)  
	{
		multiGaBPMatrixMult matMult = new multiGaBPMatrixMult(_constant);
		
		char direction;
		if (outPortNum == 0)
			direction = 'F';
		else
			direction = 'R';
		
		MultivariateMsg outMsg = _outputMsgs[outPortNum]; 
		MultivariateMsg inMsg = _inputMsgs[1-outPortNum];
		
		matMult.ComputeMsg(inMsg, outMsg, direction);
	}

}
