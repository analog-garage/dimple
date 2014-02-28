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
import static com.analog.lyric.benchmarking.utils.doublespace.IndexerFactory.sequence;
import static com.analog.lyric.benchmarking.utils.doublespace.test.Utilities.compareIterables;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.benchmarking.utils.doublespace.Indexer;
import com.google.common.collect.Iterables;

public class SequenceIndexerTest
{
	final Indexer a = range(1, 3);
	final Indexer b = range(5, 2, 9);
	final Indexer c = range(3, 7);

	@Test
	public void testSequence0Contents()
	{
		final Indexer actual = sequence();
		final List<Integer> expected = new ArrayList<Integer>();
		compareIterables(expected, actual);
	}

	@Test
	public void testSequence1Contents()
	{
		final Indexer actual = sequence(a);
		final List<Integer> expected = new ArrayList<Integer>();
		Iterables.addAll(expected, a);
		compareIterables(expected, actual);
	}

	@Test
	public void testSequence1Count()
	{
		final Indexer i = sequence(a);
		final int expected = a.getCardinality();
		final int actual = i.getCardinality();
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testSequence2Contents()
	{
		final Indexer actual = sequence(a, b);
		final List<Integer> expected = new ArrayList<Integer>();
		Iterables.addAll(expected, a);
		Iterables.addAll(expected, b);
		compareIterables(expected, actual);
	}

	@Test
	public void testSequence2Count()
	{
		final Indexer i = sequence(a, b);
		final int expected = a.getCardinality() + b.getCardinality();
		final int actual = i.getCardinality();
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testSequence3Contents()
	{
		final Indexer actual = sequence(a, b, c);
		final List<Integer> expected = new ArrayList<Integer>();
		Iterables.addAll(expected, a);
		Iterables.addAll(expected, b);
		Iterables.addAll(expected, c);
		compareIterables(expected, actual);
	}

	@Test
	public void testSequence3Count()
	{
		final Indexer i = sequence(a, b, c);
		final int expected = a.getCardinality() + b.getCardinality() + c.getCardinality();
		final int actual = i.getCardinality();
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testSequence3GetNth()
	{
		final Indexer i = sequence(a, b, c);
		assertThat(i.getNth(0), equalTo(1));
		assertThat(i.getNth(2), equalTo(3));
		assertThat(i.getNth(3), equalTo(5));
		assertThat(i.getNth(5), equalTo(9));
		assertThat(i.getNth(6), equalTo(3));
		assertThat(i.getNth(10), equalTo(7));
	}

	@Test
	public void testSequenceCount()
	{
		final Indexer i = sequence();
		final int expected = 0;
		final int actual = i.getCardinality();
		assertThat(actual, equalTo(expected));
	}
}
