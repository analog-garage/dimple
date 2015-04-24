/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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
import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SBlastFromThePast;

// TODO - push common code w/ GibbsTableFactorBlastFromThePast into superclass

/**
 * Real "blast from the past" factor for Gibbs solver.
 * 
 * @since 0.07
 */
public class GibbsRealFactorBlastFromThePast extends SBlastFromThePast implements ISolverFactorGibbs
{
	private boolean _visited;
	private int _topologicalOrder = 0;
	
	@SuppressWarnings("null")
	public GibbsRealFactorBlastFromThePast(BlastFromThePastFactor f, GibbsSolverGraph parent)
	{
		super(f, parent);
	}
	
	@Override
	public GibbsSolverEdge<?> createEdge(EdgeState edge)
	{
		Variable var = edge.getVariable(getFactor().requireParentGraph());
		
		if (var instanceof Discrete)
		{
			return new GibbsDiscreteEdge((Discrete)var);
		}

		return new GibbsGenericEdge();
	}
	
	@Override
	public GibbsSolverGraph getParentGraph()
	{
		return (GibbsSolverGraph)_parent;
	}
	
	@Override
	public ISolverVariableGibbs getSibling(int edge)
	{
		return (ISolverVariableGibbs)super.getSibling(edge);
	}
	
	@Override
	public double getPotential()
	{
		ISolverFactorGibbs otherFactor = getOtherFactor();
		final int size = otherFactor.getSiblingCount();
		Value[] currentSamples = new Value[size];
		for (int i = 0; i < size; ++i)
		{
			ISolverVariableGibbs var = otherFactor.getSibling(i);
			currentSamples[i] = var.getPrevSampleValue();
		}
	    return getFactor().getFactorFunction().evalEnergy(currentSamples);
	}
	
	@Deprecated
	public double getPotential(Object[] inputs)
	{
		return getFactor().getFactorFunction().evalEnergy(inputs);
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
	public void updateNeighborVariableValue(int variableIndex, Value value)
	{
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValue");
	}

	@Override
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValue)
	{
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValuesNow");
	}

	@Override
	public void updateEdgeMessage(EdgeState modelEdge, GibbsSolverEdge<?> solverEdge)
	{
		// Do nothing
	}
	
	@Override
	public Value getInputMsg(int portIndex)
	{
		return getOtherFactor().getSibling(portIndex).getPrevSampleValue();
	}
	
	@Override
	public Object getOutputMsg(int portIndex)
	{
		final Port port = requireNonNull(_portForOtherVar);
		final Factor otherFactor = (Factor)port.getNode().getSibling(port.getSiblingNumber());
		GibbsSolverGraph sgraph = (GibbsSolverGraph) getSolverMapping().getSolverGraph(otherFactor.requireParentGraph());
		return requireNonNull(sgraph.getSolverEdge(otherFactor.getSiblingEdgeState(portIndex))).factorToVarMsg;
	}

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}
	
	protected ISolverFactorGibbs getOtherFactor()
	{
		final Port port = requireNonNull(_portForOtherVar);
		final Factor otherFactor = (Factor)port.getNode().getSibling(port.getSiblingNumber());
		return (ISolverFactorGibbs)getSolverMapping().getSolverFactor(otherFactor);
	}
}
