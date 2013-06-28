package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.HashMap;
import java.util.LinkedList;


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
	
	public double get(LinkedList<Integer> ll)
	{
		return _indices2prob.get(ll);
	}
	
	public double get(int [] indices)
	{
		LinkedList<Integer> ll = new LinkedList<Integer>();
		for (Integer i : indices)
			ll.add(i);
		return get(ll);
	}
}
