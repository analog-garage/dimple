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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import static java.util.Objects.*;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.NonNull;

public abstract class MultivariateGaussianFactorBase extends SFactorBase
{

	protected MultivariateNormalParameters [] _inputMsgs;
	protected MultivariateNormalParameters [] _outputMsgs;
	
	@SuppressWarnings("null")
	public MultivariateGaussianFactorBase(Factor factor)
	{
		super(factor);
	}


	@Override
	public void resetEdgeMessages(int i)
	{
	}

	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		final int nVars = factor.getSiblingCount();
	    _inputMsgs = new MultivariateNormalParameters[nVars];
	    _outputMsgs = new MultivariateNormalParameters[nVars];

		//messages were created in constructor
		for (int index = 0; index < nVars; ++index)
		{
			ISolverVariable sv = requireNonNull(factor.getSibling(index).getSolver());
			Object [] messages = requireNonNull(sv.createMessages(this));
			_outputMsgs[index] = (MultivariateNormalParameters)messages[0];
			_inputMsgs[index] = (MultivariateNormalParameters)messages[1];
		}
		
	}
	@Override
	public void moveMessages(@NonNull ISolverNode other, int portNum, int otherPort)
	{
		MultivariateGaussianFactorBase s = (MultivariateGaussianFactorBase)other;
	
		_inputMsgs[portNum] = s._inputMsgs[otherPort];
		_outputMsgs[portNum] = s._outputMsgs[otherPort];

	}
	
	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex) {
		return _outputMsgs[portIndex];
	}
	
	/*---------------
	 * SNode methods
	 */
	
	@Override
	protected MultivariateNormalParameters cloneMessage(int edge)
	{
		return _outputMsgs[edge].clone();
	}
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}
}
