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

import java.util.List;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;

public class CustomMultivariateGaussianSum extends MultivariateGaussianFactorBase
{
	protected int _sumIndex;
	private int _sumPort;
	private double[] _constantSum = ArrayUtil.EMPTY_DOUBLE_ARRAY;

	
	public CustomMultivariateGaussianSum(Factor factor, SumProductSolverGraph parent)
	{
		super(factor, parent);
		_sumIndex = 0;		// Index that is the sum of all the others
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		MultivariateNormalParameters outMsg = getSiblingEdgeState(outPortNum).factorToVarMsg;
		
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

		
		for (int i = 0, end = _model.getSiblingCount(); i < end; i++ )
		{
			if (i != outPortNum)
			{
				MultivariateNormalParameters inMsg = getSiblingEdgeState(i).varToFactorMsg;
				
				if (inMsg.isNull())
				{
					// If any input is null, then no information is provided to the other variables.
					outMsg.setNull();
					return;
				}
				
				double [] inMsgMean = inMsg.getMean();
				
				if (outPortNum != _sumPort && i != _sumPort)
				{
					for (int j = size; --j>=0;)
						mean[j] -= inMsgMean[j];
				}
				else
				{
					for (int j = size; --j>=0;)
						mean[j] += inMsgMean[j];
				}
				
				double [][] inMsgCovariance = inMsg.getCovariance();
				
				for (int j = inMsgCovariance.length; --j>=0;)
				{
					final double[] row = covariance[j], inrow = inMsgCovariance[j];
					for (int k = row.length; --k>=0;)
						row[k] += inrow[k];
				}
			}
		}
		
		outMsg.setMeanAndCovariance(mean, covariance);
	}
	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Pre-compute sum associated with any constant edges
		final Factor factor = _model;
		_sumPort = factor.hasConstantAtIndex(_sumIndex) ? -1 : _sumIndex;	// If sum isn't a variable, then set port to invalid value
		int dimension = getSiblingEdgeState(0).varToFactorMsg.getVectorLength();
		_constantSum = new double[dimension];	// Assume all zero
		if (_model.hasConstants())
		{
			final List<Value> constantValues = factor.getConstantValues();
			int[] constantIndices = factor.getConstantIndices();
			for (int i = 0, n = constantValues.size(); i < n; i++)
			{
				double[] constantValue = constantValues.get(i).getDoubleArray();
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
