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

package com.analog.lyric.benchmarking.utils.doublespace.test;

import static com.analog.lyric.benchmarking.utils.doublespace.IndexerFactory.just;
import static com.analog.lyric.benchmarking.utils.doublespace.IndexerFactory.range;
import static com.analog.lyric.benchmarking.utils.doublespace.test.Utilities.compareIterables;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpace;
import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpaceFactory;
import com.analog.lyric.benchmarking.utils.doublespace.JointIndexer;

public class IndexedDoubleSpaceTest
{
	@Test
	public void testPunchthrough()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(4, 4);
		final DoubleSpace v = s.view(range(1, 3), range(0, 2));
		for (final int[] coordinates : v.coordinatesSet())
		{
			v.put(1, coordinates);
		}

		final JointIndexer shouldBeOnes = new JointIndexer(range(1, 3), range(0, 2));
		for (final int[] coordinates : shouldBeOnes.coordinatesSet())
		{
			assertThat(s.get(coordinates), equalTo(1.0));
		}
		final Comparator<int[]> coordinatesComparator = new Comparator<int[]>()
		{

			@Override
			public int compare(int[] arg0, int[] arg1)
			{
				if (arg0.length < arg1.length)
				{
					return -1;
				}
				if (arg0.length != arg1.length)
				{
					return 1;
				}
				if (Arrays.equals(arg0, arg1))
				{
					return 0;
				}
				for (int i = 0; i < arg0.length; i++)
				{
					if (arg0[i] < arg1[i])
					{
						return -1;
					}
				}
				return 1;
			}

		};
		final Set<int[]> shouldBeZeroes = new TreeSet<int[]>(coordinatesComparator);
		shouldBeZeroes.addAll(s.coordinatesSet());
		shouldBeZeroes.removeAll(shouldBeOnes.coordinatesSet());
		for (final int[] coordinates : shouldBeZeroes)
		{
			assertThat(s.get(coordinates), equalTo(0.0));
		}

	}

	@Test
	public void testSimpleCoordinateSet()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(100, 100);
		final DoubleSpace v = s.view(range(10, 20), range(40, 60));
		final JointIndexer indexer = new JointIndexer(range(0, 10), range(0, 20));
		compareIterables(indexer.coordinatesSet(), v.coordinatesSet());
	}

	@Test
	public void testSimpleDimensions()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(100, 100);
		final DoubleSpace v = s.view(range(10, 20), range(40, 60));
		assertThat(v.getDimensionsCount(), equalTo(2));
	}

	@Test
	public void testSkippedAtEndCoordinateSet()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(8, 8, 8);
		final DoubleSpace v = s.view(range(0, 2), range(4, 6));
		final JointIndexer indexer = new JointIndexer(range(0, 2), range(0, 2), range(0, 7));
		compareIterables(indexer.coordinatesSet(), v.coordinatesSet());
	}

	@Test
	public void testSkippedAtEndDimensions()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(8, 8, 8);
		final DoubleSpace v = s.view(range(0, 2), range(4, 6));
		assertThat(v.getDimensionsCount(), equalTo(3));
	}

	@Test
	public void testSkippedMiddleCoordinateSet()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(8, 8, 8);
		final DoubleSpace v = s.view(range(0, 2), just(2), range(4, 7));
		final JointIndexer indexer = new JointIndexer(range(0, 2), range(0, 3));
		compareIterables(indexer.coordinatesSet(), v.coordinatesSet());
	}

	@Test
	public void testSkippedMiddleDimensions()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(8, 8, 8);
		final DoubleSpace v = s.view(range(0, 2), just(2), range(4, 6));
		assertThat(v.getDimensionsCount(), equalTo(2));
	}

}
