
package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.IVariableMapList;
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

public abstract class SFiniteFieldFactor extends SFactorBase
{

	protected double [][] _inputMsgs;
	protected double [][] _outputMsgs;
	
	public SFiniteFieldFactor(Factor factor)
	{
		super(factor);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createMessages()
	{

		int numPorts = _factor.getSiblingCount();
		
	    _inputMsgs = new double[numPorts][];
	    _outputMsgs = new double[numPorts][];
	    
	    IVariableMapList variables = _factor.getVariables();
	    
	    for (int index = 0, end = variables.size(); index < end; index++)
	    {
	    	ISolverVariable svar =  variables.getByIndex(index).getSolver();
	    	Object [] messages = svar.createMessages(this);
	    	_outputMsgs[index] = (double[])messages[0];
	    	_inputMsgs[index] = (double[])messages[1];
	    }
	    
	}


	@Override
	public void resetEdgeMessages(int i)
	{
		SVariable sv = (SVariable)_factor.getSibling(i).getSolver();
		_inputMsgs[i] = (double[])sv.resetInputMessage(_inputMsgs[i]);
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}

	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum)
	{

		SFiniteFieldFactor sother = (SFiniteFieldFactor)other;
	    _inputMsgs[thisPortNum] = sother._inputMsgs[otherPortNum];
	    _outputMsgs[thisPortNum] = sother._outputMsgs[otherPortNum];
	}

}
