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

import java.util.ArrayList;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;


public class SRealFactor extends SFactorBase implements ISolverRealFactorGibbs, ISolverFactorGibbs
{	
	protected Factor _realFactor;
	
	
	public SRealFactor(Factor factor)
	{
		super(factor);
		_realFactor = factor;
	}
	

	public double getConditionalPotential(double value, int outPortIndex)
	{
		
		ArrayList<Port> ports = _factor.getPorts();
	    int numPorts = ports.size();
	    Object[] values = new Object[numPorts];
	    for (int port = 0; port < numPorts; port++)
	    {
	    	Port p = ports.get(port);
	    	Object message = p.getInputMsg();
	    	INode neighbor = p.getConnectedNode();
	    	
	    	if (((VariableBase)neighbor).getDomain().isDiscrete())
	    		values[port] = ((Discrete)neighbor).getDiscreteDomain().getElements()[((int[])message)[0]];
	    	else
	    		values[port] = message;
	    }
	    values[outPortIndex] = value;	// Use the requested value on the associated output port
	    
	    return _realFactor.getFactorFunction().evalEnergy(values);
	}
	
	
	public void updateEdge(int outPortNum)
	{
		ArrayList<Port> ports = _factor.getPorts();
		INode outVariable = ports.get(outPortNum).getConnectedNode();
		if (outVariable instanceof Discrete)						// Then this edge connects to a discrete variable, so send an output message
		{
			Object[] outputVariableDomain = ((Discrete)outVariable).getDiscreteDomain().getElements();
			FactorFunction factorFunction = _realFactor.getFactorFunction();
			int numPorts = ports.size();
			Object[] values = new Object[numPorts];
			for (int port = 0; port < numPorts; port++)
			{
				Port p = ports.get(port);
				Object message = p.getInputMsg();
				INode neighbor = p.getConnectedNode();
				if (neighbor instanceof Discrete)
					values[port] = ((Discrete)neighbor).getDiscreteDomain().getElements()[((int[])message)[0]];
				else
					values[port] = message;
			}

			double[] outputMsgs = (double[])ports.get(outPortNum).getOutputMsg();
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
	

	public Object getDefaultMessage(Port port) 
	{
		INode neighbor = port.getConnectedNode();
		if (neighbor instanceof Discrete)
			return new int[]{0};		// Messages from discrete variables are domain indices
		else
			return new Double(0);		// Messages from real variables are real values (doubles)
	}

	public double Potential() {return getPotential();}
	public double getPotential()
	{
		ArrayList<Port> ports = _factor.getPorts();
	    int numPorts = ports.size();
	    Object[] inPortMsgs = new Object[numPorts];
	    for (int port = 0; port < numPorts; port++)
	    {
	    	Port p = ports.get(port);
	    	Object message = p.getInputMsg();
	    	INode neighbor = p.getConnectedNode();
	    	if (((VariableBase)neighbor).getDomain().isDiscrete())
	    		inPortMsgs[port] = new Integer(((int[])message)[0]);
	    	else
	    		inPortMsgs[port] = message;
	    }
	    
	    return getPotential(inPortMsgs);
	}
	public double getPotential(Object[] inputs)
	{
	    return _realFactor.getFactorFunction().evalEnergy(inputs);
	}


}
