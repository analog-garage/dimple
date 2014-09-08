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
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;


public class CustomGaussianNegate extends GaussianFactorBase
{
	public CustomGaussianNegate(Factor factor)
	{
		super(factor);
		
		if (factor.getSiblingCount() != 2)
			throw new DimpleException("Factor must have exactly two connected varaibles");

		for (int i = 0, endi = factor.getSiblingCount(); i < endi; i++)
		{
			Variable v = factor.getSibling(i);
			
			if (v.getDomain().isDiscrete())
				throw new DimpleException("Cannot connect discrete variable to this factor");
		}
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		int inPortNum = 1 - outPortNum;		// Exactly two ports
		
		NormalParameters inputMsg = _inputMsgs[inPortNum];
		NormalParameters outMsg = _outputMsgs[outPortNum];

		outMsg.setMean(-inputMsg.getMean());	// Negate the mean
		outMsg.setPrecision(inputMsg.getPrecision());
	}


	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		
		for (int i = 0, end = factor.getSiblingCount(); i < end; i++)
		{
			Variable v = factor.getSibling(i);
			Domain domain = v.getDomain();
			
			// Must be unbounded real
			if (!domain.isReal() || domain.isBounded())
			{
				return false;
			}
		}
		return true;
	}


}
