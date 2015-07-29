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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.IUnaryFactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterables;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.google.common.collect.Lists;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class DataStack extends AbstractList<DataLayer<?>>
{
	/*-------
	 * State
	 */
	
	private final ArrayList<DataLayer<?>> _stack;

	/*--------------
	 * Construction
	 */
	
	public DataStack(Collection<DataLayer<?>> layers)
	{
		if (layers.size() == 0)
			throw new IllegalArgumentException(format("Cannot create %s with no layers.", getClass().getSimpleName()));
		
		_stack = new ArrayList<>(layers);
		
		// Ensure that all layers are for the same graph
		final FactorGraph root = _stack.get(0).rootGraph();
		for (int i = 1, n = _stack.size(); i < n; ++i)
		{
			if (_stack.get(i).rootGraph() != root)
			{
				throw new IllegalArgumentException(format("Cannot create %s with layers from different graphs",
					getClass().getSimpleName()));
			}
		}
	}
	
	public DataStack(DataLayer<?> firstLayer, DataLayer<?> ... additionalLayers)
	{
		this(Lists.asList(firstLayer, additionalLayers));
	}
	
	/*--------------
	 * List methods
	 */
	
	@Override
	public DataLayer<?> get(int index)
	{
		return _stack.get(index);
	}

	@Override
	public int size()
	{
		return _stack.size();
	}
	
	/*-------------------
	 * DataStack methods
	 */
	
	/**
	 * Computes the total energy for the graph tree represented by this data stack.
	 * <p>
	 * Computes the total energy by adding the energy evaluated for all the factors
	 * and variable priors and conditioning functions given the value specified for
	 * each variable in the data stack. Specifically:
	 * <ul>
	 * <li><b>for each factor</b>: {@linkplain #getValue(Variable) looks up the value} for each of the variables
	 * connected to the factor and passes them the {@linkplain FactorFunction#evalEnergy(Value[])
	 * evalEnergy} method of the factor's {@linkplain Factor#getFactorFunction() factor function}.
	 * 
	 * <li><b>for each variable</b>: {@linkplain #getValue(Variable) looks up the value} for the variable and
	 * passes it to the {@linkplain IUnaryFactorFunction#evalEnergy(Value) evalEnergy} method of each
	 * {@link IUnaryFactorFunction} specified for that variable in layers that precede the layer containing
	 * the variable value.
	 * </ul>
	 * <p>
	 * @since 0.08
	 * @throws IllegalStateException if any variable in the graph lacks a value.
	 */
	public double computeTotalEnergy()
	{
		final FactorGraph root = rootGraph();
		final int nLayers = _stack.size();
		
		double energy = 0.0;

		final IUnaryFactorFunction[] functions = new IUnaryFactorFunction[nLayers];
		for (Variable var : FactorGraphIterables.variables(root))
		{
			for (int i = 0; i < nLayers; ++i)
			{
				IDatum datum = _stack.get(i).get(var);
				if (datum instanceof Value)
				{
					Value value = (Value)datum;
					if (!var.getDomain().valueInDomain(value))
					{
						return Double.POSITIVE_INFINITY;
					}
					while (--i >= 0)
					{
						IUnaryFactorFunction function = functions[i];
						if (function != null)
						{
							energy += function.evalEnergy(value);
							if (energy == Double.POSITIVE_INFINITY)
							{
								return energy;
							}
						}
					}
					break;
				}
				else if (datum instanceof IUnaryFactorFunction)
				{
					functions[i] = (IUnaryFactorFunction)datum;
				}
				else
				{
					functions[i] = null;
				}
			}
		}
		
		for (Factor factor : FactorGraphIterables.factors(root))
		{
			final int nVars = factor.getSiblingCount();
			final Value[] values = new Value[nVars];
			
			for (int i = 0; i < nVars; ++i)
			{
				Variable var = factor.getSibling(i);
				Value value = getValue(var);
				if (value == null)
				{
					throw new IllegalStateException(format("There is no value for %s", var));
				}
				values[i] = value;
			}
			
			energy += factor.getFactorFunction().evalEnergy(values);
			if (energy == Double.POSITIVE_INFINITY)
			{
				return energy;
			}
		}
		
		return energy;
	}
	
	public @Nullable Value getValue(Variable var)
	{
		for (DataLayer<?> layer : _stack)
		{
			IDatum datum = layer.get(var);
			if (datum instanceof Value)
			{
				return (Value)datum;
			}
		}
		
		return null;
	}
	
	/**
	 * Root graph for all layers in this stack.
	 * @since 0.08
	 * @see DataLayer#rootGraph()
	 */
	public FactorGraph rootGraph()
	{
		return _stack.get(0).rootGraph();
	}
}