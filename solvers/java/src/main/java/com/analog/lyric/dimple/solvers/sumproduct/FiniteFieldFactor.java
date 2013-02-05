
package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
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

public abstract class FiniteFieldFactor extends SFactorBase
{

	protected double [][] _inputMsgs;
	protected double [][] _outputMsgs;
	
	public FiniteFieldFactor(Factor factor) 
	{
		super(factor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createMessages() 
	{

		int numPorts = _factor.getSiblings().size();
		
	    _inputMsgs = new double[numPorts][];
	    _outputMsgs = new double[numPorts][];
	    
	    for (int index = 0; index < _factor.getVariables().size(); index++)
	    {
	    	ISolverVariable svar =  _factor.getVariables().getByIndex(index).getSolver();
	    	Object [] messages = svar.createMessages(this);
	    	_outputMsgs[index] = (double[])messages[0];
	    	_inputMsgs[index] = (double[])messages[1];
	    }
	    
	}


	@Override
	public void initialize(int i) 
	{
		SVariable sv = (SVariable)_factor.getSiblings().get(i).getSolver();
		_inputMsgs[i] = (double[])sv.resetInputMessage(_inputMsgs[i]);
	}

	@Override
	public Object getInputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		return _outputMsgs[portIndex];
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{

		FiniteFieldFactor sother = (FiniteFieldFactor)other;
	    _inputMsgs[thisPortNum] = sother._inputMsgs[otherPortNum];
	    _outputMsgs[thisPortNum] = sother._outputMsgs[otherPortNum];
	}

}
