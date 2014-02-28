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

import java.util.ArrayList;
import java.util.List;

class IndexedDoubleSpace extends AbstractDoubleSpace
{
	private static JointIndexer computeJointIndexer(final DoubleSpace viewOf, final Indexer... originalIndexers)
	{
		final List<Indexer> tmp_indexers = new ArrayList<Indexer>();
		for (int dimension = 0; dimension < viewOf.getDimensionsCount(); dimension++)
		{
			Indexer indexer;
			if (dimension < originalIndexers.length)
			{
				indexer = originalIndexers[dimension];
			}
			else
			{
				indexer = viewOf.getIndexer(dimension);
			}
			final int order = indexer.getCardinality();
			if (order > 1)
			{
				tmp_indexers.add(new StrideIndexer(0, order - 1));
			}
		}
		return new JointIndexer(tmp_indexers);
	}

	private final int _cardinality;

	private final int[] _indexThrough;

	private final Indexer[] _originalIndexers;

	private final DoubleSpace _viewOf;

	/**
	 * Constructs a view of a given DoubleSpace.
	 * 
	 * @param viewOf
	 *            The DoubleSpace of which this represents a view.
	 * @param originalIndexers
	 *            The indexes into the given DoubleSpace that specify the
	 *            portion of it that this view represents.
	 */
	public IndexedDoubleSpace(DoubleSpace viewOf, Indexer... originalIndexers)
	{
		super(computeJointIndexer(viewOf, originalIndexers));
		_cardinality = _jointIndexer.getCardinality();
		_viewOf = viewOf;
		_originalIndexers = originalIndexers;
		_indexThrough = new int[_viewOf.getDimensionsCount()];
		for (int dimension = 0; dimension < _viewOf.getDimensionsCount(); dimension++)
		{
			Indexer indexer;
			if (dimension < originalIndexers.length)
			{
				indexer = originalIndexers[dimension];
			}
			else
			{
				indexer = _viewOf.getIndexer(dimension);
			}
			if (indexer.getCardinality() > 1)
			{
				_indexThrough[dimension] = -1;
			}
			else
			{
				_indexThrough[dimension] = indexer.getNth(0);
			}
		}
	}

	private int[] convertCoordinates(int[] coordinates)
	{
		final int dimensions = _viewOf.getDimensionsCount();
		final int[] result = new int[dimensions];
		int i_coordinate = 0;
		for (int dimension = 0; dimension < dimensions; dimension++)
		{
			result[dimension] = _indexThrough[dimension];
			if (_indexThrough[dimension] < 0)
			{
				result[dimension] = _originalIndexers[i_coordinate].getNth(coordinates[i_coordinate]);
				i_coordinate += 1;
			}
		}
		return result;
	}

	@Override
	public double get(int... coordinates)
	{
		final int[] underCoordinates = convertCoordinates(coordinates);
		return _viewOf.get(underCoordinates);
	}

	@Override
	public int getCardinality()
	{
		return _cardinality;
	}

	@Override
	public void put(double value, int... coordinates)
	{
		final int[] underCoordinates = convertCoordinates(coordinates);
		_viewOf.put(value, underCoordinates);
	}

}
