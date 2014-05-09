/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
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
import java.util.concurrent.atomic.AtomicReference;

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
	
	private boolean _visited = false;
	
	public SRealFactor(Factor factor)
	{
		super(factor);
		_realFactor = factor;
		_isDeterministicDirected = _factor.getFactorFunction().isDeterministicDirected();
	}
	

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
	}
	@Override
	protected void doUpdate()
	{
		// The Gibbs solver doesn't directly update factors, but the equivalent is instead done calls from variables
		// This is ignored and doesn't throw an error so that a custom schedule that updates factors won't cause a problem
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
		if (_isDeterministicDirected)
			return 0;

		return _realFactor.getFactorFunction().evalEnergy(_inputMsgs);
	}
	public double getPotential(Object[] inputs)
	{
	    return _realFactor.getFactorFunction().evalEnergy(inputs);
	}

	
	
	@Override
	public void updateNeighborVariableValue(int variableIndex, Value oldValue)
	{
		((SFactorGraph)getRootGraph()).scheduleDeterministicDirectedUpdate(this, variableIndex, oldValue);
	}
	
	@Override
	public void updateNeighborVariableValuesNow(Collection<IndexedValue> oldValues)
	{
		// Compute the output values of the deterministic factor function from the input values
		final Factor factor = _factor;
		final FactorFunction function = factor.getFactorFunction();
		int[] directedTo = factor.getDirectedTo();

		if (oldValues != null && _outputsValid)
		{
			AtomicReference<int[]> changedOutputsHolder = new AtomicReference<int[]>();
			function.updateDeterministic(_inputMsgs, oldValues, changedOutputsHolder);
			int[] changedOutputs = changedOutputsHolder.get();
			if (changedOutputs != null)
			{
				directedTo = changedOutputs;
			}

			// Update the directed-to variables with the computed values
			for (int outputIndex : directedTo)
			{
				VariableBase variable = factor.getSibling(outputIndex);
				Value newValue = _inputMsgs[outputIndex];
				((ISolverVariableGibbs)variable.getSolver()).setCurrentSample(newValue);
			}
		}
		else
		{
			function.evalDeterministic(factor, _inputMsgs);
			_outputsValid = true;

			// Update the directed-to variables with the computed values
			if (directedTo != null)
			{
				// Full update
				for (int outputIndex : directedTo)
				{
					VariableBase variable = factor.getSibling(outputIndex);
					Value newValue = _inputMsgs[outputIndex];
					// FIXME: is sample already set? Just need to handle side-effects?
					((ISolverVariableGibbs)variable.getSolver()).setCurrentSample(newValue);
				}
			}
		}
	}



	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		_numPorts = factor.getSiblingCount();
		_inputMsgs = new Value[_numPorts];
		_outputsValid = false;
//		_scratchValues = new Object[_numPorts];
		for (int i = 0; i < _numPorts; i++)
		{
			Object [] messages = factor.getSibling(i).getSolver().createMessages(this);
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

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}

}
