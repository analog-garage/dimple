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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteMessage;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscrete;

public class DiscreteMessageTranslator extends MessageTranslatorBase
{
	// FIXME change to DiscreteMessage
	private @Nullable double[] _inputMessage;
	private @Nullable double[] _outputMessage;

	public DiscreteMessageTranslator(SampledFactor sfactor, int edgeIndex, GibbsDiscrete svariable)
	{
		super(sfactor, edgeIndex, svariable.getModelObject());
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
		SumProductDiscrete var = (SumProductDiscrete)_sfactor.getSibling(_edgeIndex);
		_outputMessage = var.resetInputMessage(Objects.requireNonNull(_outputMessage));
		_inputMessage = var.resetInputMessage(Objects.requireNonNull(_inputMessage));
	}

	@Override
	public final void createInputMessage(Object msg)
	{
		if (msg instanceof DiscreteMessage)
		{
			_inputMessage = ((DiscreteMessage) msg).representation();
		}
		else
		{
			_inputMessage = (double[])msg;
		}
	}
	
	@Override
	public final void createOutputMessage(Object msg)
	{
		if (msg instanceof DiscreteMessage)
		{
			_outputMessage = ((DiscreteMessage) msg).representation();
		}
		else
		{
			_outputMessage = (double[])msg;
		}
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
