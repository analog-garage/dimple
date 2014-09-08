/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import static java.util.Objects.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.IMapList;

/*
 * The VariableInfo object stores an empirical distribution of all variables that it is
 * directly connected to through neighboring factors (excluding itself)
 * 
 * Additionally, it provides a method to calculate the joint probability of a particular state
 * of variables using p(joint) = p(x|neighbors)*p(neighbors)
 */
public class VariableInfo extends NodeInfo
{
	private HashSet<LinkedList<Integer>> _uniqueSamplesPerValue = new HashSet<LinkedList<Integer>>();
	private Variable [] _neighbors;
	private Discrete _var;
	private HashMap<LinkedList<Integer>,double[]> _neighbors2distributions = new HashMap<LinkedList<Integer>, double[]>();
	private HashMap<Factor, int []> _factor2mapping = new java.util.HashMap<Factor, int[]>();

	//This is a factory method for creating a variable.  This is necessary since
	//the parent class's constructor requires we already know the mapping from all variables
	//to variables of interest.
	public static VariableInfo createVariableInfo(Variable var,
			HashMap<Variable,Integer> var2index)
	{
		
		//Find the variables neighboring variables.
		Variable [] neighbors = getNeighbors(var);
		
		//Get the indices of interest for this variable.
		int [] indices = getIndices(var,var2index);
		
		return new VariableInfo(var,indices,neighbors,var2index);
	}
	

	private VariableInfo(Variable var,int [] indices, Variable [] neighbors,
			HashMap<Variable,Integer> var2index)
	{
		super(indices);
		
		_neighbors = neighbors;
		_var = (Discrete)var;
		
		//For every factor we will build a mapping from the factor's variable index
		//to an index into the full list of neighbors.
		
		Factor [] fs = var.getFactorsFlat();
		for (Factor f : fs)
		{
			final int nVars = f.getSiblingCount();
			int [] mapping = new int[nVars];
			
			for (int i = 0; i < nVars; i++)
			{
				Variable tmp = f.getSibling(i);
				
				if (var == tmp)
					//This is a special case
					mapping[i] = _neighbors.length;
				else
				{
					boolean found = false;
					
					//Silly linear search.  Fix this eventually.
					for (int j = 0; j < neighbors.length; j++)
					{
						if (neighbors[j] == tmp)
						{
							mapping[i] = j;
							found = true;
							break;
						}
					}
					if (! found)
						throw new RuntimeException("ack, this should never happen");
				}
			}
			_factor2mapping.put(f, mapping);
		}
		
	}
	
	//Cleanup when reset is called so this can be reused.
	@Override
	public void reset()
	{
		_uniqueSamplesPerValue.clear();
		invalidateDistributions();
		super.reset();
	}
	
	
	public Variable getVariable()
	{
		return _var;
	}
	
	//Distributions are cached but, as a result, we have to invalidate the cache at the right
	//time.
	public void invalidateDistributions()
	{
		_neighbors2distributions.clear();
	}

	//Given a factor, a domain value, and domain values for the neighbors, calculate the
	//factor table index.
	public int getFactorTableIndex(Factor f, int domainValue, LinkedList<Integer> domainVals)
	{
		Integer [] domainValues = new Integer[domainVals.size()];
		domainValues = domainVals.toArray(domainValues);

		return getFactorTableIndex(f, domainValue, domainValues);
	}
	
	//Retrieve the joint probability of a variable and its neighbors.
	public double getProb(int varIndex,LinkedList<Integer> neighbors)
	{
		//First get the empirical probability of a neighbor.
		double pneighbors = getDistribution().get(neighbors);
		
		
		Integer [] domainValues = new Integer[neighbors.size()];
		domainValues = neighbors.toArray(domainValues);
		
		//Cache the distribution of p(x|neighbors) since we have to calculate them all to
		//correctly normalize things.
		if (!_neighbors2distributions.containsKey(neighbors))
		{
			//initialize
			double [] distribution = new double[_var.getDiscreteDomain().size()];
			double normalizer = 0;
			
			//calculate the probability for each setting of this var.
			for (int i = 0; i < distribution.length; i++)
			{
				double total = 1;
				
				//For every factor
				for (Factor f : _factor2mapping.keySet())
				{
					//retrieve the factor table index from this vars domain
					//and the neighbor domains
					int index = getFactorTableIndex(f,i,domainValues);
					
					//retrieve the weight
					double weight = f.getFactorTable().getWeightsSparseUnsafe()[index];
										
					//TODO: should probably do this in the log domain
					//multiply it in
					total *= weight;
				}
				
				//save the value
				distribution[i] = total;
				
				//add to the normalizing constant
				normalizer += total;
			}
			
			//normalize
			for (int i = 0; i < distribution.length; i++)
				distribution[i] /= normalizer;
			
			//save
			_neighbors2distributions.put(neighbors,distribution);
		}
		
		return _neighbors2distributions.get(neighbors)[varIndex]*pneighbors;
	}
	
	


	// In addition to bulding up the empirical distribution, save all of the sample data
	// relevant to this variable.  This ends up duplicating the data across variables
	// so this trade-off for speed will cost memory.  There is probably a better way.
	// There is some compression due to storing these as a set.
	@Override
	public void addSample(int [] allDataIndices)
	{
		super.addSample(allDataIndices);
		LinkedList<Integer> otherIndices = indicesToRelevantOnes(allDataIndices);
		_uniqueSamplesPerValue.add(otherIndices);
	}

	//Returns the set of unique samples.
	public Set<LinkedList<Integer>> getUniqueSamples()
	{
		return _uniqueSamplesPerValue;
	}

	//Converts the domain value for this variable plus the neighbor domain values
	//to a factor table index.
	private int getFactorTableIndex(Factor f, int domainValue, Integer [] domainVals)
	{
		int [] mapping = _factor2mapping.get(f);
		int [] indices = new int[f.getSiblingCount()];
		
		for (int j = 0; j < mapping.length; j++)
		{
			if (mapping[j] >= domainVals.length)
				//deal with the special case.
				indices[j] = domainValue;
			else
				indices[j] = domainVals[mapping[j]];
		}
		
		int index = f.getFactorTable().sparseIndexFromIndices(indices);
		return index;
	}
	
	//uses a breadth first search to find all neighboring variables.
	private static Variable [] getNeighbors(Variable var)
	{
		IMapList<INode> ml = requireNonNull(var.getRootGraph()).depthFirstSearchFlat(var, 2);
		HashSet<Variable> neighbors = new HashSet<Variable>();
		for (INode n : ml)
		{
			if (n.isVariable() && n != var)
				neighbors.add(n.asVariable());
		}
		Variable [] retVal = new Variable[neighbors.size()];
		return neighbors.toArray(retVal);
	}

	
	//uses a breadth first search to find all neighboring variables and then builds
	//the map.
	private static int [] getIndices(Variable var,HashMap<Variable,Integer> var2index)
	{
		Variable [] neighbors = getNeighbors(var);
		int [] indices = new int[neighbors.length];
		int i = 0;
		for (Variable n : neighbors)
		{
			indices[i] = var2index.get(n);
			i++;
		}
		
		return indices;
	}
	
}
