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

package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;


public class SRealFactor extends SFactorBase implements ISolverRealFactorGibbs, ISolverFactorGibbs
{	
	protected Factor _realFactor;
	protected ObjectSample [] _inputMsgs;
	
	public SRealFactor(Factor factor)
	{
		super(factor);
		_realFactor = factor;
	}
	

	public double getConditionalPotential(double value, int outPortIndex)
	{
		
		
	    int numPorts = _factor.getSiblings().size();
	    Object[] values = new Object[numPorts];
	    for (int port = 0; port < numPorts; port++)
	    	values[port] = _inputMsgs[port].value;
	    
	    values[outPortIndex] = value;	// Use the requested value on the associated output port
	    
	    return _realFactor.getFactorFunction().evalEnergy(values);
	}
	
	
	public void updateEdge(int outPortNum)
	{
		INode var = _factor.getSiblings().get(outPortNum);
		

		if (var instanceof Discrete)						// Then this edge connects to a discrete variable, so send an output message
		{
			Object[] outputVariableDomain = ((Discrete)var).getDiscreteDomain().getElements();
			FactorFunction factorFunction = _realFactor.getFactorFunction();
			int numPorts = _factor.getSiblings().size();
			
			Object[] values = new Object[numPorts];
			
			for (int port = 0; port < numPorts; port++)
				values[port] = _inputMsgs[port].value;

			//TODO: these could be cached instead.
			double[] outputMsgs = (double[])var.getSolver().getInputMsg(_factor.getSiblingPortIndex(outPortNum)); 
			
			int outputMsgLength = outputMsgs.length;
			for (int i = 0; i < outputMsgLength; i++)
			{
				values[outPortNum] = outputVariableDomain[i];
				outputMsgs[i] = factorFunction.evalEnergy(values);		// Messages to discrete variables are energy values
			}
		}
	}
	
	
	public void update()
	{
    	throw new DimpleException("Method not supported in Gibbs sampling solver.");
	}
	

	public double getPotential()
	{
	    int numPorts = _factor.getSiblings().size();
	    Object[] inPortMsgs = new Object[numPorts];
	    for (int port = 0; port < numPorts; port++)
	    	inPortMsgs[port] = _inputMsgs[port].value;
	    
	    return getPotential(inPortMsgs);
		
	}
	public double getPotential(Object[] inputs)
	{
	    return _realFactor.getFactorFunction().evalEnergy(inputs);
	}


	@Override
	public void createMessages() 
	{
		_inputMsgs = new ObjectSample[_factor.getSiblings().size()];
		for (int i = 0; i < _factor.getSiblings().size(); i++)
		{
			Object [] messages = _factor.getVariables().getByIndex(i).getSolver().createMessages(this);
			_inputMsgs[i] = (ObjectSample)messages[1];
		}
		
	}


	@Override
	public void initialize(int portNum) 
	{
		_inputMsgs[portNum] = (ObjectSample)_factor.getVariables().getByIndex(portNum).getSolver().resetOutputMessage(_inputMsgs[portNum]);
		
	}


	@Override
	public Object getInputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		return _inputMsgs[portIndex];
	}


	@Override
	public Object getOutputMsg(int portIndex) 
	{
		// TODO Auto-generated method stub
		throw new DimpleException("not supported");
	}


	@Override
	public void moveMessages(ISolverNode other, int thisPortNum,
			int otherPortNum) 
	{
		// TODO Auto-generated method stub
		throw new DimpleException("not supported");

	}


}
