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

import static com.analog.lyric.benchmarking.utils.doublespace.IndexerFactory.range;
import static com.analog.lyric.benchmarking.utils.doublespace.test.Utilities.compareIterables;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpace;
import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpace.Entry;
import com.analog.lyric.benchmarking.utils.doublespace.DoubleSpaceFactory;
import com.analog.lyric.benchmarking.utils.doublespace.JointIndexer;
import com.google.common.collect.Iterables;

public class BottomDoubleSpaceTest
{
	@Test
	public void test2DReadback()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(2, 3);
		int value = 0;
		for (int y = 0; y < 2; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				s.put(value, y, x);
				value += 1;
			}
		}
		int expected = 0;
		for (int y = 0; y < 2; y++)
		{
			for (int x = 0; x < 3; x++)
			{
				final int actual = (int) s.get(y, x);
				assertThat(actual, equalTo(expected));
				expected += 1;
			}
		}
	}

	@Test
	public void test3DEntrySet()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(2, 3, 4);
		int value = 0;
		for (final int[] coordinates : s.coordinatesSet())
		{
			s.put(value, coordinates);
			value += 1;
		}

		final List<Entry> actual = new ArrayList<Entry>();
		Iterables.addAll(actual, s.entrySet());

		final JointIndexer indexer = new JointIndexer(range(0, 1), range(0, 2), range(0, 3));
		int i = 0;
		value = 0;
		for (final int[] coordinates : indexer.coordinatesSet())
		{
			assertThat(actual.get(i).getValue(), equalTo((double) value));
			assertThat(actual.get(i).getCoordinates(), equalTo(coordinates));
			value += 1;
			i += 1;
		}
	}

	@Test
	public void test3DValues()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(2, 3, 4);
		int value = 0;
		for (final int[] coordinates : s.coordinatesSet())
		{
			s.put(value, coordinates);
			value += 1;
		}

		final List<Double> actual = new ArrayList<Double>();
		Iterables.addAll(actual, s.values());

		final List<Double> expected = new ArrayList<Double>();
		for (int i = 0; i < 2 * 3 * 4; i++)
		{
			expected.add((double) i);
		}

		compareIterables(expected, actual);
	}

	@Test
	public void test4DCoordinateSet()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(2, 3, 3, 3);
		final JointIndexer indexer = new JointIndexer(range(0, 1), range(0, 2), range(0, 2), range(0, 2));
		compareIterables(indexer.coordinatesSet(), s.coordinatesSet());
	}

	@Test
	public void test4DReadback()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(2, 3, 3, 3);
		int value = 0;
		final JointIndexer indexer = new JointIndexer(range(0, 1), range(0, 2), range(0, 2), range(0, 2));
		for (final int[] coordinates : indexer.coordinatesSet())
		{
			s.put(value, coordinates);
			value += 1;
		}
		int expected = 0;
		for (final int[] coordinates : indexer.coordinatesSet())
		{
			final int actual = (int) s.get(coordinates);
			assertThat(actual, equalTo(expected));
			expected += 1;
		}
	}

	@Test
	public void testCardinality()
	{
		final DoubleSpace s = DoubleSpaceFactory.create(2, 3, 4);
		assertThat(2 * 3 * 4, equalTo(s.getCardinality()));
	}
}
