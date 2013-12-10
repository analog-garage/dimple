package com.analog.lyric.dimple.solvers.gibbs;

import java.util.AbstractList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.ReleasableIterableCollection;
import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;

/**
 * Represents the neighbors of a {@link ISolverVariableGibbs} that need to be included in the variable's
 * sample score. This class is only created when this list is different from the immediate siblings of
 * the variable. This can happen in two ways:
 * <ul>
 * <li>The same factor shows up more than once in the sibling list (and should not be double counted).
 * <li>The variable is an input to one or more deterministic directed factors whose outputs should be
 * inluded in the score.
 * </ul>
 */
@Immutable
class GibbsNeighborList extends AbstractList<ISolverNodeGibbs>
	implements ReleasableIterableCollection<ISolverNodeGibbs>
{
	/*-------
	 * State
	 */
	
	private final ISolverNodeGibbs[] _neighbors;
	
	/*--------------
	 * Construction
	 */
	
	private GibbsNeighborList(ISolverNodeGibbs[] neighbors)
	{
		_neighbors = neighbors;
	}
	
	private abstract static class Work
	{
		protected abstract Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counter, Queue<Work> queue);
	}
	
	private static class VarWork extends Work
	{
		private final ISolverVariableGibbs _varNode;
	
		private VarWork(ISolverVariableGibbs varNode)
		{
			_varNode = varNode;
		}
		
		@Override
		protected Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counter, Queue<Work> queue)
		{
			return addVariableNeighbors(_varNode, visited, counter, queue);
		}
	}
	
	private static class FactorWork extends Work
	{
		private final ISolverFactorGibbs _factorNode;
		private final int _inputEdge;
		
		private FactorWork(ISolverFactorGibbs factorNode, int inputEdge)
		{
			_factorNode = factorNode;
			_inputEdge = inputEdge;
		}
		
		@Override
		protected Queue<Work> handle(Deque<ISolverNodeGibbs> visited, int[] counter, Queue<Work> queue)
		{
			addFactorNeighbors(_factorNode, _inputEdge, visited, counter, queue);
			return queue;
		}
	}
	
	/**
	 * Creates a neighbor list for scoring samples of {@code svar}.
	 * 
	 * @return null if the neighbors are the same as the node's immediate siblings.
	 */
	static GibbsNeighborList create(ISolverVariableGibbs svar)
	{
		final VariableBase var = svar.getModelObject();
		final int nSiblings = var.getSiblingCount();

		// Neighbors at front of list, other visited nodes at end.
		final Deque<ISolverNodeGibbs> visited = new LinkedList<ISolverNodeGibbs>();
		visited.addLast(svar);
		svar.setVisited(true);
		
		// Counter of neighbors.
		int[] counter = new int[1];
		
		// Nodes yet to visit.
		// TODO: can we combine this with the visited list?
		final Queue<Work> queue = addVariableNeighbors(svar, visited, counter, null);

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
			
			return new GibbsNeighborList(neighbors);
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
	
	private static void addFactorNeighbors(
		ISolverFactorGibbs sfactor,
		int inputEdge,
		Deque<ISolverNodeGibbs> visited,
		int[] neighborCounter,
		Queue<Work> queue)
	{
		Factor factor = sfactor.getModelObject();
		int numEdges = factor.getSiblingCount();
		int counter = neighborCounter[0];
		for (int edge : factor.getFactorFunction().getDirectedToIndicesForInput(numEdges, inputEdge))
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
				queue.add(new VarWork(svariable));
			}
		}
		neighborCounter[0] = counter;
	}
	
	private static Queue<Work> addVariableNeighbors(
		ISolverVariableGibbs svariable,
		Deque<ISolverNodeGibbs> visited,
		int[] neighborCounter,
		Queue<Work> queue)
	{
		final VariableBase variable = svariable.getModelObject();
		final int nSiblings = variable.getSiblingCount();
		
		int counter = neighborCounter[0];
		for (int edge = 0; edge < nSiblings; ++edge)
		{
			Factor factor = variable.getSibling(edge);
			ISolverFactorGibbs sfactor = (ISolverFactorGibbs)factor.getSolver();
			if (sfactor.setVisited(true))
			{
				// FIXME: may need to visit directed factor more than once if different
				// inputs may affect different outputs.
				
				visited.add(sfactor);
				int reverseEdge;
				if (factor.getFactorFunction().isDeterministicDirected() &&
					!factor.isDirectedTo(reverseEdge = variable.getSiblingPortIndex(edge)))
				{
					visited.addLast(sfactor);
					if (queue == null)
					{
						queue = new LinkedList<Work>();
					}
					queue.add(new FactorWork(sfactor, reverseEdge));
				}
				else
				{
					visited.addFirst(sfactor);
					counter++;
				}
			}
		}
		
		neighborCounter[0] = counter;
		
		return queue;
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
	public static ReleasableIterator<ISolverNodeGibbs> iteratorFor(GibbsNeighborList list, ISolverVariableGibbs var)
	{
		return list != null ? list.iterator() : SimpleIterator.create(var.getModelObject());
	}
	
	/*--------------
	 * List methods
	 */
	
	@Override
	public ISolverNodeGibbs get(int i)
	{
		return _neighbors[i];
	}

	@Override
	public final int size()
	{
		return _neighbors.length;
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
