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

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;


public class SVariable extends SDiscreteVariableBase
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */	
	protected double[][] _inPortMsgs = null;
	protected double[][] _outMsgArray = null;
	protected double[][] _savedOutMsgArray;
	protected double[] _dampingParams = new double[0];
	protected double[] _input;
	protected boolean _dampingInUse = false;
	protected boolean _initCalled = true;

	
	public SVariable(VariableBase var) 
	{
		super(var);
		initializeInputs();
	}


	public void initializeInputs()
	{
		_input = MessageConverter.initialValue(((DiscreteDomain)_var.getDomain()).size());		
	}
	
	public Object getInitialMsgValue()
	{
		int domainLength = ((DiscreteDomain)_var.getDomain()).size();
		double[] retVal = new double[domainLength];
		Arrays.fill(retVal, 0);
		return retVal;
	}

	public Object getDefaultMessage(Port port) 
	{
		return getInitialMsgValue();
	}

	public void updateEdge(int outPortNum)
	{
		ensureCacheUpdated();

		double[] priors = (double[])_input;
		int numPorts = _var.getPorts().size();
		int numValue = priors.length;

		// Compute the sum of all messages   
		double minPotential = Double.POSITIVE_INFINITY;
		double[] outMsgs = _outMsgArray[outPortNum];

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
				if (port != outPortNum) out += _inPortMsgs[port][i];
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




	public void update()
	{
		ensureCacheUpdated();

		double[] priors = (double[])_input;
		int numPorts = _var.getPorts().size();
		int numValue = priors.length;

		// Compute the sum of all messages   
		double[] beliefs = new double[numValue];

		for (int i = 0; i < numValue; i++)
		{
			double sum = priors[i];
			for (int port = 0; port < numPorts; port++) 
				sum += _inPortMsgs[port][i];
			beliefs[i] = sum;
		}


		// Now compute output messages for each outgoing edge
		for (int port = 0; port < numPorts; port++ )
		{
			double[] outMsgs = _outMsgArray[port];
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

			double[] inPortMsgsThisPort = _inPortMsgs[port];
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

	public Object getBelief() 
	{
		ensureCacheUpdated();

		double[] priors = (double[])_input;
		double[] outBelief = new double[priors.length];
		int numValue = priors.length;
		int numPorts = _var.getPorts().size();


		for (int i = 0; i < numValue; i++)
		{
			double sum = priors[i];
			for (int port = 0; port < numPorts; port++) sum += _inPortMsgs[port][i];
			outBelief[i] = sum;
		}

		// Convert to probabilities since that's what the interface expects        
		return MessageConverter.toProb(outBelief);
	}


	public void setInput(Object value) 
	{
		// Convert from probabilities since that's what the interface provides        
		_input = MessageConverter.fromProb((double[])value);
	}
	
	
	public double getScore()
	{
		return _input[getGuessIndex()];
	}
	


	protected void updateMessageCache()
	{
		int numPorts = _var.getPorts().size();
		_initCalled = false;
		_inPortMsgs = new double[numPorts][];
		_outMsgArray = new double[numPorts][];
		if (_dampingInUse)
			_savedOutMsgArray = new double[numPorts][];

		if (_dampingParams.length != numPorts)
		{
			double[] tmp = new double[numPorts];
			for (int i = 0; i < _dampingParams.length; i++)
				if (i < tmp.length)
					tmp[i] = _dampingParams[i];
			_dampingParams = tmp;
		}
		
		for (int i = 0; i < numPorts; i++)
		{
			_inPortMsgs[i] = (double[])_var.getPorts().get(i).getInputMsg();
			_outMsgArray[i] = (double[])_var.getPorts().get(i).getOutputMsg();
			if (_dampingInUse)
				_savedOutMsgArray[i] = new double[_outMsgArray[i].length];
		}
		
	}

	public void remove(Factor factor)
	{
		_initCalled = true;
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
	}

	public double getDamping(int portIndex)
	{
		if (portIndex >= _dampingParams.length)
			return 0;
		else
			return _dampingParams[portIndex];
	}


	@Override
	public void connectPort(Port p)  
	{
		_initCalled = true;
	}

}
