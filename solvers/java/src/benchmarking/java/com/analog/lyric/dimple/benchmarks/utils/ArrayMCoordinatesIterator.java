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
