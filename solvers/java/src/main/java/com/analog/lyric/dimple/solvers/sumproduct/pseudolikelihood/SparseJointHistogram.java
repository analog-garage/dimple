package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;


public class SparseJointHistogram
{
	private int _dims;
	private HashMap<LinkedList<Integer>, Integer> _indices2prob = new HashMap<LinkedList<Integer>, Integer>();
	private int _total = 0;
	
	public SparseJointHistogram(int dimensions)
	{
		_dims = dimensions;
	}
	public void add(LinkedList<Integer> ll)
	{
		add(ll,1);
	}
	
	public Set<LinkedList<Integer>> getKeys()
	{
		return _indices2prob.keySet();
	}
	
	public int get(LinkedList<Integer> ll)
	{
		return _indices2prob.get(ll);
	}
	
	public void add(LinkedList<Integer> ll, int count)
	{
		_indices2prob.put(ll, _indices2prob.get(ll)+count);
		_total+=count;
	}
	public int getTotal()
	{
		return _total;
	}
	
	public SparseJointDistribution getDistribution()
	{
		return new SparseJointDistribution(this);
	}
}