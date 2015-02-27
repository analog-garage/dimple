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

import static java.util.Objects.*;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.DoubleArrayList;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductReal;

public class NormalMessageTranslator extends MessageTranslatorBase
{
	private @Nullable NormalParameters _inputMessage;
	private @Nullable NormalParameters _outputMessage;
	private final Normal _variableInput;
	private final GibbsReal _solverVariable;

	public NormalMessageTranslator(SampledFactor sfactor, int edgeIndex, GibbsReal svariable)
	{
		super(sfactor, edgeIndex, svariable.getModelObject());
		_solverVariable = svariable;
		_variableInput = new Normal(0,1);	// Create a Normal factor function with fixed parameters to be used later as the variable input
	}


	@SuppressWarnings("null")
	@Override
	public final void setMessageDirection(MessageDirection messageDirection)
	{
		_solverVariable.setOption(GibbsOptions.saveAllSamples, messageDirection == MessageDirection.OUTPUT);
	}
	
	@Override
	public final void setVariableInputFromInputMessage()
	{
		if (!_variable.hasFixedValue())		// Only set the input if there isn't already a fixed value
		{
			NormalParameters inputMessage = (NormalParameters)requireNonNull(getInputMessage());
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
	}
	
	@Override
	public final void setVariableInputUniform()
	{
		if (!_variable.hasFixedValue())		// Only set the input if there isn't already a fixed value
			_variable.setInputObject(null);
	}

	@Override
	public final void setOutputMessageFromVariableBelief()
	{
		final NormalParameters outputMessage = Objects.requireNonNull(_outputMessage);
		
		// Get the raw sample array to avoid making a copy; this is unsafe, so be careful not to modify it
		@SuppressWarnings("null")
		DoubleArrayList sampleValues = _solverVariable._getSampleArrayUnsafe();
		@SuppressWarnings("null")
		int numSamples = sampleValues.size();

		// For all sample values, compute the output message
		double sum = 0;
		double sumsq = 0;
		for (int i = 0; i < numSamples; i++)
		{
			double tmp = sampleValues.get(i);
			if (Double.isInfinite(tmp) || Double.isNaN(tmp))
			{
				outputMessage.setNull();
				return;
			}
			sum += tmp;
			sumsq += tmp*tmp;
		}
		double mean = sum / numSamples;
		double variance = (sumsq - sum*mean) / (numSamples - 1);
		
		outputMessage.setMean(mean);
		outputMessage.setVariance(variance);
	}
	
	
	@SuppressWarnings("null")
	@Override
	public final void initialize()
	{
		SumProductReal var = (SumProductReal)_sfactor.getSibling(_edgeIndex);
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
		_inputMessage = ((NormalMessageTranslator)other)._inputMessage;
		_outputMessage = ((NormalMessageTranslator)other)._outputMessage;
	}
}
