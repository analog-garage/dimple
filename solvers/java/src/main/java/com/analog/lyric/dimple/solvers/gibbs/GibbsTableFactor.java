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

import static java.util.Objects.*;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;
import com.analog.lyric.util.misc.Internal;

/**
 * Solver table factor for Gibbs solver.
 * 
 * @since 0.07
 */
public class GibbsTableFactor extends STableFactorBase implements ISolverFactorGibbs
{
	/*-------
	 * State
	 */
	
    protected DiscreteValue[] _currentSamples = new DiscreteValue[0];
    protected boolean _isDeterministicDirected;
    private boolean _visited = false;
	private int _topologicalOrder = 0;
    
    /*--------------
     * Construction
     */
    
	public GibbsTableFactor(Factor factor, GibbsSolverGraph parent)
	{
		super(factor, parent);
		_isDeterministicDirected = _model.getFactorFunction().isDeterministicDirected();
	}
	
	/*---------------
	 * SNode methods
	 */
	
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
	public GibbsDiscrete getSibling(int edge)
	{
		return (GibbsDiscrete)super.getSibling(edge);
	}
	
	/*----------------------------
	 * ISolverFactorGibbs methods
	 */
	
	@Override
	public @Nullable GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		return null;
	}

	@Override
	public void updateEdgeMessage(int outPortNum)
	{
		// Generate message representing conditional distribution of selected edge variable
		// This should be called only for a table factor that is not a deterministic directed factor

		if (_isDeterministicDirected) throw new DimpleException("Invalid call to updateEdge");
		
		double[] outMessage = getSiblingEdgeState(outPortNum).factorToVarMsg.representation();

		final int numPorts = _currentSamples.length;

		final IFactorTable factorTable = getFactorTableIfComputed();
		if (factorTable != null)
		{
			int[] inPortMsgs = new int[numPorts];
			for (int port = 0; port < numPorts; port++)
				inPortMsgs[port] = _currentSamples[port].getIndex();

			inPortMsgs[outPortNum] = 0;
			
			factorTable.getEnergySlice(outMessage, outPortNum, inPortMsgs);
		}
		else
		{
			final Value changedValue = _currentSamples[outPortNum];
			final FactorFunction function = _model.getFactorFunction();
			final int savedIndex = changedValue.getIndex();
			final int sliceLength = outMessage.length;

			changedValue.setIndex(0);
			outMessage[0] = function.evalEnergy(_currentSamples);

			if (function.useUpdateEnergy(_currentSamples, 1))
			{
				final Value prevValue = changedValue.clone();
				final IndexedValue[] changedValues = new IndexedValue[] { new IndexedValue(outPortNum, prevValue) };

				double energy = outMessage[0];
				for (int i = 1; i < sliceLength; ++i)
				{
					changedValue.setIndex(i);
					prevValue.setIndex(i - 1);
					outMessage[i] = energy = function.updateEnergy(_currentSamples, changedValues, energy);
				}
			}
			else
			{
				for (int i = 1; i < sliceLength; ++i)
				{
					changedValue.setIndex(i);
					outMessage[i] = function.evalEnergy(_currentSamples);
				}
			}
			
			changedValue.setIndex(savedIndex);
		}
	}
	
	@Override
	public double getPotential()
	{
		if (_isDeterministicDirected)
			return 0;

		final int size = _currentSamples.length;
		if (size == 0)
		{
			// Probably because initalize() not yet called.
			return Double.POSITIVE_INFINITY;
		}

		if (getFactorTableIfComputed() == null)
		{
			// Avoid creating table because it may be very large.
			// FIXME - think more about this. Should this be conditional on something?
			final double energy = getFactor().getFactorFunction().evalEnergy(_currentSamples);
			if (energy != energy)	// Faster isNaN
				return Double.POSITIVE_INFINITY;
			return energy;
		}
		
		int[] inPortMsgs = new int[size];
		for (int port = 0; port < size; port++)
			inPortMsgs[port] = _currentSamples[port].getIndex();

		return getPotential(inPortMsgs);
	}
	
	@Internal
	public double getPotential(int[] inputs)
	{
		return getFactorTable().getEnergyForIndices(inputs);
	}
		
	@Override
	public final int getTopologicalOrder()
	{
		return _topologicalOrder ;
	}
	
	@Override
	public final void setTopologicalOrder(int order)
	{
		_topologicalOrder = order;
	}
	
	
	@Override
	public void updateNeighborVariableValue(int variableIndex, Value oldValue)
	{
		((GibbsSolverGraph)requireNonNull(getRootSolverGraph())).scheduleDeterministicDirectedUpdate(this, variableIndex, oldValue);
	}
	
	@Override
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValues)
	{
		// Compute the output values of the deterministic factor function from the input values
		final Factor factor = _model;
		Value[] values = factor.getFactorFunction().evalDeterministicToCopy(_currentSamples);
		
		// Update the directed-to variables with the computed values
		SolverNodeMapping solvers = getSolverMapping();
		int[] directedTo = factor.getDirectedTo();
		if (directedTo != null)
		{
			for (int outputIndex : directedTo)
			{
				Variable variable = factor.getSibling(outputIndex);
				// FIXME: is sample value already set? Just need to handle side effects?
				ISolverVariableGibbs svar = (ISolverVariableGibbs) solvers.getSolverVariable(variable);
				svar.setCurrentSample(values[outputIndex]);
			}
		}
	}

	@Override
	public void initialize()
	{
		super.initialize();
		_isDeterministicDirected = _model.getFactorFunction().isDeterministicDirected();
		
    	final int size = getSiblingCount();
    	
    	if (_currentSamples.length != size)
    	{
    		_currentSamples = new DiscreteValue[size];
    	}
    	
	    for (int port = 0; port < size; port++)
	    {
	    	_currentSamples[port] = (DiscreteValue)((ISolverVariableGibbs)getSibling(port)).getCurrentSampleValue();
	    }
	}

	@Deprecated
	@Override
	public DiscreteValue getInputMsg(int portIndex)
	{
		return _currentSamples[portIndex];
	}

	@Deprecated
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return getSiblingEdgeState(portIndex).factorToVarMsg.representation();
	}

	/*--------------------------
	 * STableFactorBase methods
	 */

	@Override
	protected void setTableRepresentation(IFactorTable table)
	{
		if (_isDeterministicDirected)
		{
			table.setRepresentation(FactorTableRepresentation.DETERMINISTIC);
		}
		else
		{
			table.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		}
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
	public GibbsDiscreteEdge getSiblingEdgeState(int siblingIndex)
	{
		return (GibbsDiscreteEdge)getSiblingEdgeState_(siblingIndex);
	}
}
