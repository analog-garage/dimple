package com.analog.lyric.dimple.benchmarks.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class ViewM implements Iterable<int[]>
{
	private final ArrayM _arrayM;

	private final Indexer[] _indexers;

	public ViewM(ArrayM arrayM, Indexer... slices)
	{
		_arrayM = arrayM;
		_indexers = slices;
	}

	public ViewM slice(Indexer slice)
	{
		Indexer[] slices = new Indexer[_indexers.length + 1];
		for (int i = 0; i < _indexers.length; i++)
		{
			slices[i] = _indexers[i];
		}
		slices[_indexers.length] = slice;
		return new ViewM(_arrayM, slices);
	}

	public ViewM slice()
	{
		return slice(Indexer.All);
	}

	public ViewM slice(int just)
	{
		return slice(new Indexer(just));
	}

	public ViewM slice(int lowIndex, int highIndex)
	{
		return slice(new Indexer(lowIndex, highIndex));
	}

	public ViewM slice(int lowIndex, int stride, int highIndex)
	{
		return slice(new Indexer(lowIndex, stride, highIndex));
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
		Iterator<int[]> dstIterator = result.slice(Indexer.All).iterator();
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
