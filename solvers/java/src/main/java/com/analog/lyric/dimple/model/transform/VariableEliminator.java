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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.BinaryHeap;
import com.analog.lyric.collect.IHeap;
import com.analog.lyric.collect.IHeap.IEntry;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.FactorBase;
import com.analog.lyric.dimple.model.factors.FactorList;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.google.common.collect.Iterators;

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
 * unless they have fixed values and {@link #usesConditioning()} is true. It also
 * and it does not take into account the contents of the factor tables.
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
	 * If true, then variables with fixed values will be eliminated first and will be
	 * considered to be disjoint from the rest of the graph.
	 */
	private final boolean _useConditioning;
	
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
	 * Invokes {@link #VariableEliminator(FactorGraph, boolean, Random)} with new {@link Random} instance.
	 */
	public VariableEliminator(FactorGraph model, boolean useConditioning)
	{
		this(model, useConditioning, new Random());
	}
	
	/**
	 * Initialize for given model and using given random number
	 * generator.
	 * @param useConditioning sets value of {@link #usesConditioning()}
	 * @param rand is the random number generator used to randomly
	 * break ties for variables with the same cost. If null, then
	 * ties will be broken deterministically by favoring the variable
	 * with the lower id ({@link VariableBase#getId()}), which is useful
	 * for testing.
	 */
	public VariableEliminator(FactorGraph model, boolean useConditioning, Random rand)
	{
		_model = model;
		_rand = rand;
		_useConditioning = useConditioning;
		_nVariables = model.getVariableCount();
	}
	
	/*---------
	 * Methods
	 */

	/**
	 * Computes a variable elimination order by iteratively retrying using one or more cost functions
	 * and choosing the best fit according to the specified threshold statistics.
	 * <p>
	 * This function builds an eliminator for specified {@code mode} and {@code useConditioning} attribute.
	 * It then iteratively up to {@code nAttempts} times picks a cost function at random from {@code costFunctions}
	 * and uses it to build an {@link OrderIterator} from which it generates an ordering. After each iteration,
	 * the global statistics (from {@link OrderIterator#getStats()}) are compared against the best statistics
	 * so far using {@link Stats#compareTo(Stats, Stats)} to determine whether to keep the ordering. If the
	 * stats at any point satisfy the specified threshold values (as determined by {@link Stats#meetsThreshold(Stats)}
	 * then the function will return immediately.
	 * <p>
	 * For example, the following call will generate an order by conditioning out any fixed value variables,
	 * and will randomly try weighted min neighbors or weighted min fill cost functions up to ten iterations
	 * and returning the first one to achieve a max clique cardinality of no more than 42, otherwise returns
	 * the order with the best max clique cardinality:
	 * 
	 * <pre>{@code
	 *     Ordering order = generateStochastically(fg, true, 10,
	 *         new Stats().maxCliqueCardinality(42),
	 *         VariableCost.WEIGHTED_MIN_NEIGHBORS, VariableCost.WEIGHTED_MIN_FILL)
	 * }</pre>
	 * <p>
	 * @param model is the graph for which the eliminator order is being computed.
	 * @param useConditioning specifies whether to use conditioning (see {@link #usesConditioning()}
	 * @param nAttempts is the number of potential iteration orders to compute. If not a positive value,
	 * then each cost function will be tried once deterministically.
	 * @param threshold specifies which statistics should be used to evaluate the goodness of a given
	 * ordering (see {@link Stats#compareTo(Stats, Stats)}) and also threshold values for each
	 * statistic that will terminate the function before all {@code nAttempts} have been tried
	 * (see {@link Stats#meetsThreshold(Stats)}). Only statistics with non-negative threshold
	 * values will be considered.
	 * @param costFunctions is a list of cost functions to be used. If empty, all will be tried.
	 *
	 * @return the variable elimination order that best satisfied the {@code threshold} statistics.
	 */
	public static Ordering generate(
		FactorGraph model,
		boolean useConditioning,
		int nAttempts,
		Stats threshold,
		VariableCost ... costFunctions)
	{
		final boolean deterministic = nAttempts <= 0;
		final VariableEliminator eliminator =
			deterministic?
				new VariableEliminator(model, useConditioning, null) :
				new VariableEliminator(model, useConditioning);

		return generate(eliminator, nAttempts, threshold, costFunctions);
	}

	/**
	 * Computes a variable elimination order by iteratively retrying using one or more cost functions
	 * and choosing the best fit according to the specified threshold statistics.
	 * <p>
	 * Same as {@link #generate(FactorGraph, boolean, int, Stats, VariableCost...)} but uses provided
	 * eliminator instead of building a new one.
	 */
	public static Ordering generate(
		VariableEliminator eliminator,
		int nAttempts,
		Stats threshold,
		VariableCost ... costFunctions)
	{
		final boolean deterministic = nAttempts <= 0;

		if (costFunctions.length == 0)
		{
			costFunctions = VariableCost.values();
		}
		final int nFunctions = costFunctions.length;
		
		if (deterministic)
		{
			nAttempts = nFunctions;
		}
		
		ArrayList<VariableBase> curList = new ArrayList<VariableBase>(eliminator._nVariables);
		ArrayList<VariableBase> bestList = new ArrayList<VariableBase>(eliminator._nVariables);
		Stats bestStats = null;
		
		for (int attempt = 0; attempt < nAttempts; ++attempt)
		{
			// Pick a cost function
			VariableCost cost = costFunctions[0];
			if (nFunctions > 1)
			{
				cost = costFunctions[deterministic ? attempt : eliminator._rand.nextInt(nFunctions)];
			}
			
			// Run variable elimination
			OrderIterator iterator = eliminator.orderIterator(cost);
			Iterators.addAll(curList, iterator);
			
			// Compare stats
			Stats curStats = iterator.getStats();
			
			if (curStats.addedEdges() == 0)
			{
				bestStats = curStats;
				bestList = curList;
				break;
			}
			
			if (bestStats == null || curStats.compareTo(bestStats, threshold) < 0)
			{
				ArrayList<VariableBase> tmp = curList;
				curList = bestList;
				curList.clear();
				bestList = tmp;
				bestStats = curStats;
				
				if (bestStats.meetsThreshold(threshold))
				{
					break;
				}
			}
		}
		
		return new Ordering(bestList, bestStats);
	}
	
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
	 * @see #VariableEliminator(FactorGraph, boolean, Random)
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
		return new OrderIterator(this, cost);
	}
	
	/**
	 * True if eliminator takes into account variables that are conditioned with
	 * a fixed value. If true, then such variables will be eliminated first and
	 * will be treated as if they have no siblings. Value is set during construction.
	 */
	public boolean usesConditioning()
	{
		return _useConditioning;
	}
	
	/*----------
	 * Ordering
	 */
	
	@Immutable
	public static class Ordering
	{
		public final ArrayList<VariableBase> variables;
		public final Stats stats;
		
		private Ordering(ArrayList<VariableBase> variables, Stats stats)
		{
			this.variables = variables;
			this.stats = stats;
		}
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
		private final VariableEliminator _eliminator;
		private final CostFunction _costFunction;
		private final IHeap<Var> _heap;
		private final Stats _stats;
		
		/*--------------
		 * Construction
		 */
		
		private OrderIterator(VariableEliminator eliminator, VariableCost cost)
		{
			_eliminator = eliminator;
			_costFunction = cost._costFunction;
			_stats = new Stats(cost, 0);
		
			final List<Var> adjacencyList = eliminator.buildAdjacencyList(_stats);
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
			final boolean isConditioned =_eliminator.isConditioned(var._variable);
			if (isConditioned)
			{
				_stats.addConditionedVariable();
			}
			long cliqueCardinality = isConditioned ? 1 : var.cardinality();
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
		 * The number of variables left to be returned by calls to {@link #next()}.
		 */
		public int size()
		{
			return _heap.size();
		}
		
		/**
		 * Identifies cost evaluator used by this iterator.
		 */
		public VariableCost getCostEvaluator()
		{
			return _stats.cost();
		}
		
		/**
		 * The {@link VariableEliminator} that created this iterator.
		 */
		public VariableEliminator getEliminator()
		{
			return _eliminator;
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
	public static class Stats implements Cloneable
	{
		private final VariableCost _cost;
		
		private int _addedEdges;
		private long _addedEdgeWeight;
		private int _conditionedVariables;
		private int _factorsWithDuplicateVariables;
		private int _maxClique;
		private long _maxCliqueCardinality;

		/*--------------
		 * Construction
		 */
		
		/**
		 * All values are initialized to -1.
		 */
		public Stats()
		{
			this(null, -1);
		}
		
		private Stats(VariableCost cost, int value)
		{
			_cost = cost;
			
			_addedEdges = value;
			_addedEdgeWeight = value;
			_conditionedVariables = value;
			_factorsWithDuplicateVariables = value;
			_maxClique = value;
			_maxCliqueCardinality = value;
		}
		
		public Stats(Stats that)
		{
			_cost = that._cost;
			_addedEdges = that._addedEdges;
			_addedEdgeWeight = that._addedEdgeWeight;
			_conditionedVariables = that._conditionedVariables;
			_factorsWithDuplicateVariables = that._factorsWithDuplicateVariables;
			_maxClique = that._maxClique;
			_maxCliqueCardinality = that._maxCliqueCardinality;
		}
		
		@Override
		public Stats clone()
		{
			return new Stats();
		}
		
		/*--------------------
		 * Evaluation methods
		 */

		/**
		 * False if statistics indicates that the original graph does not need to be transformed
		 * to do efficient exact inference.
		 * <p>
		 * True if {@link #addedEdges()} and {@link #conditionedVariables()} and
		 * {@link #factorsWithDuplicateVariables()} are all zero.
		 */
		public boolean alreadyGoodForFastExactInference()
		{
			return _addedEdges == 0 && _conditionedVariables == 0 && _factorsWithDuplicateVariables == 0;
		}
		
		/**
		 * Returns -1/0/1 if these stats are deemed better than/same as/worse than {@code other} stats given specified
		 * threshold definition. Attributes for which {@code threshold} has a negative value
		 * will not be considered (other than that the {@code threshold} attributes are ignored).
		 * Attributes are compared in the following order:
		 * <ol>
		 * <li>{@link #maxCliqueCardinality()}
		 * <li>{@link #addedEdgeWeight()}
		 * <li>{@link #maxCliqueSize()}
		 * <li>{@link #addedEdges()}
		 * </ol>
		 * The first of these that are not equal will be used for the comparison.
		 */
		public int compareTo(Stats other, Stats threshold)
		{
			long diff = 0;
			
			if (threshold._maxCliqueCardinality >= 0)
			{
				diff = _maxCliqueCardinality - other._maxCliqueCardinality;
			}
			if (diff == 0 && threshold._addedEdgeWeight >= 0)
			{
				diff = _addedEdgeWeight - other._addedEdgeWeight;
			}
			if (diff == 0 && threshold._maxClique >= 0)
			{
				diff = _maxClique - other._maxClique;
			}
			if (diff == 0 && threshold._addedEdges >= 0)
			{
				diff = _addedEdges - other._addedEdges;
			}
			
			return Long.signum(diff);
		}
		
		/**
		 * True if these statistics satisfy the given threshold statistics.
		 * <p>
		 * Specifically, compares the values of the following attributes:
		 * <ul>
		 *   <li>{@link #addedEdges()}
		 *   <li>{@link #addedEdgeWeight()}
		 *   <li>{@link #maxCliqueSize()}
		 *   <li>{@link #maxCliqueCardinality()}
		 * </ul>
		 * If for each these attribute of {@code threshold} that have a non-negative value, the
		 * current object has value that is less than or equal to the threshold value, then the
		 * threshold is satisfied.
		 */
		public boolean meetsThreshold(Stats threshold)
		{
			return
				(threshold._addedEdges < 0 || threshold._addedEdges >= _addedEdges) &&
				(threshold._addedEdgeWeight < 0 || threshold._addedEdgeWeight >= _addedEdgeWeight) &&
				(threshold._maxClique < 0 || threshold._maxClique >= _maxClique) &&
				(threshold._maxCliqueCardinality < 0 || threshold._maxCliqueCardinality >= _maxCliqueCardinality)
				;
		}
		
		/*------------
		 * Attributes
		 */
		
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
		 * The number of variables that were eliminated by conditioning. That is, the number of variables
		 * with a fixed value when the variable eliminator is using conditioning.
		 * <p>
		 * Note: this attribute is not used by {@link #compareTo(Stats, Stats)} or {@link #meetsThreshold(Stats)}.
		 * <p>
		 * @see VariableBase#hasFixedValue()
		 * @see VariableEliminator#usesConditioning()
		 */
		public int conditionedVariables()
		{
			return _conditionedVariables;
		}

		/**
		 * Sets value of {@link #conditionedVariables()} and returns this object.
		 */
		public Stats conditionedVariables(int n)
		{
			_conditionedVariables = n;
			return this;
		}

		/**
		 * The cost function used to generate these stats, if from {@link OrderIterator}.
		 */
		public VariableCost cost()
		{
			return _cost;
		}
		
		/**
		 * The number of factors with more than one edge to the same variable.
		 * <p>
		 * Note: this attribute is not used by {@link #compareTo(Stats, Stats)} or {@link #meetsThreshold(Stats)}.
		 */
		public int factorsWithDuplicateVariables()
		{
			return _factorsWithDuplicateVariables;
		}
		
		/**
		 * Sets value of {@link #factorsWithDuplicateVariables()} and returns this object.
		 */
		public Stats factorsWithDuplicateVariables(int n)
		{
			_factorsWithDuplicateVariables = n;
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
		
		private void addConditionedVariable()
		{
			++_conditionedVariables;
		}

		public void addFactorWithDuplicateVars(FactorBase factor)
		{
			++_factorsWithDuplicateVariables;
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
		
		final boolean _isConditioned;
		
		private Var(VariableBase variable, double incrementalCost, boolean isConditioned)
		{
			_variable = variable;
			_incrementalCost = incrementalCost;
			_neighborMap = new HashMap<Var, VarLink>(variable.getSiblingCount());
			_isConditioned = isConditioned;
		}
		
		@Override
		public String toString()
		{
			return _variable.getName();
		}
		
		private boolean addNeighbor(Var neighbor)
		{
			if (neighbor != this && !_neighborMap.containsKey(neighbor))
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
			final int nNeighbors = var.nNeighbors();
			
			// It is always better to first eliminate variables connected by no more
			// than one edge because their removal will not expand the tree width.
			switch (nNeighbors)
			{
			case 0:
				// Return -2 if conditioned, or -1 for other variables with no edges
				// (which is unlikely). This ensures that conditioned variables will
				// always come first in the elimination order.
				return var._isConditioned ? -2 : -1;
			case 1:
				// Return 0 if there is only one edge.
				return 0;
			default:
				break;
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
	
	private List<Var> buildAdjacencyList(Stats stats)
	{
		final List<Var> list = new LinkedList<Var>();
		final VariableList variables = _model.getVariables();
		final Map<VariableBase,Var> map = new HashMap<VariableBase,Var>(variables.size());
		
		for (VariableBase variable : variables)
		{
			if (!variable.getDomain().isDiscrete() && !isConditioned(variable))
			{
				throw new DimpleException("VariableEliminator cannot handle non-discrete variable '%s'", variable);
			}
			Var var = new Var(variable, generateCostIncrement(variable), isConditioned(variable));
			map.put(variable, var);
			list.add(var);
			variable.clearMarked();
		}
		
		final FactorList factors = _model.getFactors();
		Var[] vars = new Var[10];
		
		for (FactorBase factor : factors)
		{
			final int nVars = factor.getSiblingCount();
			if (vars.length < nVars)
			{
				vars = new Var[nVars];
			}
			
			int nIncludedVars = 0;
			boolean factorHasDuplicateVars = false;
			
			for (int i = 0; i < nVars; ++i)
			{
				final VariableBase neighbor = factor.getSibling(i);
				if (!isConditioned(neighbor))
				{
					factorHasDuplicateVars |= neighbor.isMarked();
					neighbor.setMarked();
					vars[nIncludedVars++] = map.get(neighbor);
				}
			}
			
			if (factorHasDuplicateVars)
			{
				stats.addFactorWithDuplicateVars(factor);
			}

			// Connect all variables in factor to each other.
			for (int i = nIncludedVars; --i>=1;)
			{
				final Var vari = vars[i];
				vari._variable.clearMarked();
				for (int j = i; --j>=0;)
				{
					final Var varj = vars[j];
					vari.addNeighbor(varj);
					varj.addNeighbor(vari);
				}
			}
			if (nIncludedVars > 0)
			{
				vars[0]._variable.clearMarked();
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
	
	private boolean isConditioned(VariableBase variable)
	{
		return _useConditioning && variable.hasFixedValue();
	}
	
}
