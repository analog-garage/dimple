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
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

public class CustomMultivariateGaussianSum extends MultivariateGaussianFactorBase
{

	public CustomMultivariateGaussianSum(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdge(int outPortNum)
	{
		MultivariateNormalParameters outMsg = _outputMsgs[outPortNum];
		
		int size = outMsg.getMean().length;
		
		double [] vector = new double[size];
		double [][] matrix = new double[size][];
		for (int i = 0; i < matrix.length; i++)
			matrix[i] = new double[size];
		
		for (int i = 0, end = _factor.getSiblingCount(); i < end; i++ )
		{
			if (i != outPortNum)
			{
				MultivariateNormalParameters inMsg = _inputMsgs[i];
				
				double [] inMsgVector = inMsg.getMean();
				
				for (int j = 0; j < vector.length; j++)
				{
					if (outPortNum != 0 && i != 0)
						vector[j] -= inMsgVector[j];
					else
						vector[j] += inMsgVector[j];
				}
				
				double [][] inMsgMatrix = inMsg.getCovariance();
				
				for (int j = 0; j < inMsgMatrix.length; j++)
				{
					for (int k = 0; k < inMsgMatrix[j].length; k++)
					{
						matrix[j][k] += inMsgMatrix[j][k];
					}
				}
			}
		}
		
		outMsg.setMeanAndCovariance(vector,matrix);
	}
	
	
	// Utility to indicate whether or not a factor is compatible with the requirements of this custom factor
	public static boolean isFactorCompatible(Factor factor)
	{
		
		for (int i = 0, end = factor.getSiblingCount(); i < end; i++)
		{
			VariableBase v = factor.getSibling(i);
			
			// Must be real
			if (v.getDomain().isDiscrete())
				return false;
			
			// Must be multivariate
			if (v instanceof Real)
				return false;
		}
		return true;
	}


}
