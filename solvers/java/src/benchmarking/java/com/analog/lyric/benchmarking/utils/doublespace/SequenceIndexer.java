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

package com.analog.lyric.benchmarking.utils.doublespace;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

class SequenceIndexer implements Indexer
{
	private final List<Indexer> _indexers;

	private final Iterable<Integer> _iterable;

	public SequenceIndexer(Indexer... indexers)
	{
		_indexers = Arrays.asList(indexers);
		_iterable = Iterables.concat(indexers);
	}

	@Override
	public int getCardinality()
	{
		int acc = 0;
		for (final Indexer indexer : _indexers)
		{
			acc += indexer.getCardinality();
		}
		return acc;
	}

	@Override
	public int getNth(int i)
	{
		int p = 0;
		for (final Indexer indexer : _indexers)
		{
			final int order = indexer.getCardinality();
			if (p + order > i)
			{
				return indexer.getNth(i - p);
			}
			p += order;
		}
		throw new IllegalArgumentException();
	}

	@Override
	public Iterator<Integer> iterator()
	{
		return _iterable.iterator();
	}

}
