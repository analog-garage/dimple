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
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public abstract class STableFactorIntArray extends STableFactorBase
{
	public STableFactorIntArray(Factor factor, ISolverFactorGraph parent)
	{
		super(factor, parent);
	}
	
	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
	}

	@Override
	public void setInputMsgValues(int portIndex, Object obj)
	{
		final int[] inputMsg = getEdge(portIndex).varToFactorMsg;
		System.arraycopy(obj,  0, inputMsg, 0, inputMsg.length);
	}
	
	@Override
	public void setOutputMsgValues(int portIndex, Object obj)
	{
		final int[] outputMsg = getEdge(portIndex).factorToVarMsg;
		System.arraycopy(obj,  0, outputMsg, 0, outputMsg.length);
	}

	@SuppressWarnings({ "null", "unchecked" })
	@Override
	public SEdgeWithIntArrayMessages getEdge(int siblingIndex)
	{
		return (SEdgeWithIntArrayMessages) super.getEdge(siblingIndex);
	}
}
