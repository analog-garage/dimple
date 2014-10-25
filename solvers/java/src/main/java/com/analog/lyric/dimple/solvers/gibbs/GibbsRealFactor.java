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

import static java.util.Objects.*;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * Real solver factor under Gibbs solver.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
@SuppressWarnings("deprecation") // TODO: remove when SRealFactor removed
public class GibbsRealFactor extends SRealFactor implements ISolverFactorGibbs
{
	protected Factor _realFactor;
	protected @Nullable Value [] _inputMsgs;
	//	private Object[] _scratchValues;
	protected int _numPorts;
	protected boolean _isDeterministicDirected;
	private int _topologicalOrder = 0;
	/**
	 * True if output samples in {@link #_inputMsgs} have been computed.
	 */
	private boolean _outputsValid = false;
	
	private boolean _visited = false;
	
	public GibbsRealFactor(Factor factor)
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
		INode node = requireNonNull(_factor.getSibling(outPortNum));

		if (node instanceof Discrete)
		{
			final Discrete var = (Discrete)node;
			
			// This edge connects to a discrete variable, so send an output message
			// This method only considers the current conditional values, and does propagate
			// to any other variables (unlike get ConditionalPotential)
			// This should only be called if this factor is not a deterministic directed factor
			DiscreteDomain outputVariableDomain = var.getDiscreteDomain();
			FactorFunction factorFunction = _realFactor.getFactorFunction();
			int numPorts = _factor.getSiblingCount();
			
			Object[] values = new Object[numPorts];
			
			final Value[] inputMsgs = Objects.requireNonNull(_inputMsgs);
			for (int port = 0; port < numPorts; port++)
				values[port] = inputMsgs[port].getObject();

			//TODO: these could be cached instead.
			ISolverVariable svar = var.getSolver();
			@SuppressWarnings("null")
			double[] outputMsgs = (double[])svar.getInputMsg(_factor.getSiblingPortIndex(outPortNum));
			
			@SuppressWarnings("null")
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

		final Value[] inputMsgs = _inputMsgs;
		if (inputMsgs != null)
		{
			final double energy = _realFactor.getFactorFunction().evalEnergy(inputMsgs);
			if (energy != energy)	// Faster isNaN
				return Double.POSITIVE_INFINITY;
			return energy;
		}
		else
			return Double.POSITIVE_INFINITY;
	}
	
	@Override
	public double getPotential(Object[] inputs)
	{
	    return _realFactor.getFactorFunction().evalEnergy(inputs);
	}

	@Override
	public final int getTopologicalOrder()
	{
		return _topologicalOrder;
	}
	
	@Override
	public final void setTopologicalOrder(int order)
	{
		_topologicalOrder = order;
	}
	
	@SuppressWarnings("null")
	@Override
	public void updateNeighborVariableValue(int variableIndex, Value oldValue)
	{
		((GibbsSolverGraph)getRootGraph()).scheduleDeterministicDirectedUpdate(this, variableIndex, oldValue);
	}
	
	@Override
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValues)
	{
		// Compute the output values of the deterministic factor function from the input values
		final Factor factor = _factor;
		final FactorFunction function = factor.getFactorFunction();
		int[] directedTo = factor.getDirectedTo();

		final Value[] inputMsgs = requireNonNull(_inputMsgs);
		
		if (oldValues != null && _outputsValid)
		{
			AtomicReference<int[]> changedOutputsHolder = new AtomicReference<int[]>();
			function.updateDeterministic(inputMsgs, oldValues, changedOutputsHolder);
			int[] changedOutputs = changedOutputsHolder.get();
			if (changedOutputs != null)
			{
				directedTo = changedOutputs;
			}

			// Update the directed-to variables with the computed values
			if (directedTo != null)
			{
				for (int outputIndex : directedTo)
				{
					Variable variable = requireNonNull(factor.getSibling(outputIndex));
					Value newValue = inputMsgs[outputIndex];
					((ISolverVariableGibbs)variable.requireSolver("updateNeighborVariableValuesNow")).setCurrentSample(newValue);
				}
			}
		}
		else
		{
			function.evalDeterministic(inputMsgs);
			_outputsValid = true;

			// Update the directed-to variables with the computed values
			if (directedTo != null)
			{
				// Full update
				for (int outputIndex : directedTo)
				{
					Variable variable = requireNonNull(factor.getSibling(outputIndex));
					Value newValue = inputMsgs[outputIndex];
					// FIXME: is sample already set? Just need to handle side-effects?
					((ISolverVariableGibbs)variable.requireSolver("updateNeighborVariableValuesNow")).setCurrentSample(newValue);
				}
			}
		}
	}



	@SuppressWarnings("null")
	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		_numPorts = factor.getSiblingCount();
		final Value[] inputMsgs = _inputMsgs = new Value[_numPorts];
		_outputsValid = false;
//		_scratchValues = new Object[_numPorts];
		for (int i = 0; i < _numPorts; i++)
		{
			Object [] messages = factor.getSibling(i).getSolver().createMessages(this);
			inputMsgs[i] = (Value)messages[1];
		}
	}


	@Override
	public void resetEdgeMessages(int portNum)
	{
		//_inputMsgs[portNum] = (ObjectSample)_factor.getVariables().getByIndex(portNum).getSolver().resetOutputMessage(_inputMsgs[portNum]);
	}


	@Override
	public @Nullable Value getInputMsg(int portIndex)
	{
		final Value[] inputMsgs = _inputMsgs;
		return inputMsgs != null ? inputMsgs[portIndex] : null;
	}


	@Override
	public @Nullable Object getOutputMsg(int portIndex)
	{
		return null;	// No output message by default (except for custom factors)
	}


	@SuppressWarnings("null")
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		_outputsValid = false;
		_inputMsgs[thisPortNum] = ((GibbsRealFactor)other)._inputMsgs[otherPortNum];
	}

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}

}
