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
 * Utility for building a histogram
 */
public class SparseJointHistogram
{
	private HashMap<LinkedList<Integer>, Integer> _indices2prob = new HashMap<LinkedList<Integer>, Integer>();
	private int _total = 0;
	
	public SparseJointHistogram(int dimensions)
	{
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
		Integer tmp = _indices2prob.get(ll);
		if (tmp == null)
			tmp = 0;
		_indices2prob.put(ll, tmp+count);
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
