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

package com.analog.lyric.dimple.solvers.gibbs;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class GibbsDiscreteEdge extends GibbsSolverEdge<DiscreteEnergyMessage>
{
	public GibbsDiscreteEdge(Discrete var)
	{
		super(new DiscreteEnergyMessage(var.getDomain().size()));
	}
	
	@Override
	public void setFactorToVarMsg(@Nullable Object msg)
	{
		if (msg == null)
		{
			factorToVarMsg.setNull();
		}
		else if (msg instanceof double[])
		{
			factorToVarMsg.setWeights((double[])msg);
		}
		else
		{
			factorToVarMsg.setFrom((IParameterizedMessage)msg);
		}
	}
}
