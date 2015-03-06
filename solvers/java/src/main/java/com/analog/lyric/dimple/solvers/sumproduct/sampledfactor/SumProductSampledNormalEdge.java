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

import cern.colt.list.DoubleArrayList;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductNormalEdge;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 * @category internal
 */
@Internal
class SumProductSampledNormalEdge extends SumProductNormalEdge
	implements ISumProductSampledEdge<NormalParameters>
{
	private final GibbsReal _svar;
	
	SumProductSampledNormalEdge(GibbsReal svar)
	{
		super();
		_svar = svar;
		setVariableInputUniform();
	}
	
	@Override
	public void moveMessages(ISumProductSampledEdge<?> other)
	{
		varToFactorMsg.setFrom(((SumProductSampledNormalEdge)other).varToFactorMsg);
		factorToVarMsg.setFrom(((SumProductSampledNormalEdge)other).factorToVarMsg);
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
		Real var = _svar.getModelObject();
		if (!var.hasFixedValue())		// Only set the input if there isn't already a fixed value
		{
			NormalParameters inputMessage = varToFactorMsg;
			if (varToFactorMsg.getPrecision() == 0)
			{
				var.setInputObject(null);		// If zero precision, then set the input to null to avoid numerical issues
			}
			else
			{
				var.setInputObject(new Normal(inputMessage));
			}
		}
	}
	
	@Override
	public void setFactorToVarMsgFromSamples()
	{
		final NormalParameters outputMessage = factorToVarMsg;
		
		// Get the raw sample array to avoid making a copy; this is unsafe, so be careful not to modify it
		@SuppressWarnings("null")
		DoubleArrayList sampleValues = _svar._getSampleArrayUnsafe();
		@SuppressWarnings("null")
		int numSamples = sampleValues.size();

		// For all sample values, compute the output message
		double sum = 0;
		double sumsq = 0;
		for (int i = 0; i < numSamples; i++)
		{
			double tmp = sampleValues.get(i);
			if (Double.isInfinite(tmp) || Double.isNaN(tmp))
			{
				outputMessage.setNull();
				return;
			}
			sum += tmp;
			sumsq += tmp*tmp;
		}
		double mean = sum / numSamples;
		double variance = (sumsq - sum*mean) / (numSamples - 1);
		
		outputMessage.setMean(mean);
		outputMessage.setVariance(variance);
	}

	private final void setVariableInputUniform()
	{
		Real var = _svar.getModelObject();
		if (!var.hasFixedValue())		// Only set the input if there isn't already a fixed value
		{
			var.setInputObject(null);
		}
	}

}
