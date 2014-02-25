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

import static com.analog.lyric.benchmarking.utils.doublespace.IndexerFactory.list;
import static com.analog.lyric.benchmarking.utils.doublespace.test.Utilities.compareIterables;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.benchmarking.utils.doublespace.Indexer;

public class ListIndexerTest
{

	@Test
	public void testListIndexerContents()
	{
		final List<Integer> l = Arrays.asList(1, 4, 5);
		final Indexer actual = list(l);
		compareIterables(l, actual);
	}

	@Test
	public void testListIndexerCount()
	{
		final List<Integer> l = Arrays.asList(1, 4, 5);
		final Indexer i = list(l);
		final int expected = l.size();
		final int actual = i.getCardinality();
		assertThat(actual, equalTo(expected));
	}

	@Test
	public void testPositiveGetNth()
	{
		final List<Integer> l = Arrays.asList(1, 4, 5);
		final Indexer i = list(l);
		final int actual = i.getNth(2);
		final int expected = 5;
		assertThat(actual, equalTo(expected));
	}
}
