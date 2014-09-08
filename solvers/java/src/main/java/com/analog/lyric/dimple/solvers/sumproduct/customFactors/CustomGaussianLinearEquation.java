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

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.factorfunctions.LinearEquation;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;

public class CustomGaussianLinearEquation extends GaussianFactorBase
{
	private double[] _weightVector = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	private double _initialWeightedSum;

	
	public CustomGaussianLinearEquation(Factor factor)
	{
		super(factor);
	}
	
	
	@Override
	public void doUpdateEdge(int outPortNum)
	{
		double mean;
		double variance;
		
		if (_weightVector[outPortNum] == 0)
		{
			mean = 0;
			variance = Double.POSITIVE_INFINITY;
		}
		else
		{
			mean = _initialWeightedSum;
			variance = 0;
			
			for (int i = 0; i < _inputMsgs.length; i++)
			{
				if (i != outPortNum)
				{
					double constantsi = _weightVector[i];
					NormalParameters msg = _inputMsgs[i];
					mean -= msg.getMean() * constantsi;
					variance += msg.getVariance() * constantsi * constantsi;
				}
			}
			double constantsout = _weightVector[outPortNum];
			mean /= constantsout;
			variance /= (constantsout*constantsout);
		}
		 
		NormalParameters msg = _outputMsgs[outPortNum];
		msg.setMean(mean);
		msg.setVariance(variance);
	}
	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Pre-compute for any constant edges
		FactorFunction factorFunction = _factor.getFactorFunction();
		LinearEquation specificFactorFunction = (LinearEquation)factorFunction.getContainedFactorFunction();
		
		
		double[] specifiedWeightVector = specificFactorFunction.getWeightArray();
		double[] extendedWeightVector = new double[specifiedWeightVector.length + 1];
		System.arraycopy(specifiedWeightVector, 0, extendedWeightVector, 1, specifiedWeightVector.length);
		extendedWeightVector[0] = -1; // Treat output as another variable with constant -1, so append to beginning of weight vector
		int extendedWeigthVectorLength = extendedWeightVector.length;
		
		// Account for constant and variable inputs; pre-compute a weighted sum for all constant inputs
		_initialWeightedSum = 0;
		_weightVector = new double[extendedWeigthVectorLength - factorFunction.getConstantCount()];
		for (int index = 0, edge = 0; index < extendedWeigthVectorLength; index++)
		{
			if (factorFunction.isConstantIndex(index))
			{
				// Constant in this position, so subtract off the initial weighted sum (move to the other side of the equation)
				_initialWeightedSum -=
					extendedWeightVector[index] * (Double)requireNonNull(factorFunction.getConstantByIndex(index));
			}
			else
			{
				// Variable in this position, so include in the weight vector
				_weightVector[edge++] = extendedWeightVector[index];
			}
		}
		
	}
	
	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		for (int i = 0, end = factor.getSiblingCount(); i < end; i++)
		{
			Variable v = factor.getSibling(i);
			
			Domain domain = v.getDomain();
			
			// Must be an unbounded univariate real
			if (!domain.isReal() || domain.isBounded())
			{
				return false;
			}
		}
		return true;
	}

}
