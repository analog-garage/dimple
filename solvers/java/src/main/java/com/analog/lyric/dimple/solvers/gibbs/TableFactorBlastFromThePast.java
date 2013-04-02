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

package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.solvers.core.SBlastFromThePast;

public class TableFactorBlastFromThePast extends SBlastFromThePast implements ISolverFactorGibbs 
{
    protected DiscreteSample[] _inPortMsgs = null;
    protected int _numPorts;
	private double [] _outputMsg;
	private DiscreteSample _inputMsg;

	public TableFactorBlastFromThePast(BlastFromThePastFactor f) 
	{
		super(f);
	}

	@Override
	public void createMessages(VariableBase var, Port port)
	{
		super.createMessages(var,port);
		getMessages();
	}
	
	private void getMessages()
	{
		_outputMsg = (double[])getOtherVariablePort().node.getSolver().getInputMsg(getOtherVariablePort().index);
		_inputMsg = (DiscreteSample)getOtherVariablePort().node.getSolver().getOutputMsg(getOtherVariablePort().index);
		
	}
	
	@Override
	public void advance()
	{
		super.advance();
		getMessages();
	}

	@Override
	public double getPotential()
	{
		return _outputMsg[_inputMsg.index];
	}
	
	@Override
	public void updateNeighborVariableValue(int portIndex) 
	{
		throw new DimpleException("not implemented");		
	}

	@Override
	public double getConditionalPotential(int portIndex) 
	{
		throw new DimpleException("not implemented");
	}

	@Override
	public void updateEdgeMessage(int portIndex) 
	{
		//NOP
	}


}
