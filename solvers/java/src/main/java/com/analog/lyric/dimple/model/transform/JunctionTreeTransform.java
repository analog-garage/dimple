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

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BinaryHeap;
import com.analog.lyric.collect.IHeap;
import com.analog.lyric.collect.SkipSet;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.Uniform;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTableIterator;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransformMap.AddedJointDiscreteVariable;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransformMap.AddedJointVariable;
import com.analog.lyric.dimple.model.transform.VariableEliminator.CostFunction;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Ordering;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Stats;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.SetMultimap;

/**
 * This class implements the junction tree transformation on an input model to create
 * a semantically equivalent version of the model that is singly connected and thus
 * can be used for exact inference using belief propagation.
 * <p>
 * The algorithm as implemented in this class is as follows:
 * 
 * <ol>
 * <li>Determine a variable elimination order.

 * <li>Makes a copy of the original model and creates a mapping from old to new nodes.
 * Creates initial version of the {@link JunctionTreeTransformMap}.

 * <li>If {@link #useConditioning()} is true, then any variables that have a fixed value
 * will be disconnected in the new graph using {@link Factor#removeFixedVariables()} and
 * will be recorded in the transform map.
 * 
 * <li>Organize the new graph into "cliques" of mutually connected variables using the variable
 * elimination order. For each variable in order, find all neighboring variables that have
 * not already been "eliminated" create a new clique containing the variable and its neighbors
 * and create a temporary factor that connects the neighbors (but not the variable itself) to each
 * other. All non-temporary factors that are connected to the variable and that have not been assigned to a
 * previous clique are assigned to the new clique. After all variables have been "eliminated", remove any
 * temporary factors from the new graph.
 * 
 * <li>Use a modified version of Prim's algorithm to build a max spanning tree over the cliques where the edge weight
 * is the number of variables in common between two cliques. Ties are broken in favor of edges with lower
 * joint variable cardinality in order to favor smaller messages. When a new edge contains all of the variables
 * of one of the cliques, then instead of adding the edge, the two cliques are merged into one.
 * 
 * <li>For each clique, create a new factor that connects all of its variables by combining all of the
 * factors that have been assigned to it using {@link FactorGraph#join(Variable[], Factor...)}.
 * 
 * <li>Create half-edges for variables that are in only one clique and therefore will not be in any edge
 * created during spanning tree construction.
 * 
 * <li>For each clique that has any multi-variable edge, create a new variable for each such edge and
 * rewrite the factor to connect to the new edge variables. This will not increase the number of entries
 * in the underlying factor table but will require it to be converted to a sparse representation and possibly
 * reordered. It is possible for multiple edge variables for the same combinations of original variables to
 * exist in the same graph. At the end of inference, they should all of the same beliefs but may differ before
 * that.
 * 
 * <li>The previous step may orphan some variables from the graph because they are subsumed by one or more new
 * joint variables. Find any such variables and reconnect to the graph by adding a new deterministic factor that
 * marginalizes out the variable value from the smallest joint variable that contains it.
 * </ol>
 * 
 * <h2>References</h2>
 * <ul>
 * <li>David Barber.
 * <a href="http://www.cs.ucl.ac.uk/staff/d.barber/brml/">
 * Bayesian Reasoning and Machine Learning.</a>
 * Chapter 6.
 * 
 * <li>Daphne Koller &amp; Nir Friedman.
 * <a href="http://mitpress.mit.edu/books/probabilistic-graphical-models">
 * Probabilistic Graphical Models: <em>Principals and Techniques</em></a>
 * Chapter 10.
 * </ul>
 * <p>
 * 
 * @since 0.05
 * @author Christopher Barber
 */
public class JunctionTreeTransform
{
	/*-------
	 * State
	 */

	/**
	 * Default value of {@link #maxTransformationAttempts()}
	 */
	public static final int DEFAULT_MAX_TRANSFORMATION_ATTEMPTS = 10;
	
	private int _nEliminationAttempts = DEFAULT_MAX_TRANSFORMATION_ATTEMPTS;
	private boolean _useConditioning = false;
	private CostFunction[] _costFunctions = {};
	private Random _rand = new Random();
	
	/**
	 * Orders variables by id.
	 */
	private static final Comparator<Variable> _variableComparator = Variable.orderById;
	
	/*--------------
	 * Construction
	 */
	
	public JunctionTreeTransform()
	{
	}
	
	/*---------
	 * Options
	 */
	
	/**
	 * The random number generator used by this transformer. This is only used when determining
	 * the variable elimination ordering and is passed to the underlying {@link VariableEliminator}.
	 * @see #random(Random)
	 */
	public Random random()
	{
		return _rand;
	}

	/**
	 * Sets {@link #random()} to specified generator.
	 * <p>
	 * Only intended for use in testing to allow for reproduction of test results from a known seed.
	 * 
	 * @return this
	 */
	public JunctionTreeTransform random(Random rand)
	{
		_rand = rand;
		return this;
	}
	
	/**
	 * If true, then the transformation will condition out any variables that have a fixed value.
	 * This will produce a more efficient graph but will prevent it from being reused if the fixed
	 * value changes.
	 * <p>
	 * False by default.
	 * @see #useConditioning(boolean)
	 */
	public boolean useConditioning()
	{
		return _useConditioning;
	}
	
	/**
	 * Sets {@link #useConditioning()} to specified value.
	 * @return this
	 */
	public JunctionTreeTransform useConditioning(boolean value)
	{
		_useConditioning = value;
		return this;
	}
	
	/**
	 * The cost functions used by {@link VariableEliminator} to determine the variable
	 * elimination ordering. If empty (the default), then all of the standard {@link VariableCost}
	 * functions will be tried.
	 * 
	 * @see #variableEliminatorCostFunctions(VariableEliminator.CostFunction...)
	 * @see #variableEliminatorCostFunctions(VariableEliminator.VariableCost...)
	 */
	public CostFunction[] variableEliminatorCostFunctions()
	{
		return _costFunctions.clone();
	}
	
	/**
	 * Sets {@link #variableEliminatorCostFunctions()} to specified value.
	 * @return this
	 * @see #variableEliminatorCostFunctions(VariableEliminator.VariableCost...)
	 */
	public JunctionTreeTransform variableEliminatorCostFunctions(CostFunction ... costFunctions)
	{
		_costFunctions = costFunctions.clone();
		return this;
	}

	/**
	 * Sets {@link #variableEliminatorCostFunctions()} to specified value.
	 * @return this
	 * @see #variableEliminatorCostFunctions(VariableEliminator.CostFunction...)
	 */
	public JunctionTreeTransform variableEliminatorCostFunctions(VariableCost ... costFunctions)
	{
		_costFunctions = VariableCost.toFunctions(costFunctions);
		return this;
	}

	/**
	 * Specifies the maximum number of times to attempt to determine an optimal junction tree
	 * transformation.
	 * <p>
	 * This is the number of iterations of the {@link VariableEliminator} algorithm when attempting
	 * to determine the variable elimination ordering that determines the junction tree
	 * transofmration Each iteration will pick a cost function from
	 * {@link #variableEliminatorCostFunctions()} at random and will randomize the order of
	 * variables that have equivalent costs. A higher number of iterations may produce a better
	 * ordering.
	 * <p>
	 * Default value is specified by {@link #DEFAULT_MAX_TRANSFORMATION_ATTEMPTS}.
	 * <p>
	 * 
	 * @see #maxTransformationAttempts(int)
	 */
	public int maxTransformationAttempts()
	{
		return _nEliminationAttempts;
	}
	
	/**
	 * Sets {@link #maxTransformationAttempts()} to the specified value.
	 * @return this
	 */
	public JunctionTreeTransform maxTransformationAttempts(int attempts)
	{
		_nEliminationAttempts = attempts;
		return this;
	}

	/*------------------------------
	 * Inner implementation classes
	 */
	
	private static class Clique
	{
		/**
		 * Factors that make up the clique.
		 */
		private Factor[] _factors;
		
		/**
		 * Variables in clique
		 */
		private Discrete[] _variables;
		
		private final SkipSet<CliqueEdge> _edges;
		
		private boolean _hasMultiVariableEdge = false;
		
		/**
		 * Merged factor for the clique. May be null if all of the original factors
		 * were already incorporated into another clique.
		 */
		private @Nullable Factor _mergedFactor = null;
		
		//
		// Temporary spanning tree state
		//
		
		private boolean _inSpanningTree = false;
		private @Nullable IHeap.IEntry<Clique> _heapEntry = null;
		private @Nullable CliqueEdge _bestEdge = null;
		
		/*--------------
		 * Construction
		 */
		
		private Clique(List<Discrete> variables, List<Factor> factors)
		{
			_factors = factors.toArray(new Factor[factors.size()]);
			_variables = variables.toArray(new Discrete[variables.size()]);
			_edges = SkipSet.create();
			Arrays.sort(_variables, _variableComparator);
		}
		
		/*----------------
		 * Object methods
		 */
		
		@Override
		public String toString()
		{
			return Arrays.toString(_variables);
		}
		
		/*---------
		 * Methods
		 */
		
		/**
		 * Merges variables and factors from {@code absorbee} into this clique and removes
		 * {@code absorbee} from {@code varToCliques}.
		 * 
		 * @param absorbee must not have any edges assigned to it.
		 * @param varToCliques
		 * @return true if variables were added to this clique
		 */
		private boolean absorbClique(Clique absorbee, SetMultimap<Discrete, Clique> varToCliques)
		{
			assert(absorbee._edges.isEmpty());
			
			boolean variablesAdded = false;
			
			for (Discrete var : absorbee._variables)
			{
				varToCliques.remove(var, absorbee);
				varToCliques.put(var, this);
			}
			
			if (absorbee._variables.length > _variables.length)
			{
				_variables = absorbee._variables;
				variablesAdded = true;
			}
			absorbee._variables = new Discrete[0];
			
			_factors = ObjectArrays.concat(_factors, absorbee._factors, Factor.class);
			absorbee._factors = new Factor[0];
			
			return variablesAdded;
		}
		
		private void addEdge(CliqueEdge edge)
		{
			_hasMultiVariableEdge |= edge._variables.length > 1;
			_edges.add(edge);
		}
		
		private void addToMap(SetMultimap<Discrete, Clique> map)
		{
			for (Discrete variable : _variables)
			{
				map.put(variable, this);
			}
		}

		private int indexOfVariable(Discrete variable)
		{
			return Arrays.binarySearch(_variables, variable, _variableComparator);
		}
		
		private boolean joinMultivariateEdges()
		{
			if (!_hasMultiVariableEdge)
				return false;
			
			final int nEdges = _edges.size();
			final CliqueEdge[] edges = _edges.toArray(new CliqueEdge[nEdges]);
			
			// Build mapping from new to old indices.
			final Discrete[] newVariables = new Discrete[nEdges];
			final DiscreteDomain[] newDomains = new DiscreteDomain[nEdges];
			final int[][] newFromOld = new int[nEdges][];
			final int[][] scratchIndices = new int[nEdges][];
			
			for (int edgei = 0; edgei < nEdges; ++edgei)
			{
				final CliqueEdge edge = edges[edgei];
				final int nEdgeVars = edge._variables.length;
				final int[] a = new int[nEdgeVars];
				
				final Discrete jointVar = requireNonNull(edge._jointVariable);
				newVariables[edgei] = jointVar;
				newDomains[edgei] = jointVar.getDiscreteDomain();
				for (int vari = 0; vari < nEdgeVars; ++vari)
				{
					final Discrete edgeVar = edge._variables[vari];
					a[vari] = indexOfVariable(edgeVar);
				}
				newFromOld[edgei] = a;
				if (nEdgeVars > 1)
				{
					scratchIndices[edgei] = new int[nEdgeVars];
				}
			}
			
			// Compute new factor table energies and indices.
			final Factor mergedFactor = requireNonNull(_mergedFactor);
			final IFactorTable oldFactorTable = requireNonNull(mergedFactor.getFactorTable());
			final int nEntries = oldFactorTable.countNonZeroWeights();

			final int[][] indices = new int[nEntries][];
			final double[] energies = new double[nEntries];
			
			final IFactorTableIterator oldIter = oldFactorTable.iterator();
			int si = 0;
			while (oldIter.advance())
			{
				final int[] oldIndices = oldIter.indicesUnsafe();
				final int[] newIndices = new int[nEdges];
				
				for (int edgei = 0; edgei < nEdges; ++edgei)
				{
					final CliqueEdge edge = edges[edgei];
					final int nEdgeVars = edge._variables.length;
					final int[] map = newFromOld[edgei];
					
					if (nEdgeVars == 1)
					{
						newIndices[edgei] = oldIndices[map[0]];
					}
					else
					{
						final int[] scratch = scratchIndices[edgei];
						for (int vari = 0; vari < nEdgeVars; ++vari)
						{
							scratch[vari] = oldIndices[map[vari]];
						}
						newIndices[edgei] = ((JointDiscreteDomain<?>)newDomains[edgei]).getIndexFromIndices(scratch);
					}
				}

				energies[si] = oldIter.energy();
				indices[si] = newIndices;
				++si;
			}
			
			// Create the new table
			final IFactorTable newFactorTable = FactorTable.create(newDomains);
			newFactorTable.setEnergiesSparse(indices, energies);

			// Remove the old factor
			FactorGraph graph = requireNonNull(mergedFactor.getParentGraph());
			graph.remove(mergedFactor);
			
			// Create new factor attached to edge variables
			_mergedFactor = graph.addFactor(newFactorTable, newVariables);
			
			return true;
		}
		
		private boolean updateBestEdge(CliqueEdge incomingEdge)
		{
			CliqueEdge bestEdge = _bestEdge;
			if (bestEdge == null || bestEdge._weight < incomingEdge._weight)
			{
				_bestEdge = incomingEdge;
				return true;
			}
			
			return false;
		}
		
		/**
		 * The number of variables in the clique including the eliminated variable.
		 */
		private int size()
		{
			return _variables.length;
		}
	}
	
	/**
	 * Represents an edge between two cliques/factors. Will be represented as a single
	 * variable in the new graph.
	 */
	private static class CliqueEdge implements Comparable<CliqueEdge>
	{
		private final @Nullable Clique _from;
		private final Clique _to;
		
		/**
		 * Variables that are transmitted across this edge.
		 */
		private final Discrete[] _variables;
		
		/**
		 * The variable representing this edge in the new graph.
		 */
		private @Nullable Discrete _jointVariable;
		
		/**
		 * Weight is # of variables on edge plus the reciprocal of the joint cardinality.
		 * This will favor the edge with the most variables and smallest cardinality.
		 */
		private final double _weight;
		
		private CliqueEdge(@Nullable Clique from, Clique to, Discrete ... variables)
		{
			_from = from;
			_to = to;
			_variables = variables;
			
			long cardinality = 1;
			for (Discrete variable : variables)
			{
				cardinality *= variable.getDomain().size();
			}
			
			_weight = variables.length + 1 / (double)cardinality;
			
			if (variables.length == 1)
			{
				_jointVariable = variables[0].asDiscreteVariable();
			}

			assert(ArrayUtil.isSorted(variables, _variableComparator));
		}
		
		@Override
		public String toString()
		{
			return String.format("%s =%s=> %s", _from, Arrays.toString(_variables), _to);
		}
		
		@Override
		@NonNullByDefault(false)
		public int compareTo(CliqueEdge that)
		{
			return _variableComparator.compare(this._jointVariable, that._jointVariable);
		}
		
		/**
		 * @return true if cliques connected by this edge could be merged because one is a subset of the other.
		 */
		private boolean isMergeable()
		{
			// Since the edge contains all variables that are in common between the two cliques,
			// we only need to compare the lengths to see if there is a subset relationship.
			final Clique from = _from;
			final int size = _variables.length;
			return from != null && (_to._variables.length == size || from._variables.length == size);
		}
		
		private @Nullable AddedJointVariable<?> makeJointVariable(FactorGraph targetModel)
		{
			final Discrete[] edgeVars = _variables;
			final int nEdgeVars = edgeVars.length;
			AddedJointVariable<?> addedVar = null;
			
			if (nEdgeVars > 1)
			{
				// Create joint variable for edge
				final DiscreteDomain[] edgeDomains = new DiscreteDomain[edgeVars.length];
				for (int i  = 0; i < nEdgeVars; ++i)
				{
					edgeDomains[i] = edgeVars[i].getDomain();
				}
				
				final Discrete jointVar = new Discrete(DiscreteDomain.joint(edgeDomains));
				StringBuilder jointName = new StringBuilder();
				for (int i = 0; i < nEdgeVars; ++i)
				{
					if (i > 0)
					{
						jointName.append("+");
					}
					jointName.append(edgeVars[i].getLabel());
				}
				jointVar.setLabel(jointName.toString());
				targetModel.addVariables(jointVar);
				_jointVariable = jointVar;
				
				addedVar = new AddedJointDiscreteVariable(jointVar, edgeVars);
			}
			
			return addedVar;
		}
	}
	
	/*---------
	 * Methods
	 */

	/**
	 * Build junction tree transformation.
	 * <p>
	 * @see #transform(FactorGraph, ArrayList)
	 * @see #transform(FactorGraph, VariableEliminator.Ordering)
	 */
	public JunctionTreeTransformMap transform(FactorGraph model)
	{
		return transform(model, buildEliminationOrder(model));
	}

	/**
	 * Build junction tree transformation using a specified variable elimination ordering.
	 * @param eliminationOrder is an ordering of the variables in the {@code model}. It must include
	 * every variable exactly once.
	 * <p>
	 * @see #transform(FactorGraph)
	 * @see #transform(FactorGraph, VariableEliminator.Ordering)
	 */
	public JunctionTreeTransformMap transform(FactorGraph model, ArrayList<Variable> eliminationOrder)
	{
		// Validate variables
		final VariableList variables = model.getVariables();
		if (eliminationOrder.size() != model.getVariableCount() || !variables.containsAll(eliminationOrder))
		{
			throw new IllegalArgumentException("Elimination order does not specify same variables as the model");
		}
		
		Stats stats = new Stats();
		
		if (_useConditioning)
		{
			// Make sure conditioned variables are at front of the ordering.
			Collections.sort(eliminationOrder, new Comparator<Variable>() {
				@Override
				@NonNullByDefault(false)
				public int compare(Variable var1, Variable var2)
				{
					return var1.hasFixedValue() ? (var2.hasFixedValue() ? 0 : -1) : (var2.hasFixedValue() ? 1 : 0);
				}
			});
			
			int nConditioned = 0;
			for (Variable var : eliminationOrder)
			{
				if (var.hasFixedValue())
				{
					++nConditioned;
				}
				else
				{
					break;
				}
			}
			stats.conditionedVariables(nConditioned);
		}
		
		if (model.isForest())
		{
			// If not a forest, then at least two factors would have to be merged.
			stats.mergedFactors(2);
		}
		
		return transform(model, new Ordering(eliminationOrder, stats));
	}
	
	/**
	 * Build junction tree transformation using a specified variable elimination ordering.
	 * <p>
	 * @param eliminationOrder is a valid variable ordering for this graph created by {@link VariableEliminator}
	 * on this {@code model}.
	 * 
	 * @see #transform(FactorGraph)
	 * @see #transform(FactorGraph, ArrayList)
	 */
	public JunctionTreeTransformMap transform(FactorGraph model, Ordering eliminationOrder)
	{
		// 1) Determine an elimination order
		
		final Stats orderStats = eliminationOrder.stats;
		
		if (orderStats.alreadyGoodForFastExactInference())
		{
			// If elimination order introduces no edges, graph is already a tree. Done.
			return JunctionTreeTransformMap.identity(model);
		}
		
		if (orderStats.factorsWithDuplicateVariables() > 0)
		{
			 // FIXME - support duplicate variables in JunctionTreeTransform
			throw DimpleException.unsupported("factors with duplicate variables");
		}
		
		// 2) Make copy of the factor graph
		
		final ArrayList<Variable> variables = eliminationOrder.variables;
		final int nVariables = variables.size();
		final int nFactors = model.getFactorCount();

		final BiMap<Node,Node> old2new = HashBiMap.create(nVariables * 2);
		final FactorGraph targetModel = model.copyRoot(old2new);
		targetModel.setSchedule(null); // don't use the copied schedule!
		
		// Make copied factors undirected.
		for (Factor factor : targetModel.getFactorsFlat())
		{
			factor.setUndirected();
		}
		
		final JunctionTreeTransformMap transformMap = JunctionTreeTransformMap.create(model, targetModel);
		
		for (Entry<Node,Node> entry : old2new.entrySet())
		{
			final Node source = entry.getKey();
			final Variable var = source.asVariable();
			if (var != null)
			{
				transformMap.addVariableMapping(var, Objects.requireNonNull(entry.getValue().asVariable()));
			}
		}

		// 3) Disconnect conditioned variables from other variables in new graph

		disconnectConditionedVariables(eliminationOrder, transformMap);
		
		// 4) Create cliques using variable elimination order
		
		final List<Clique> cliques = createCliques(eliminationOrder, transformMap);
		final SetMultimap<Discrete, Clique> varToCliques =
			HashMultimap.create(nVariables, nVariables/Math.max(1,nFactors));
		for (Clique clique : cliques)
		{
			clique.addToMap(varToCliques);
		}
		
		// 5) Use Prim's algorithm to build max spanning tree over clique graph where the edge weight
		//    is the number of variables in common between the two cliques along each edge.
		
		final List<CliqueEdge> multiVariateEdges = formSpanningTree(transformMap, cliques, varToCliques);
		
		// 6) Merge factors in cliques
		
		for (Clique clique : cliques)
		{
			if (clique._variables.length > 0)
			{
				clique._mergedFactor = targetModel.join(clique._variables, clique._factors);
			}
		}
		
		// 7) Rewrite factors with multivariate edges
		
		for (Clique clique : cliques)
		{
			clique.joinMultivariateEdges();
			for (Factor cliqueFactor : clique._factors)
			{
				Factor sourceFactor = (Factor) old2new.inverse().get(cliqueFactor);
				transformMap.addFactorMapping(sourceFactor, Objects.requireNonNull(clique._mergedFactor));
			}
		}
		
		
		// 8) Find and reconnect orphaned variables.
		
		reconnectOrphanVariables(targetModel, multiVariateEdges);
		
		return transformMap;
	}

	//-----------------
	// Private methods
	//
	
	private Ordering buildEliminationOrder(FactorGraph model)
	{
		// Find max cardinality of existing factors - we can't do better than that.
		int maxCardinality = 0;
		for (Factor factor : model.getFactors())
		{
			if (factor.hasFactorTable())
			{
				maxCardinality = Math.max(maxCardinality, factor.getFactorTable().getDomainIndexer().getCardinality());
			}
		}
		
		VariableEliminator.Stats threshold = new VariableEliminator.Stats().maxCliqueCardinality(maxCardinality);
		
		VariableEliminator eliminator = new VariableEliminator(model, _useConditioning, _rand);
		
		return VariableEliminator.generate(eliminator, _nEliminationAttempts, threshold, _costFunctions);
	}

	private int disconnectConditionedVariables(Ordering eliminationOrder, JunctionTreeTransformMap transformMap)
	{
		final int nConditioned = _useConditioning ? eliminationOrder.stats.conditionedVariables() : 0;
		if (nConditioned > 0)
		{
			// Build list of factors that need to be modified
			final Map<Factor, Factor> factors = new LinkedHashMap<Factor, Factor>();

			// Conditioned variables are guaranteed to be at the head of the list.
			// Conditioned variables aren't necessarily discrete but the remaining ones
			// should be discrete.
			for (int i = 0; i < nConditioned; ++i)
			{
				Variable sourceVariable = eliminationOrder.variables.get(i);
				Variable variable = transformMap.sourceToTargetVariable(sourceVariable);
				transformMap.addConditionedVariable(sourceVariable);
				for (int j = 0, endj = variable.getSiblingCount(); j < endj; ++j)
				{
					final Factor factor = variable.getSibling(j);
					factors.put(factor, factor);
				}
			}

			for (Factor factor : factors.values())
			{
				factor.removeFixedVariables();
			}
		}
		
		return nConditioned;
	}
	
	private List<Clique> createCliques(Ordering eliminationOrder, JunctionTreeTransformMap transformMap)
	{
		final List<Clique> cliques = new LinkedList<Clique>();
		final ArrayList<Variable> variables = eliminationOrder.variables;
		final int nVariables = variables.size();
		final int nConditioned = eliminationOrder.stats.conditionedVariables();
		final FactorGraph targetModel = transformMap.target();
		final List<Factor> temporaryFactors = Lists.newLinkedList();
		
		for (int vari = nConditioned; vari < nVariables; ++vari)
		{
			final Discrete var = (Discrete) transformMap.sourceToTargetVariable(variables.get(vari));
			// Mark variable as "eliminated". Note that there is no need to clear the mark
			// at the start because all of the variables are newly created.
			var.setMarked();

			final List<Factor> cliqueFactors = new LinkedList<Factor>();
			final List<Discrete> cliqueVars = new LinkedList<Discrete>();

			final int nVarFactors = var.getSiblingCount();
			for (int fi = nVarFactors; --fi>=0;)
			{
				final Factor neighborFactor = var.getSibling(fi);
				if (!neighborFactor.isMarked())
				{
					cliqueFactors.add(neighborFactor);
					// Mark factor to indicate it has been assigned to a clique.
					neighborFactor.setMarked();
				}
				for (int vi = neighborFactor.getSiblingCount(); --vi>=0;)
				{
					// Non-Discrete variables should already have been removed during conditioning step.
					final Discrete neighborVar = neighborFactor.getSibling(vi).asDiscreteVariable();
					if (!neighborVar.isMarked() && !neighborVar.wasVisited())
					{
						cliqueVars.add(neighborVar);
						neighborVar.setVisited();
					}
				}
			}

			for (Discrete cliqueVar : cliqueVars)
			{
				cliqueVar.clearVisited();
			}

			// Add temporary factor to connect remaining variables in clique to each other.
			if (cliqueVars.size() > 1 && nVarFactors > 1)
			{
				final Factor temporaryFactor = targetModel.addFactor(Uniform.INSTANCE, cliqueVars.toArray());
				temporaryFactor.setMarked();
				temporaryFactors.add(temporaryFactor);
			}

			cliqueVars.add(var);
			Clique clique = new Clique(cliqueVars, cliqueFactors);
			cliques.add(clique);
		}

		for (Factor temporaryFactor : temporaryFactors)
		{
			targetModel.remove(temporaryFactor);
		}
		
		return cliques;
	}
	
	private List<CliqueEdge> formSpanningTree(
		JunctionTreeTransformMap transformMap,
		List<Clique> cliques,
		SetMultimap<Discrete, Clique> varToCliques)
	{
		final int nCliques = cliques.size();
		assert(nCliques > 0);
		final IHeap<Clique> heap = BinaryHeap.create(nCliques);
		
		heap.deferOrderingForBulkAdd(nCliques);
		Clique maxClique = null;
		for (Clique clique : cliques)
		{
			if (maxClique == null || clique.size() > maxClique.size())
			{
				maxClique = clique;
			}
			clique._heapEntry = heap.offer(clique, Double.POSITIVE_INFINITY);
		}
		requireNonNull(maxClique);
		heap.changePriority(requireNonNull(maxClique._heapEntry), Double.NEGATIVE_INFINITY);
		
		// Edges with more than one variable
		List<CliqueEdge> multiVariateEdges = Lists.newArrayListWithCapacity(nCliques - 1);
		final FactorGraph targetModel = transformMap.target();
		
		for (Clique clique; (clique = heap.poll()) != null;)
		{
			clique._inSpanningTree = true;
			clique._heapEntry = null;
			
			final CliqueEdge addedEdge = clique._bestEdge;
			if (addedEdge != null)
			{
				final Clique prevClique = addedEdge._from;
				if (addedEdge.isMergeable())
				{
					if (requireNonNull(prevClique).absorbClique(clique, varToCliques))
					{
						clique = prevClique;
					}
					else
					{
						clique = null;
					}
				}
				else
				{
					final AddedJointVariable<?> addedVar = addedEdge.makeJointVariable(targetModel);
					if (addedVar != null)
					{
						multiVariateEdges.add(addedEdge);
						transformMap.addDeterministicVariable(addedVar);
					}
					requireNonNull(prevClique).addEdge(addedEdge);
					clique.addEdge(addedEdge);
				}
			}
			
			if (clique != null)
			{
				for (CliqueEdge edge : edgesNotInTree(clique, varToCliques))
				{
					final Clique targetClique = edge._to;
					if (targetClique.updateBestEdge(edge))
					{
						// Use negative weight because IHeap implements a min heap.
						heap.changePriority(Objects.requireNonNull(targetClique._heapEntry),  -edge._weight);
					}
				}
			}
		}

		// Add half-edges for variables that are in only one clique and therefore won't be in any
		// edge created in the previous step.
		
		for (Discrete var : varToCliques.keySet())
		{
			final Set<Clique> cliquesForVar = varToCliques.get(var);
			if (cliquesForVar.size() == 1)
			{
				final Clique clique = Iterables.getOnlyElement(cliquesForVar);
				clique.addEdge(new CliqueEdge(null, clique, var));
			}
		}
		
		return multiVariateEdges;
	}
	
	private void reconnectOrphanVariables(FactorGraph targetModel, List<CliqueEdge> multiVariateEdges)
	{
		// Find orphaned variables and map each to the smallest joint variable that subsumes it
		
		final Map<Discrete, Tuple2<Discrete,Integer>> orphanVarToJointVar = Maps.newLinkedHashMap();
		for (CliqueEdge edge : multiVariateEdges)
		{
			final Discrete jointVar = Objects.requireNonNull(edge._jointVariable);
			for (int vari = 0, nVars = edge._variables.length; vari < nVars; ++vari)
			{
				final Discrete var = edge._variables[vari];
				
				if (!var.wasVisited())
				{
					var.setVisited();
					if (var.getSiblingCount() == 0)
					{
						final Tuple2<Discrete,Integer> tuple = orphanVarToJointVar.get(var);
						if (tuple == null || tuple.first.getDomain().size() > jointVar.getDomain().size())
						{
							orphanVarToJointVar.put(var, Tuple2.create(jointVar, vari));
						}
					}
				}
			}
		}
		
		// Create marginal factors that connects each orphan variable to a joint variable.
		//
		// TODO: if two or more orphaned variables are attached to the same joint variable, it
		// can be expressed using a single factor instead of one per variable
		
		for (Entry<Discrete,Tuple2<Discrete,Integer>> entry : orphanVarToJointVar.entrySet())
		{
			final Discrete orphan = entry.getKey();
			final Discrete joint = entry.getValue().first;
			final int subindex= entry.getValue().second;
			final JointDiscreteDomain<?> jointd = (JointDiscreteDomain<?>)joint.getDomain();
			
			Factor factor = targetModel.addFactor(FactorTable.createMarginal(subindex, jointd), orphan, joint);
			factor.setDirectedTo(new int[] { 0 });
		}
	}
	
	/**
	 * Computes list of edges from {@code clique} to other cliques that are not yet in the
	 * spanning tree.
	 * 
	 * @param clique
	 * @param varToCliques is a mapping from variable to the cliques that contain it.
	 */
	private List<CliqueEdge> edgesNotInTree(Clique clique, SetMultimap<Discrete, Clique> varToCliques)
	{
		final SetMultimap<Clique, Discrete> cliqueToCommonVars = LinkedHashMultimap.create();
		
		// NOTE: because clique._variables is sorted the variables in the edge list will also be sorted.
		
		for (Discrete variable : clique._variables)
		{
			for (Clique neighbor : varToCliques.get(variable))
			{
				if (!neighbor._inSpanningTree)
				{
					cliqueToCommonVars.put(neighbor, variable);
				}
			}
		}
		
		List<CliqueEdge> edges = new ArrayList<CliqueEdge>(cliqueToCommonVars.size());
		
		for (Clique neighbor : cliqueToCommonVars.keySet())
		{
			final Set<Discrete> commonVars = cliqueToCommonVars.get(neighbor);
			edges.add(new CliqueEdge(clique, neighbor, ArrayUtil.copy(Discrete.class, commonVars)));
		}
		
		return edges;
	}
	
}
