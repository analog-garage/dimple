/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.model.core;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ReleasableIterators;
import com.analog.lyric.collect.SingleIterator;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.variables.Variable;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Contains static methods for constructing {@link Iterator}s over contents of {@link FactorGraph}s.
 * @since 0.08
 * @author Christopher Barber
 * @see FactorGraphIterables
 */
public class FactorGraphIterators
{
	/**
	 * Returns iterator over the boundary variables for graph.
	 * 
	 * @since 0.08
	 */
	public static IFactorGraphChildIterator<Variable> boundary(FactorGraph graph)
	{
		return new BoundaryVariableIterator(graph);
	}
	
	/**
	 * Returns iterator over all {@link Factor} objects contained in this graph.
	 * <p>
	 * Factors are visited in top-down order, i.e. those in a subgraph are visited after those from their parent.
	 * <p>
	 * @param root is the root graph whose factors will be visited. Only factors contained in this
	 * graph or its subgraphs will be included.
	 * @since 0.08
	 */
	public static IFactorGraphChildIterator<Factor> factors(FactorGraph root)
	{
		return new NestedFactorIterator(root, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterator over all {@link Factor} objects contained in this graph down to specified depth below root.
	 * <p>
	 * Factors are visited in top-down order, i.e. those in a subgraph are visited after those from their parent.
	 * <p>
	 * @param root is the root graph whose factors will be visited. Only factors contained in this
	 * graph or its subgraphs will be included.
	 * @param maxNestingDepth specifies how deep in the subgraph tree to visit. If zero is specified, only
	 * factors contained directly in the root will be included.
	 * @since 0.08
	 */
	public static IFactorGraphChildIterator<Factor> factorsDownto(FactorGraph root, int maxNestingDepth)
	{
		return new NestedFactorIterator(root, maxNestingDepth);
	}
	
	/**
	 * Returns iterator over all {@link Factor} and leaf {@link FactorGraph} objects contained in this graph
	 * down to specified depth below root.
	 * <p>
	 * Factors are visited in top-down order, i.e. those in a subgraph are visited after those from their parent.
	 * Subgraph objects will only be included if the maximum depth has been reached; for instance, if
	 * {@code maxNestingDepth} is one immediate child subgraph objects will not be included but any grandchild
	 * subgraph will be included.
	 * <p>
	 * @param root is the root graph whose factors will be visited. Only nodes contained in this
	 * graph or its subgraphs will be included.
	 * @param maxNestingDepth specifies how deep in the subgraph tree to visit. If zero is specified, only
	 * nodes contained directly in the root will be included.
	 * @since 0.08
	 */
	public static IFactorGraphChildIterator<FactorBase> factorsAndLeafSubgraphsDownto(FactorGraph root, int maxNestingDepth)
	{
		return new NestedFactorBaseIterator(root, maxNestingDepth);
	}
	
	/**
	 * Returns iterator over {@link Factor}s directly owned by a graph.
	 * @since 0.08
	 */
	public static Iterator<Factor> ownedFactors(FactorGraph graph)
	{
		return graph.ownedFactorIterator();
	}
	
	/**
	 * Returns iterator over subgraphs directly owned by a graph.
	 * @since 0.08
	 */
	public static Iterator<FactorGraph> ownedSubgraphs(FactorGraph graph)
	{
		return graph.ownedGraphIterator();
	}
	

	/**
	 * Returns iterator over {@link Variable}s directly owned by a graph.
	 * @since 0.08
	 */
	public static Iterator<Variable> ownedVariables(FactorGraph graph)
	{
		return graph.ownedVariableIterator();
	}
	
	/**
	 * Returns iterator over all nested subgraphs of the root graph, including the root itself.
	 * <p>
	 * Subgraphs are visited in top-down order, i.e. those farther away from the root will be visited after
	 * those closer to the root. The root itself will be the first graph returned.
	 * <p>
	 * @param root is the root graph whose subgraphs will be visited. Only subgraphs contained in this
	 * graph or its subgraphs will be included.
	 * @since 0.08
	 */
	public static IFactorGraphChildIterator<FactorGraph> subgraphs(FactorGraph root)
	{
		return subgraphsDownto(root, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterator over all nested subgraphs of the root graph, including the root itself, down to specified depth.
	 * <p>
	 * Subgraphs are visited in top-down order, i.e. those farther away from the root will be visited after
	 * those closer to the root. The root itself will be the first graph returned.
	 * <p>
	 * @param root is the root graph whose subgraphs will be visited. Only subgraphs contained in this
	 * graph or its subgraphs will be included.
	 * @param maxNestingDepth specifies how deep in the subgraph heirarchy to visit. If zero is specified, only
	 * the root graph will be visited, if one is specified the root and its immediate subgraphs will be included.
	 * Note that this behaves differently from how the correspondingly named argument to the other functions
	 * in this class behaves!
	 * @since 0.08
	 */
	public static IFactorGraphChildIterator<FactorGraph> subgraphsDownto(FactorGraph root, int maxNestingDepth)
	{
		return new NestedFactorGraphIterator(root, maxNestingDepth);
	}
	
	/**
	 * Returns iterator over all {@link Variable} objects contained in this graph.
	 * <p>
	 * Variable are visited in top-down order, i.e. those in a subgraph are visited after those from their parent.
	 * <p>
	 * @param root is the root graph whose variables will be visited. Only variables contained in this
	 * graph or its subgraphs will be included. Note that boundary variables that are not owned by the root
	 * will not be included.
	 * @since 0.08
	 * @see #variablesAndBoundary(FactorGraph)
	 */
	public static IFactorGraphChildIterator<Variable> variables(FactorGraph root)
	{
		return variablesDownto(root, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterator over all {@link Variable} objects contained in this graph down to specified depth.
	 * <p>
	 * Variable are visited in top-down order, i.e. those in a subgraph are visited after those from their parent.
	 * <p>
	 * @param root is the root graph whose variables will be visited. Only variables contained in this
	 * graph or its subgraphs will be included. Note that boundary variables that are not owned by the root
	 * will not be included.
	 * @param maxNestingDepth specifies how deep in the subgraph tree to visit. If zero is specified, only
	 * factors contained directly in the root will be included.
	 * @since 0.08
	 * @see #variablesAndBoundaryDownto(FactorGraph, int)
	 */
	public static IFactorGraphChildIterator<Variable> variablesDownto(FactorGraph root, int maxNestingDepth)
	{
		return  new NestedVariableIterator(root, maxNestingDepth, false);
	}
	
	/**
	 * Returns iterator over all {@link Variable} objects contained in this graph including its boundary variables.
	 * <p>
	 * Variable are visited in top-down order, i.e. those in a subgraph are visited after those from their parent.
	 * <p>
	 * @param root is the root graph whose variables will be visited. Only variables contained in this
	 * graph or its subgraphs will be included. Boundary variables that are not owned by the root
	 * will be included.
	 * @since 0.08
	 * @see #variables(FactorGraph)
	 */
	public static IFactorGraphChildIterator<Variable> variablesAndBoundary(FactorGraph root)
	{
		return variablesAndBoundaryDownto(root, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterator over all {@link Variable} objects contained in this graph including its boundary variables
	 * down to specified depth.
	 * <p>
	 * Variable are visited in top-down order, i.e. those in a subgraph are visited after those from their parent.
	 * <p>
	 * @param root is the root graph whose variables will be visited. Only variables contained in this
	 * graph or its subgraphs will be included. Boundary variables that are not owned by the root
	 * will be included.
	 * @param maxNestingDepth specifies how deep in the subgraph tree to visit. If zero is specified, only
	 * factors contained directly in the root will be included.
	 * @since 0.08
	 * @see #variablesDownto(FactorGraph,int)
	 */
	public static IFactorGraphChildIterator<Variable> variablesAndBoundaryDownto(FactorGraph root, int maxNestingDepth)
	{
		return  new NestedVariableIterator(root, maxNestingDepth, true);
	}

	/*-----------------
	 * Implementations
	 */
	
	private static class NestedFactorGraphIterator
		extends UnmodifiableIterator<FactorGraph>
		implements IFactorGraphChildIterator<FactorGraph>
	{
		private final FactorGraph _root;
		private final List<Iterator<FactorGraph>> _iteratorStack;
		private final int _maxNestingDepth;
		private int _lastDepth;
	
		private NestedFactorGraphIterator(FactorGraph graph, int maxNestingDepth)
		{
			_root = graph;
			_iteratorStack = new ArrayList<>();
			_maxNestingDepth = Math.max(maxNestingDepth,0);
			reset();
		}
	
		@Override
		public boolean hasNext()
		{
			for (int i = _iteratorStack.size(); --i >= 0;)
			{
				if (_iteratorStack.get(i).hasNext())
				{
					return true;
				}
				_iteratorStack.remove(i);
			}

			return false;
		}

		@Override
		public @Nullable FactorGraph next()
		{
			for (int i = _iteratorStack.size(); --i >= 0;)
			{
				final FactorGraph graph = _iteratorStack.get(i).next();
				if (graph != null)
				{
					_lastDepth = i;
					if (i < _maxNestingDepth)
					{
						_iteratorStack.add(graph.ownedGraphIterator());
					}
					return graph;
				}
				_iteratorStack.remove(i);
			}

			_lastDepth = -1;
			return null;
		}

		@Override
		public int lastDepth()
		{
			return _lastDepth;
		}

		@Override
		public int maxNestingDepth()
		{
			return _maxNestingDepth;
		}
	
		@Override
		public void reset()
		{
			_iteratorStack.clear();
			_iteratorStack.add(new SingleIterator<>(_root));
			_lastDepth = -1;
		}
	
		@Override
		public FactorGraph root()
		{
			return _root;
		}
	}

	private abstract static class NestedChildIterator<T>
		extends UnmodifiableIterator<T>
		implements IFactorGraphChildIterator<T>
	{
		protected final NestedFactorGraphIterator _graphIterator;
		protected Iterator<? extends T> _childIterator;

		private NestedChildIterator(FactorGraph graph, int maxNestingDepth)
		{
			_graphIterator = new NestedFactorGraphIterator(graph, maxNestingDepth);
			_childIterator = ReleasableIterators.emptyIterator();
		}

		@Override
		public boolean hasNext()
		{
			while (!_childIterator.hasNext())
			{
				FactorGraph graph = _graphIterator.next();
				if (graph != null)
				{
					_childIterator = childIterator(graph, _graphIterator.lastDepth());
				}
				else
				{
					return false;
				}
			}
			
			return true;
		}

		@Override
		@Nullable
		public T next()
		{
			return hasNext() ? _childIterator.next() : null;
		}

		@Override
		public int lastDepth()
		{
			return _graphIterator.lastDepth();
		}
		
		@Override
		public final int maxNestingDepth()
		{
			return _graphIterator.maxNestingDepth();
		}
		
		@Override
		public void reset()
		{
			_graphIterator.reset();
			_childIterator = childIterator(requireNonNull(_graphIterator.next()), _graphIterator.lastDepth());
		}

		@Override
		public final FactorGraph root()
		{
			return _graphIterator.root();
		}
		
		protected abstract Iterator<? extends T> childIterator(FactorGraph graph, int depth);
	}
	
	private static class NestedFactorIterator extends NestedChildIterator<Factor>
	{
		private NestedFactorIterator(FactorGraph graph, int maxNestingDepth)
		{
			super(graph, maxNestingDepth);
		}

		@Override
		protected Iterator<Factor> childIterator(FactorGraph graph, int depth)
		{
			return graph.ownedFactorIterator();
		}
	}

	private static class NestedFactorBaseIterator extends NestedChildIterator<FactorBase>
	{
		private NestedFactorBaseIterator(FactorGraph graph, int maxNestingDepth)
		{
			super(graph, maxNestingDepth);
		}

		@Override
		protected Iterator<? extends FactorBase> childIterator(FactorGraph graph, int depth)
		{
			if (depth == maxNestingDepth())
			{
				return Iterators.concat(graph.ownedFactorIterator(), graph.ownedGraphIterator());
			}
			else
			{
				return graph.ownedFactorIterator();
			}
		}
	}
	
	private static class NestedVariableIterator extends NestedChildIterator<Variable>
	{
		/**
		 * Whether to include boundary variables at root that are not owned by the root.
		 */
		private final boolean _includeBoundary;

		private NestedVariableIterator(FactorGraph graph, int maxNestingDepth, boolean includeBoundary)
		{
			super(graph, maxNestingDepth);
			_includeBoundary = includeBoundary;
		}

		@Override
		protected Iterator<Variable> childIterator(FactorGraph graph, int depth)
		{
			Iterator<Variable> iterator = graph.ownedVariableIterator();
			if (depth == 0 && _includeBoundary)
			{
				iterator = Iterators.concat(graph.externalBoundaryVariableIterator(), iterator);
			}
			return iterator;
		}
	}
	
	private static class BoundaryVariableIterator extends NestedChildIterator<Variable>
	{
		private BoundaryVariableIterator(FactorGraph graph)
		{
			super(graph, 0);
		}
		
		@Override
		protected Iterator<Variable> childIterator(FactorGraph graph, int depth)
		{
			return graph.boundaryVariableIterator();
		}
	}
}
