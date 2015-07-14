/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import static java.lang.String.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.PrimitiveIterable;
import com.analog.lyric.dimple.data.DataLayerBase;
import com.analog.lyric.dimple.data.FactorGraphData;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
class GibbsFactorGraphData extends FactorGraphData<Variable, Value>
{
	/*-------
	 * State
	 */
	
	final GibbsSolverGraph _solverGraph;
	
	/*--------------
	 * Construction
	 */
	
	static class Constructor implements FactorGraphData.Constructor<Variable, Value>
	{
		private final ISolverFactorGraph _rootSolver;
		
		Constructor(ISolverFactorGraph rootSolver)
		{
			_rootSolver = rootSolver;
		}
		
		@Override
		public FactorGraphData<Variable, Value> apply(DataLayerBase<Variable, ? super Value> layer, FactorGraph graph)
		{
			ISolverFactorGraph sgraph = _rootSolver.getSolverSubgraph(graph);
			if (sgraph instanceof GibbsSolverGraph)
				return new GibbsFactorGraphData(layer, (GibbsSolverGraph)sgraph);
			
			throw new IllegalStateException(format("%s is not a GibbsSolverGraph", sgraph));
		}

		@Override
		public boolean createOnRead()
		{
			return true;
		}
		
		@Override
		public Class<Variable> keyType()
		{
			return Variable.class;
		}

		@Override
		public Class<Value> baseType()
		{
			return Value.class;
		}
	}
	
	public static FactorGraphData.Constructor<Variable, Value> constructor(ISolverFactorGraph rootSolver)
	{
		return new Constructor(rootSolver);
	}
		
	protected GibbsFactorGraphData(DataLayerBase<Variable, ? super Value> layer, GibbsSolverGraph sgraph)
	{
		super(layer, sgraph.getModel(), Variable.class, Value.class);
		_solverGraph = sgraph;
	}

	@Override
	public FactorGraphData<Variable, Value> clone(DataLayerBase<Variable, ? super Value> newLayer)
	{
		return new GibbsFactorGraphData(newLayer, _solverGraph);
	}
	
	/*----------------
	 * Object methods
	 */

	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj instanceof GibbsFactorGraphData)
		{
			GibbsFactorGraphData other = (GibbsFactorGraphData)obj;
			
			// Since this object doesn't directly hold any data, if two the solver graph is
			// the same, the data must be the same.
			return other._solverGraph == _solverGraph;
		}
		
		return super.equals(obj);
	}
	
	/*-------------
	 * Map methods
	 */
	
	/**
	 * Clear is not supported for Gibbs sample layer.
	 * <p>
	 * @throws UnsupportedOperationException always
	 */
	@Override
	public void clear()
	{
		throw GibbsSampleLayer.removalNotSupported();
	}

	
	@Override
	public int size()
	{
		// There should be a sample for every variable
		return _graph.getOwnedVariableCount();
	}

	/*-------------------------
	 * FactorGraphData methods
	 */
	
	@Override
	public boolean containsLocalIndex(int index)
	{
		return getByLocalIndex(index) != null;
	}

	@Override
	public @Nullable Value getByLocalIndex(int index)
	{
		ISolverVariableGibbs svar = (ISolverVariableGibbs)_solverGraph.getSolverVariableByIndex(index);
		return svar != null ? svar.getCurrentSampleValue() : null;
	}

	@Override
	public PrimitiveIterable.OfInt getLocalIndices()
	{
		return _solverGraph.getSolverVariableIndices();
	}

	/**
	 * Returns true to indicate that this is a view of sample values held directly in the Gibbs solver variables.
	 * <p>
	 * Because this is a view, cloning this object does not create a distinct copy of the values.
	 */
	@Override
	public boolean isView()
	{
		return true;
	}

	@Override
	public @Nullable Value setByLocalIndex(int index, @Nullable Value value)
	{
		if (value == null)
		{
			throw GibbsSampleLayer.removalNotSupported();
		}
		
		Value curValue = getByLocalIndex(index);
		
		if (curValue == null)
		{
			throw new IllegalArgumentException(format("There is no value for variable with index %d", index));
		}
		
		curValue.setFrom(value);
		
		return curValue;
	}
}
