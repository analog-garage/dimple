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

import static java.util.Objects.*;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;


public class CustomNormalConstantParameters extends GaussianFactorBase
{
	private static final int MEAN_PARAMETER_INDEX = 0;
	private static final int PRECISION_PARAMETER_INDEX = 1;

	
	public CustomNormalConstantParameters(Factor factor)
	{
		super(factor);
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
		FactorFunction factorFunction = _factor.getFactorFunction();
		Normal specificFactorFunction = (Normal)factorFunction.getContainedFactorFunction(); 	// In case it's wrapped

		NormalParameters outputMessage;
		if (specificFactorFunction.hasConstantParameters())
			outputMessage = specificFactorFunction.getParameters();
		else if (factorFunction.isConstantIndex(MEAN_PARAMETER_INDEX) && factorFunction.isConstantIndex(PRECISION_PARAMETER_INDEX))
		{
			double mean = requireNonNull((Double)factorFunction.getConstantByIndex(MEAN_PARAMETER_INDEX));
			double precision = requireNonNull((Double)factorFunction.getConstantByIndex(PRECISION_PARAMETER_INDEX));
			outputMessage = new NormalParameters(mean, precision);
		}
		else
			throw new DimpleException("Normal factor must have constant parameters");
		
		// Can set the output messages now since it will never change
		for (int edge = 0; edge < _outputMsgs.length; edge++)
			_outputMsgs[edge].set(outputMessage);
	}
	
	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		FactorFunction factorFunction = factor.getFactorFunction();
		Normal specificFactorFunction = (Normal)factorFunction.getContainedFactorFunction(); 	// In case it's wrapped
		
		boolean constantParameters = false;
		if (specificFactorFunction.hasConstantParameters())
			constantParameters = true;
		else if (factorFunction.isConstantIndex(MEAN_PARAMETER_INDEX) && factorFunction.isConstantIndex(PRECISION_PARAMETER_INDEX))
			constantParameters = true;

		if (!constantParameters)
			return false;
		
		for (int i = 0, end = factor.getSiblingCount(); i < end; i++)
		{
			Variable v = factor.getSibling(i);
			Domain domain = v.getDomain();
			
			// Must be unbounded univariate real
			if (!domain.isReal() || domain.isBounded())
			{
				return false;
			}
		}
		return true;
	}


}
