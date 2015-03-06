
package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

/**
 * Solver variable for finite field factors under Sum-Product solver
 * 
 * @since 0.07
 */
public abstract class SumProductFiniteFieldFactor extends SFactorBase
{

	public SumProductFiniteFieldFactor(Factor factor, ISolverFactorGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		return getEdge(portIndex).varToFactorMsg;
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return getEdge(portIndex).factorToVarMsg;
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum)
	{
	}

	/*---------------
	 * SNode methods
	 */
	
	@Override
	public DiscreteMessage cloneMessage(int edge)
	{
		return getEdge(edge).factorToVarMsg.clone();
	}
	
	@Override
	public boolean supportsMessageEvents()
	{
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public SumProductDiscreteEdge getEdge(int siblingIndex)
	{
		return (SumProductDiscreteEdge)super.getEdge(siblingIndex);
	}
}
