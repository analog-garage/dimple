package com.analog.lyric.dimple.solvers.gibbs;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.ReleasableIterable;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;

/**
 * Represents the neighbors of a {@link ISolverVariableGibbs} that need to be included in the variable's
 * sample score. This class is only created when the contents differ from the immediate siblings of
 * the variable. This can happen in two ways:
 * <ul>
 * <li>The same factor shows up more than once in the sibling list (and should not be double counted).
 * <li>The variable is an input to one or more deterministic directed factors whose outputs should be
 * included in the score (and in turn their deterministic dependents recursively).
 * </ul>
 */
@Immutable
class GibbsNeighbors implements ReleasableIterable<ISolverNodeGibbs>
{
	/*-------
	 * State
	 */
	
	private final ISolverNodeGibbs[] _neighbors;
	
	/*--------------
	 * Construction
	 */
	
	private GibbsNeighbors(ISolverNodeGibbs[] neighbors)
	{
		_neighbors = neighbors;
	}
	
	/**
	 * Creates a neighbor list for scoring samples of {@code svar}.
	 * 
	 * @return null if the neighbors are the same as the node's immediate siblings.
	 */
	static GibbsNeighbors create(ISolverVariableGibbs svar)
	{
		final VariableBase var = svar.getModelObject();
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
		
		if (queue != null)
		{
			for (Work work = null; (work = queue.poll()) != null;)
			{
				work.handle(visited, counter, queue);
			}
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
			
			return new GibbsNeighbors(neighbors);
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
		
		protected abstract Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counter, Queue<Work> queue);
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
		protected Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counterHolder, Queue<Work> queue)
		{
			final VariableBase variable = _varNode.getModelObject();
			final int nSiblings = variable.getSiblingCount();
			
			int counter = counterHolder[0];
			for (int edge = 0; edge < nSiblings; ++edge)
			{
				if (edge == _incomingEdge)
					continue;
				
				final Factor factor = variable.getSibling(edge);
				final ISolverFactorGibbs sfactor = (ISolverFactorGibbs)factor.getSolver();
				
				int reverseEdge;
				if (factor.getFactorFunction().isDeterministicDirected() &&
					!factor.isDirectedTo(reverseEdge = variable.getSiblingPortIndex(edge)))
				{
					// Do not mark deterministic directed factors as visited because we may
					// need to visit them again from a different input variable and may get
					// different outputs
					//
					// TODO: This can only happen with some types of factor functions,
					// such as MatrixProduct, that direct many inputs to many outputs, so it may
					// be worthwhile to be able to identify if this is one of those cases to avoid
					// some extra work.
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
		protected Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counterHolder, Queue<Work> queue)
		{
			final Factor factor = _factorNode.getModelObject();
			final FactorFunction function = factor.getFactorFunction();
			int[] outputEdges = function.getDirectedToIndicesForInput(factor, _incomingEdge);
			if (outputEdges == null)
			{
				// TODO: in this case, all of the outputs will be visited the first time, so
				// don't revisit this node if we come to it again from a different input.
				outputEdges = function.getDirectedToIndices(factor.getSiblingCount());
			}
			
			int counter = counterHolder[0];
			for (int edge : outputEdges)
			{
				VariableBase variable = factor.getSibling(edge);
				ISolverVariableGibbs svariable = (ISolverVariableGibbs)variable.getSolver();
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
			counterHolder[0] = counter;
			return queue;
		}
	}
	
	/*------------------------------
	 * [Releasable]Iterable methods
	 */
	
	@Override
	public ReleasableIterator<ISolverNodeGibbs> iterator()
	{
		return ArrayIterator.create(_neighbors);
	}
	
	/**
	 * Returns an iterator that visits the contents of {@code list} if not null, and which otherwise iterates
	 * over the solver siblings of {@code var}.
	 */
	static ReleasableIterator<ISolverNodeGibbs> iteratorFor(GibbsNeighbors list, ISolverVariableGibbs var)
	{
		return list != null ? list.iterator() : SimpleIterator.create(var.getModelObject());
	}
	
	/*--------------------------
	 * Iterator implementations
	 */
	
	/**
	 * Iterator that visits immediate solver nodes of source model node.
	 */
	@NotThreadSafe
	private static class SimpleIterator implements ReleasableIterator<ISolverNodeGibbs>
	{
		private Node _modelNode;
		private int _size;
		private int _index;

		private static final AtomicReference<SimpleIterator> _reusableInstance = new AtomicReference<SimpleIterator>();
		
		static SimpleIterator create(Node node)
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
		public ISolverNodeGibbs next()
		{
			return (ISolverNodeGibbs)_modelNode.getSibling(_index++).getSolver();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void release()
		{
			_modelNode = null;
			_reusableInstance.lazySet(this);
		}
		
		void reset(Node node)
		{
			_modelNode = node;
			_size = node.getSiblingCount();
			_index = 0;
		}
	}
	
	/**
	 * Iterator that visits all of the solver nodes in an array.
	 */
	@NotThreadSafe
	private static class ArrayIterator implements ReleasableIterator<ISolverNodeGibbs>
	{
		private ISolverNodeGibbs[] _neighbors;
		private int _size;
		private int _index;
		
		private static final AtomicReference<ArrayIterator> _reusableInstance = new AtomicReference<ArrayIterator>();
		
		static ArrayIterator create(ISolverNodeGibbs[] neighbors)
		{
			ArrayIterator iter = _reusableInstance.getAndSet(null);
			if (iter == null)
			{
				iter = new ArrayIterator();
			}
			iter.reset(neighbors);
			return iter;
		}
		
		@Override
		public boolean hasNext()
		{
			return _index < _size;
		}

		@Override
		public ISolverNodeGibbs next()
		{
			return _neighbors[_index++];
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void release()
		{
			_neighbors = null;
			_reusableInstance.lazySet(this);
		}
		
		void reset(ISolverNodeGibbs[] neighbors)
		{
			_neighbors = neighbors;
			_size = neighbors.length;
			_index = 0;
		}
	}
	
}
