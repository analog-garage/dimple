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

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.*;
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
	public void initialize() 
	{
		for (int i = 0; i < _inputMsgs.length; i++)
		{
			MultivariateVariable sv = (MultivariateVariable)_factor.getSiblings().get(i).getSolver();
			_inputMsgs[i] = (MultivariateMsg)sv.resetMessage(_inputMsgs[i]);
		}
	}

	@Override
	protected void createMessages() 
	{
		int numPorts = _factor.getSiblings().size();
	    _inputMsgs = new MultivariateMsg[numPorts];
	    _outputMsgs = new MultivariateMsg[numPorts];
	    
	    for (int port = 0; port < numPorts; port++) 
	    	_inputMsgs[port] = (MultivariateMsg)((ISolverVariable)(_factor.getSiblings().get(port).getSolver())).createDefaultMessage();

	}

	//TODO: genericize this?
	@Override
	protected void connectToVariables() 
	{
		//messages were created in constructor
		int index = 0;
		for (VariableBase vb : _factor.getVariables())
		{
			ISolverVariable sv = vb.getSolver();
			_outputMsgs[index] = (MultivariateMsg) sv.createMessages(this, _inputMsgs[index]);
			index++;
		}		
		
	}

}
