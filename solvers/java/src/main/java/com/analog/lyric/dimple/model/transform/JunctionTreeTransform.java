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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BinaryHeap;
import com.analog.lyric.collect.IHeap;
import com.analog.lyric.collect.Tuple2;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTableIterator;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Ordering;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Stats;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.primitives.Ints;

public class JunctionTreeTransform
{
	/*-------
	 * State
	 */

	private int _nEliminationAttempts = 10;
	private boolean _useConditioning = true;
	private final VariableCost[] _costFunctions = {};
	
	/**
	 * Orders factors by decreasing order of non-zero factor table entries.
	 */
	private static final Comparator<Factor> _factorTableComparator = new Comparator<Factor>() {
		@Override
		public int compare(Factor f1, Factor f2)
		{
			return Ints.compare(f2.getFactorTable().countNonZeroWeights(), f1.getFactorTable().countNonZeroWeights());
		}
	};
	
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
		private final Discrete _eliminatedVariable;
		
		/**
		 * Original factors for eliminated variable.
		 */
		private final ArrayList<Factor> _factors;
		
		/**
		 * Variables in clique
		 */
		private final ArrayList<Discrete> _variables = new ArrayList<Discrete>();
		
		private final ArrayList<CliqueEdge> _edges = new ArrayList<CliqueEdge>();
		
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
		
		private Clique(Discrete eliminatedVariable)
		{
			_eliminatedVariable = eliminatedVariable;
			_variables.add(eliminatedVariable);
			_factors = new ArrayList<Factor>(eliminatedVariable.getSiblingCount());
		}
		
		/*---------
		 * Methods
		 */
		
		private void addEdge(CliqueEdge edge)
		{
			_hasMultiVariableEdge |= edge._variables.length > 1;
			_edges.add(edge);
		}
		
		private void addFactor(Factor factor)
		{
			_factors.add(factor);
		}
		
		private void addVariable(Discrete neighbor)
		{
			_variables.add(neighbor);
		}
		
		private void joinMultivariateEdges()
		{
			if (!_hasMultiVariableEdge)
				return;
			
			final int nEdges = _edges.size();
			
			// Build mapping from new to old indices.
			final Discrete[] newVariables = new Discrete[nEdges];
			final DiscreteDomain[] newDomains = new DiscreteDomain[nEdges];
			final int[][] newFromOld = new int[nEdges][];
			final int[][] scratchIndices = new int[nEdges][];
			
			for (int edgei = 0; edgei < nEdges; ++edgei)
			{
				final CliqueEdge edge = _edges.get(edgei);
				final int nVars = edge._variables.length;
				final int[] a = new int[nVars];
				
				newVariables[edgei] = edge._jointVariable;
				newDomains[edgei] = edge._jointVariable.getDiscreteDomain();
				for (int vari = 0; vari < nVars; ++vari)
				{
					a[vari] = _variables.indexOf(edge._variables[vari]);
				}
				newFromOld[edgei] = a;
				if (nVars > 1)
				{
					scratchIndices[edgei] = new int[nVars];
				}
			}
			
			// Compute new factor table energies and indices.
			final IFactorTable oldFactorTable = _mergedFactor.getFactorTable();
			final int nEntries = oldFactorTable.countNonZeroWeights();

			final int[][] indices = new int[nEntries][];
			final double[] energies = new double[nEntries];
			
			final IFactorTableIterator oldIter = oldFactorTable.iterator();
			while (oldIter.advance())
			{
				final int[] oldIndices = oldIter.indicesUnsafe();
				final int[] newIndices = new int[nEdges];
				
				for (int edgei = 0; edgei < nEdges; ++edgei)
				{
					final CliqueEdge edge = _edges.get(edgei);
					final int nVars = edge._variables.length;
					final int[] map = newFromOld[edgei];
					
					if (nVars == 1)
					{
						newIndices[edgei] = oldIndices[map[0]];
					}
					else
					{
						final int[] scratch = scratchIndices[edgei];
						for (int vari = 0; vari < nVars; ++vari)
						{
							scratch[vari] = oldIndices[map[vari]];
						}
						newIndices[edgei] = ((JointDiscreteDomain<?>)newDomains[edgei]).getIndexFromIndices(scratch);
					}
					
					final int si = oldIter.sparseIndex();
					energies[si] = oldIter.energy();
					indices[si] = newIndices;
				}
			}
			
			// Create the new table
			final IFactorTable newFactorTable = FactorTable.create(newDomains);
			newFactorTable.setEnergiesSparse(indices, energies);

			// Remove the old factor
			FactorGraph graph = _mergedFactor.getParentGraph();
			graph.remove(_mergedFactor);
			
			// Create new factor attached to edge variables
			graph.addFactor(newFactorTable, newVariables);
		}
		
		private void sortFactors()
		{
			Collections.sort(_factors, _factorTableComparator);
		}
		
		private void sortVariables()
		{
			Collections.sort(_variables, _variableComparator);
		}
		
		private boolean updateBestEdge(CliqueEdge incomingEdge)
		{
			if (_bestEdge == null || _bestEdge._weight > incomingEdge._weight)
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
			return _variables.size();
		}
	}
	
	/**
	 * Represents an edge between two cliques/factors. Will be represented as a single
	 * variable in the new graph.
	 */
	private static class CliqueEdge
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
		
		private CliqueEdge(Clique from, Clique to, Discrete[] variables)
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
		}
		
		private void makeJointVariable(FactorGraph targetModel)
		{
			final Discrete[] edgeVars = _variables;
			final int nEdgeVars = edgeVars.length;
			
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
					jointName.append(edgeVars[i].getName());
				}
				jointVar.setName(jointName.toString());
				targetModel.addVariables(jointVar);
				_jointVariable = jointVar;
			}
		}
	}
	
	/*---------
	 * Methods
	 */

	public FactorGraphTransformMap transform(FactorGraph model)
	{
		//
		// 1) Determine an elimination order
		//
		
		final Ordering eliminationOrder = buildEliminationOrder(model);
		final Stats orderStats = eliminationOrder.stats;
		
		// If elimination order introduces no edges, graph is already a tree. Done.
		if (eliminationOrder.stats.alreadyGoodForFastExactInference())
		{
			return null;
		}
		
		//
		// 2) Make copy of the factor graph
		//
		
		final ArrayList<VariableBase> variables = eliminationOrder.variables;
		final int nVariables = variables.size();

		final BiMap<Node,Node> old2new = HashBiMap.create(nVariables * 2);
		final FactorGraph targetModel = model.copyRoot(old2new);
		
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
		
		if (orderStats.factorsWithDuplicateVariables() > 0)
		{
			// FIXME
			throw DimpleException.unsupported("factors with duplicate variables");
		}
		
		//
		// 4) "Eliminate" variables and create cliques.
		//

		final int nCliques = nVariables - nConditioned;
		final Map<VariableBase, Clique> cliqueMap = new LinkedHashMap<VariableBase, Clique>(nCliques);

		// Create a clique for each remaining variable
		for (int i = nConditioned; i < nVariables; ++i)
		{
			final Discrete var = (Discrete) old2new.get(variables.get(i));
			cliqueMap.put(var, new Clique(var));
		}
		
		final Collection<Clique> cliques = cliqueMap.values();
		
		// Add neighboring variables to each clique
		Clique maxClique = null;
		int totalCliqueSize = 0;
		for (Clique clique : cliques)
		{
			final Discrete var = clique._eliminatedVariable;
			// Mark variable as "eliminated". Note that there is no need to clear the mark
			// at the start because all of the variables are newly created.
			var.setMarked();
			
			for (int i = 0, endi = var.getSiblingCount(); i < endi; ++i)
			{
				final Factor factor = var.getSibling(i);
				if (!factor.isMarked())
				{
					factor.setMarked();
					clique.addFactor(factor);
					for (int j = 0, endj = factor.getSiblingCount(); j < endj; ++j)
					{
						final Discrete neighborVar = factor.getSibling(j).asDiscreteVariable();
						if (!neighborVar.isMarked())
						{
							// Add to clique
							clique.addVariable(neighborVar);
						}
					}
				}
			}
			
			clique.sortFactors();
			clique.sortVariables();
			
			// Update clique statistics
			totalCliqueSize += clique.size();
			if (maxClique == null || clique.size() > maxClique.size())
			{
				maxClique = clique;
			}
		}
		final int averageCliqueSize = (int) Math.round((double)totalCliqueSize / (double)nCliques);

		//
		// 5) Merge factors in clique
		//
		
		for (Clique clique : cliques)
		{
			// Be polite and clear the mark bit.
			clique._eliminatedVariable.clearMarked();
			clique._mergedFactor = targetModel.join(_variableComparator, ArrayUtil.copy(Factor.class, clique._factors));
		}
		
		//
		// 6) Build mapping from variables to set of cliques that contain them.
		//
		
		final SetMultimap<Discrete, Clique> varToCliques = HashMultimap.create(nCliques, averageCliqueSize);
		for (Clique clique : cliques)
		{
			for (Discrete neighbor : clique._variables)
			{
				varToCliques.put(neighbor, clique);
			}
		}

		//
		// 7) Use Prim's algorithm to build max spanning tree over clique graph where the edge weight
		//    is the number of variables in common between the two cliques along each edge.
		//
		final IHeap<Clique> heap = new BinaryHeap<Clique>(cliqueMap.size());
		
		heap.deferOrderingForBulkAdd(nCliques);
		for (Clique clique : cliques)
		{
			clique._heapEntry = heap.offer(clique, Double.POSITIVE_INFINITY);
		}
		heap.changePriority(maxClique._heapEntry, Double.NEGATIVE_INFINITY);
		
		// Edges with more than one variable
		List<CliqueEdge> multiVariateEdges = new ArrayList<CliqueEdge>(nCliques - 1);
		
		while (!heap.isEmpty())
		{
			final Clique clique = heap.poll();
			clique._inSpanningTree = true;
			clique._heapEntry = null;
			
			final CliqueEdge bestEdge = clique._bestEdge;
			if (bestEdge != null)
			{
				if (bestEdge._variables.length > 1)
				{
					multiVariateEdges.add(bestEdge);
					bestEdge.makeJointVariable(targetModel);
				}
				bestEdge._from.addEdge(bestEdge);
				bestEdge._to.addEdge(bestEdge);
			}
			
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

		//
		// 8) Rewrite factors with multivariate edges
		//
		
		for (Clique clique : cliques)
		{
			clique.joinMultivariateEdges();
		}
		
		//
		// 10) Find orphaned variables and map each to the smallest joint variable that subsumes it
		//
		
		final Map<Discrete, Tuple2<Discrete,Integer>> orphanVarToJointVar =
			new LinkedHashMap<Discrete, Tuple2<Discrete,Integer>>();
		for (CliqueEdge edge : multiVariateEdges)
		{
			for (int vari = 0, nVars = edge._variables.length; vari < nVars; ++vari)
			{
				final Discrete var = edge._variables[vari];
				
				if (var.isMarked())
				{
					continue;
				}
			
				var.setMarked();
				if (var.getSiblingCount() > 0)
				{
					continue;
				}
				
				final Tuple2<Discrete,Integer> tuple = orphanVarToJointVar.get(var);
				if (tuple == null || tuple.first.getDomain().size() > edge._jointVariable.getDomain().size())
				{
					orphanVarToJointVar.put(var, Tuple2.create(edge._jointVariable, vari));
				}
			}
		}
		
		//
		// 11) Create marginal factors that connects each orphan variable to a joint variable.
		//
		
		for (Entry<Discrete,Tuple2<Discrete,Integer>> entry : orphanVarToJointVar.entrySet())
		{
			final Discrete orphan = entry.getKey();
			final Discrete joint = entry.getValue().first;
			final int subindex= entry.getValue().second;
			final JointDiscreteDomain<?> jointd = (JointDiscreteDomain<?>)joint.getDomain();
			
			targetModel.addFactor(FactorTable.createMarginal(subindex, jointd), orphan, joint);
		}
		
		return new FactorGraphTransformMap(model, targetModel, old2new);
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
		
		return VariableEliminator.generate(model, _useConditioning, _nEliminationAttempts,
			threshold, _costFunctions);
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
		final ListMultimap<Clique, Discrete> cliqueToCommonVars = LinkedListMultimap.create();
		
		
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
		
		for (Clique neighbor : cliqueToCommonVars.keys())
		{
			final List<Discrete> commonVars = cliqueToCommonVars.get(neighbor);
			edges.add(new CliqueEdge(clique, neighbor, ArrayUtil.copy(Discrete.class, commonVars)));
		}
		
		return edges;
	}
	
}
