/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct.sampledfactor;

import java.util.List;

import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealJoint;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductMultivariateNormalEdge;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 * @category internal
 */
@Internal
class SumProductSampledMultivariateNormalEdge extends SumProductMultivariateNormalEdge
	implements ISumProductSampledEdge<MultivariateNormalParameters>
{
	private final GibbsRealJoint _svar;
	
	SumProductSampledMultivariateNormalEdge(GibbsRealJoint svar)
	{
		super(svar.getModelObject());
		_svar = svar;
		setVariableInputUniform();
	}
	
	@Override
	public MultivariateNormalParameters getFactorToVarMsg()
	{
		return factorToVarMsg;
	}
	
	@Override
	public MultivariateNormalParameters getVarToFactorMsg()
	{
		return varToFactorMsg;
	}
	
	@Override
	public void moveMessages(ISumProductSampledEdge<?> other)
	{
		varToFactorMsg.setFrom(((SumProductSampledMultivariateNormalEdge)other).varToFactorMsg);
		factorToVarMsg.setFrom(((SumProductSampledMultivariateNormalEdge)other).factorToVarMsg);
	}
	
	@Override
	public void setFactorToVarDirection()
	{
		_svar.setOption(GibbsOptions.saveAllSamples, true);
		setVariableInputUniform();
	}
	
	@Override
	public void setVarToFactorDirection()
	{
		_svar.setOption(GibbsOptions.saveAllSamples, false);
		RealJoint var = _svar.getModelObject();
		if (!var.hasFixedValue())		// Only set the input if there isn't already a fixed value
		{
			MultivariateNormalParameters inputMessage = varToFactorMsg;
			if (inputMessage.isNull())
			{
				var.setInputObject(null);		// If zero precision, then set the input to null to avoid numerical issues
			}
			else
			{
				var.setInputObject(new MultivariateNormal(inputMessage));
			}
		}
	}
	
	@Override
	public void setFactorToVarMsgFromSamples()
	{
		// Get the raw sample array to avoid making a copy; this is unsafe, so be careful not to modify it
		@SuppressWarnings("null")
		List<double[]> sampleValues = _svar._getSampleArrayUnsafe();
		@SuppressWarnings("null")
		int numSamples = sampleValues.size();
		int dimension = sampleValues.get(0).length;

		// For all sample values, compute the mean
		double[] mean = new double[dimension];
		for (int sample = 0; sample < numSamples; sample++)
		{
			double[] tmp = sampleValues.get(sample);
			for (int i = 0; i < dimension; i++)
				mean[i] += tmp[i];
		}
		for (int i = 0; i < dimension; i++)
			mean[i] /= numSamples;
		
		// For all sample values, compute the covariance matrix
		double[] diff = new double[dimension];
		double[][] covariance = new double[dimension][dimension];
		for (int sample = 0; sample < numSamples; sample++)
		{
			double[] tmp = sampleValues.get(sample);
			for (int i = 0; i < dimension; i++)
				diff[i] = tmp[i] - mean[i];
			for (int row = 0; row < dimension; row++)
			{
				double[] covarianceRow = covariance[row];
				for (int col = row; col < dimension; col++)		// Compute only the upper triangular half for now
					covarianceRow[col] += diff[row] * diff[col];
			}
		}
		double numSamplesMinusOne = numSamples - 1;
		for (int row = 0; row < dimension; row++)
		{
			for (int col = row; col < dimension; col++)
			{
				double value = covariance[row][col] / numSamplesMinusOne;
				covariance[row][col] = value;
				covariance[col][row] = value;	// Fill in lower triangular half
			}
		}
		
		factorToVarMsg.setMeanAndCovariance(mean, covariance);
	}
	
	private final void setVariableInputUniform()
	{
		RealJoint var = _svar.getModelObject();
		if (!var.hasFixedValue())		// Only set the input if there isn't already a fixed value
		{
			var.setInputObject(null);
		}
	}

}
