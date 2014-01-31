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

package com.analog.lyric.dimple.model.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.analog.lyric.collect.BinaryHeap;
import com.analog.lyric.collect.IHeap;
import com.analog.lyric.collect.IHeap.IEntry;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.model.variables.VariableList;

/**
 * Computes a variable elimination order for a factor graph using a greedy
 * algorithm over various cost functions.
 * <p>
 * A variable elimination order is an ordering of the variables in a graph
 * that is intended to minimize the cost of various exact inference algorithms
 * that are based on it. While the exact inference cost will depend on the
 * algorithm, in general, orderings that minimize the cost of the classic
 * variable elimination algorithm are also good for other algorithms as
 * well, including the construction of junction trees, finding loop cuts
 * for loop cut conditioning, and for constructing graph partition trees
 * for recursive conditioning schemes.
 * <p>
 * The problem of finding an optimal ordering is NP-hard, but the heuristic
 * greed approach implemented by this class has been shown to produce good
 * results with reasonable time complexity. The algorithm implemented here
 * is as follows:
 * <p>
 * <ol>
 * <li>Build a variable to variable adjacency list representation for
 * the variables in the factor graph. Note that this representation will
 * be modified during the execution of the algorithm so the actual model
 * representation cannot be used directly.
 *
 * <li>Pick a cost function that will be used to order variables in the
 * graph. The specific cost functions will be described below.
 * 
 * <li>Build a priority queue (heap) containing all of the variables ordered according
 * to cost, with the minimum cost at the front.
 * 
 * <li>While the priority queue is not empty:
 *    <ol>
 *    <li>Remove the variable with the lowest cost from the queue.
 * 
 *    <li>Add the variable to the end of the variable elimination order
 * 
 *    <li>Connect all of the variable's neighbors with each other by adding
 *    edges as necessary.
 * 
 *    <li>Remove the variable from the graph by removing it from its neighbors
 *    adjacency sets.
 * 
 *    <li>Update the priority queue as appropriate to reflect the changes to
 *    the value of the cost function resulting from the changes to the graph.
 *    </ol>
 * </ol>
 * <p>
 * There are four standard cost functions that are described in the literature
 * and supported by this implementation:
 * <ul>
 * <li>{@link VariableCost#MIN_NEIGHBORS}
 * <li>{@link VariableCost#WEIGHTED_MIN_NEIGHBORS}
 * <li>{@link VariableCost#MIN_FILL}
 * <li>{@link VariableCost#WEIGHTED_MIN_FILL}
 * </ul>
 * <p>
 * NOTE: this implementation currently does not handle models that contain non-Discrete variables
 * and it does not currently take into account fixed values (conditioning) or the contents of the
 * factors.
 *
 * @author Christopher Barber
 * @since 0.05
 */
public class VariableEliminator
{
	/**
	 * Describes the variable cost functions supported by {@link VariableEliminator}.
	 * See members for details.
	 */
	public static enum VariableCost
	{
		/**
		 * Cost is the number of neighboring variables that have not yet
		 * been eliminated.
		 */
		MIN_NEIGHBORS(new MinNeighbors()),
		/**
		 * Cost is the product of the domain cardinalities of the neighboring variables
		 * that have not yet been eliminated.
		 */
		WEIGHTED_MIN_NEIGHBORS(new MinWeight()),
		/**
		 * Cost is the number of edges that would be introduced between neighboring variables
		 * if this variable were to be eliminated at this step.
		 */
		MIN_FILL(new MinFill()),
		/**
		 * Cost is the sum of the weights of the edges that would be introduced between neighboring variables
		 * if this variable were to be eliminated at this step, where the edge weights are the products of
		 * the domain cardinalities of the variables connected by the edge.
		 */
		WEIGHTED_MIN_FILL(new WeightedMinFill());
		
		private final CostFunction _costFunction;
		
		private VariableCost(CostFunction costFunction)
		{
			_costFunction = costFunction;
		}
	}
	
	/*-------
	 * State
	 */
	
	private final FactorGraph _model;
	private final Random _rand;
	
	/**
	 * The number of variables in the model. Used to preallocate capacity for data structures.
	 */
	private int _nVariables;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Initialize for given model.
	 * <p>
	 * Invokes {@link #VariableEliminator(FactorGraph, Random)} with new {@link Random} instance.
	 */
	public VariableEliminator(FactorGraph model)
	{
		this(model, new Random());
	}
	
	/**
	 * Initialize for given model and using given random number
	 * generator.
	 * 
	 * @param rand is the random number generator used to randomly
	 * break ties for variables with the same cost. If null, then
	 * ties will be broken deterministically by favoring the variable
	 * with the lower id ({@link VariableBase#getId()}), which is useful
	 * for testing.
	 */
	public VariableEliminator(FactorGraph model, Random rand)
	{
		_model = model;
		_rand = rand;
		_nVariables = model.getVariableCount();
	}
	
	/*---------
	 * Methods
	 */
	
	/**
	 * The model for which ordering can be computed.
	 */
	public FactorGraph getModel()
	{
		return _model;
	}
	
	/**
	 * The randomizer used to break ties between variables with the same cost.
	 * When null, ties are broken deterministically.
	 * 
	 * @see #VariableEliminator(FactorGraph, Random)
	 */
	public Random getRandomizer()
	{
		return _rand;
	}
	
	/**
	 * Returns an iterator to produce the variable ordering for the given cost function.
	 * This may be invoked multiple times with different cost functions. When {@link #getRandomizer()}
	 * is non-null, then running with the same cost function can produce different orderings.
	 */
	public OrderIterator orderIterator(VariableCost cost)
	{
		return new OrderIterator(buildAdjacencyList(), cost);
	}
	
	/*----------------
	 * OrderIterator
	 */

	/**
	 * Produces a variable elimination order based on a given variable cost function.
	 * <p>
	 * Once iterator has terminated (i.e. {@link #hasNext()} is false), you can use
	 * {@link #getStats()} to access statistics that can be used to measure the goodness
	 * of the resulting ordering.
	 * <p>
	 * @see VariableEliminator#orderIterator(VariableCost)
	 */
	public static class OrderIterator implements Iterator<VariableBase>
	{
		private final VariableCost _cost;
		private final CostFunction _costFunction;
		private final IHeap<Var> _heap;
		private final Stats _stats = new Stats();
		
		/*--------------
		 * Construction
		 */
		
		private OrderIterator(List<Var> adjacencyList, VariableCost cost)
		{
			_cost = cost;
			_costFunction = cost._costFunction;
		
			final int size = adjacencyList.size();
		
			final IHeap<Var> heap = _heap = new BinaryHeap<Var>(size);
			for (Var var : adjacencyList)
			{
				var._heapEntry = heap.offer(var, var.adjustedCost(_costFunction));
			}
		}

		/*------------------
		 * Iterator methods
		 */
		
		@Override
		public boolean hasNext()
		{
			return !_heap.isEmpty();
		}

		@Override
		public VariableBase next()
		{
			final CostFunction costFunction = _costFunction;
			final IHeap<Var> heap = _heap;
			
			Var var = heap.poll();
			if (var == null)
			{
				return null;
			}

			// Remove variable from graph
			long cliqueCardinality = var.cardinality();
			for (VarLink link = var._neighborList._next; link._var != null; link = link._next)
			{
				final Var neighbor = link._var;
				neighbor.removeNeighbor(var);
				cliqueCardinality *= neighbor.cardinality();
			}
			
			_stats.addClique(1 + var.nNeighbors(), cliqueCardinality);

			// Add edges between remaining neighbors
			for (VarLink link1 = var._neighborList._next; link1._var != null; link1 = link1._next)
			{
				final Var neighbor1 = link1._var;
				for (VarLink link2 = link1._next; link2._var != null; link2 = link2._next)
				{
					final Var neighbor2 = link2._var;
					if (neighbor1.addNeighbor(neighbor2))
					{
						neighbor2.addNeighbor(neighbor1);
						// Update added edge statistics
						_stats.addEdgeWeight(neighbor1.cardinality() * neighbor2.cardinality());
					}
				}
			}

			// Update priorities
			if (costFunction.neighborsOnly())
			{
				heap.deferOrderingForBulkChange(var.nNeighbors());
				for (VarLink link = var._neighborList._next; link._var != null; link = link._next)
				{
					final Var neighbor = link._var;
					heap.changePriority(neighbor._heapEntry, neighbor.adjustedCost(costFunction));
				}
			}
			else
			{
				Set<Var> changeSet = new HashSet<Var>();
				for (VarLink link1 = var._neighborList._next; link1._var != null; link1 = link1._next)
				{
					final Var neighbor = link1._var;
					changeSet.add(neighbor);
					for (VarLink link2 = neighbor._neighborList._next; link2._var != null; link2 = link2._next)
					{
						changeSet.add(link2._var);
					}
				}
				heap.deferOrderingForBulkChange(changeSet.size());
				for (Var change : changeSet)
				{
					heap.changePriority(change._heapEntry, change.adjustedCost(costFunction));
				}
			}

			return var._variable;
		}

		/**
		 * Not supported.
		 * @throws UnsupportedOperationException
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
		
		/*---------------
		 * Local methods
		 */
		
		/**
		 * Identifies cost evaluator used by this iterator.
		 */
		public VariableCost getCostEvaluator()
		{
			return _cost;
		}
		
		/**
		 * Incrementally updated statistics for the elimination order, which can be used to
		 * measure the relative goodness of the resulting ordering.
		 */
		public Stats getStats()
		{
			return _stats;
		}
		
	} // OrderIterator
	
	/*-------------------
	 * Elimination stats
	 */
	
	/**
	 * Elimination quality statistics computed for an ordering.
	 * <p>
	 * @see OrderIterator#getStats()
	 */
	public static class Stats
	{
		private int _addedEdges = 0;
		private long _addedEdgeWeight = 0;
		private int _maxClique = 0;
		private long _maxCliqueCardinality = 0;
		
		/**
		 * All values are initialized to zero.
		 */
		public Stats()
		{
		}
		
		/**
		 * The number of edges that were added during the execution of the algorithm.
		 */
		public int addedEdges()
		{
			return _addedEdges;
		}

		/**
		 * Sets value of {@link #addedEdges()} and returns this object.
		 */
		public Stats addedEdges(int edges)
		{
			_addedEdges = edges;
			return this;
		}
		
		/**
		 * The total weight of edges that were added during the execution of the algorithm where
		 * the weight is defined as the product of the cardinality of its variables.
		 */
		public long addedEdgeWeight()
		{
			return _addedEdgeWeight;
		}
		
		/**
		 * Sets value of {@link #addedEdgeWeight()} and returns this object.
		 */
		public Stats addedEdgeWeight(long weight)
		{
			_addedEdgeWeight = weight;
			return this;
		}
		
		/**
		 * Returns the size of the largest clique induced by the execution of the algorithm.
		 * The clique size is determined when a variable is eliminated and is equivalent to
		 * the number of non-eliminated neighbors of the variable plus one (for the variable itself).
		 */
		public int maxCliqueSize()
		{
			return _maxClique;
		}
		
		/**
		 * Sets value of {@link #maxCliqueSize()} and returns this object.
		 */
		public Stats maxCliqueSize(int size)
		{
			_maxClique = size;
			return this;
		}
		
		/**
		 * Returns the cardinality of the largest clique induced by the execution of the algorithm.
		 * Like {@link #maxCliqueSize()} but instead of the number of variables in the clique, this
		 * is based on the product of the cardinality of the variables in the clique.
		 */
		public long maxCliqueCardinality()
		{
			return _maxCliqueCardinality;
		}
		
		/**
		 * Sets value of {@link #maxCliqueCardinality()} and returns this object.
		 */
		public Stats maxCliqueCardinality(long cardinality)
		{
			_maxCliqueCardinality = cardinality;
			return this;
		}
		
		/*-----------------
		 * Private methods
		 */
		
		private void addEdgeWeight(long weight)
		{
			++_addedEdges;
			_addedEdgeWeight += weight;
		}
		
		private void addClique(int size, long cardinality)
		{
			_maxClique = Math.max(_maxClique, size);
			_maxCliqueCardinality = Math.max(_maxCliqueCardinality, cardinality);
		}
	}
	
	/*-----------------------
	 * Private inner classes
	 */
	
	private static class Var
	{
		final VariableBase _variable;
		final VarLink _neighborList = new VarLink(null);
		final Map<Var, VarLink> _neighborMap;
		
		/**
		 * Pointer to heap entry for this object for use in efficient reprioritization.
		 */
		IEntry<Var> _heapEntry = null;
		
		/**
		 * Can be set to a value in the range [0.0 and 1.0) to be used by
		 * Prioritizer to break to randomly order elements with the same
		 * integer priority.
		 */
		final double _incrementalCost;
		
		private Var(VariableBase variable, double incrementalCost)
		{
			_variable = variable;
			_incrementalCost = incrementalCost;
			_neighborMap = new HashMap<Var, VarLink>(variable.getSiblingCount());
		}
		
		@Override
		public String toString()
		{
			return _variable.getName();
		}
		
		private boolean addNeighbor(Var neighbor)
		{
			if (!_neighborMap.containsKey(neighbor))
			{
				VarLink link = new VarLink(neighbor);
				_neighborMap.put(neighbor, link);
				link.insertBefore(_neighborList);
				return true;
			}
			
			return false;
		}
		
		private double adjustedCost(CostFunction costFunction)
		{
			return costFunction.cost(this) + _incrementalCost;
		}
		
		private int cardinality()
		{
			return _variable.getDomain().asDiscrete().size();
		}
		
		private boolean isAdjacent(Var other)
		{
			return _neighborMap.containsKey(other);
		}
		
		private int nNeighbors()
		{
			return _neighborMap.size();
		}
		
		private void removeNeighbor(Var neighbor)
		{
			_neighborMap.remove(neighbor).remove();
		}
	}

	private static final class VarLink
	{
		private final Var _var;
		private VarLink _prev = this;
		private VarLink _next = this;
		
		VarLink(Var info)
		{
			_var = info;
		}
		
		void insertBefore(VarLink next)
		{
			_next = next;
			_prev = next._prev;
			next._prev = this;
			_prev._next = this;
		}
		
		void remove()
		{
			_prev._next = _next;
			_next._prev = _prev;
			_next = this;
			_prev = this;
		}
	}
	
	/*-------------------------------
	 * Cost function implementations
	 */
	
	private static abstract class CostFunction
	{
		final double cost(Var var)
		{
			if (var.nNeighbors() <= 1)
			{
				// It is always better to first eliminate variables connected by no more
				// than one edge because their removal will not expand the tree width.
				return 0.0;
			}
			
			return computeCost(var);
		}
		
		abstract double computeCost(Var var);
		
		/**
		 * True if evaluation only depends on immediate neighbors.
		 */
		abstract boolean neighborsOnly();
	}

	/**
	 * Cost is the number of neighbors of the variable in the current graph.
	 */
	private static class MinNeighbors extends CostFunction
	{
		@Override
		double computeCost(Var var)
		{
			return var.nNeighbors();
		}
		
		@Override
		boolean neighborsOnly()
		{
			return true;
		}
	}
	
	/**
	 * Cost is the product of the domain cardinalities of all of the neighboring
	 * variables in the current graph.
	 */
	private static class MinWeight extends CostFunction
	{
		@Override
		double computeCost(Var var)
		{
			double weight = 1.0;
			
			for (VarLink link = var._neighborList._next; link._var != null; link = link._next)
			{
				weight *= link._var.cardinality();
			}
			
			return weight;
		}

		@Override
		boolean neighborsOnly()
		{
			return true;
		}
	}
	
	/**
	 * Cost is the number of edges that would be added if this variable were to be eliminated
	 * from the current graph, i.e the number of unique neighbor variable pairs that are not
	 * already adjacent to each other.
	 */
	private static class MinFill extends CostFunction
	{
		@Override
		double computeCost(Var var)
		{
			double count = 0.0;
			
			for (VarLink link1 = var._neighborList._next; link1._var != null; link1 = link1._next)
			{
				final Var neighbor1 = link1._var;
				for (VarLink link2 = link1._next; link2._var != null; link2 = link2._next)
				{
					final Var neighbor2 = link2._var;
					if (!neighbor1.isAdjacent(neighbor2))
					{
						++count;
					}
				}
			}
			
			return count;
		}

		@Override
		boolean neighborsOnly()
		{
			return false;
		}
	}
	
	/**
	 * Similar to {@link MinFill} but instead of counting edges that would be added, it
	 * counts the sum of the weights of added edges where the weight is the product of
	 * the domain cardinalities at each end.
	 */
	private static class WeightedMinFill extends CostFunction
	{
		@Override
		double computeCost(Var var)
		{
			double weight = 0.0;
			
			for (VarLink link1 = var._neighborList._next; link1._var != null; link1 = link1._next)
			{
				final Var neighbor1 = link1._var;
				for (VarLink link2 = link1._next; link2._var != null; link2 = link2._next)
				{
					final Var neighbor2 = link2._var;
					if (!neighbor1.isAdjacent(neighbor2))
					{
						weight += neighbor1.cardinality() * neighbor2.cardinality();
					}
				}
			}
			
			return weight;
		}

		@Override
		boolean neighborsOnly()
		{
			return false;
		}
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private List<Var> buildAdjacencyList()
	{
		return buildAdjacencyList(new ArrayList<Var>(_nVariables));
	}
	
	private List<Var> buildAdjacencyList(List<Var> list)
	{
		final VariableList variables = _model.getVariables();
		final Map<VariableBase,Var> map = new HashMap<VariableBase,Var>(variables.size());
		
		for (VariableBase variable : variables)
		{
			if (!variable.getDomain().isDiscrete())
			{
				throw new DimpleException("VariableEliminator cannot handle non-discrete variable '%s'", variable);
			}
			Var info = new Var(variable, generateCostIncrement(variable));
			map.put(variable, info);
			list.add(info);
		}
		
		FactorList factors = _model.getFactors();
		
		for (FactorBase factor : factors)
		{
			int nVars = factor.getSiblingCount();
			Var[] vars = new Var[nVars];
			
			for (int i = 0; i < nVars; ++i)
			{
				VariableBase neighbor = factor.getSibling(i);
				vars[i] = map.get(neighbor);
			}
			
			for (int i = nVars; --i>=1;)
			{
				for (int j = i; --j>=0;)
				{
					Var vari = vars[i];
					Var varj = vars[j];
					vari.addNeighbor(varj);
					varj.addNeighbor(vari);
				}
			}
		}
		
		return list;
	}
	
	/**
	 * Generates a cost-increment in the range [0, 1) to break ties between
	 * variables with same integer cost.
	 */
	private double generateCostIncrement(VariableBase variable)
	{
		if (_rand == null)
		{
			return (double)variable.getId() / (double)Integer.MAX_VALUE;
		}
		else
		{
			return _rand.nextDouble();
		}
	}
	
}
