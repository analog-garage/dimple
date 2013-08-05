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

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.gibbs.sample.ObjectSample;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;


public class SRealFactor extends SFactorBase implements ISolverFactorGibbs
{	
	protected Factor _realFactor;
	protected ObjectSample [] _inputMsgs;
	protected int _numPorts;
	protected boolean _isDeterministicDirected;
	
	public SRealFactor(Factor factor)
	{
		super(factor);
		_realFactor = factor;
		_isDeterministicDirected = _factor.getFactorFunction().isDeterministicDirected();
	}
	

	@Override
	public void updateEdge(int outPortNum)
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
	}
	@Override
	public void update()
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
	}
	

	@Override
	public double getConditionalPotential(int portIndex)
	{
		double result = getPotential();
		
		// If this is a deterministic directed factor, and the request is from a directed-from variable,
		// Then propagate the request through the directed-to variables and sum up the results
		if (_isDeterministicDirected && !_factor.isDirectedTo(portIndex))
		{
			ArrayList<INode> siblings = _factor.getSiblings();
		    for (int port = 0; port < _numPorts; port++)
		    {
		    	VariableBase v = (VariableBase)siblings.get(port);
		    	if (_factor.isDirectedTo(v))
		    		result += ((ISolverVariableGibbs)v.getSolver()).getConditionalPotential(_factor.getSiblingPortIndex(port));
		    }
		}

		return result;
	}

	
	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		INode var = _factor.getSiblings().get(outPortNum);

		if (var instanceof Discrete)
		{
			// This edge connects to a discrete variable, so send an output message
			// This method only considers the current conditional values, and does propagate
			// to any other variables (unlike get ConditionalPotential)
			// This should only be called if this factor is not a deterministic directed factor
			Object[] outputVariableDomain = ((Discrete)var).getDiscreteDomain().getElements();
			FactorFunction factorFunction = _realFactor.getFactorFunction();
			int numPorts = _factor.getSiblings().size();
			
			Object[] values = new Object[numPorts];
			
			for (int port = 0; port < numPorts; port++)
				values[port] = _inputMsgs[port].getObject();

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
	
	
	public double getPotential()
	{
	    int numPorts = _factor.getSiblings().size();
	    Object[] inPortMsgs = new Object[numPorts];
	    for (int port = 0; port < numPorts; port++)
	    	inPortMsgs[port] = _inputMsgs[port].getObject();
	    
	    return getPotential(inPortMsgs);
	}
	public double getPotential(Object[] inputs)
	{
	    return _realFactor.getFactorFunction().evalEnergy(inputs);
	}

	
	
	// Set the value of a neighboring variable
	// If this is a deterministic directed factor, and this variable is a directed input (directed-from)
	// then re-compute the directed outputs and propagate the result to the directed-to variables
	public void updateNeighborVariableValue(int portIndex)
	{
		if (!_isDeterministicDirected) return;
		if (_factor.isDirectedTo(portIndex)) return;
		
		// Compute the output values of the deterministic factor function from the input values
	    Object[] values = new Object[_numPorts];
	    for (int port = 0; port < _numPorts; port++)
	    	values[port] = _inputMsgs[port].getObject();
		_factor.getFactorFunction().evalDeterministicFunction(values);
		
		// Update the directed-to variables with the computed values
		ArrayList<INode> siblings = _factor.getSiblings();
	    for (int port = 0; port < _numPorts; port++)
	    {
	    	VariableBase v = (VariableBase)siblings.get(port);
	    	if (_factor.isDirectedTo(v))
	    		((ISolverVariableGibbs)v.getSolver()).setCurrentSample(values[port]);
	    }
	}




	@Override
	public void createMessages() 
	{
		_numPorts = _factor.getSiblings().size();
		_inputMsgs = new ObjectSample[_numPorts];
		for (int i = 0; i < _numPorts; i++)
		{
			Object [] messages = _factor.getVariables().getByIndex(i).getSolver().createMessages(this);
			_inputMsgs[i] = (ObjectSample)messages[1];
		}
	}


	@Override
	public void resetEdgeMessages(int portNum) 
	{
		//_inputMsgs[portNum] = (ObjectSample)_factor.getVariables().getByIndex(portNum).getSolver().resetOutputMessage(_inputMsgs[portNum]);
	}


	@Override
	public Object getInputMsg(int portIndex) 
	{
		return _inputMsgs[portIndex];
	}


	@Override
	public Object getOutputMsg(int portIndex) 
	{
		throw new DimpleException("not supported");
	}


	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum) 
	{
		//protected ObjectSample [] _inputMsgs;
		_inputMsgs[thisPortNum] = ((SRealFactor)other)._inputMsgs[otherPortNum];

		//throw new DimpleException("not supported");
	}

	@Override
	public void setDirectedTo(int [] indices)
	{
		for (VariableBase vb : _factor.getVariables())
		{
			((ISolverVariableGibbs)vb.getSolver()).updateDirectedCache();
		}
	}

}
