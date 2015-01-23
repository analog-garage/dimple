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

import static java.util.Objects.*;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ExtendedArrayList;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.NodeId;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.IParameterizedSolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Internal;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Provides shared implementation of management of common solver state.
 * <p>
 * @since 0.08
 * @category internal
 */
@Internal
public class FactorGraphSolverState<SFactor extends ISolverFactor, SVariable extends ISolverVariable, SEdge>
{
	/*-------
	 * State
	 */

	/**
	 * The solver graph that owns this state.
	 */
	private final IParameterizedSolverFactorGraph<SFactor,SVariable,SEdge> _owner;
	
	/**
	 * Solver factors belonging to {@link _owner} indexed by {@link Factor}s local index.
	 */
	private final ExtendedArrayList<SFactor> _factors;
	
	/**
	 * Solver variables belonging to {@link _owner} indexed by {@link Variable}s local index.
	 */
	private final ExtendedArrayList<SVariable> _variables;
	
	/**
	 * Solver subgraphs belonging to {@link _owner} indexed by each {@link FactorGraph}s local index.
	 */
	private final ExtendedArrayList<ISolverFactorGraph> _subgraphs;
	
	private final @Nullable ExtendedArrayList<SEdge> _edges;
	
	/*--------------
	 * Construction
	 */
	
	public FactorGraphSolverState(FactorGraph graph, IParameterizedSolverFactorGraph<SFactor,SVariable,SEdge> sgraph)
	{
		_owner = sgraph;
		_factors = new ExtendedArrayList<>(graph.getFactorCount(0));
		_variables = new ExtendedArrayList<>(graph.getVariableCount(0));
		_subgraphs = new ExtendedArrayList<>(graph.getOwnedGraphs().size());
		_edges = sgraph.hasEdgeState() ? new ExtendedArrayList<SEdge>(graph.getGraphEdgeCount()) : null;
	}
	
	/*---------------
	 * Inner classes
	 * 
	 * These provide iterable views of solver objects in this graph and subgraphs.
	 */
	
	// TODO - make implicit instantiation optional

	private abstract static class SNodeIterator<N extends Node,SN extends ISolverNode>
		extends UnmodifiableIterator<SN>
	{
		private final Iterator<N> _iter;
		
		private SNodeIterator(Collection<N> collection)
		{
			_iter = collection.iterator();
		}
		
		@Override
		public final boolean hasNext()
		{
			return _iter.hasNext();
		}
		
		@Override
		public final SN next()
		{
			return map(_iter.next());
		}
		
		abstract SN map(N node);
	}
	
	private abstract static class SNodes<N extends Node, SN extends ISolverNode>
		extends AbstractCollection<SN>
	{
		final Collection<N> _nodes;
		
		private SNodes(Collection<N> nodes)
		{
			_nodes = nodes;
		}
		
		@Override
		public int size()
		{
			return _nodes.size();
		}
	}
	
	private class OwnedSFactorIterator extends SNodeIterator<Factor,SFactor>
	{
		private OwnedSFactorIterator(Collection<Factor> iterable)
		{
			super(iterable);
			_factors.setSize(iterable.size());
		}
		
		@Override
		public SFactor map(Factor factor)
		{
			return requireNonNull(getSolverFactor(factor, true));
		}
	}
	
	private class OwnedSFactors extends SNodes<Factor, SFactor>
	{
		private OwnedSFactors()
		{
			super(getModelGraph().getOwnedFactors());
		}
		
		@Override
		public Iterator<SFactor> iterator()
		{
			return new OwnedSFactorIterator(_nodes);
		}
	}

	private class OwnedSVarIterator extends SNodeIterator<Variable,SVariable>
	{
		private OwnedSVarIterator(Collection<Variable> iterable)
		{
			super(iterable);
			_variables.setSize(iterable.size());
		}
		
		@Override
		public SVariable map(Variable variable)
		{
			return requireNonNull(getSolverVariable(variable, true));
		}
	}
	
	private class OwnedSVars extends SNodes<Variable, SVariable>
	{
		private OwnedSVars()
		{
			super(getModelGraph().getOwnedVariables());
		}
		
		@Override
		public Iterator<SVariable> iterator()
		{
			return new OwnedSVarIterator(_nodes);
		}
	}

	private class OwnedSubgraphIterator extends SNodeIterator<FactorGraph,ISolverFactorGraph>
	{
		private OwnedSubgraphIterator(Collection<FactorGraph> iterable)
		{
			super(iterable);
			_subgraphs.setSize(iterable.size());
		}
		
		@Override
		public ISolverFactorGraph map(FactorGraph subgraph)
		{
			return instantiateSubgraph(subgraph);
		}
	}
	
	private class OwnedSubgraphs extends SNodes<FactorGraph, ISolverFactorGraph>
	{
		private OwnedSubgraphs()
		{
			super(getModelGraph().getOwnedGraphs());
		}
		
		@Override
		public Iterator<ISolverFactorGraph> iterator()
		{
			return new OwnedSubgraphIterator(_nodes);
		}
	}

	/**
	 * Collection of subgraphs rooted at _owner in breadth-first order
	 */
	private class RecursiveSubgraphs extends ArrayList<ISolverFactorGraph>
	{
		private static final long serialVersionUID = 1L;

		private RecursiveSubgraphs()
		{
			super();
			
			add(_owner);
			
			// Add all subgraphs recursively in bread-first order.
			for (int i = 0; i < size(); ++i)
			{
				ISolverFactorGraph subgraph = get(i);
				addAll(subgraph.getSolverSubgraphs());
			}
		}
	}
	
	private abstract class RecursiveSNodeIterator<SN extends ISolverNode> extends UnmodifiableIterator<SN>
	{
		private final Iterator<ISolverFactorGraph> _sgraphIterator = getSolverSubgraphsRecursive().iterator();
		private Iterator<? extends SN> _snodeIterator = Collections.emptyIterator();
		
		@Override
		public boolean hasNext()
		{
			while (!_snodeIterator.hasNext() && _sgraphIterator.hasNext())
			{
				_snodeIterator = getNodes(_sgraphIterator.next()).iterator();
			}
			
			return _snodeIterator.hasNext();
		}
	
		@Override
		public SN next()
		{
			hasNext();
			
			return _snodeIterator.next();
		}
		
		int count()
		{
			int n = 0;

			while (_sgraphIterator.hasNext())
			{
				n += getNodes(_sgraphIterator.next()).size();
			}
			
			return n;
		}
		
		abstract Collection<? extends SN> getNodes(ISolverFactorGraph graph);
	}
	
	private abstract class RecursiveSNodes<SN extends ISolverNode> extends AbstractCollection<SN>
	{
		@Override
		public abstract RecursiveSNodeIterator<SN> iterator();
		
		@Override
		public int size()
		{
			return iterator().count();
		}
	}

	private class RecursiveSFactorIterator extends RecursiveSNodeIterator<ISolverFactor>
	{
		@Override
		Collection<? extends ISolverFactor> getNodes(ISolverFactorGraph graph)
		{
			return graph.getSolverFactors();
		}
	}
	
	private class RecursiveSFactors extends RecursiveSNodes<ISolverFactor>
	{
		@Override
		public RecursiveSFactorIterator iterator()
		{
			return new RecursiveSFactorIterator();
		}
	}
	
	private class RecursiveSVariableIterator extends RecursiveSNodeIterator<ISolverVariable>
	{
		@Override
		Collection<? extends ISolverVariable> getNodes(ISolverFactorGraph graph)
		{
			return graph.getSolverVariables();
		}
	}
	
	private class RecursiveSVariables extends RecursiveSNodes<ISolverVariable>
	{
		@Override
		public RecursiveSVariableIterator iterator()
		{
			return new RecursiveSVariableIterator();
		}
	}
	
	/*---------
	 * Methods
	 */
	
	public final FactorGraph getModelGraph()
	{
		return _owner.getModelObject();
	}
	
	public final IParameterizedSolverFactorGraph<SFactor,SVariable,SEdge> getSolverGraph()
	{
		return _owner;
	}
	
	/**
	 * Unmodifiable collection over owned solver factors, implicitly instantiated if necessary.
	 * @since 0.08
	 */
	public Collection<SFactor> getSolverFactors()
	{
		return new OwnedSFactors();
	}
	
	public Collection<ISolverFactor> getSolverFactorsRecursive()
	{
		return new RecursiveSFactors();
	}
	
	/**
	 * Unmodifiable collection over owned solver variables, implicitly instantiated if necessary.
	 * @since 0.08
	 */
	public Collection<SVariable> getSolverVariables()
	{
		return new OwnedSVars();
	}
	
	public Collection<ISolverVariable> getSolverVariablesRecursive()
	{
		return new RecursiveSVariables();
	}
	
	public @Nullable ISolverFactor getSolverFactorRecursive(Factor factor, boolean create)
	{
		final FactorGraph factorParent = factor.requireParentGraph();
		
		if (factorParent == getModelGraph())
		{
			return getSolverFactor(factor, create);
		}
		
		ISolverFactorGraph ssubgraph = getSolverSubgraphRecursive(factorParent, true);
		return ssubgraph != null ? ssubgraph.getSolverFactor(factor, create) : null;
	}
	
	public @Nullable ISolverFactorGraph getSolverSubgraphRecursive(FactorGraph subgraph, boolean create)
	{
		final FactorGraph graph = getModelGraph();
		FactorGraph parent = subgraph.getParentGraph();
		
		if (parent == graph)
		{
			return instantiateSubgraph(subgraph);
		}
		
		Deque<FactorGraph> subgraphs = new ArrayDeque<>();
		
		final FactorGraph originalSubgraph = subgraph;
		
		while (true)
		{
			if (parent == null)
			{
				throw new IllegalArgumentException(
					String.format("Cannot get solver graph for %s because it is not in %s", originalSubgraph, graph));
			}

			subgraphs.push(subgraph);
			subgraph = parent;
			parent = subgraph.getParentGraph();

			if (parent == graph)
			{
				ISolverFactorGraph sgraph = instantiateSubgraph(subgraph);
				while (!subgraphs.isEmpty())
				{
					subgraph = subgraphs.pop();
					sgraph = sgraph.getSolverSubgraph(subgraph);
					if (sgraph == null)
					{
						break;
					}
				}
				return sgraph;
			}
		}
	}
	
	public Collection<ISolverFactorGraph> getSolverSubgraphs()
	{
		return new OwnedSubgraphs();
	}
	
	public Collection<ISolverFactorGraph> getSolverSubgraphsRecursive()
	{
		return new RecursiveSubgraphs();
	}
	
	public @Nullable ISolverVariable getSolverVariableRecursive(Variable variable, boolean create)
	{
		final FactorGraph factorParent = variable.requireParentGraph();
		
		if (factorParent == getModelGraph())
		{
			return getSolverVariable(variable, create);
		}
		
		ISolverFactorGraph ssubgraph = getSolverSubgraphRecursive(factorParent, true);
		return ssubgraph != null ? ssubgraph.getSolverVariable(variable, create) : null;
	}
	
	public @Nullable SEdge getSolverEdge(FactorGraphEdgeState edge, boolean create)
	{
		final ExtendedArrayList<SEdge> edges = _edges;
		if (edges == null)
		{
			return null;
		}
		
		final int index = edge.edgeIndex();
		SEdge result = edges.get(index);
		
		if (result == null)
		{
			if (create)
			{
				result = _owner.createEdgeState(edge);
			}
			edges.set(index,  result);
		}
		
		return result;
	}
	
	public @Nullable SEdge getSolverEdge(int edgeIndex, boolean create)
	{
		final ExtendedArrayList<SEdge> edges = _edges;
		if (edges == null)
		{
			return null;
		}
		
		SEdge result = edges.getOrNull(edgeIndex);
		
		if (result == null)
		{
			if (create)
			{
				result = _owner.createEdgeState(getModelGraph().getGraphEdgeState(edgeIndex));
			}
			edges.set(edgeIndex,  result);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public @Nullable SFactor getSolverFactor(Factor factor, boolean create)
	{
		assertSameGraph(factor);
		
		final int index = NodeId.indexFromLocalId(factor.getLocalId());
		final ExtendedArrayList<SFactor> factors = _factors;
		
		@SuppressWarnings("unchecked")
		SFactor sfactor = factors.getOrNull(index);

		if (sfactor == null || sfactor.getModelObject() != factor)
		{
			if (create)
			{
				if (factor instanceof BlastFromThePastFactor)
				{
					// FIXME - hacky
					sfactor = (SFactor)_owner.createBlastFromThePast((BlastFromThePastFactor)factor);
					sfactor.setParent(_owner);
				}
				else
				{
					sfactor = _owner.createFactor(factor);
					sfactor.setParent(_owner);
				}
//				sfactor.createMessages();
			}
			else
			{
				sfactor = null;
			}
			factors.set(index, sfactor);
		}
		
		return sfactor;
	}
	
	public void instantiateSolverEdges()
	{
		ExtendedArrayList<SEdge> edges = _edges;
		if (edges != null)
		{
			final int n = getModelGraph().getGraphEdgeCount();
			edges.setSize(n);
			for (int i = 0; i < n; ++i)
			{
				getSolverEdge(i, true);
			}
		}
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
			sgraph = _owner.createSubgraph(subgraph);
			sgraph.setParent(_owner);
			graphs.set(index, sgraph);
			if (_owner == getModelGraph().getSolver())
			{
				// If parent is default solver for it's graph, make this the default solver for the subgraph.
				subgraph.setSolver(sgraph);
			}
		}
		
		return sgraph;
	}
	
	public @Nullable SVariable getSolverVariable(Variable variable, boolean create)
	{
		assertSameGraph(variable);
		
		final int index = NodeId.indexFromLocalId(variable.getLocalId());
		final ExtendedArrayList<SVariable> variables = _variables;
		
		@SuppressWarnings("unchecked")
		SVariable svar = variables.getOrNull(index);
		
		if (svar == null || svar.getModelObject() != variable)
		{
			if (create)
			{
				svar = _owner.createVariable(variable);
				svar.setParent(_owner);
//				svar.createNonEdgeSpecificState();
//				svar.setInputOrFixedValue(variable.getInputObject(), variable.getFixedValueObject());
			}
			else
			{
				svar = null;
			}
			variables.set(index, svar);
		}
		
		return svar;
	}
	
//	public void instantiateAll()
//	{
//		// Instantiate and get solver subgraphs
//		ArrayList<ISolverFactorGraph> sgraphs = new ArrayList<>(getSolverSubgraphsRecursive());
//
//		// Instantiate all solver variables
//		Iterators.size(getSolverVariablesRecursive().iterator());
//
//		// Instantiate solver factors from bottom up (see bug 404)
//		for (int i = sgraphs.size(); --i>=0;)
//		{
//			Iterators.size(sgraphs.get(i).getSolverFactors().iterator());
//		}
//	}
	
	public void setSubgraphSolver(FactorGraph subgraph, @Nullable ISolverFactorGraph sgraph)
	{
		assertSameGraph(subgraph);
		
		final int index = NodeId.indexFromLocalId(subgraph.getLocalId());
		final ExtendedArrayList<ISolverFactorGraph> graphs = _subgraphs;
		if (sgraph != null)
		{
			sgraph.setParent(_owner);
		}
		graphs.set(index,  sgraph);
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private void assertSameGraph(Node node)
	{
		if (node.getParentGraph() != _owner.getModelObject())
		{
			throw new IllegalArgumentException(String.format("The %s '%s' does not belong to graph.",
				node.getNodeType().name().toLowerCase(), node));
		}
	}
}
