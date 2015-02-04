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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class SDiscreteEnergyEdge extends SDiscreteEdge<DiscreteEnergyMessage>
{

	/**
	 * @param varToFactorMsg
	 * @param factorToVarMsg
	 * @since 0.08
	 */
	public SDiscreteEnergyEdge(DiscreteEnergyMessage varToFactorMsg, DiscreteEnergyMessage factorToVarMsg)
	{
		super(varToFactorMsg, factorToVarMsg);
	}

	public SDiscreteEnergyEdge(int size)
	{
		super(new DiscreteEnergyMessage(size), new DiscreteEnergyMessage(size));
	}
	
	public SDiscreteEnergyEdge(Discrete var)
	{
		this(var.getDomain().size());
	}
}
