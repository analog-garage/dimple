package com.analog.lyric.dimple.benchmarks.utils;

import java.util.Iterator;

public class ArrayMCoordinatesIterator implements Iterator<int[]>
{
	private final Indexer[] _indexers;
	private final int[] _current;
	private boolean _first;

	public ArrayMCoordinatesIterator(Indexer[] indexers)
	{
		_indexers = indexers;
		if (indexers.length > 0 && indexers[0].indexAfter(-1) != -1)
		{
			_current = new int[indexers.length];
			for (int d = 0; d < indexers.length; d++)
			{
				_current[d] = indexers[d].indexAfter(-1);
			}
		}
		else
		{
			_current = null;
		}
		_first = true;
	}

	public boolean hasNext()
	{
		if (_first && _current != null)
		{
			return true;
		}
		for (int d = 0; d < _indexers.length; d++)
		{
			if (_indexers[d].indexAfter(_current[d]) != -1)
			{
				return true;
			}
		}
		return false;
	}

	public int[] next()
	{
		if (_first)
		{
			_first = false;
			return _current;
		}
		else
		{
			for (int d = _indexers.length - 1; d >= 0; d--)
			{
				int proposal = _indexers[d].indexAfter(_current[d]);
				if (proposal != -1)
				{
					_current[d] = proposal;
					return _current;
				}
				_current[d] = _indexers[d].indexAfter(-1);
			}
			return null;
		}
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
