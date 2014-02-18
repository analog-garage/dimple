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

import java.util.List;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SRealVariable;

public class NormalMessageTranslator extends MessageTranslatorBase
{
	private NormalParameters _inputMessage;
	private NormalParameters _outputMessage;
	private Normal _variableInput;

	public NormalMessageTranslator(Port port, VariableBase variable)
	{
		super(port, variable);
		
		if (!(_port.getConnectedNode() instanceof Real))
			throw new DimpleException("Expected Real variable.");
		
		_variableInput = new Normal(0,1);	// Create a Normal factor function with fixed parameters to be used later as the variable input
	}


	@Override
	public final void setVariableInputFromInputMessage()
	{
		NormalParameters inputMessage = (NormalParameters)getInputMessage();
		if (inputMessage.getPrecision() == 0)
		{
			_variable.setInputObject(null);		// If zero precision, then set the input to null to avoid numerical issues
		}
		else
		{
			_variableInput.setMean(inputMessage.getMean());
			_variableInput.setPrecision(inputMessage.getPrecision());
			_variable.setInputObject(_variableInput);
		}
	}
	
	@Override
	public final void setVariableInputUniform()
	{
		_variable.setInputObject(null);
	}

	@Override
	public final void setOutputMessageFromVariableBelief()
	{
		// Get the raw sample array to avoid making a copy; this is unsafe, so be careful not to modify it
		List<Double> sampleValues = ((com.analog.lyric.dimple.solvers.gibbs.SRealVariable)(_variable.getSolver()))._getSampleArrayUnsafe();
		int numSamples = sampleValues.size();

		// For all sample values, compute the output message
		double sum = 0;
		double sumsq = 0;
		for (int i = 0; i < numSamples; i++)
		{
			double tmp = sampleValues.get(i);
			if (Double.isInfinite(tmp) || Double.isNaN(tmp))
			{
				_outputMessage.setNull();				
				return;
			}
			sum += tmp;
			sumsq += tmp*tmp;
		}
		double mean = sum / numSamples;
		double variance = (sumsq - sum*mean) / (numSamples - 1);
		
		_outputMessage.setMean(mean);
		_outputMessage.setVariance(variance);
	}
	
	
	@Override
	public final void initialize()
	{
		SRealVariable var = (SRealVariable)_port.node.getSibling(_port.index).getSolver();
		_outputMessage = (NormalParameters)var.resetInputMessage(_outputMessage);
		_inputMessage = (NormalParameters)var.resetInputMessage(_inputMessage);
	}
	
	@Override
	public final void createInputMessage(Object msg)
	{
		_inputMessage = (NormalParameters)msg;
	}

	@Override
	public final void createOutputMessage(Object msg)
	{
		_outputMessage = (NormalParameters)msg;
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
		_inputMessage = ((NormalMessageTranslator)other)._inputMessage;
		_outputMessage = ((NormalMessageTranslator)other)._outputMessage;
	}
}
