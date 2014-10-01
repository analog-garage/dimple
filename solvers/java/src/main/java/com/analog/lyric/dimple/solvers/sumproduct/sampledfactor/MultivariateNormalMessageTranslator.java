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

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealJoint;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductRealJoint;

public class MultivariateNormalMessageTranslator extends MessageTranslatorBase
{
	private @Nullable MultivariateNormalParameters _inputMessage;
	private @Nullable MultivariateNormalParameters _outputMessage;
	private @Nullable MultivariateNormal _variableInput;
	private @Nullable GibbsRealJoint _solverVariable;

	public MultivariateNormalMessageTranslator(Port port, Variable variable)
	{
		super(port, variable);
		
		if (!(_port.getConnectedNode() instanceof RealJoint))
			throw new DimpleException("Expected Real variable.");
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
			MultivariateNormalParameters inputMessage = (MultivariateNormalParameters)requireNonNull(getInputMessage());
			if (inputMessage.isNull())
			{
				_variable.setInputObject(null);		// If zero precision, then set the input to null to avoid numerical issues
			}
			else
			{
				MultivariateNormal variableInput = _variableInput;
				if (variableInput == null)
					variableInput = _variableInput = new MultivariateNormal(inputMessage);
				else
					variableInput.setParameters(inputMessage);
				_variable.setInputObject(variableInput);
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
		// Get the raw sample array to avoid making a copy; this is unsafe, so be careful not to modify it
		@SuppressWarnings("null")
		List<double[]> sampleValues = _solverVariable._getSampleArrayUnsafe();
		@SuppressWarnings("null")
		int numSamples = sampleValues.size();
		int dimension = sampleValues.get(0).length;

		// For all sample values, compute the mean
		double[] mean = new double[dimension];
		for (int sample = 0; sample < numSamples; sample++)
		{
			double[] tmp = sampleValues.get(sample);
			for (int i = 0; i < dimension; i++)
				mean[i] += tmp[i];
		}
		for (int i = 0; i < dimension; i++)
			mean[i] /= numSamples;
		
		// For all sample values, compute the covariance matrix
		double[] diff = new double[dimension];
		double[][] covariance = new double[dimension][dimension];
		for (int sample = 0; sample < numSamples; sample++)
		{
			double[] tmp = sampleValues.get(sample);
			for (int i = 0; i < dimension; i++)
				diff[i] = tmp[i] - mean[i];
			for (int row = 0; row < dimension; row++)
			{
				double[] covarianceRow = covariance[row];
				for (int col = row; col < dimension; col++)		// Compute only the upper triangular half for now
					covarianceRow[col] += diff[row] * diff[col];
			}
		}
		double numSamplesMinusOne = numSamples - 1;
		for (int row = 0; row < dimension; row++)
		{
			for (int col = row; col < dimension; col++)
			{
				double value = covariance[row][col] / numSamplesMinusOne;
				covariance[row][col] = value;
				covariance[col][row] = value;	// Fill in lower triangular half
			}
		}
		
		Objects.requireNonNull(_outputMessage).setMeanAndCovariance(mean, covariance);
	}
	
	
	@SuppressWarnings("null")
	@Override
	public final void initialize()
	{
		SumProductRealJoint var = (SumProductRealJoint)_port.node.getSibling(_port.index).getSolver();
		_outputMessage = (MultivariateNormalParameters)var.resetInputMessage(_outputMessage);
		_inputMessage = (MultivariateNormalParameters)var.resetInputMessage(_inputMessage);
		_solverVariable = (GibbsRealJoint)_variable.getSolver();
	}
	
	@Override
	public final void createInputMessage(Object msg)
	{
		_inputMessage = (MultivariateNormalParameters)msg;
	}

	@Override
	public final void createOutputMessage(Object msg)
	{
		_outputMessage = (MultivariateNormalParameters)msg;
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
		_inputMessage = ((MultivariateNormalMessageTranslator)other)._inputMessage;
		_outputMessage = ((MultivariateNormalMessageTranslator)other)._outputMessage;
	}
}
