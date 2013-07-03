package com.analog.lyric.dimple.solvers.sumproduct.pseudolikelihood;

import java.util.LinkedList;



public class NodeInfo
{
	private SparseJointHistogram _hist;
	private SparseJointDistribution _dist;
	private int [] _indices;

	public NodeInfo(int [] indices)
	{
		_indices = indices;
		_hist = new SparseJointHistogram(_indices.length);
	}
	
	protected LinkedList<Integer> indicesToRelevantOnes(int [] indices)
	{
		LinkedList<Integer> ll = new LinkedList<Integer>();
		
		for (Integer i : _indices)
			ll.add(indices[i]);
		return ll;

	}
	
	public void addSample(int [] allDataIndices)
	{
		_hist.add(indicesToRelevantOnes(allDataIndices));				
		_dist = null;
	}
	

	public SparseJointDistribution getDistribution()
	{
		if (_dist == null)
			_dist = _hist.getDistribution();
		
		return _dist;
	}
	
	public void reset()
	{
		_hist = new SparseJointHistogram(_indices.length);
		_dist = null;
	}
}