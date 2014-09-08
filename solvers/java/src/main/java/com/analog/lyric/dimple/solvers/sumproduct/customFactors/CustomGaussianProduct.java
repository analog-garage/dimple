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
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;



public class CustomGaussianProduct extends GaussianFactorBase
{

	private double _constant;
	private final static int PRODUCT_INDEX = 0;
	private final static int MULTIPLICAND_INDEX = 1;
	
	public CustomGaussianProduct(Factor factor)
	{
		super(factor);
		
		// Make sure this is of the form a = b*c where either b or c is a non-zero constant.
		if (factor.getSiblingCount() != 2)
			throw new DimpleException("Factor must be of form a = b*c where b or c is a constant");
		
		FactorFunction ff = factor.getFactorFunction();
		if (ff.getConstantCount() != 1)
			throw new DimpleException("Expected one constant");
		_constant = (Double)ff.getConstants()[0];
		if (_constant == 0)
			throw new DimpleException("Constant of 0 not supported");
		
		Variable a = factor.getSibling(0);
		Variable b = factor.getSibling(1);
		
		if (a.getDomain().isDiscrete() || b.getDomain().isDiscrete())
			throw new DimpleException("Variables must be reals");
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
		NormalParameters outMsg = _outputMsgs[PRODUCT_INDEX];
		NormalParameters inMsg = _inputMsgs[MULTIPLICAND_INDEX];
		
		// Up = C*Uv
		outMsg.setMean(inMsg.getMean() * _constant);
		
		// SigmaP = C*sigmav
		outMsg.setStandardDeviation(inMsg.getStandardDeviation() * _constant);
	}

	private void updateMultiplicand()
	{
		NormalParameters outMsg = _outputMsgs[MULTIPLICAND_INDEX];
		NormalParameters inMsg = _inputMsgs[PRODUCT_INDEX];
		
		// Uv = Up/C
		outMsg.setMean(inMsg.getMean() / _constant);
		
		// Sigmav = sigmaP/c
		outMsg.setStandardDeviation(inMsg.getStandardDeviation() / Math.abs(_constant));
	}
	
	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		// Must be of the form form a = b*c where either b or c is a constant.
		if (factor.getSiblingCount() != 2)
			return false;

		// Must have exactly one constant
		FactorFunction ff = factor.getFactorFunction();
		if (ff.getConstantCount() != 1)
			return false;
		
		// Variables must be real and univariate
		Variable a = factor.getSibling(0);
		Variable b = factor.getSibling(1);
		
		Domain aDomain = a.getDomain();
		Domain bDomain = b.getDomain();
		
		if (!aDomain.isReal() || !bDomain.isReal() || aDomain.isBounded() || bDomain.isBounded())
		{
			return false;
		}
		
		// Constant must be non-zero
		double constant = (Double)ff.getConstants()[0];
		if (constant == 0)
			return false;

		return true;
	}

}
