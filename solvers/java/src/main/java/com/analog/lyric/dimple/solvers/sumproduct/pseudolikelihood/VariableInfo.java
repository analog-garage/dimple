package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.util.misc.MapList;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;


public class VariableInfo extends NodeInfo
{
	private HashSet<LinkedList<Integer>> _uniqueSamplesPerValue = new HashSet<LinkedList<Integer>>();
	private int _thisIndex;
	private VariableBase [] _neighbors;
	private Discrete _var;
	private HashMap<LinkedList<Integer>,double[]> _neighbors2distributions = new HashMap<LinkedList<Integer>, double[]>();
	private HashMap<Factor, int []> _factor2mapping = new java.util.HashMap<Factor, int[]>();

	public static VariableInfo createVariableInfo(VariableBase var,HashMap<VariableBase,Integer> var2index)
	{
		VariableBase [] neighbors = getNeighbors(var);
		int [] indices = getIndices(var,var2index);
		return new VariableInfo(var,indices,neighbors,var2index);
	}
	
	public void reset()
	{
		_uniqueSamplesPerValue.clear();
		invalidateDistributions();
		super.reset();
	}
	
	private VariableInfo(VariableBase var,int [] indices, VariableBase [] neighbors,
			HashMap<VariableBase,Integer> var2index)
	{
		super(indices);
		_thisIndex = var2index.get(var);
		_neighbors = neighbors;
		_var = (Discrete)var;
		
		Factor [] fs = var.getFactorsFlat();
		for (Factor f : fs)
		{
			VariableList vs = f.getVariables();
			
			int [] mapping = new int[vs.size()];
			
			for (int i = 0; i < vs.size(); i++)
			{
				VariableBase tmp = vs.getByIndex(i);
				if (var == tmp)
					mapping[i] = _neighbors.length;
				else
				{
					boolean found = false;
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
				//mapping[i] = ???
			}
			_factor2mapping.put(f, mapping);
		}
		
		//I should create the necessary info so that I can later 
	}
	
	public VariableBase getVariable()
	{
		return _var;
	}
	
	public void invalidateDistributions()
	{
		_neighbors2distributions.clear();
	}

	public int getFactorTableIndex(Factor f, int domainValue, LinkedList<Integer> domainVals)
	{
		Integer [] domainValues = new Integer[domainVals.size()];
		domainValues = domainVals.toArray(domainValues);

		return getFactorTableIndex(f, domainValue, domainValues);
	}
	public int getFactorTableIndex(Factor f, int domainValue, Integer [] domainVals)
	{
		int [] mapping = _factor2mapping.get(f);
		int [] indices = new int[f.getVariables().size()];
		
		for (int j = 0; j < mapping.length; j++)
		{
			if (mapping[j] >= domainVals.length)
				indices[j] = domainValue;
			else
				indices[j] = domainVals[mapping[j]];
		}
		
		int index = f.getFactorTable().getWeightIndexFromTableIndices(indices);
		return index;
	}
	
	public double getProb(int varIndex,LinkedList<Integer> neighbors)
	{
		double pneighbors = getDistribution().get(neighbors);
		
		Integer [] domainValues = new Integer[neighbors.size()];
		domainValues = neighbors.toArray(domainValues);
		
		if (!_neighbors2distributions.containsKey(neighbors))
		{
			double [] distribution = new double[_var.getDiscreteDomain().getElements().length];
			double normalizer = 0;
			for (int i = 0; i < distribution.length; i++)
			{
				double total = 1;
				for (Factor f : _factor2mapping.keySet())
				{
					int index = getFactorTableIndex(f,i,domainValues);
					
					//TODO: is this a prob or energy?
					double weight = f.getFactorTable().getWeights()[index];
					total *= weight;
				}
				distribution[i] = total;
				normalizer += total;
			}
			for (int i = 0; i < distribution.length; i++)
				distribution[i] /= normalizer;
			
			_neighbors2distributions.put(neighbors,distribution);
		}		
		
		return _neighbors2distributions.get(neighbors)[varIndex]*pneighbors;
	}
	
	
	private static VariableBase [] getNeighbors(VariableBase var)
	{
		MapList<INode> ml = var.getRootGraph().depthFirstSearchFlat(var, 2);
		HashSet<VariableBase> neighbors = new HashSet<VariableBase>();
		for (INode n : ml)
		{
			if (n.isVariable() && n != var)
				neighbors.add(n.asVariable());
		}
		VariableBase [] retVal = new VariableBase[neighbors.size()]; 
		return neighbors.toArray(retVal);
	}

	
	private static int [] getIndices(VariableBase var,HashMap<VariableBase,Integer> var2index)
	{
		VariableBase [] neighbors = getNeighbors(var);
		int [] indices = new int[neighbors.length];
		int i = 0;
		for (VariableBase n : neighbors)
		{
			indices[i] = var2index.get(n);
			i++;
		}
		
		return indices;
	}
	
	
	@Override
	public void addSample(int [] allDataIndices)
	{
		super.addSample(allDataIndices);		
		LinkedList<Integer> otherIndices = indicesToRelevantOnes(allDataIndices);		
		_uniqueSamplesPerValue.add(otherIndices);		
	}
	
	public Set<LinkedList<Integer>> getUniqueSamples()
	{
		return _uniqueSamplesPerValue;
	}
	
}