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

package com.analog.lyric.dimple.solvers.gaussian;

import java.util.ArrayList;
import java.util.Arrays;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SRealVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;



public class SVariable extends SRealVariableBase 
{
	/*
	 * We cache all of the double arrays we use during the update.  This saves
	 * time when performing the update.
	 */	
	private double [] _input;
	private double [][] _inputMsgs = new double[0][];
	private double [][] _outputMsgs = new double[0][];
    
	public SVariable(VariableBase var) 
    {
		super(var);
	}


	@Override
	public void setInputOrFixedValue(Object input, Object fixedValue, boolean hasFixedValue)
	{
		if (hasFixedValue)
			_input = createFixedValueMessage((Double)fixedValue);
		else if (input == null)
    		_input = (double[]) createDefaultMessage();
    	else
    	{
	    	double [] vals = (double[])input;
	    	if (vals.length != 2)
	    		throw new DimpleException("expect priors to be a vector of mean and sigma");
	    	
	    	if (vals[1] < 0)
	    		throw new DimpleException("expect sigma to be >= 0");
	    	
	    	_input = vals.clone();
    	}
    	
    }
	
    public void updateEdge(int outPortNum) 
    {
    	// If fixed value, just return the input, which has been set to a zero-variance message
    	if (_var.hasFixedValue())
    	{
        	double [] outMsg = _outputMsgs[outPortNum];
        	outMsg[0] = _input[0];
        	outMsg[1] = _input[1];
        	return;
    	}
    	
    	ArrayList<INode> ports = _var.getSiblings();
    	
    	double R = 1/(_input[1]*_input[1]);
    	double Mu = _input[0]*R;
    	
    	boolean anyRisInfinite = false;
    	double MuOfInf = 0;
    	
    	if (R == Double.POSITIVE_INFINITY)
    	{
    		anyRisInfinite = true;
    		MuOfInf = _input[0];
    	}
    	
    	
    	for (int i = 0; i < ports.size(); i++)
    	{
    		if (i != outPortNum)
    		{
    			double [] msg = _inputMsgs[i];
    			double tmpR = 1/(msg[1]*msg[1]);
    			
    			if (tmpR == Double.POSITIVE_INFINITY)
    			{
    				if (!anyRisInfinite)
    				{
	    				anyRisInfinite = true;
	    				MuOfInf = msg[0];    					
    				}
    				else
    				{
    					if (MuOfInf != msg[0])
    						throw new DimpleException("variable node failed in gaussian solver because " +
    								"two incoming messages were certain of conflicting things.");
    								
    				}
    			}
    			else
    			{	    			
	    			R += tmpR;
	    			Mu += tmpR * msg[0];
    			}
    		}
    	}
    	
    	double sigma = Math.sqrt(1/R);
    	
    	if (R == Double.POSITIVE_INFINITY && ! anyRisInfinite)
    		throw new DimpleException("this case isn't handled yet");
    	
    	if (anyRisInfinite)
    	{
    		Mu = MuOfInf;
    		sigma = 0;
    	}
    	else
    	{
	    	if (R != 0)
	    		Mu /= R;
	    	else
	    		Mu = 0;
    	}
    	
    	double [] outMsg = _outputMsgs[outPortNum];
    	outMsg[0] = Mu;
    	outMsg[1] = sigma;
    }
    

    
    public Object getBelief() 
    {
    	// If fixed value, just return the input, which has been set to a zero-variance message
    	if (_var.hasFixedValue()) return _input.clone();
    	
    	double R = 1/(_input[1]*_input[1]);
    	double Mu = _input[0]*R;
    	
    	boolean anyRisInfinite = false;
    	double MuOfInf = 0;
    	
    	if (R == Double.POSITIVE_INFINITY)
    	{
    		anyRisInfinite = true;
    		MuOfInf = _input[0];
    	}

    	
    	for (int i = 0; i < _inputMsgs.length; i++)
    	{
			double [] msg = _inputMsgs[i];
			double tmpR = 1/(msg[1]*msg[1]);
			
			
			if (tmpR == Double.POSITIVE_INFINITY)
			{
				if (!anyRisInfinite)
				{
    				anyRisInfinite = true;
    				MuOfInf = msg[0];    					
				}
				else
				{
					if (MuOfInf != msg[0])
						throw new DimpleException("variable node failed in gaussian solver because " +
								"two incoming messages were certain of conflicting things.");
								
				}
			}
			else
			{	    			
    			R += tmpR;
    			Mu += tmpR * msg[0];
			}

			/*
			R += tmpR;
			Mu += tmpR * msg[0];
			*/
    	}

    	double sigma = Math.sqrt(1/R);

    	if (R == Double.POSITIVE_INFINITY && ! anyRisInfinite)
    		throw new DimpleException("this case isn't handled yet");

    	if (anyRisInfinite)
    	{
    		Mu = MuOfInf;
    		sigma = 0;
    	}
    	else
    	{
	    	if (R != 0)
	    		Mu /= R;
	    	else
	    		Mu = 0;
    	}
    	
    	
    	return new double []{Mu,sigma};
    
    }
    
    
	@Override
	public Object getValue()
	{
		double[] belief = (double[])getBelief();
		return new Double(belief[0]);
	}
	

	@Override
	public Object [] createMessages(ISolverFactor factor) 
	{
		int portNum = _var.getPortNum(factor.getModelObject());
		int newArraySize = Math.max(_inputMsgs.length,portNum + 1);
		_inputMsgs = Arrays.copyOf(_inputMsgs,newArraySize);
		_inputMsgs[portNum] = createDefaultMessage();
		_outputMsgs = Arrays.copyOf(_outputMsgs, newArraySize);
		_outputMsgs[portNum] = createDefaultMessage();
		return new Object [] {_inputMsgs[portNum],_outputMsgs[portNum]};
	}

	public double [] createDefaultMessage() 
	{
		double [] message = new double[2];
		return (double[])resetInputMessage(message);
	}

	@Override
	public Object resetInputMessage(Object message) 
	{
		double [] m = (double[])message;
		m[0] = 0;
		m[1] = Double.POSITIVE_INFINITY;
		return m;
	}

	@Override
	public void resetEdgeMessages(int i) 
	{
		_inputMsgs[i] = (double[])resetInputMessage(_inputMsgs[i]);
		_outputMsgs[i] = (double[])resetOutputMessage(_outputMsgs[i]);

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
		SVariable s = (SVariable)other;
	
		_inputMsgs[portNum] = s._inputMsgs[otherPort];
		_outputMsgs[portNum] = s._outputMsgs[otherPort];

	}

	@Override
	public void setInputMsg(int portIndex, Object obj) {
		_inputMsgs[portIndex] = (double[])obj;
	}
	
	public double[] createFixedValueMessage(double fixedValue)
	{
		double[] message = new double[2];
		message[0] = fixedValue;
		message[1] = 0;
		return message;
	}

	
}
