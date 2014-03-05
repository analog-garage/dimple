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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;

/**
 * Combines indexers to produce multidimensional indexes.
 */
public class JointIndexer
{
	private static int computeCardinality(List<Indexer> indexers)
	{
		int result = 1;
		for (final Indexer indexer : indexers)
		{
			result *= indexer.getCardinality();
		}
		return result;
	}

	private final int _cardinality;

	private final Set<int[]> _coordinatesSet = Collections.unmodifiableSet(new AbstractSet<int[]>()
	{

		@Override
		public Iterator<int[]> iterator()
		{
			return new CoordinatesIterator();
		}

		@Override
		public int size()
		{
			return getCardinality();
		}

	});

	private final int[] _dimensions;

	private final List<Indexer> _indexers;

	/**
	 * Constructs a joint indexer given an array of indexers.
	 * 
	 * @param indexers
	 *            The individual indexers, one for each dimension to index.
	 */
	public JointIndexer(Indexer... indexers)
	{
		this(Arrays.asList(indexers));
	}

	/**
	 * Constructs a joint indexer given an Iterable of indexers.
	 * 
	 * @param indexers
	 *            The individual indexers, one for each dimension to index.
	 */
	public JointIndexer(Iterable<Indexer> indexers)
	{
		_indexers = new ArrayList<Indexer>();
		Iterables.addAll(_indexers, indexers);
		_cardinality = computeCardinality(_indexers);
		_dimensions = new int[_indexers.size()];
		for (int i = 0; i < _dimensions.length; i++)
		{
			_dimensions[i] = _indexers.get(i).getCardinality();
		}
	}

	/**
	 * Gets a set of all joint indexes, representing the cross product of the
	 * individual indexers.
	 */
	public Set<int[]> coordinatesSet()
	{
		return _coordinatesSet;
	}

	/**
	 * Gets the quantity of joint indexes.
	 */
	public int getCardinality()
	{
		return _cardinality;
	}

	/**
	 * Gets an array containing the cardinality of each of the dimensions.
	 */
	public int[] getDimensions()
	{
		return _dimensions.clone();
	}

	/**
	 * Gets the quantity of dimensions.
	 */
	public int getDimensionsCount()
	{
		return _dimensions.length;
	}

	/**
	 * Gets the nth represented index.
	 * 
	 * @param n
	 *            The zero-based position of the desired index.
	 */
	public Indexer getIndexer(int n)
	{
		return _indexers.get(n);
	}

	private class CoordinatesIterator implements Iterator<int[]>
	{
		private int[] _current;

		private final List<Iterator<Integer>> _iterators;

		public CoordinatesIterator()
		{
			final int dimensions = getDimensionsCount();
			_iterators = new ArrayList<Iterator<Integer>>(dimensions);
			for (int i = 0; i < dimensions; i++)
			{
				_iterators.add(_indexers.get(i).iterator());
			}
		}

		@Override
		public boolean hasNext()
		{
			final int dimensions = getDimensionsCount();
			for (int i = dimensions - 1; i >= 0; i--)
			{
				if (_iterators.get(i).hasNext())
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public int[] next()
		{
			final int dimensions = getDimensionsCount();
			if (_current == null)
			{
				_current = new int[dimensions];
				for (int i = dimensions - 1; i >= 0; i--)
				{
					final Iterator<Integer> it = _iterators.get(i);
					if (it.hasNext())
					{
						_current[i] = it.next();
					}
				}
			}
			else
			{
				for (int i = dimensions - 1; i >= 0; i--)
				{
					final Iterator<Integer> it = _iterators.get(i);
					if (it.hasNext())
					{
						_current[i] = it.next();
						for (int j = i + 1; j < dimensions; j++)
						{
							final Iterator<Integer> it_j = _indexers.get(j).iterator();
							_iterators.set(j, it_j);
							if (it_j.hasNext())
							{
								_current[j] = it_j.next();
							}
							else
							{
								throw new InternalError();
							}
						}
						break;
					}
				}

			}
			return _current.clone();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

}
