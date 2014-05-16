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

package com.analog.lyric.dimple.solvers.minsum;

import java.util.Arrays;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableDoubleArray;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;


public class SVariable extends SDiscreteVariableDoubleArray
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	protected double[][] _savedOutMsgArray = new double[0][];
	protected double[] _dampingParams = new double[0];
	protected double[] _input;
	protected boolean _dampingInUse = false;

	
	public SVariable(VariableBase var)
	{
		super(var);
	}

	@Override
	protected void doUpdateEdge(int outPortNum)
	{

		double[] priors = _input;
		int numPorts = _var.getSiblingCount();
		int numValue = priors.length;

		// Compute the sum of all messages
		double minPotential = Double.POSITIVE_INFINITY;
		double[] outMsgs = _outputMessages[outPortNum];

		// Save previous output for damping
		if (_dampingInUse)
		{
			double damping = _dampingParams[outPortNum];
			if (damping != 0)
			{
				double[] saved = _savedOutMsgArray[outPortNum];
				for (int i = 0; i < outMsgs.length; i++)
					saved[i] = outMsgs[i];
			}
		}

		for (int i = 0; i < numValue; i++)
		{
			double out = priors[i];
			for (int port = 0; port < numPorts; port++)
				if (port != outPortNum) out += _inputMessages[port][i];
			outMsgs[i] = out;

			if (out < minPotential)
				minPotential = out;
		}

		// Damping
		if (_dampingInUse)
		{
			double damping = _dampingParams[outPortNum];
			if (damping != 0)
			{
				double[] saved = _savedOutMsgArray[outPortNum];
				for (int m = 0; m < numValue; m++)
					outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
			}
		}

		// Normalize the min
		for (int i = 0; i < numValue; i++)
			outMsgs[i] -= minPotential;
	}




	@Override
	protected void doUpdate()
	{

		double[] priors = _input;
		int numPorts = _var.getSiblingCount();
		int numValue = priors.length;

		// Compute the sum of all messages
		double[] beliefs = new double[numValue];

		for (int i = 0; i < numValue; i++)
		{
			double sum = priors[i];
			for (int port = 0; port < numPorts; port++)
				sum += _inputMessages[port][i];
			beliefs[i] = sum;
		}


		// Now compute output messages for each outgoing edge
		for (int port = 0; port < numPorts; port++ )
		{
			double[] outMsgs = _outputMessages[port];
			double minPotential = Double.POSITIVE_INFINITY;
			
			// Save previous output for damping
			if (_dampingInUse)
			{
				double damping = _dampingParams[port];
				if (damping != 0)
				{
					double[] saved = _savedOutMsgArray[port];
					for (int i = 0; i < outMsgs.length; i++)
						saved[i] = outMsgs[i];
				}
			}

			double[] inPortMsgsThisPort = _inputMessages[port];
			for (int i = 0; i < numValue; i++)
			{
				double out = beliefs[i] - inPortMsgsThisPort[i];
				if (out < minPotential)
					minPotential = out;
				outMsgs[i] = out;
			}

			// Damping
			if (_dampingInUse)
			{
				double damping = _dampingParams[port];
				if (damping != 0)
				{
					double[] saved = _savedOutMsgArray[port];
					for (int m = 0; m < numValue; m++)
						outMsgs[m] = outMsgs[m]*(1-damping) + saved[m]*damping;
				}
			}

			// Normalize the min
			for (int i = 0; i < numValue; i++)
				outMsgs[i] -= minPotential;
		}


	}

	@Override
	public double[] getBelief()
	{

		double[] priors = _input;
		double[] outBelief = new double[priors.length];
		int numValue = priors.length;
		int numPorts = _var.getSiblingCount();


		for (int i = 0; i < numValue; i++)
		{
			double sum = priors[i];
			for (int port = 0; port < numPorts; port++) sum += _inputMessages[port][i];
			outBelief[i] = sum;
		}

		// Convert to probabilities since that's what the interface expects
		return MessageConverter.toProb(outBelief);
	}


	@Override
	public void setInputOrFixedValue(Object input,Object fixedValue, boolean hasFixedValue)
	{
		if (input == null)
			_input = MessageConverter.initialValue(((DiscreteDomain)_var.getDomain()).size());
		else
			// Convert from probabilities since that's what the interface provides
			_input = MessageConverter.fromProb((double[])input);
	}
	
	
	@Override
	public double getScore()
	{
		if (!_var.hasFixedValue())
			return _input[getGuessIndex()];
		else
			return 0;	// If the value is fixed, ignore the guess
	}


	public void setDamping(int portIndex, double dampingVal)
	{
		if (portIndex >= _dampingParams.length)
		{
			double[] tmp = new double [portIndex+1];
			for (int i = 0; i < _dampingParams.length; i++)
				tmp[i] = _dampingParams[i];

			_dampingParams = tmp;
		}

		_dampingParams[portIndex] = dampingVal;
		
		if (dampingVal != 0)
			_dampingInUse = true;
		
		_savedOutMsgArray = new double[_dampingParams.length][];
		for (int i = 0; i < _inputMessages.length; i++)
			_savedOutMsgArray[i] = new double[_inputMessages[i].length];

	}

	public double getDamping(int portIndex)
	{
		if (portIndex >= _dampingParams.length)
			return 0;
		else
			return _dampingParams[portIndex];
	}


	@Override
	public Object [] createMessages(ISolverFactor factor)
	{
		Object [] retval = super.createMessages(factor);
		int portNum = _var.getPortNum(factor.getModelObject());
		int newArraySize = _inputMessages.length;
		
		
		if (_dampingInUse)
		{
			_savedOutMsgArray = Arrays.copyOf(_savedOutMsgArray,newArraySize);
		}

		_dampingParams = Arrays.copyOf(_dampingParams,newArraySize);
		
		
		
		if (_dampingInUse)
			_savedOutMsgArray[portNum] = new double[_outputMessages[portNum].length];
		
		return retval;
	}

	@Override
	public Object resetInputMessage(Object message)
	{
		Arrays.fill((double[])message, 0);
		return message;
	}

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		super.moveMessages(other, portNum, otherPort);
		SVariable sother = (SVariable)other;
		
		if (_dampingInUse)
			_savedOutMsgArray[portNum] = sother._savedOutMsgArray[otherPort];

		_dampingParams[portNum] = sother._dampingParams[otherPort];
		
	}

	/*---------------
	 * SNode methods
	 */
	
	@Override
	protected DiscreteEnergyMessage cloneMessage(int edge)
	{
		return new DiscreteEnergyMessage(_outputMessages[edge]);
	}
	
	@Override
	protected boolean supportsMessageEvents()
	{
		return true;
	}
}
