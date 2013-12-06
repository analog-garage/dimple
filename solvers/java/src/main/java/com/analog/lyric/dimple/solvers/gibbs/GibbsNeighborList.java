package com.analog.lyric.dimple.solvers.gibbs;

import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
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
	
	/**
	 * Creates a neighbor list for scoring samples of {@code svar}.
	 * 
	 * @return null if the neighbors are the same as the node's immediate siblings.
	 */
	static GibbsNeighborList create(ISolverVariableGibbs svar)
	{
		final VariableBase var = svar.getModelObject();
		final int nSiblings = var.getSiblingCount();

		final Set<ISolverNodeGibbs> visited = new HashSet<ISolverNodeGibbs>(nSiblings);
		visited.add(svar);
		
		final List<ISolverNodeGibbs> neighbors = new ArrayList<ISolverNodeGibbs>(nSiblings);
		final Queue<ISolverNodeGibbs> queue = addVariableNeighbors(svar, visited, neighbors, null);

		final boolean createList = queue != null || neighbors.size() != nSiblings;
		
		if (queue != null)
		{
			for (ISolverNodeGibbs node = null; (node = queue.poll()) != null;)
			{
				if (node instanceof ISolverFactorGibbs)
				{
					addFactorNeighbors((ISolverFactorGibbs)node, visited, neighbors, queue);
				}
				else
				{
					addVariableNeighbors((ISolverVariableGibbs)node, visited, neighbors, queue);
				}
			}
		}
		
		return createList ? new GibbsNeighborList(neighbors.toArray(new ISolverNodeGibbs[neighbors.size()])) : null;
	}
	
	private static void addFactorNeighbors(
		ISolverFactorGibbs sfactor,
		Set<ISolverNodeGibbs> visited,
		List<ISolverNodeGibbs> neighbors,
		Queue<ISolverNodeGibbs> queue)
	{
		Factor factor = sfactor.getModelObject();
		for (int edge : factor.getDirectedTo())
		{
			VariableBase variable = factor.getSibling(edge);
			ISolverVariableGibbs svariable = (ISolverVariableGibbs)variable.getSolver();
			if (visited.add(svariable))
			{
				if (svariable.hasPotential())
				{
					// Only add to neighbors set if getPotential() can return something other than 0.
					neighbors.add(svariable);
				}
				queue.add(svariable);
			}
		}
	}
	
	private static Queue<ISolverNodeGibbs> addVariableNeighbors(
		ISolverVariableGibbs svariable,
		Set<ISolverNodeGibbs> visited,
		List<ISolverNodeGibbs> neighbors,
		Queue<ISolverNodeGibbs> queue)
	{
		final VariableBase variable = svariable.getModelObject();
		final int nSiblings = variable.getSiblingCount();
		
		for (int edge = 0; edge < nSiblings; ++edge)
		{
			Factor factor = variable.getSibling(edge);
			ISolverFactorGibbs sfactor = (ISolverFactorGibbs)factor.getSolver();
			if (visited.add(sfactor))
			{
				if (factor.getFactorFunction().isDeterministicDirected() &&
					!factor.isDirectedTo(variable.getSiblingPortIndex(edge)))
				{
					if (queue == null)
					{
						queue = new ArrayDeque<ISolverNodeGibbs>();
					}
					queue.add(sfactor);
				}
				else
				{
					neighbors.add(sfactor);
				}
			}
		}
		
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
