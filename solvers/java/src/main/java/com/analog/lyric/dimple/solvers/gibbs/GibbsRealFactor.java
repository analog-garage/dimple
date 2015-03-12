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
import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;

/**
 * Real solver factor under Gibbs solver.
 * 
 * @since 0.07
 * @author Christopher Barber
 */
@SuppressWarnings("deprecation") // TODO: remove when SRealFactor removed
public class GibbsRealFactor extends SRealFactor implements ISolverFactorGibbs
{
	protected Value [] _currentSamples = new Value[0];
	//	private Object[] _scratchValues;
	protected boolean _isDeterministicDirected;
	private int _topologicalOrder = 0;
	/**
	 * True if output samples in {@link #_currentSamples} have been computed.
	 */
	private boolean _outputsValid = false;
	
	private boolean _visited = false;
	
	public GibbsRealFactor(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
		_isDeterministicDirected = _model.getFactorFunction().isDeterministicDirected();
	}
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		_outputsValid = false;
    	final int size = getSiblingCount();
    	
    	if (_currentSamples.length != size)
    	{
    		_currentSamples = new Value[size];
    	}
    	
	    for (int port = 0; port < size; port++)
	    {
	    	_currentSamples[port] = getSibling(port).getCurrentSampleValue();
	    }
	}
	
	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(FactorGraphEdgeState edge)
	{
		return null;
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
	public ISolverVariableGibbs getSibling(int edge)
	{
		return (ISolverVariableGibbs)super.getSibling(edge);
	}

	@Override
	public GibbsSolverGraph getParentGraph()
	{
		return (GibbsSolverGraph)_parent;
	}
	
	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		Variable var = requireNonNull(_model.getSibling(outPortNum));

		if (var instanceof Discrete)
		{
			final Discrete discrete = (Discrete)var;
			
			// This edge connects to a discrete variable, so send an output message
			// This method only considers the current conditional values, and does propagate
			// to any other variables (unlike get ConditionalPotential)
			// This should only be called if this factor is not a deterministic directed factor
			final DiscreteDomain outputVariableDomain = discrete.getDiscreteDomain();
			final FactorFunction factorFunction = _model.getFactorFunction();
			final int numPorts = getSiblingCount();
			
			final Object[] values = new Object[numPorts];
			
			final Value[] inputMsgs = Objects.requireNonNull(_currentSamples);
			for (int port = 0; port < numPorts; port++)
				values[port] = inputMsgs[port].getObject();

			final GibbsDiscreteEdge edge = requireNonNull((GibbsDiscreteEdge)getSiblingEdgeState(outPortNum));
			double[] outputMsgs = edge.factorToVarMsg.representation();
			
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

		final Value[] inputMsgs = _currentSamples;
		if (inputMsgs.length > 0)
		{
			final double energy = _model.getFactorFunction().evalEnergy(inputMsgs);
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
	    return _model.getFactorFunction().evalEnergy(inputs);
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
		((GibbsSolverGraph)getRootSolverGraph()).scheduleDeterministicDirectedUpdate(this, variableIndex, oldValue);
	}
	
	@Override
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValues)
	{
		// Compute the output values of the deterministic factor function from the input values
		final Factor factor = _model;
		final FactorFunction function = factor.getFactorFunction();
		int[] directedTo = factor.getDirectedTo();

		final Value[] inputMsgs = requireNonNull(_currentSamples);
		
		final SolverNodeMapping solvers = requireNonNull(getParentGraph()).getSolverMapping();
		
		if (oldValues != null && _outputsValid)
		{
			AtomicReference<int[]> changedOutputsHolder = new AtomicReference<int[]>();
			Value[] values = function.updateDeterministicToCopy(inputMsgs, oldValues, changedOutputsHolder);
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
					Value newValue = values[outputIndex];
					((ISolverVariableGibbs)solvers.getSolverVariable(variable)).setCurrentSample(newValue);
				}
			}
		}
		else
		{
			Value[] values = function.evalDeterministicToCopy(inputMsgs);
			_outputsValid = true;

			// Update the directed-to variables with the computed values
			if (directedTo != null)
			{
				// Full update
				for (int outputIndex : directedTo)
				{
					Variable variable = requireNonNull(factor.getSibling(outputIndex));
					Value newValue = values[outputIndex];
					((ISolverVariableGibbs)solvers.getSolverVariable(variable)).setCurrentSample(newValue);
				}
			}
		}
	}

	@Deprecated
	@Override
	public Value getInputMsg(int portIndex)
	{
		return _currentSamples[portIndex];
	}

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}

	@SuppressWarnings("null")
	@Override
	public GibbsSolverEdge<?> getSiblingEdgeState(int siblingIndex)
	{
		return (GibbsSolverEdge<?>)getSiblingEdgeState_(siblingIndex);
	}
}
