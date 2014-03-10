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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

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
import com.analog.lyric.dimple.model.transform.FactorGraphTransformMap.AddedDeterministicVariable;
import com.analog.lyric.dimple.model.transform.FactorGraphTransformMap.AddedJointDiscreteVariable;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Ordering;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Stats;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.SetMultimap;

public class JunctionTreeTransform implements IFactorGraphTransform
{
	/*-------
	 * State
	 */

	private int _nEliminationAttempts = 10;
	private boolean _useConditioning = false;
	private final VariableCost[] _costFunctions = {};
	private Random _rand = new Random();
	
	/**
	 * Orders variables by id.
	 */
	private static final Comparator<VariableBase> _variableComparator = VariableBase.orderById;
	
	/*--------------
	 * Construction
	 */
	
	public JunctionTreeTransform()
	{
	}
	
	/*---------
	 * Options
	 */
	
	public Random random()
	{
		return _rand;
	}
	
	public JunctionTreeTransform random(Random rand)
	{
		_rand = rand;
		return this;
	}
	
	public boolean useConditioning()
	{
		return _useConditioning;
	}
	
	public JunctionTreeTransform useConditioning(boolean value)
	{
		_useConditioning = value;
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
		private Factor _mergedFactor = null;
		
		//
		// Temporary spanning tree state
		//
		
		private boolean _inSpanningTree = false;
		private IHeap.IEntry<Clique> _heapEntry = null;
		private CliqueEdge _bestEdge = null;
		
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
				
				newVariables[edgei] = edge._jointVariable;
				newDomains[edgei] = edge._jointVariable.getDiscreteDomain();
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
			final IFactorTable oldFactorTable = _mergedFactor.getFactorTable();
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
			FactorGraph graph = _mergedFactor.getParentGraph();
			graph.remove(_mergedFactor);
			
			// Create new factor attached to edge variables
			graph.addFactor(newFactorTable, newVariables);
			
			return true;
		}
		
		private boolean updateBestEdge(CliqueEdge incomingEdge)
		{
			if (_bestEdge == null || _bestEdge._weight < incomingEdge._weight)
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
		private final Clique _from;
		private final Clique _to;
		
		/**
		 * Variables that are transmitted across this edge.
		 */
		private final Discrete[] _variables;
		
		/**
		 * The variable representing this edge in the new graph.
		 */
		private Discrete _jointVariable;
		
		/**
		 * Weight is # of variables on edge plus the reciprocal of the joint cardinality.
		 * This will favor the edge with the most variables and smallest cardinality.
		 */
		private final double _weight;
		
		private CliqueEdge(Clique from, Clique to, Discrete ... variables)
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
			
			return _to._variables.length == _variables.length || _from._variables.length == _variables.length;
		}
		
		private AddedDeterministicVariable makeJointVariable(FactorGraph targetModel)
		{
			final Discrete[] edgeVars = _variables;
			final int nEdgeVars = edgeVars.length;
			AddedDeterministicVariable addedVar = null;
			
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

	/*
	 * 
	 */
	@Override
	public FactorGraphTransformMap transform(FactorGraph model)
	{
		//
		// 1) Determine an elimination order
		//
		
		final Ordering eliminationOrder = buildEliminationOrder(model);
		final Stats orderStats = eliminationOrder.stats;
		
		// If elimination order introduces no edges, graph is already a tree. Done.
		if (orderStats.alreadyGoodForFastExactInference())
		{
			return FactorGraphTransformMap.identity(model);
		}
		
		//
		// 2) Make copy of the factor graph
		//
		
		final ArrayList<VariableBase> variables = eliminationOrder.variables;
		final int nVariables = variables.size();
		final int nFactors = model.getFactorCount();

		final Map<Node,Node> old2new = new HashMap<Node,Node>(nVariables * 2);
		final FactorGraph targetModel = model.copyRoot(old2new);
		
		final FactorGraphTransformMap transformMap = FactorGraphTransformMap.create(model, targetModel, old2new);

		//
		// 3) Disconnect conditioned variables in new graph
		//

		final int nConditioned = orderStats.conditionedVariables();
		if (nConditioned > 0)
		{
			// Build list of factors that need to be modified
			final Map<Factor, Factor> factors = new LinkedHashMap<Factor, Factor>();
			
			// Conditioned variables are guaranteed to be at the head of the list.
			// Conditioned variables aren't necessarily discrete but the remaining ones
			// should be discrete.
			for (int i = 0; i < nConditioned; ++i)
			{
				VariableBase variable = (VariableBase)old2new.get(eliminationOrder.variables.get(i));
				transformMap.addConditionedVariable(variable);
				for (int j = 0, endj = variable.getSiblingCount(); j < endj; ++j)
				{
					final Factor factor = variable.getSibling(j);
					factors.put(factor, factor);
				}
			}
			
			for (Factor factor : factors.values())
			{
				if (factor.removeFixedVariables() > 0)
				{
					// Factors are no longer the same.
					old2new.remove(factor);
				}
			}
		}
		
		if (orderStats.factorsWithDuplicateVariables() > 0)
		{
			// FIXME
			throw DimpleException.unsupported("factors with duplicate variables");
		}
		
		//
		// 4) Create cliques using variable elimination order
		//
		
		final SetMultimap<Discrete, Clique> varToCliques = HashMultimap.create(nVariables, nVariables/nFactors);
		final List<Clique> cliques = Lists.newLinkedList();
		final List<Factor> temporaryFactors = Lists.newLinkedList();
		for (int vari = nConditioned; vari < nVariables; ++vari)
		{
			final Discrete var = (Discrete) old2new.get(variables.get(vari));
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
			clique.addToMap(varToCliques);
		}
		
		for (Factor temporaryFactor : temporaryFactors)
		{
			targetModel.remove(temporaryFactor);
		}
		
		//
		// 5) Use Prim's algorithm to build max spanning tree over clique graph where the edge weight
		//    is the number of variables in common between the two cliques along each edge.
		//
		
		final int nCliques = cliques.size();
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
		heap.changePriority(maxClique._heapEntry, Double.NEGATIVE_INFINITY);
		
		// Edges with more than one variable
		List<CliqueEdge> multiVariateEdges = Lists.newArrayListWithCapacity(nCliques - 1);
		
		while (!heap.isEmpty())
		{
			Clique clique = heap.poll();
			clique._inSpanningTree = true;
			clique._heapEntry = null;
			
			final CliqueEdge addedEdge = clique._bestEdge;
			if (addedEdge != null)
			{
				final Clique prevClique = addedEdge._from;
				if (addedEdge.isMergeable())
				{
					if (prevClique.absorbClique(clique, varToCliques))
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
					if (addedEdge._variables.length > 1)
					{
						multiVariateEdges.add(addedEdge);
						transformMap.addDeterministicVariable(addedEdge.makeJointVariable(targetModel));
					}
					prevClique.addEdge(addedEdge);
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
						heap.changePriority(targetClique._heapEntry,  -edge._weight);
					}
				}
			}
		}

		//
		// 6) Merge factors in clique
		//
		
		for (Clique clique : cliques)
		{
			if (clique._variables.length > 0)
			{
				clique._mergedFactor = targetModel.join(clique._variables, clique._factors);
			}
		}
		
		//
		// 7) Add half-edges for variables that are in only one clique and therefore won't be in any
		//    edge created in the previous step.
		//
		
		for (Discrete var : varToCliques.keySet())
		{
			final Set<Clique> cliquesForVar = varToCliques.get(var);
			if (cliquesForVar.size() == 1)
			{
				final Clique clique = Iterables.getOnlyElement(cliquesForVar);
				clique.addEdge(new CliqueEdge(null, clique, var));
			}
		}
		
		//
		// 8) Rewrite factors with multivariate edges
		//
		
		for (Clique clique : cliques)
		{
			clique.joinMultivariateEdges();
		}
		
		//
		// 9) Find orphaned variables and map each to the smallest joint variable that subsumes it
		//
		
		final Map<Discrete, Tuple2<Discrete,Integer>> orphanVarToJointVar = Maps.newLinkedHashMap();
		for (CliqueEdge edge : multiVariateEdges)
		{
			for (int vari = 0, nVars = edge._variables.length; vari < nVars; ++vari)
			{
				final Discrete var = edge._variables[vari];
				
				if (!var.wasVisited())
				{
					var.setVisited();
					if (var.getSiblingCount() == 0)
					{
						final Tuple2<Discrete,Integer> tuple = orphanVarToJointVar.get(var);
						if (tuple == null || tuple.first.getDomain().size() > edge._jointVariable.getDomain().size())
						{
							orphanVarToJointVar.put(var, Tuple2.create(edge._jointVariable, vari));
						}
					}
				}
			}
		}
		
		//
		// 10) Create marginal factors that connects each orphan variable to a joint variable.
		//
		//    TODO: if two or more orphaned variables are attached to the same joint variable, it
		//    can be expressed using a single factor instead of one per variable
		
		for (Entry<Discrete,Tuple2<Discrete,Integer>> entry : orphanVarToJointVar.entrySet())
		{
			final Discrete orphan = entry.getKey();
			final Discrete joint = entry.getValue().first;
			final int subindex= entry.getValue().second;
			final JointDiscreteDomain<?> jointd = (JointDiscreteDomain<?>)joint.getDomain();
			
			targetModel.addFactor(FactorTable.createMarginal(subindex, jointd), orphan, joint);
		}
		
		return transformMap;
	}

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
