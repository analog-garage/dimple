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

import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscreteEdge;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 * @category internal
 */
@Internal
class SumProductSampledDiscreteEdge extends SumProductDiscreteEdge
	implements ISumProductSampledEdge<DiscreteMessage>
{
	private final GibbsDiscrete _svar;
	
	SumProductSampledDiscreteEdge(GibbsDiscrete svar)
	{
		super(svar.getModelObject());
		_svar = svar;
		setVariableInputUniform();
	}
	
	@Override
	public void moveMessages(ISumProductSampledEdge<?> other)
	{
		varToFactorMsg.setFrom(((SumProductSampledDiscreteEdge)other).varToFactorMsg);
		factorToVarMsg.setFrom(((SumProductSampledDiscreteEdge)other).factorToVarMsg);
	}
	
	@Override
	public void setFactorToVarDirection()
	{
		setVariableInputUniform();
	}
	
	@Override
	public void setVarToFactorDirection()
	{
		Discrete var = _svar.getModelObject();
		if (!var.hasFixedValue())		// Only set the input if there isn't already a fixed value
			var.setInputObject(varToFactorMsg.representation());
	}
	
	@Override
	public void setFactorToVarMsgFromSamples()
	{
		factorToVarMsg.setWeights(_svar.getBelief());
	}
	
	private void setVariableInputUniform()
	{
		Discrete var = _svar.getModelObject();
		if (!var.hasFixedValue())		// Only set the input if there isn't already a fixed value
		{
			var.setInputObject(null);
		}
	}
}
