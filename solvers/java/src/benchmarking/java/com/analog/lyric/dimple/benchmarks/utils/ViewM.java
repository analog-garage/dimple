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

package com.analog.lyric.dimple.benchmarks.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class ViewM implements Iterable<int[]>
{
	private final ArrayM _arrayM;

	private final Indexer[] _indexers;

	public ViewM(ArrayM arrayM, Indexer... indexers)
	{
		_arrayM = arrayM;
		_indexers = indexers;
	}

	public ViewM index(Indexer index)
	{
		Indexer[] indexes = new Indexer[_indexers.length + 1];
		for (int i = 0; i < _indexers.length; i++)
		{
			indexes[i] = _indexers[i];
		}
		indexes[_indexers.length] = index;
		return new ViewM(_arrayM, indexes);
	}

	public ViewM index()
	{
		return index(Indexer.All);
	}

	public ViewM index(int just)
	{
		return index(new Indexer(just));
	}

	public ViewM index(int lowIndex, int highIndex)
	{
		return index(new Indexer(lowIndex, highIndex));
	}

	public ViewM index(int lowIndex, int stride, int highIndex)
	{
		return index(new Indexer(lowIndex, stride, highIndex));
	}

	public Iterator<int[]> iterator()
	{
		int[] dims = _arrayM.getDimensions();
		Indexer[] indexers = getCompleteIndexers();
		for (int i = 0; i < indexers.length; i++)
		{
			if (indexers[i] == Indexer.All)
			{
				indexers[i] = new Indexer(0, dims[i] - 1);
			}
		}
		return new ArrayMCoordinatesIterator(indexers);
	}

	private Indexer[] getCompleteIndexers()
	{
		int[] dims = _arrayM.getDimensions();
		Indexer[] indexers = _indexers;
		if (indexers.length < dims.length)
		{
			Indexer[] completeIndexers = new Indexer[dims.length];
			for (int i = 0; i < indexers.length; i++)
			{
				completeIndexers[i] = indexers[i];
			}
			for (int i = indexers.length; i < dims.length; i++)
			{
				completeIndexers[i] = Indexer.All;
			}
			indexers = completeIndexers;
		}
		return indexers;
	}

	public ArrayM get()
	{
		int[] dims = _arrayM.getDimensions();
		Indexer[] indexers = getCompleteIndexers();
		ArrayList<Integer> orders = new ArrayList<Integer>();
		for (int i = 0; i < indexers.length; i++)
		{
			if (indexers[i] == Indexer.All && dims[i] > 1)
			{
				orders.add(dims[i]);
			}
			else
			{
				int order = indexers[i].order();
				if (order > 1)
				{
					orders.add(order);
				}
			}
		}
		if (orders.size() == 0)
		{
			orders.add(1);
		}
		int[] resultDims = new int[orders.size()];
		for (int i = 0; i < resultDims.length; i++)
		{
			resultDims[i] = orders.get(i);
		}
		ArrayM result = new ArrayM(resultDims);
		Iterator<int[]> dstIterator = result.index(Indexer.All).iterator();
		for (int[] srcIndex : this)
		{
			if (!dstIterator.hasNext())
			{
				throw new IllegalStateException();
			}
			int[] dstIndex = dstIterator.next();
			result.set(_arrayM.get(srcIndex), dstIndex);
		}
		return result;
	}
}
