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

package com.analog.lyric.dimple.data;

import static java.lang.String.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterables;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * {@link DataLayer} implementation for {@link com.analog.lyric.dimple.model.variables.Variable#getPrior()
 * variable priors}.
 * <p>
 * Priors are stored directly in Variable instances, so this class simply maps to those
 * and has no variable-specific local state.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public final class PriorDataLayer extends GenericDataLayer
{
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct prior data layer for given graph.
	 * <p>
	 * @param graph
	 * @since 0.08
	 */
	public PriorDataLayer(FactorGraph graph)
	{
		super(graph, PriorFactorGraphData.constructor());
	}
	
	@Override
	public PriorDataLayer clone()
	{
		return new PriorDataLayer(rootGraph());
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj instanceof PriorDataLayer)
		{
			return rootGraph() == ((PriorDataLayer)obj).rootGraph();
		}
		
		return super.equals(obj);
	}
	
	/*-------------
	 * Map methods
	 */
	
	@Override
	public void clear()
	{
		for (Variable var : FactorGraphIterables.variables(rootGraph()))
		{
			var.setPrior(null);
		}
	}
	
	@Override
	public boolean containsKey(@Nullable Object key)
	{
		return get(key) != null;
	}
	
	@Override
	public @Nullable IDatum get(Variable var)
	{
		return sharesRoot(var) ? var.getPrior() : null;
	}
	
	@NonNullByDefault(false)
	@Override
	public @Nullable IDatum put(Variable var, @Nullable IDatum value)
	{
		assertSharesRoot(var);

		IDatum priorPrior = var.getPrior();
		var.setPrior(value);
		return priorPrior;
	}

	@Override
	public @Nullable IDatum remove(Variable var)
	{
		return sharesRoot(var) ? var.setPrior(null) : null;
	}
	
	/**
	 * Number of priors set in layer.
	 * <p>
	 * The size is not cached and must be computed every time this method is called,
	 * which requires looking at every variable in the graph tree.
	 */
	@Override
	public int size()
	{
		int count = 0;
		for (Variable var : FactorGraphIterables.variables(rootGraph()))
		{
			if (var.getPrior() != null)
			{
				++count;
			}
		}
		return count;
	}
	
	/*-------------------------
	 * DataLayer(Base) methods
	 */
	
	@Override
	public boolean containsDataFor(Variable key)
	{
		return get(key) != null;
	}
	
	/**
	 * Returns true to indicate that this is a view of priors held directly in the {@link Variable} objects.
	 * <p>
	 * Because this is a view, cloning this object does not create a distinct copy of the values.
	 */
	@Override
	public boolean isView()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @param data must be a {@link PriorFactorGraphData}
	 */
	@Override
	public @Nullable FactorGraphData<Variable, IDatum> setDataForGraph(FactorGraphData<Variable, IDatum> data)
	{
		if (data instanceof PriorFactorGraphData)
		{
			return super.setDataForGraph(data);
		}
		
		throw new IllegalArgumentException(format("%s is not a %s", data, PriorFactorGraphData.class.getSimpleName()));
	}
}
