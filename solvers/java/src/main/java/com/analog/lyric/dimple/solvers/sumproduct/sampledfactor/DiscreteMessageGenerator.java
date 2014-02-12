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

import java.util.Arrays;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.sumproduct.SVariable;

public class DiscreteMessageGenerator implements IMessageGenerator
{
	private Port _p;
	private double[] _inputMessage;
	private double[] _outputMessage;
	

	public DiscreteMessageGenerator(Port p)
	{
		_p = p;
	
		if (!(_p.getConnectedNode() instanceof Discrete))
			throw new DimpleException("Expected Discrete variable.");
	}


	@Override
	public void generateOutputMessageFromSamples(Object samples)
	{
		// In this case, input samples are index values
		int[] sampleIndices = (int[])samples;
	
		Arrays.fill(_outputMessage, 0);
		for (int i = 0; i < sampleIndices.length; i++)
			_outputMessage[sampleIndices[i]]++;
		
		// Normalize
		for (int i = 0; i < _outputMessage.length; i++ )
			_outputMessage[i] /= sampleIndices.length;
	}

	@Override
	public void initialize()
	{
		SVariable var = (SVariable)_p.node.getSibling(_p.index).getSolver();
		_outputMessage = (double[])var.resetInputMessage(_outputMessage);
		_inputMessage = (double[])var.resetInputMessage(_inputMessage);
	}

	@Override
	public void createInputMessage(Object msg)
	{
		_inputMessage = (double[])msg;
	}
	
	@Override
	public void createOutputMessage(Object msg)
	{
		_outputMessage = (double[])msg;
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


	@Override
	public void moveMessages(IMessageGenerator other)
	{
		_inputMessage = ((DiscreteMessageGenerator)other)._inputMessage;
		_outputMessage = ((DiscreteMessageGenerator)other)._outputMessage;
	}

}
