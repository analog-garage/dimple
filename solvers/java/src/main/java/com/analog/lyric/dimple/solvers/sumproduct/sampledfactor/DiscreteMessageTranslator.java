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

package com.analog.lyric.dimple.solvers.sumproduct.sampledfactor;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.sumproduct.SVariable;

public class DiscreteMessageTranslator extends MessageTranslatorBase
{
	private double[] _inputMessage;
	private double[] _outputMessage;
	

	public DiscreteMessageTranslator(Port port, VariableBase variable)
	{
		super(port, variable);
	
		if (!(_port.getConnectedNode() instanceof Discrete))
			throw new DimpleException("Expected Discrete variable.");
	}

	@Override
	public final void setVariableInputFromInputMessage()
	{
		_variable.setInputObject(getInputMessage());
	}
	
	@Override
	public final void setVariableInputUniform()
	{
		_variable.setInputObject(null);
	}

	@Override
	public final void setOutputMessageFromVariableBelief()
	{
		double[] message = (double[])_variable.getBeliefObject();
		System.arraycopy(message, 0, _outputMessage, 0, message.length);
	}
	

	@Override
	public final void initialize()
	{
		SVariable var = (SVariable)_port.node.getSibling(_port.index).getSolver();
		_outputMessage = (double[])var.resetInputMessage(_outputMessage);
		_inputMessage = (double[])var.resetInputMessage(_inputMessage);
	}

	@Override
	public final void createInputMessage(Object msg)
	{
		_inputMessage = (double[])msg;
	}
	
	@Override
	public final void createOutputMessage(Object msg)
	{
		_outputMessage = (double[])msg;
	}
		
	@Override
	public final Object getInputMessage()
	{
		return _inputMessage;
	}
	
	@Override
	public final Object getOutputMessage()
	{
		return _outputMessage;
	}

	@Override
	public final void moveMessages(MessageTranslatorBase other)
	{
		_inputMessage = ((DiscreteMessageTranslator)other)._inputMessage;
		_outputMessage = ((DiscreteMessageTranslator)other)._outputMessage;
	}

}
