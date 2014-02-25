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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.benchmarking.utils.doublespace.Indexer;

public class StrideIndexerTest
{
	@Test
	public void negativeStride1HasCorrectCount()
	{
		final Indexer i = range(7, 5);
		final int expected = 3;
		final int actual = i.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void negativeStride1HasProperSequence()
	{
		final Indexer actual = range(7, 5);
		final List<Integer> expected = Arrays.asList(7, 6, 5);
		compareIterables(expected, actual);
	}

	@Test
	public void negativeStrideMatchedHasCorrectCount()
	{
		final Indexer i = range(11, -2, 5);
		final int expected = 4;
		final int actual = i.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void negativeStrideMatchedHasProperSequence()
	{
		final Indexer actual = range(11, -2, 5);
		final List<Integer> expected = Arrays.asList(11, 9, 7, 5);
		compareIterables(expected, actual);
	}

	@Test
	public void negativeStrideMismatchedHasCorrectCount()
	{
		final Indexer i = range(12, -2, 5);
		final int expected = 4;
		final int actual = i.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void negativeStrideMismatchedHasProperSequence()
	{
		final Indexer actual = range(12, -2, 5);
		final List<Integer> expected = Arrays.asList(12, 10, 8, 6);
		compareIterables(expected, actual);
	}

	@Test
	public void positiveStride1HasCorrectCount()
	{
		final Indexer i = range(5, 7);
		final int expected = 3;
		final int actual = i.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void positiveStride1HasProperSequence()
	{
		final Indexer actual = range(5, 7);
		final List<Integer> expected = Arrays.asList(5, 6, 7);
		compareIterables(expected, actual);
	}

	@Test
	public void positiveStrideMatchedHasCorrectCount()
	{
		final Indexer i = range(5, 2, 11);
		final int expected = 4;
		final int actual = i.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void positiveStrideMatchedHasProperSequence()
	{
		final Indexer actual = range(5, 2, 11);
		final List<Integer> expected = Arrays.asList(5, 7, 9, 11);
		compareIterables(expected, actual);
	}

	@Test
	public void positiveStrideMismatchedHasCorrectCount()
	{
		final Indexer i = range(5, 2, 12);
		final int expected = 4;
		final int actual = i.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void positiveStrideMismatchedHasProperSequence()
	{
		final Indexer actual = range(5, 2, 12);
		final List<Integer> expected = Arrays.asList(5, 7, 9, 11);
		compareIterables(expected, actual);
	}

	@Test
	public void testJustHasCount1()
	{
		final Indexer i = just(5);
		final int expected = 1;
		final int actual = i.getCardinality();
		assertThat(expected, equalTo(actual));
	}

	@Test
	public void testJustHasProperSequence()
	{
		final Indexer actual = just(5);
		final List<Integer> expected = Arrays.asList(5);
		compareIterables(expected, actual);
	}

	@Test
	public void testNegativeGetNth()
	{
		final Indexer i = range(18, -3, 12);
		final int actual = i.getNth(2);
		final int expected = 12;
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testPositiveGetNth()
	{
		final Indexer i = range(12, 3, 18);
		final int actual = i.getNth(2);
		final int expected = 18;
		assertThat(actual, equalTo(expected));
	}

}
