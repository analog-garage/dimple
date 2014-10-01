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

import java.util.Objects;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscrete;
import org.eclipse.jdt.annotation.Nullable;

public class DiscreteMessageTranslator extends MessageTranslatorBase
{
	private @Nullable double[] _inputMessage;
	private @Nullable double[] _outputMessage;

	public DiscreteMessageTranslator(Port port, Variable variable)
	{
		super(port, variable);
	
		if (!(_port.getConnectedNode() instanceof Discrete))
			throw new DimpleException("Expected Discrete variable.");
	}

	@Override
	public final void setMessageDirection(MessageDirection messageDirection)
	{
		// Nothing to be done
	}
	
	@Override
	public final void setVariableInputFromInputMessage()
	{
		if (!_variable.hasFixedValue())		// Only set the input if there isn't already a fixed value
			_variable.setInputObject(getInputMessage());
	}
	
	@Override
	public final void setVariableInputUniform()
	{
		if (!_variable.hasFixedValue())		// Only set the input if there isn't already a fixed value
			_variable.setInputObject(null);
	}

	@SuppressWarnings("null")
	@Override
	public final void setOutputMessageFromVariableBelief()
	{
		double[] message = (double[])_variable.getBeliefObject();
		System.arraycopy(message, 0, _outputMessage, 0, message.length);
	}
	

	@SuppressWarnings("null")
	@Override
	public final void initialize()
	{
		SumProductDiscrete var = (SumProductDiscrete)_port.node.getSibling(_port.index).getSolver();
		_outputMessage = var.resetInputMessage(Objects.requireNonNull(_outputMessage));
		_inputMessage = var.resetInputMessage(Objects.requireNonNull(_inputMessage));
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
	public final @Nullable Object getInputMessage()
	{
		return _inputMessage;
	}
	
	@Override
	public final @Nullable Object getOutputMessage()
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
