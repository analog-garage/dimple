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

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunctionWithConstants;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.RealJoint;
import com.analog.lyric.dimple.model.VariableBase;



public class GaussianConstMult extends GaussianFactorBase
{

	private double _constant;
	private int _varIndex;
	
	public GaussianConstMult(Factor factor) 
	{
		super(factor);
		
		//Make sure this is of the form a = b*c where either b or c is a constant.
		if (factor.getSiblings().size() != 2)
			throw new DimpleException("Factor must be of form a = b*c where b or c is a constant");
		
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)factor.getFactorFunction();
		if (ff.getConstants().length != 1)
			throw new DimpleException("Expected one constant");
		double constant = (Double)ff.getConstants()[0];
		
		
		VariableBase a = (VariableBase)factor.getSiblings().get(0);
		VariableBase b = (VariableBase)factor.getSiblings().get(1);
		
		if (a.getDomain().isDiscrete() || b.getDomain().isDiscrete())
			throw new DimpleException("Variables must be reals");
		
		_varIndex = 1;
		
		_constant = constant;
		
		if (_constant == 0)
			throw new DimpleException("Constant of 0 not supported");
		
	}	

	public void updateEdge(int outPortNum) 
	{
		if (outPortNum == 0)
		{
			updateProduct();
		}
		else if (outPortNum == _varIndex)
		{
			updateVariable();
		}
	}


	public void updateProduct()
	{
		double [] outMsg = _outputMsgs[0];
		double [] inMsg = _inputMsgs[_varIndex];
		
		//Up = C*Uv
		outMsg[0] = inMsg[0]*_constant;
		
		//SigmaP = C*sigmav
		outMsg[1] = inMsg[1]*_constant;
		
	}

	public void updateVariable()
	{
		double [] outMsg = _outputMsgs[_varIndex];
		double [] inMsg = _inputMsgs[0];
		
		//Uv = Up/C
		outMsg[0] = inMsg[0]/_constant;
		
		//Sigmav = sigmaP/c
		outMsg[1] = inMsg[1]/Math.abs(_constant);

	}
	
	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		// Must be of the form form a = b*c where either b or c is a constant.
		if (factor.getSiblings().size() != 2)
			return false;

		// Must have exactly one constant
		FactorFunctionWithConstants ff = (FactorFunctionWithConstants)factor.getFactorFunction();
		if (ff.getConstants().length != 1)
			return false;
		double constant = (Double)ff.getConstants()[0];
		
		// Variables must be real and univariate
		VariableBase a = (VariableBase)factor.getSiblings().get(0);
		VariableBase b = (VariableBase)factor.getSiblings().get(1);
		if (a.getDomain().isDiscrete() || b.getDomain().isDiscrete())
			return false;
		if (a instanceof RealJoint || b instanceof RealJoint)
			return false;
		
		// Constant must be non-zero
		if (constant == 0)
			return false;

		return true;
	}



}
