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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/*
 * Class for building a distribution from a histogram.
 */
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
