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

import java.util.Collection;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.IVariableMapList;


public class SRealFactor extends SFactorBase implements ISolverFactorGibbs
{
	protected Factor _realFactor;
	protected Value [] _inputMsgs;
	//	private Object[] _scratchValues;
	protected int _numPorts;
	protected boolean _isDeterministicDirected;
	/**
	 * True if output samples in {@link #_inputMsgs} have been computed.
	 */
	private boolean _outputsValid = false;
	
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
		// REFACTOR: implementation identical to STableFactor, find a way to share it.
		
		// If this is a deterministic directed factor, and the request is from a directed-from variable,
		// Then propagate the request through the directed-to variables and sum up the results
		// No need to get the potential for this factor since we should have already set outputs
		// to equal the deterministic function of the inputs (so the potential should be zero)
		if (_isDeterministicDirected && !_factor.isDirectedTo(portIndex))
		{
			double result = 0;
			int[] directedTo = _factor.getDirectedTo();
			if (directedTo != null)
			{
				IVariableMapList variables = _factor.getVariables();
				for (int port : directedTo)
				{
					VariableBase v = variables.getByIndex(port);
		    		result += ((ISolverVariableGibbs)v.getSolver()).getConditionalPotential(_factor.getSiblingPortIndex(port));
				}
			}
			return result;
		}
		else	// Not deterministic directed, so get the potential for this factor
		{
			return getPotential();
		}
	}

	
	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		INode var = _factor.getSibling(outPortNum);

		if (var instanceof Discrete)
		{
			// This edge connects to a discrete variable, so send an output message
			// This method only considers the current conditional values, and does propagate
			// to any other variables (unlike get ConditionalPotential)
			// This should only be called if this factor is not a deterministic directed factor
			DiscreteDomain outputVariableDomain = ((Discrete)var).getDiscreteDomain();
			FactorFunction factorFunction = _realFactor.getFactorFunction();
			int numPorts = _factor.getSiblingCount();
			
			Object[] values = new Object[numPorts];
			
			for (int port = 0; port < numPorts; port++)
				values[port] = _inputMsgs[port].getObject();

			//TODO: these could be cached instead.
			double[] outputMsgs = (double[])var.getSolver().getInputMsg(_factor.getSiblingPortIndex(outPortNum));
			
			int outputMsgLength = outputMsgs.length;
			for (int i = 0; i < outputMsgLength; i++)
			{
				values[outPortNum] = outputVariableDomain.getElement(i);
				outputMsgs[i] = factorFunction.evalEnergy(values);		// Messages to discrete variables are energy values
			}
		}
	}

	@Override
	public double getPotential()
	{
		// REFACTOR: implementation identical to STableFactor, find a way to share it.
	    int numPorts = _factor.getSiblingCount();
	    Object[] inPortMsgs = new Object[numPorts]; //_scratchValues;
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
	@Override
	public void updateNeighborVariableValue(int variableIndex, Value oldValue)
	{
		// REFACTOR: implementation identical to STableFactor, find a way to share it.
		
		if (!_isDeterministicDirected) return;
		if (_factor.isDirectedTo(variableIndex)) return;
		
		((SFactorGraph)getRootGraph()).scheduleDeterministicDirectedUpdate(this, variableIndex, oldValue);
	}
	
	@Override
	public void updateNeighborVariableValuesNow(Collection<IndexedValue> oldValues)
	{
		// Compute the output values of the deterministic factor function from the input values
		final FactorFunction function = _factor.getFactorFunction();
		final int[] directedTo = _factor.getDirectedTo();

		Object[] values = null;
		if (oldValues != null && _outputsValid)
		{
			function.updateDeterministic(_inputMsgs, oldValues);
		}
		else
		{
			values = Value.toObjects(_inputMsgs);
			function.evalDeterministic(values);
			if (directedTo != null)
			{
				for (int to : directedTo)
				{
					_inputMsgs[to].setObject(values[to]);
				}
			}
			_outputsValid = true;
		}
		
		// Update the directed-to variables with the computed values
		if (directedTo != null)
		{
			IVariableMapList variables = _factor.getVariables();
			if (values != null)
			{
				for (int outputIndex : directedTo)
				{
					VariableBase variable = variables.getByIndex(outputIndex);
					Object newValue = values[outputIndex];
					((ISolverVariableGibbs)variable.getSolver()).setCurrentSample(newValue);
				
				}
			}
			else
			{
				for (int outputIndex : directedTo)
				{
					VariableBase variable = variables.getByIndex(outputIndex);
					Object newValue = _inputMsgs[outputIndex].getObject();
					((ISolverVariableGibbs)variable.getSolver()).setCurrentSample(newValue);
				}
			}
		}
	}




	@Override
	public void createMessages()
	{
		_numPorts = _factor.getSiblingCount();
		_inputMsgs = new Value[_numPorts];
		_outputsValid = false;
//		_scratchValues = new Object[_numPorts];
		IVariableMapList variables = _factor.getVariables();
		for (int i = 0; i < _numPorts; i++)
		{
			Object [] messages = variables.getByIndex(i).getSolver().createMessages(this);
			_inputMsgs[i] = (Value)messages[1];
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
		return null;	// No output message by default (except for custom factors)
	}


	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		_outputsValid = false;
		_inputMsgs[thisPortNum] = ((SRealFactor)other)._inputMsgs[otherPortNum];
	}

}
