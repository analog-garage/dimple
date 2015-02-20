/*******************************************************************************
*   Copyright 2012-2015 Analog Devices, Inc.
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
import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SBlastFromThePast;

/**
 * @since 0.07
 */
public class GibbsTableFactorBlastFromThePast extends SBlastFromThePast implements ISolverFactorGibbs
{
	private boolean _visited = false;
	private int _topologicalOrder = 0;
	
	public GibbsTableFactorBlastFromThePast(BlastFromThePastFactor f)
	{
		super(f);
	}

	@Override
	public GibbsSolverEdge<?> createEdge(FactorGraphEdgeState edge)
	{
		return new GibbsDiscreteEdge((Discrete)edge.getVariable(getFactor().requireParentGraph()));
	}
	
	@Override
	public void createMessages(Variable var, Port port)
	{
		super.createMessages(var,port);
	}
	
	@Override
	public Value getInputMsg(int portIndex)
	{
		final GibbsSolverGraph sgraph = (GibbsSolverGraph)requireNonNull(getParentGraph());
		final GibbsDiscrete svar = sgraph.getDiscrete((Discrete)_portForOtherVar.node);
		return svar.getPrevSampleValue();
	}
	
	@SuppressWarnings("null")
	@Override
	public double getPotential()
	{
		final GibbsSolverGraph sgraph = (GibbsSolverGraph)getParentGraph();
		final GibbsDiscrete svar = (GibbsDiscrete)sgraph.getSolverVariable((Variable)_portForOtherVar.node);
		final DiscreteValue inputMsg = svar.getPrevSampleValue();
		final GibbsDiscreteEdge sedge = svar.getDiscreteEdge(_portForOtherVar.index);
		return sedge.factorToVarMsg.getEnergy(inputMsg.getIndex());
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
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValue");
	}

	@Override
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValue)
	{
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValuesNow");
	}

	@Override
	public void updateEdgeMessage(int portIndex)
	{
		//NOP
	}

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}
}
