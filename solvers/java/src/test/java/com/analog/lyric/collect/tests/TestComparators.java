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

package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import org.junit.Test;

import com.analog.lyric.collect.Comparators;
import com.analog.lyric.util.misc.NonNullByDefault;

public class TestComparators
{
	@Test
	public void test()
	{
		//
		// Test Comparators.fromCollection
		//
		
		assertNull(Comparators.fromCollection(new ArrayList<Object>()));
		assertNull(Comparators.fromCollection(new TreeSet<Integer>()));
		
		Comparator<? super Integer> comparator = new Comparator<Integer>() {
			@Override
			@NonNullByDefault(false)
			public int compare(Integer arg0, Integer arg1)
			{
				return arg1.compareTo(arg0);
			}
		};
		
		TreeSet<Integer> set = new TreeSet<Integer>(comparator);
		assertSame(comparator, Comparators.fromCollection(set));
		
		//
		// Test Comparators.lexicalIntArray
		//
		
		Comparator<int[]> lexical = Comparators.lexicalIntArray();
		assertTrue(lexical.compare(new int[] {1,2,3}, new int[] {1,3,2}) < 0);
		assertTrue(lexical.compare(new int[] {1,3,2}, new int[] {1,2,3}) > 0);
		assertEquals(0, lexical.compare(new int[] {1,2,3,4}, new int[] {1,2,3,4}));
		assertTrue(lexical.compare(new int[] {4,3,2}, new int[] {20, 1}) > 0);
		
		//
		// Test Comparators.reverseLexicalIntArray
		//
		
		Comparator<int[]> rlexical = Comparators.reverseLexicalIntArray();
		assertTrue(rlexical.compare(new int[] {1,2,3}, new int[] {1,3,2}) > 0);
		assertTrue(rlexical.compare(new int[] {1,3,2}, new int[] {1,2,3}) < 0);
		assertEquals(0, rlexical.compare(new int[] {1,2,3,4}, new int[] {1,2,3,4}));
		assertTrue(rlexical.compare(new int[] {4,3,2}, new int[] {20, 1}) > 0);
	}
}
