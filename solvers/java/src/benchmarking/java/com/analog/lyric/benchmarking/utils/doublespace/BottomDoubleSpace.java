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

import com.analog.lyric.benchmarking.utils.functional.BinaryOp;

/**
 * Provides an implementation of the DoubleSpace interface that is backed by a
 * non-sparse array of doubles.
 */
class BottomDoubleSpace extends AbstractDoubleSpace
{
	private static JointIndexer computeJointIndexer(int... dimensions)
	{
		final Indexer[] indexers = new StrideIndexer[dimensions.length];
		for (int i = 0; i < dimensions.length; i++)
		{
			indexers[i] = new StrideIndexer(0, dimensions[i] - 1);
		}
		return new JointIndexer(indexers);
	}

	private final double[] _data;

	public BottomDoubleSpace(DoubleSpace s)
	{
		this(s.getDimensions());
		binaryOp(s, new BinaryOp()
		{

			@Override
			public double apply(double a, double b)
			{
				return b;
			}
		});
	}

	public BottomDoubleSpace(int... dimensions)
	{
		super(computeJointIndexer(dimensions));
		_data = new double[_jointIndexer.getCardinality()];
	}

	private int computeIndex(int... coordinates)
	{
		if (coordinates.length != _jointIndexer.getDimensionsCount())
		{
			throw new IllegalArgumentException("Mismatched quantity of coordinates and dimensions.");
		}
		int index = 0;
		int scale = 1;
		for (int i = 0; i < coordinates.length; i++)
		{
			if (coordinates[i] < 0 || coordinates[i] > _jointIndexer.getIndexer(i).getCardinality() - 1)
			{
				throw new IllegalArgumentException("Coordinate out of bounds.");
			}
			index += coordinates[i] * scale;
			scale *= _jointIndexer.getIndexer(i).getCardinality();
		}
		return index;
	}

	@Override
	public double get(int... coordinates)
	{
		final int p = computeIndex(coordinates);
		return _data[p];
	}

	@Override
	public int getCardinality()
	{
		return _data.length;
	}

	@Override
	public void put(double value, int... coordinates)
	{
		final int p = computeIndex(coordinates);
		_data[p] = value;
	}

}
