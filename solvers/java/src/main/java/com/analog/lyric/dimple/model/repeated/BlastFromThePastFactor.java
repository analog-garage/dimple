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

package com.analog.lyric.dimple.model.repeated;

import com.analog.lyric.dimple.FactorFunctions.NopFactorFunction;
import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;

public class BlastFromThePastFactor extends Factor 
{
	
	public BlastFromThePastFactor(int id, FactorFunction factorFunc,
			VariableBase[] variables) {
		super(id, factorFunc, variables);
		// TODO Auto-generated constructor stub
	}

	private Object _msg;
	//private Port _factorPort;
	private Port _variablePort;
	//private Object _initMsg;
	private Port _newVarPort;
	
	public BlastFromThePastFactor(int id, VariableBase var, Port oldVariablePort) 
	{
		super(id,new NopFactorFunction("BlastFromThePast"),new VariableBase[]{var});
		_variablePort = oldVariablePort;
		_newVarPort = new Port(var,-1);
	}

	public void advance()
	{
		setOutputMsg(_variablePort.getInputMsg());
	}
	
	public void setOutputMsg(Object msg) 
	{
		if (_newVarPort.index == -1)
			_newVarPort.index = _newVarPort.node.getPortNum(this);
		_msg = msg;
		getSiblings().get(0).getSolver().setInputMsg(_newVarPort.index, _msg);
	}
		
	
	@Override
	public void update()  
	{		
	}

	@Override
	public void updateEdge(int outPortNum)  
	{	
	}
	
	
}
