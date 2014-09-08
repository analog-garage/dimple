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

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;

public class CustomMultivariateGaussianSum extends MultivariateGaussianFactorBase
{
	protected int _sumIndex;
	private int _sumPort;
	private double[] _constantSum = ArrayUtil.EMPTY_DOUBLE_ARRAY;

	
	public CustomMultivariateGaussianSum(Factor factor)
	{
		super(factor);
		_sumIndex = 0;		// Index that is the sum of all the others
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		MultivariateNormalParameters outMsg = _outputMsgs[outPortNum];
		
		int size = outMsg.getMean().length;
		
		double [] mean = new double[size];
		double [][] covariance = new double[size][];
		for (int i = 0; i < covariance.length; i++)
			covariance[i] = new double[size];
		
		
		// Initialize mean with sum of constants
		if (outPortNum == _sumPort)
			for (int i = 0; i < size; i++)
				mean[i] = _constantSum[i];		// For the sum, use constant sum directly
		else
			for (int i = 0; i < size; i++)
				mean[i] = -_constantSum[i];		// For summands, use negative of constant sum

		
		for (int i = 0, end = _factor.getSiblingCount(); i < end; i++ )
		{
			if (i != outPortNum)
			{
				MultivariateNormalParameters inMsg = _inputMsgs[i];
				
				double [] inMsgMean = inMsg.getMean();
				
				for (int j = 0; j < mean.length; j++)
				{
					if (outPortNum != _sumPort && i != _sumPort)
						mean[j] -= inMsgMean[j];
					else
						mean[j] += inMsgMean[j];
				}
				
				double [][] inMsgCovariance = inMsg.getCovariance();
				
				for (int j = 0; j < inMsgCovariance.length; j++)
					for (int k = 0; k < inMsgCovariance[j].length; k++)
						covariance[j][k] += inMsgCovariance[j][k];
			}
		}
		
		outMsg.setMeanAndCovariance(mean, covariance);
	}
	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Pre-compute sum associated with any constant edges
		FactorFunction factorFunction = _factor.getFactorFunction();
		_sumPort = factorFunction.isConstantIndex(_sumIndex) ? -1 : _sumIndex;	// If sum isn't a variable, then set port to invalid value
		int dimension = _inputMsgs[0].getVectorLength();
		_constantSum = new double[dimension];	// Assume all zero
		if (factorFunction.hasConstants())
		{
			Object[] constantValues = factorFunction.getConstants();
			int[] constantIndices = factorFunction.getConstantIndices();
			for (int i = 0; i < constantValues.length; i++)
			{
				double[] constantValue = (double[])constantValues[i];
				if (constantIndices[i] == _sumIndex)
					for (int v = 0; v < dimension; v++)
						_constantSum[v] -= constantValue[v];	// Constant sum value counts as negative
				else
					for (int v = 0; v < dimension; v++)
						_constantSum[v] += constantValue[v];	// Constant summand value counts as positive
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
			
			// Must be unbounded multivariate real
			if (!domain.isRealJoint() || domain.isBounded())
			{
				return false;
			}
		}
		return true;
	}


}
