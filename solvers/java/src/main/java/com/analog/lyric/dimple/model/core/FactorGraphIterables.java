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

import java.util.AbstractCollection;
import java.util.Iterator;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;

/**
 * Contains static methods for constructing {@link Iterable}s over contents of {@link FactorGraph}s.
 * <p>
 * Note that the methods in this class typically return an unmodifiable subclass of {@link java.util.Collection}
 * for convenience.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see FactorGraphIterators
 */
public class FactorGraphIterables
{
	/**
	 * Returns iterable over the boundary variables for a graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#boundary(FactorGraph)}
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Variable> boundary(FactorGraph graph)
	{
		return new BoundaryVariables(graph);
	}
	
	/**
	 * Returns iterable over all {@link Constant} objects contained in this graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#constants(FactorGraph)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Constant> constants(FactorGraph graph)
	{
		return constantsDownto(graph, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterable over all {@link Constant} objects contained in this graph down to specified depth.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#constantsDownto(FactorGraph, int)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Constant> constantsDownto(FactorGraph graph, int maxNestingDepth)
	{
		return new NestedConstants(graph, maxNestingDepth);
	}
	
	/**
	 * Returns iterable over all {@link Factor} objects contained in this graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#factors(FactorGraph)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Factor> factors(FactorGraph graph)
	{
		return factorsDownto(graph, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterable over all {@link Factor} objects contained in this graph down to specified depth.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#factorsDownto(FactorGraph, int)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Factor> factorsDownto(FactorGraph graph, int maxNestingDepth)
	{
		return new NestedFactors(graph, maxNestingDepth);
	}
	
	/**
	 * Returns iterable over all {@link Factor} objects contained in this graph down to specified depth
	 * and any {@link FactorGraph} objects at specified depth.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#factorsAndLeafSubgraphsDownto(FactorGraph, int)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<FactorBase> factorsAndLeafSubgraphsDownto(FactorGraph graph, int maxNestingDepth)
	{
		return new NestedFactorsAndGraphs(graph, maxNestingDepth);
	}
	
	/**
	 * Returns iterable over all {@link FactorGraph} objects contained in this graph including this graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#subgraphs(FactorGraph)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<FactorGraph> subgraphs(FactorGraph graph)
	{
		return subgraphsDownto(graph, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterable over all {@link FactorGraph} objects contained in this graph including this graph
	 * down to specified depth.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#subgraphsDownto(FactorGraph, int)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<FactorGraph> subgraphsDownto(FactorGraph graph, int maxNestingDepth)
	{
		return new NestedFactorGraphs(graph, maxNestingDepth);
	}
	
	/**
	 * Returns iterable over all {@link Variable} objects contained in this graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#variables(FactorGraph)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Variable> variables(FactorGraph graph)
	{
		return variablesDownto(graph, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterable over all {@link Variable} objects contained in this graph down to specified depth.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#variablesDownto(FactorGraph, int)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Variable> variablesDownto(FactorGraph graph, int maxNestingDepth)
	{
		return  new NestedVariables(graph, maxNestingDepth, false);
	}
	
	/**
	 * Returns iterable over all {@link Variable} objects contained in this graph including boundary
	 * variables of this graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#variablesAndBoundary(FactorGraph)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Variable> variablesAndBoundary(FactorGraph graph)
	{
		return variablesAndBoundaryDownto(graph, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterable over all {@link Variable} objects contained in this graph down to specified depth and
	 * including boundary variables of this graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#variablesAndBoundaryDownto(FactorGraph, int)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<Variable> variablesAndBoundaryDownto(FactorGraph graph, int maxNestingDepth)
	{
		return new NestedVariables(graph, maxNestingDepth, true);
	}

	/**
	 * Returns iterable over all {@link VariableBlock} objects contained in this graph.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#variableBlocks(FactorGraph)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<VariableBlock> variableBlocks(FactorGraph graph)
	{
		return variableBlocksDownto(graph, Integer.MAX_VALUE);
	}

	/**
	 * Returns iterable over all {@link VariableBlock} objects contained in this graph down to specified depth.
	 * <p>
	 * Iterates using {@link FactorGraphIterators#variableBlocksDownto(FactorGraph, int)}.
	 * @since 0.08
	 */
	public static IFactorGraphChildren<VariableBlock> variableBlocksDownto(FactorGraph graph, int maxNestingDepth)
	{
		return new NestedVariableBlocks(graph, maxNestingDepth);
	}
	
	/*-----------------
	 * Implementations
	 */
	
	private static class NestedFactorGraphs
		extends AbstractCollection<FactorGraph>
		implements IFactorGraphChildren<FactorGraph>
	{
		/*-------
		 * State
		 */
		
		private final FactorGraph _root;
		private final int _maxNestingDepth;
		
		private NestedFactorGraphs(FactorGraph root, int maxNestingDepth)
		{
			_root = root;
			_maxNestingDepth = maxNestingDepth;
		}
		
		@Override
		public IFactorGraphChildIterator<FactorGraph> iterator()
		{
			return FactorGraphIterators.subgraphsDownto(_root, _maxNestingDepth);
		}

		@Override
		public int size()
		{
			switch (_maxNestingDepth)
			{
			case 0:
				return 1;
			case 1:
				return 1 + _root.ownedGraphCount();
			}

			final Iterator<FactorGraph> iter = iterator();
			int count = 0;
			while (iter.hasNext())
			{
				iter.next();
				++count;
			}
			
			return count;
		}

		@Override
		public FactorGraph root()
		{
			return _root;
		}
		
		@Override
		public int maxNestingDepth()
		{
			return _maxNestingDepth;
		}
	}

	private static abstract class NestedFactorGraphChildren<T>
		extends AbstractCollection<T>
		implements IFactorGraphChildren<T>
	{
		protected final FactorGraph _root;
		protected final int _maxNestingDepth;
		
		private NestedFactorGraphChildren(FactorGraph graph, int maxNestingDepth)
		{
			_root = graph;
			_maxNestingDepth = maxNestingDepth;
		}
		
		@Override
		public abstract IFactorGraphChildIterator<T> iterator();

		@Override
		public int size()
		{
			if (_maxNestingDepth == 0 || _root.ownedGraphCount() == 0)
			{
				return childCount(_root, 0);
			}
			
			final IFactorGraphChildIterator<FactorGraph> graphIter =
				FactorGraphIterators.subgraphsDownto(_root, _maxNestingDepth);
			int count = 0;
			while (true)
			{
				FactorGraph graph = graphIter.next();
				if (graph != null)
				{
					count += childCount(graph, graphIter.lastDepth());
				}
				else
				{
					return count;
				}
			}
		}
		
		@Override
		public int maxNestingDepth()
		{
			return _maxNestingDepth;
		}
		
		@Override
		public FactorGraph root()
		{
			return _root;
		}

		protected abstract int childCount(FactorGraph graph, int depth);
	}

	private static class NestedConstants extends NestedFactorGraphChildren<Constant>
	{
		private NestedConstants(FactorGraph graph, int maxNestingDepth)
		{
			super(graph, maxNestingDepth);
		}

		@Override
		protected int childCount(FactorGraph graph, int depth)
		{
			return graph.ownedConstantCount();
		}

		@Override
		public IFactorGraphChildIterator<Constant> iterator()
		{
			return FactorGraphIterators.constantsDownto(_root, _maxNestingDepth);
		}
	}

	private static class NestedFactors extends NestedFactorGraphChildren<Factor>
	{
		private NestedFactors(FactorGraph graph, int maxNestingDepth)
		{
			super(graph, maxNestingDepth);
		}

		@Override
		protected int childCount(FactorGraph graph, int depth)
		{
			return graph.ownedFactorCount();
		}

		@Override
		public IFactorGraphChildIterator<Factor> iterator()
		{
			return FactorGraphIterators.factorsDownto(_root, _maxNestingDepth);
		}
	}

	private static class NestedFactorsAndGraphs extends NestedFactorGraphChildren<FactorBase>
	{
		/**
		 * @param graph
		 * @param maxNestingDepth
		 * @since 0.08
		 */
		private NestedFactorsAndGraphs(FactorGraph graph, int maxNestingDepth)
		{
			super(graph, maxNestingDepth);
		}

		@Override
		protected int childCount(FactorGraph graph, int depth)
		{
			int count = graph.ownedFactorCount();
			if (depth == _maxNestingDepth)
			{
				count += graph.ownedGraphCount();
			}
			return count;
		}

		@Override
		public IFactorGraphChildIterator<FactorBase> iterator()
		{
			return FactorGraphIterators.factorsAndLeafSubgraphsDownto(_root, _maxNestingDepth);
		}
	}

	private static class NestedVariables extends NestedFactorGraphChildren<Variable>
	{
		private final boolean _includeBoundary;
		
		private NestedVariables(FactorGraph graph, int maxNestingDepth, boolean includeBoundary)
		{
			super(graph, maxNestingDepth);
			_includeBoundary = includeBoundary;
		}

		@Override
		public IFactorGraphChildIterator<Variable> iterator()
		{
			if (_includeBoundary)
			{
				return FactorGraphIterators.variablesAndBoundaryDownto(_root, _maxNestingDepth);
			}
			else
			{
				return FactorGraphIterators.variablesDownto(_root, _maxNestingDepth);
			}
		}
		
		@Override
		protected int childCount(FactorGraph graph, int depth)
		{
			int count = graph.ownedVariableCount();
			if (depth == 0 && _includeBoundary)
			{
				Iterator<?> iter = graph.externalBoundaryVariableIterator();
				while (iter.hasNext())
				{
					iter.next();
					++count;
				}
			}
			return count;
		}
	}
	
	private static class BoundaryVariables extends NestedFactorGraphChildren<Variable>
	{
		private BoundaryVariables(FactorGraph graph)
		{
			super(graph, 0);
		}

		@Override
		public IFactorGraphChildIterator<Variable> iterator()
		{
			return FactorGraphIterators.boundary(_root);
		}

		@Override
		protected int childCount(FactorGraph graph, int depth)
		{
			return _root.getBoundaryVariableCount();
		}
	}
	
	private static class NestedVariableBlocks extends NestedFactorGraphChildren<VariableBlock>
	{
		private NestedVariableBlocks(FactorGraph graph, int maxNestingDepth)
		{
			super(graph, maxNestingDepth);
		}

		@Override
		protected int childCount(FactorGraph graph, int depth)
		{
			return graph.ownedVariableBlockCount();
		}

		@Override
		public IFactorGraphChildIterator<VariableBlock> iterator()
		{
			return FactorGraphIterators.variableBlocksDownto(_root, _maxNestingDepth);
		}
	}


}
