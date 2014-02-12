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
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.sumproduct.SRealVariable;

public class GaussianMessageGenerator implements IMessageGenerator
{
	private Port _p;
	private NormalParameters _inputMessage;
	private NormalParameters _outputMessage;

	public GaussianMessageGenerator(Port p)
	{
		_p = p;
		
		if (!(_p.getConnectedNode() instanceof Real))
			throw new DimpleException("Expected Real variable.");
	}


	@Override
	public void generateOutputMessageFromSamples(Object samples)
	{
		// In this case, input samples are real values
		double[] sampleValues = (double[])samples;
		int numSamples = sampleValues.length;
		
		double mean = 0;
		for (int i = 0; i < numSamples; i++)
		{
			double tmp = sampleValues[i];
			if (Double.isInfinite(tmp) || Double.isNaN(tmp))
			{
				_outputMessage.setNull();				
				return;
			}
			mean += tmp;
		}
		mean /= numSamples;
		
		
		double sigmasquared = 0;
		for (int i = 0; i < numSamples; i++)
		{
			double tmp = (sampleValues[i] - mean);
			sigmasquared += tmp*tmp;
		}
		sigmasquared /= numSamples;
		
		_outputMessage.setMean(mean);
		_outputMessage.setVariance(sigmasquared);
	}

	
	@Override
	public void initialize()
	{
		SRealVariable var = (SRealVariable)_p.node.getSibling(_p.index).getSolver();
		_outputMessage = (NormalParameters)var.resetInputMessage(_outputMessage);
		_inputMessage = (NormalParameters)var.resetInputMessage(_inputMessage);
	}
	
	@Override
	public void createInputMessage(Object msg)
	{
		_inputMessage = (NormalParameters)msg;
	}

	@Override
	public void createOutputMessage(Object msg)
	{
		_outputMessage = (NormalParameters)msg;
	}


	@Override
	public void moveMessages(IMessageGenerator other)
	{
		_inputMessage = ((GaussianMessageGenerator)other)._inputMessage;
		_outputMessage = ((GaussianMessageGenerator)other)._outputMessage;
	}

	@Override
	public Object getInputMsg()
	{
		return _inputMessage;
	}

	@Override
	public Object getOutputMsg()
	{
		return _outputMessage;
	}

}
