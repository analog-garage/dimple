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

package com.analog.lyric.dimple.solvers.sumproduct;

import java.util.Arrays;
import java.util.List;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;



public class SRealVariable extends SRealVariableBase
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */
	private GaussianMessage _input;
	private GaussianMessage[] _inputMsgs = new GaussianMessage[0];
	private GaussianMessage[] _outputMsgs = new GaussianMessage[0];
    
	public SRealVariable(VariableBase var)
    {
		super(var);
	}


	@Override
	public void setInputOrFixedValue(Object input, Object fixedValue, boolean hasFixedValue)
	{
		if (hasFixedValue)
			_input = createFixedValueMessage((Double)fixedValue);
		else if (input == null)
    		_input = createDefaultMessage();
    	else
    	{
    		if (input instanceof Normal)	// Input is a Normal factor function with fixed parameters
    		{
    			Normal normalInput = (Normal)input;
    			if (!normalInput.hasConstantParameters())
    				throw new DimpleException("Normal factor function used as Input must have constant parameters");
    			_input = new GaussianMessage();
    			_input.setMean(normalInput.getMean());
    			_input.setPrecision(normalInput.getPrecision());
    		}
    		else	// Input is array in the form [mean, standard deviation]
    		{
    			double[] vals = (double[])input;
    			if (vals.length != 2)
    				throw new DimpleException("Expect a two-element vector of mean and standard deviation");

    			if (vals[1] < 0)
    				throw new DimpleException("Expect standard deviation to be >= 0");

    			_input = new GaussianMessage();
    			_input.setMean(vals[0]);
    			_input.setStandardDeviation(vals[1]);
    		}
    	}
    	
    }
	
    @Override
	public void updateEdge(int outPortNum)
    {
    	// If fixed value, just return the input, which has been set to a zero-variance message
    	if (_var.hasFixedValue())
    	{
        	((GaussianMessage)_outputMsgs[outPortNum]).set(_input);
        	return;
    	}
    	
    	List<INode> ports = _var.getSiblings();
    	
    	double tau = _input.getPrecision();
    	double mu = _input.getMean() * tau;
    	
    	boolean anyTauIsInfinite = false;
    	double muOfInf = 0;
    	
    	if (tau == Double.POSITIVE_INFINITY)
    	{
    		anyTauIsInfinite = true;
    		muOfInf = _input.getMean();
    	}
    	
    	
    	for (int i = 0; i < ports.size(); i++)
    	{
    		if (i != outPortNum)
    		{
    			GaussianMessage msg = _inputMsgs[i];
    			double tmpTau = msg.getPrecision();
    			
    			if (tmpTau == Double.POSITIVE_INFINITY)
    			{
    				if (!anyTauIsInfinite)
    				{
	    				anyTauIsInfinite = true;
	    				muOfInf = msg.getMean();
    				}
    				else
    				{
    					if (muOfInf != msg.getMean())
    						throw new DimpleException("Real variable failed because two incoming messages were certain of conflicting things.");
    				}
    			}
    			else
    			{
	    			tau += tmpTau;
	    			mu += tmpTau * msg.getMean();
    			}
    		}
    	}
    	
    	if (tau == Double.POSITIVE_INFINITY && !anyTauIsInfinite)
    		throw new DimpleException("This case isn't handled yet.");
    	
    	if (anyTauIsInfinite)
    	{
    		mu = muOfInf;
    		tau = Double.POSITIVE_INFINITY;
    	}
    	else
    	{
	    	if (tau != 0)
	    		mu /= tau;
	    	else
	    		mu = 0;
    	}
    	
    	GaussianMessage outMsg = _outputMsgs[outPortNum];
    	outMsg.setMean(mu);
    	outMsg.setPrecision(tau);
    }
    

    
    @Override
	public Object getBelief()
    {
    	// If fixed value, just return the input, which has been set to a zero-variance message
    	if (_var.hasFixedValue())
        	return new double[]{_input.getMean(), _input.getStandardDeviation()};
    	
    	double tau = _input.getPrecision();
    	double mu = _input.getMean() * tau;
    	
    	boolean anyTauIsInfinite = false;
    	double muOfInf = 0;
    	
    	if (tau == Double.POSITIVE_INFINITY)
    	{
    		anyTauIsInfinite = true;
    		muOfInf = _input.getMean();
    	}

    	
    	for (int i = 0; i < _inputMsgs.length; i++)
    	{
			GaussianMessage msg = _inputMsgs[i];
			double tmpTau = msg.getPrecision();
			
			
			if (tmpTau == Double.POSITIVE_INFINITY)
			{
				if (!anyTauIsInfinite)
				{
    				anyTauIsInfinite = true;
    				muOfInf = msg.getMean();
				}
				else
				{
					if (muOfInf != msg.getMean())
						throw new DimpleException("Real variable failed because two incoming messages were certain of conflicting things.");
				}
			}
			else
			{
    			tau += tmpTau;
    			mu += tmpTau * msg.getMean();
			}
    	}

    	if (tau == Double.POSITIVE_INFINITY && ! anyTauIsInfinite)
    		throw new DimpleException("This case isn't handled yet.");

    	if (anyTauIsInfinite)
    	{
    		mu = muOfInf;
    		tau = Double.POSITIVE_INFINITY;
    	}
    	else
    	{
	    	if (tau != 0)
	    		mu /= tau;
	    	else
	    		mu = 0;
    	}
    	
    	return new double[]{mu, Math.sqrt(1/tau)};
    }
    
    
	@Override
	public Object getValue()
	{
		double[] belief = (double[])getBelief();
		return new Double(belief[0]);
	}
	

	@Override
	public Object[] createMessages(ISolverFactor factor)
	{
		int portNum = _var.getPortNum(factor.getModelObject());
		int newArraySize = Math.max(_inputMsgs.length,portNum + 1);
		_inputMsgs = Arrays.copyOf(_inputMsgs,newArraySize);
		_inputMsgs[portNum] = createDefaultMessage();
		_outputMsgs = Arrays.copyOf(_outputMsgs, newArraySize);
		_outputMsgs[portNum] = createDefaultMessage();
		return new Object [] {_inputMsgs[portNum],_outputMsgs[portNum]};
	}

	public GaussianMessage createDefaultMessage()
	{
		GaussianMessage message = new GaussianMessage();
		return (GaussianMessage)resetInputMessage(message);
	}

	@Override
	public Object resetInputMessage(Object message)
	{
		((GaussianMessage)message).setNull();
		return message;
	}

	@Override
	public void resetEdgeMessages(int i)
	{
		_inputMsgs[i] = (GaussianMessage)resetInputMessage(_inputMsgs[i]);
		_outputMsgs[i] = (GaussianMessage)resetOutputMessage(_outputMsgs[i]);
	}
    
    
	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}


	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		SRealVariable s = (SRealVariable)other;
	
		_inputMsgs[portNum] = s._inputMsgs[otherPort];
		_outputMsgs[portNum] = s._outputMsgs[otherPort];
	}

	@Override
	public void setInputMsg(int portIndex, Object obj) {
		_inputMsgs[portIndex] = (GaussianMessage)obj;
	}
	
	public GaussianMessage createFixedValueMessage(double fixedValue)
	{
		GaussianMessage message = new GaussianMessage();
		message.setMean(fixedValue);
		message.setPrecision(Double.POSITIVE_INFINITY);
		return message;
	}
	
}
