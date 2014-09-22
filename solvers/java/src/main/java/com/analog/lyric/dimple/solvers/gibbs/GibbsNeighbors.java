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

package com.analog.lyric.dimple.solvers.gibbs;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ReleasableArrayIterator;
import com.analog.lyric.collect.ReleasableIterable;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.collect.UnmodifiableReleasableIterator;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * Represents the neighbors of a {@link ISolverVariableGibbs} that need to be included in the variable's
 * sample score. This class is only created when the contents differ from the immediate siblings of
 * the variable. This can happen in two ways:
 * <ul>
 * <li>The same factor shows up more than once in the sibling list (and should not be double counted).
 * <li>The variable is an input to one or more deterministic directed factors whose outputs should be
 * included in the score (and in turn their deterministic dependents recursively). In this case, the
 * adjacent deterministic factors are also stored in this instance.
 * </ul>
 */
@Immutable
public final class GibbsNeighbors implements ReleasableIterable<ISolverNodeGibbs>
{
	/*-------
	 * State
	 */
	
	private final ISolverNodeGibbs[] _neighbors;
	private final GibbsSolverGraph _rootSolverGraph;
	
	/**
	 * Contains list of directed deterministic factors that are directed from the
	 * starting variable. null if none.
	 */
	private final @Nullable FactorWork[] _adjacentDependentFactors;
	
	/*--------------
	 * Construction
	 */
	
	private GibbsNeighbors(ISolverNodeGibbs[] neighbors, @Nullable FactorWork[] immediateDependentFactors, GibbsSolverGraph rootSolverGraph)
	{
		_neighbors = neighbors;
		_adjacentDependentFactors = immediateDependentFactors;
		_rootSolverGraph = rootSolverGraph;
	}
	
	/**
	 * Creates a neighbor list for scoring samples of {@code svar}.
	 * 
	 * @return null if the neighbors are the same as the node's immediate siblings.
	 */
	public static @Nullable GibbsNeighbors create(ISolverVariableGibbs svar)
	{
		final Variable var = requireNonNull(svar.getModelObject());
		final int nSiblings = var.getSiblingCount();

		// Neighbors at front of list, other visited nodes at end. The counter indicates
		// where the boundary is.
		final Deque<ISolverNodeGibbs> visited = new LinkedList<ISolverNodeGibbs>();
		visited.addLast(svar);
		svar.setVisited(true);
		
		// Counter of neighbors.
		int[] counter = new int[1];
		
		// Nodes yet to visit.
		// TODO: can we combine this with the visited list?
		final Queue<Work> queue = new VarWork(svar, -1).handle(visited,  counter, null);

		final boolean createList = queue != null || counter[0] != nSiblings;
		
		FactorWork[] adjacentDependentFactors = null;
		
		if (queue != null)
		{
			ArrayList<FactorWork> adjacentFactors = new ArrayList<FactorWork>(nSiblings);
			boolean processingAdjacentFactors = true;
			
			for (Work work = null; (work = queue.poll()) != null;)
			{
				if (processingAdjacentFactors)
				{
					// The FactorWork objects at the head of the queue up to the first VarWork
					// must be for adjacent factors.
					FactorWork factorWork = work.asFactorWork();
					if (factorWork == null)
					{
						processingAdjacentFactors = false;
					}
					else
					{
						adjacentFactors.add(factorWork);
					}
				}
				work.handle(visited, counter, queue);
			}
			
			adjacentDependentFactors = adjacentFactors.toArray(new FactorWork[adjacentFactors.size()]);
		}
		
		if (createList)
		{
			final int size = counter[0];
			ISolverNodeGibbs[] neighbors = new ISolverNodeGibbs[size];

			int i = 0;
			for (ISolverNodeGibbs node : visited)
			{
				node.setVisited(false);
				if (i < size)
				{
					neighbors[i++] = node;
				}
			}
			
			return new GibbsNeighbors(neighbors, adjacentDependentFactors, (GibbsSolverGraph)requireNonNull(svar.getRootGraph()));
		}
		else
		{
			for (ISolverNodeGibbs node : visited)
			{
				node.setVisited(false);
			}
			
			return null;
		}
	}
	
	private abstract static class Work
	{
		final int _incomingEdge;
		
		private Work(int incomingEdge)
		{
			_incomingEdge = incomingEdge;
		}
		
		protected @Nullable FactorWork asFactorWork() { return null; }
		protected abstract @Nullable Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counter, Queue<Work> queue);
	}
	
	private static final class VarWork extends Work
	{
		private final ISolverVariableGibbs _varNode;
	
		private VarWork(ISolverVariableGibbs varNode, int incomingEdge)
		{
			super(incomingEdge);
			_varNode = varNode;
		}
		
		@Override
		protected @Nullable Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counterHolder,
			@Nullable Queue<Work> queue)
		{
			final Variable variable = requireNonNull(_varNode.getModelObject());
			final int nSiblings = variable.getSiblingCount();
			
			int counter = counterHolder[0];
			for (int edge = 0; edge < nSiblings; ++edge)
			{
				if (edge == _incomingEdge)
					continue;
				
				final Factor factor = variable.getSibling(edge);
				final ISolverFactorGibbs sfactor = (ISolverFactorGibbs)Objects.requireNonNull(factor.getSolver());
				
				int reverseEdge;
				if (factor.getFactorFunction().isDeterministicDirected() &&
					!factor.isDirectedTo(reverseEdge = variable.getSiblingPortIndex(edge)))
				{
					// Do not mark deterministic directed factors as visited because we may
					// need to visit them again from a different input variable and may get
					// different outputs. See FactorWork.handle()
					if (queue == null)
					{
						queue = new LinkedList<Work>();
					}
					queue.add(new FactorWork(sfactor, reverseEdge));
				}
				else if (sfactor.setVisited(true))
				{
					visited.addFirst(sfactor);
					counter++;
				}
			}
			
			counterHolder[0] = counter;
			
			return queue;
		}
	}
	
	private static final class FactorWork extends Work
	{
		private final ISolverFactorGibbs _factorNode;
		
		private FactorWork(ISolverFactorGibbs factorNode, int incomingEdge)
		{
			super(incomingEdge);
			_factorNode = factorNode;
		}
		
		@Override
		protected FactorWork asFactorWork()
		{
			return this;
		}
		
		@Override
		protected Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counterHolder, Queue<Work> queue)
		{
			final Factor factor = requireNonNull(_factorNode.getModelObject());
			final FactorFunction function = factor.getFactorFunction();
			int[] outputEdges = function.getDirectedToIndicesForInput(factor, _incomingEdge);
			if (outputEdges == null)
			{
				// In this case, all of the outputs will be visited the first time, so
				// don't revisit this node if we come to it again from a different input.
				if (_factorNode.setVisited(true))
				{
					visited.addLast(_factorNode);
				}
				else
				{
					return queue;
				}
				
				outputEdges = function.getDirectedToIndices(factor.getSiblingCount());
			}
			
			int counter = counterHolder[0];
			if (outputEdges != null)
			{
				for (int edge : outputEdges)
				{
					Variable variable = factor.getSibling(edge);
					ISolverVariableGibbs svariable = requireNonNull((ISolverVariableGibbs)variable.getSolver());
					if (svariable.setVisited(true))
					{
						if (svariable.hasPotential())
						{
							visited.addFirst(svariable);
							counter++;
						}
						else
						{
							visited.addLast(svariable);
						}
						queue.add(new VarWork(svariable, factor.getSiblingPortIndex(edge)));
					}
			}
			}
			counterHolder[0] = counter;
			return queue;
		}
	}
	
	/*------------------------------
	 * [Releasable]Iterable methods
	 */
	
	@Override
	public @NonNull ReleasableIterator<ISolverNodeGibbs> iterator()
	{
		return ReleasableArrayIterator.create(_neighbors);
	}
	
	/**
	 * Returns an iterator that visits the contents of {@code list} if not null, and which otherwise iterates
	 * over the solver siblings of {@code var}.
	 */
	static ReleasableIterator<ISolverNodeGibbs> iteratorFor(@Nullable GibbsNeighbors list, ISolverVariableGibbs var)
	{
		return list != null ? list.iterator() : SimpleIterator.create(var.getModelObject());
	}
	
	/*---------------
	 * Local methods
	 */
	
	boolean hasDeterministicDependents()
	{
		return _adjacentDependentFactors != null;
	}
	
	/**
	 * Update the deterministic outputs that depend on the original variable.
	 * 
	 * @param oldValue is the previous value of the variable. The new sample value should
	 * already have been set before this is invoked.
	 */
	void update(Value oldValue)
	{
		final FactorWork[] adjacentDependentFactors = _adjacentDependentFactors;
		if (adjacentDependentFactors != null)
		{
			_rootSolverGraph.deferDeterministicUpdates();
			ReleasableIterator<FactorWork> dependentFactors = ReleasableArrayIterator.create(adjacentDependentFactors);
			while (dependentFactors.hasNext())
			{
				FactorWork factor = dependentFactors.next();
				factor._factorNode.updateNeighborVariableValue(factor._incomingEdge, oldValue);
			}
			dependentFactors.release();
			_rootSolverGraph.processDeferredDeterministicUpdates();
		}
	}
	
	/*--------------------------
	 * Iterator implementations
	 */
	
	/**
	 * Iterator that visits immediate solver nodes of source model node.
	 */
	@NotThreadSafe
	private static class SimpleIterator extends UnmodifiableReleasableIterator<ISolverNodeGibbs>
	{
		private @Nullable Node _modelNode;
		private int _size;
		private int _index;

		private static final AtomicReference<SimpleIterator> _reusableInstance = new AtomicReference<SimpleIterator>();
		
		static SimpleIterator create(@Nullable Node node)
		{
			SimpleIterator iter = _reusableInstance.getAndSet(null);
			if (iter == null)
			{
				iter = new SimpleIterator();
			}
			iter.reset(node);
			return iter;
		}
		
		@Override
		public final boolean hasNext()
		{
			return _index < _size;
		}

		@Override
		public @Nullable ISolverNodeGibbs next()
		{
			return (ISolverNodeGibbs)Objects.requireNonNull(_modelNode).getSibling(_index++).getSolver();
		}

		@Override
		public void release()
		{
			_modelNode = null;
			_reusableInstance.lazySet(this);
		}
		
		void reset(@Nullable Node node)
		{
			_modelNode = node;
			_size = node != null ? node.getSiblingCount() : 0;
			_index = 0;
		}
	}
	
}
