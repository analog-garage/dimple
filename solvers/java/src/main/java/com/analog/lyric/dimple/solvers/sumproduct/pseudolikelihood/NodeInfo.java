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

import java.util.LinkedList;

import org.eclipse.jdt.annotation.Nullable;

/*
 * Used to store information about Factors and Variables.
 * There is a one-to-one relationship between NodeInfos and variables and factors.
 * They store empirical distributions of variables of interest given a set of samples.
 */
public class NodeInfo
{
	private SparseJointHistogram _hist;
	private @Nullable SparseJointDistribution _dist;
	private int [] _indices;

	public NodeInfo(int [] indices)
	{
		//Store the indices of interest
		_indices = indices;
		
		//Create a histogram object
		_hist = new SparseJointHistogram(_indices.length);
	}
	
	//Add one sample from the data set.
	public void addSample(int [] allDataIndices)
	{
		//picks off only the indices of interest from all of the indices
		//add this to the histogram.
		_hist.add(indicesToRelevantOnes(allDataIndices));
		
		//invalidate the distribution.
		_dist = null;
	}
	
	//Retrieve a distribution from the histogram.
	public SparseJointDistribution getDistribution()
	{
		SparseJointDistribution dist = _dist;

		if (dist == null)
			dist = _dist = _hist.getDistribution();
		
		return dist;
	}
	
	//Reset counting.
	public void reset()
	{
		_hist = new SparseJointHistogram(_indices.length);
		_dist = null;
	}
	
	//builds a linked list (for hashing) of the relevant indices.
	protected LinkedList<Integer> indicesToRelevantOnes(int [] indices)
	{
		LinkedList<Integer> ll = new LinkedList<Integer>();
		
		for (Integer i : _indices)
			ll.add(indices[i]);
		return ll;

	}
	
}
