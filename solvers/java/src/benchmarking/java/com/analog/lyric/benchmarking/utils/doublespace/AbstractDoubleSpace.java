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

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.analog.lyric.benchmarking.utils.functional.BinaryOp;
import com.analog.lyric.benchmarking.utils.functional.FoldFunction;
import com.analog.lyric.benchmarking.utils.functional.Functions;
import com.analog.lyric.benchmarking.utils.functional.IterFunctionWithCoordinates;
import com.analog.lyric.benchmarking.utils.functional.TransformFunction;

/**
 * Provides a skeletal implementation of the DoubleSpace interface.
 */
public abstract class AbstractDoubleSpace implements DoubleSpace
{

	private final Set<Entry> _entrySet = Collections.unmodifiableSet(new AbstractSet<Entry>()
	{

		@Override
		public Iterator<Entry> iterator()
		{
			return new EntryIterator();
		}

		@Override
		public int size()
		{
			return getCardinality();
		}

	});

	protected final JointIndexer _jointIndexer;

	private final Collection<Double> _valuesCollection = Collections.unmodifiableCollection(new AbstractCollection<Double>()
	{

		@Override
		public Iterator<Double> iterator()
		{
			return new ValueIterator();
		}

		@Override
		public int size()
		{
			return getCardinality();
		}

	});

	/**
	 * Constructor that must be invoked by derived classes.
	 * 
	 * @param indexer
	 *            The provided joint indexer describes the domain of the space.
	 */
	protected AbstractDoubleSpace(final JointIndexer indexer)
	{
		_jointIndexer = indexer;
	}

	@Override
	public DoubleSpace add(DoubleSpace rhs)
	{
		return binaryOp(rhs, Functions.add);
	}

	@Override
	public DoubleSpace binaryOp(DoubleSpace rhs, BinaryOp op)
	{
		final int dimensions = getDimensionsCount();
		if (dimensions != rhs.getDimensionsCount())
		{
			throw new IllegalArgumentException("dimensions must match.");
		}
		for (int dimension = 0; dimension < dimensions; dimension++)
		{
			if (getIndexer(dimension).getCardinality() != rhs.getIndexer(dimension).getCardinality())
			{
				throw new IllegalArgumentException("dimensions must match.");
			}
		}
		final Iterator<int[]> it_coords_this = coordinatesSet().iterator();
		final Iterator<int[]> it_coords_that = rhs.coordinatesSet().iterator();
		while (it_coords_this.hasNext() && it_coords_that.hasNext())
		{
			final int[] coords_this = it_coords_this.next();
			final int[] coords_that = it_coords_that.next();
			put(op.apply(get(coords_this), rhs.get(coords_that)), coords_this);
		}
		return this;
	}

	@Override
	public Set<int[]> coordinatesSet()
	{
		return _jointIndexer.coordinatesSet();
	}

	@Override
	public Set<Entry> entrySet()
	{
		return _entrySet;
	}

	@Override
	public <T> T fold(T init, FoldFunction<T> fn)
	{
		T acc = init;
		for (final int[] coordinates : coordinatesSet())
		{
			final double value = get(coordinates);
			acc = fn.apply(acc, value);
		}
		return acc;
	}

	@Override
	public int[] getDimensions()
	{
		final int[] orders = new int[getDimensionsCount()];
		for (int dimension = 0; dimension < orders.length; dimension++)
		{
			orders[dimension] = getIndexer(dimension).getCardinality();
		}
		return orders;
	}

	@Override
	public int getDimensionsCount()
	{
		return _jointIndexer.getDimensionsCount();
	}

	@Override
	public Indexer getIndexer(int dimension)
	{
		return _jointIndexer.getIndexer(dimension);
	}

	@Override
	public void iter(IterFunctionWithCoordinates fn)
	{
		for (final int[] coordinates : coordinatesSet())
		{
			final double value = get(coordinates);
			fn.apply(value, coordinates);
		}
	}

	@Override
	public DoubleSpace transform(TransformFunction transformer)
	{
		for (final int[] coords : coordinatesSet())
		{
			put(transformer.apply(get(coords)), coords);
		}
		return this;
	}

	@Override
	public Collection<Double> values()
	{
		return _valuesCollection;
	}

	@Override
	public DoubleSpace view(Indexer... indexers)
	{
		return new IndexedDoubleSpace(this, indexers);
	}

	private final class EntryIterator implements Iterator<Entry>
	{
		private final Iterator<int[]> _coordinatesIterator;

		public EntryIterator()
		{
			_coordinatesIterator = coordinatesSet().iterator();
		}

		@Override
		public boolean hasNext()
		{
			return _coordinatesIterator.hasNext();
		}

		@Override
		public Entry next()
		{
			final int[] coordinates = _coordinatesIterator.next();
			return new Entry(AbstractDoubleSpace.this, coordinates);
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	private final class ValueIterator implements Iterator<Double>
	{
		private final Iterator<int[]> _coordinatesIterator;

		public ValueIterator()
		{
			_coordinatesIterator = coordinatesSet().iterator();
		}

		@Override
		public boolean hasNext()
		{
			return _coordinatesIterator.hasNext();
		}

		@Override
		public Double next()
		{
			final int[] coordinates = _coordinatesIterator.next();
			final double value = get(coordinates);
			return value;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
