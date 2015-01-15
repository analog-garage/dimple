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

package com.analog.lyric.dimple.solvers.core;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ExtendedArrayList;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.NodeId;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.IParameterizedSolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Internal;
import com.google.common.collect.UnmodifiableIterator;

/**
 * 
 * @since 0.08
 * @category internal
 */
@Internal
public class FactorGraphSolverState<SFactor extends ISolverFactor, SVariable extends ISolverVariable>
{
	/*-------
	 * State
	 */

	private final IParameterizedSolverFactorGraph<SFactor,SVariable> _sgraph;
	private final ExtendedArrayList<SFactor> _factors;
	private final ExtendedArrayList<SVariable> _variables;
	private final ExtendedArrayList<ISolverFactorGraph> _subgraphs;
//	private final ObjectArrayList _edges;
	
	/*--------------
	 * Construction
	 */
	
	public FactorGraphSolverState(FactorGraph graph, IParameterizedSolverFactorGraph<SFactor,SVariable> sgraph)
	{
		_sgraph = sgraph;
		_factors = new ExtendedArrayList<>(graph.getFactorCount(0));
		_variables = new ExtendedArrayList<>(graph.getVariableCount(0));
		_subgraphs = new ExtendedArrayList<>(graph.getOwnedGraphs().size());
//		_edges = new ObjectArrayList(graph.getGraphEdgeCount());
	}
	
	/*---------------
	 * Inner classes
	 */
	
	private abstract static class SNodeIterator<T> extends UnmodifiableIterator<T>
	{
		private final List<T> _list;
		private int _index;
		
		private SNodeIterator(List<T> list)
		{
			_list = list;
			_index = 0;
		}
		
		@Override
		public final boolean hasNext()
		{
			return peek() != null;
		}

		@Override
		public final T next()
		{
			final T element = peek();
			
			if (element != null)
			{
				++_index;
				return element;
			}
			
			throw new NoSuchElementException();
		}
		
		private @Nullable T peek()
		{
			T element = null;
			
			for (; _index < _list.size(); ++_index)
			{
				element = _list.get(_index);
				
				if (element != null)
				{
					break;
				}
				
				element = instantiate(_index);
				
				if (element != null)
				{
					break;
				}
			}
			
			return element;
		}
		
		abstract @Nullable T instantiate(int index);
	}
	
	private class SFactorIterator extends SNodeIterator<SFactor>
	{
		SFactorIterator()
		{
			super(_factors);
		}

		@Override
		@Nullable SFactor instantiate(int index)
		{
			Factor factor = null; // FIXME modelGraph().getFactorByLocalId(NodeId.factorIdFromIndex(index));
			return factor != null ? instantiateFactor(factor) : null;
		}
	}
	
	private class SVariableIterator extends SNodeIterator<SVariable>
	{
		SVariableIterator()
		{
			super(_variables);
		}

		@Override
		@Nullable SVariable instantiate(int index)
		{
			Variable variable = null; // FIXME modelGraph().getVariableByLocalId(NodeId.variableIdFromIndex(index));
			return variable != null ? instantiateVariable(variable) : null;
		}
	}
	
	/*---------
	 * Methods
	 */
	
	public final FactorGraph modelGraph()
	{
		return _sgraph.getModelObject();
	}
	
	public Collection<SFactor> factors()
	{
		final int nFactors = modelGraph().getFactorCount(0);
		_factors.setSize(nFactors);
		
		return new AbstractCollection<SFactor>() {
			@Override
			public Iterator<SFactor> iterator()
			{
				return new SFactorIterator();
			}

			@Override
			public int size()
			{
				return nFactors;
			}
		};
	}
	
	public SFactor instantiateFactor(Factor factor)
	{
		assertSameGraph(factor);
		
		final int index = NodeId.indexFromLocalId(factor.getLocalId());
		final ExtendedArrayList<SFactor> factors = _factors;
		
		@SuppressWarnings("unchecked")
		SFactor sfactor = factors.getOrNull(index);
		
		if (sfactor == null || sfactor.getModelObject() != factor)
		{
			sfactor = _sgraph.createFactor(factor);
			sfactor.createMessages();
			factors.set(index, sfactor);
		}
		
		return sfactor;
	}
	
	public ISolverFactorGraph instantiateSubgraph(FactorGraph subgraph)
	{
		assertSameGraph(subgraph);
		
		final int index = NodeId.indexFromLocalId(subgraph.getLocalId());
		final ExtendedArrayList<ISolverFactorGraph> graphs = _subgraphs;
		
		@SuppressWarnings("unchecked")
		ISolverFactorGraph sgraph = graphs.getOrNull(index);
		
		if (sgraph == null || sgraph.getModelObject() != subgraph)
		{
//			sgraph = _sgraph.createSubgraph(subgraph);
			graphs.set(index, sgraph);
		}
		
		return sgraph;
	}
	
	public SVariable instantiateVariable(Variable variable)
	{
		assertSameGraph(variable);
		
		final int index = NodeId.indexFromLocalId(variable.getLocalId());
		final ExtendedArrayList<SVariable> variables = _variables;
		
		@SuppressWarnings("unchecked")
		SVariable svar = variables.getOrNull(index);
		
		if (svar == null || svar.getModelObject() != variable)
		{
			svar = _sgraph.createVariable(variable);
			svar.createNonEdgeSpecificState();
			svar.setInputOrFixedValue(variable.getInputObject(), variable.getFixedValueObject());
			variables.set(index, svar);
		}
		
		return svar;
	}
	
	public Collection<SVariable> variables()
	{
		final int nVariables = modelGraph().getVariableCount(0);
		_variables.setSize(nVariables);
		
		return new AbstractCollection<SVariable>() {
			@Override
			public Iterator<SVariable> iterator()
			{
				return new SVariableIterator();
			}

			@Override
			public int size()
			{
				return nVariables;
			}
		};
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private void assertSameGraph(Node node)
	{
		if (node.getParentGraph() != _sgraph.getModelObject())
		{
			throw new IllegalArgumentException(String.format("The %s '%s' does not belong to graph",
				node.getNodeType().name().toLowerCase(), node));
		}
	}
}
