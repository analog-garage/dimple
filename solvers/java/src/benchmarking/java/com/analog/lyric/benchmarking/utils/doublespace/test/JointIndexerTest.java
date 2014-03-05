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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.benchmarking.utils.doublespace.Indexer;
import com.analog.lyric.benchmarking.utils.doublespace.JointIndexer;

public class JointIndexerTest
{
	final Iterable<Indexer> indexers = Arrays.asList(range(0, 2), range(10, 11), range(98, 99));

	@Test
	public void testCardinality()
	{
		final JointIndexer o = new JointIndexer(indexers);
		final int expected = 3 * 2 * 2;
		final int actual = o.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testCoordinatesSet()
	{
		final JointIndexer o = new JointIndexer(indexers);
		final Iterable<int[]> expected = Arrays.asList(new int[][] {
				{
						0, 10, 98
				}, {
						0, 10, 99
				}, {
						0, 11, 98
				}, {
						0, 11, 99
				}, {
						1, 10, 98
				}, {
						1, 10, 99
				}, {
						1, 11, 98
				}, {
						1, 11, 99
				}, {
						2, 10, 98
				}, {
						2, 10, 99
				}, {
						2, 11, 98
				}, {
						2, 11, 99
				}

		});
		final Iterable<int[]> actual = o.coordinatesSet();
		compareIterables(expected, actual);
	}

	@Test
	public void testDimensions()
	{
		final JointIndexer o = new JointIndexer(indexers);
		final int expected = 3;
		final int actual = o.getDimensionsCount();
		assertThat(expected, equalTo(actual));
	}

}
