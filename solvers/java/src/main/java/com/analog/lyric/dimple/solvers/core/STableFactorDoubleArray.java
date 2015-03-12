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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public abstract class STableFactorDoubleArray extends STableFactorBase
{
	public STableFactorDoubleArray(Factor factor, ISolverFactorGraph parent)
	{
		super(factor, parent);
	}

	@Deprecated
	@Override
	public Object getInputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).varToFactorMsg.representation();
	}

	@Deprecated
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).factorToVarMsg.representation();
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		final DiscreteMessage message = getSiblingEdgeState(portIndex).varToFactorMsg;
		
		if (obj instanceof DiscreteMessage)
		{
			message.setFrom((DiscreteMessage)obj);
		}
		else
		{
			double[] target  = message.representation();
			System.arraycopy(obj, 0, target, 0, target.length);
		}
	}
	
	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		final DiscreteMessage message = getSiblingEdgeState(portIndex).factorToVarMsg;
		
		if (obj instanceof DiscreteMessage)
		{
			message.setFrom((DiscreteMessage)obj);
		}
		else
		{
			double[] target  = message.representation();
			System.arraycopy(obj, 0, target, 0, target.length);
		}
	}

	
	@SuppressWarnings({ "null" })
	@Override
	public SDiscreteEdge<?> getSiblingEdgeState(int siblingIndex)
	{
		return (SDiscreteEdge<?>)getSiblingEdgeState_(siblingIndex);
	}
}
