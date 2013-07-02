package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


public class SparseJointDistribution
{
	private HashMap<LinkedList<Integer>, Double> _indices2prob = new HashMap<LinkedList<Integer>, Double>();
	
	public SparseJointDistribution(SparseJointHistogram hist)
	{
		for (LinkedList<Integer> ll : hist.getKeys())
		{
			double prob = ((double)hist.get(ll)) / hist.getTotal();
			_indices2prob.put(ll,prob);
		}
	}
	
	public Set<LinkedList<Integer>> getNonZeroKeys()
	{
		return _indices2prob.keySet();
	}
	
	public double get(LinkedList<Integer> ll)
	{
		Double tmp =  _indices2prob.get(ll);
		if (tmp == null)
			return 0;
		else
			return tmp;
	}
	
	public double get(int [] indices)
	{
		LinkedList<Integer> ll = new LinkedList<Integer>();
		for (Integer i : indices)
			ll.add(i);
		return get(ll);
	}
}
