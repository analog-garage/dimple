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

package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.*;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class MultivariateFactorBase extends SFactorBase
{

	protected MultivariateMsg [] _inputMsgs;
	protected MultivariateMsg [] _outputMsgs;
	
	public MultivariateFactorBase(Factor factor) 
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
		int numPorts = _factor.getSiblings().size();
	    _inputMsgs = new MultivariateMsg[numPorts];
	    _outputMsgs = new MultivariateMsg[numPorts];

		//messages were created in constructor
		int index = 0;
		for (VariableBase vb : _factor.getVariables())
		{
			ISolverVariable sv = vb.getSolver();
			Object [] messages = sv.createMessages(this);
			_outputMsgs[index] = (MultivariateMsg)messages[0]; 
			_inputMsgs[index] = (MultivariateMsg)messages[1];
			index++;
		}		
		
	}
	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort) 
	{
		MultivariateFactorBase s = (MultivariateFactorBase)other;
	
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
}
