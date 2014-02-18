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
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SRealJointVariable;

public class MultivariateNormalMessageTranslator extends MessageTranslatorBase
{
	private MultivariateNormalParameters _inputMessage;
	private MultivariateNormalParameters _outputMessage;
	private MultivariateNormal _variableInput;
	private com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable _solverVariable;

	public MultivariateNormalMessageTranslator(Port port, VariableBase variable)
	{
		super(port, variable);
		
		if (!(_port.getConnectedNode() instanceof RealJoint))
			throw new DimpleException("Expected Real variable.");
	}


	@Override
	public final void setMessageDirection(MessageDirection messageDirection)
	{
		if (messageDirection == MessageDirection.OUTPUT)
			_solverVariable.saveAllSamples();
		else
			_solverVariable.disableSavingAllSamples();
	}
	
	@Override
	public final void setVariableInputFromInputMessage()
	{
		MultivariateNormalParameters inputMessage = (MultivariateNormalParameters)getInputMessage();
		if (inputMessage.isNull())
		{
			_variable.setInputObject(null);		// If zero precision, then set the input to null to avoid numerical issues
		}
		else
		{
			if (_variableInput == null)
				_variableInput = new MultivariateNormal(inputMessage);
			else
				_variableInput.setParameters(inputMessage);
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
		List<double[]> sampleValues = _solverVariable._getSampleArrayUnsafe();
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
		
		_outputMessage.setMeanAndCovariance(mean, covariance);
	}
	
	
	@Override
	public final void initialize()
	{
		SRealJointVariable var = (SRealJointVariable)_port.node.getSibling(_port.index).getSolver();
		_outputMessage = (MultivariateNormalParameters)var.resetInputMessage(_outputMessage);
		_inputMessage = (MultivariateNormalParameters)var.resetInputMessage(_inputMessage);
		_solverVariable = (com.analog.lyric.dimple.solvers.gibbs.SRealJointVariable)_variable.getSolver();
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
		_inputMessage = ((MultivariateNormalMessageTranslator)other)._inputMessage;
		_outputMessage = ((MultivariateNormalMessageTranslator)other)._outputMessage;
	}
}
