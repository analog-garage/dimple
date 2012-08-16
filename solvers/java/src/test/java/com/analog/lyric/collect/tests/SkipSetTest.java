/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.collect.SkipSet;

public class SkipSetTest
{

	@Test
	public void test()
	{
		Comparator<Integer> naturalOrder = new Comparator<Integer>() {
			@Override
			public int compare(Integer i1, Integer i2)
			{
				return i1.compareTo(i2);
			}
		};
		
		SkipSetTester<Integer> tester = new SkipSetTester<Integer>();
		SkipSet<Integer> set1 = new SkipSet<Integer>(naturalOrder);
		tester.validateSkipSet(set1);
		
		assertEquals(set1.comparator(), naturalOrder);
		assertTrue(set1.isEmpty());
		set1.clear();
		assertTrue(set1.isEmpty());
		
		set1.add(42);
		assertEquals(set1.size(), 1);
		tester.validateSkipSet(set1);
		
		set1.add(23);
		set1.add(151);
		set1.add(23);
		assertEquals(set1.size(), 3);
		assertEquals(set1.first(), new Integer(23));
		assertEquals(set1.last(), new Integer(151));
		assertEquals(null, set1.ceiling(Integer.MAX_VALUE));
		assertEquals(null, set1.floor(Integer.MIN_VALUE));
		tester.validateSkipSet(set1);
		
		Integer[] inArray = new Integer[set1.size() - 1];
		Integer[] outArray = set1.toArray(inArray);
		assertNotSame(inArray, outArray);
		assertEquals(outArray.length, set1.size());
		
		assertFalse(set1.remove(null));
		
		for (Integer i : set1)
		{
			assertTrue(set1.remove(i));
			assertFalse(set1.remove(i));
		}
		assertTrue(set1.isEmpty());
		
		Random rand = new Random(1234);
		int[] testInput = new int[1000];
		for (int i = 0; i < testInput.length; ++i)
		{
			testInput[i] = rand.nextInt();
		}
		
		HashSet<Integer> hashSet = new HashSet<Integer>(testInput.length);
		SkipSet<Integer> set2 = new SkipSet<Integer>(naturalOrder);
		for (int i : testInput)
		{
			boolean shouldBeAdded = hashSet.add(i);
			boolean wasAdded = set2.add(i);
			assertEquals(shouldBeAdded, wasAdded);
		}
		tester.validateSkipSet(set2);
		
		assertTrue(set2.containsAll(hashSet));
		
		for (int i = 0; i < 10; ++i)
		{
			Integer first = set2.first();
			Integer firstPolled = set2.pollFirst();
			assertEquals(first, firstPolled);
			
			Integer next = set2.first();
			assertTrue(first.intValue() < next.intValue());
			
			Integer last = set2.last();
			Integer lastPolled = set2.pollLast();
			assertEquals(last, lastPolled);
			
			Integer nextLast = set2.last();
			assertTrue(nextLast.intValue() < last.intValue());
		}
		tester.validateSkipSet(set2);
		
		assertEquals(set2.size(), testInput.length - 20);
		
		HashSet<Integer> retainList = new HashSet<Integer>(20);
		for (int i = 0; i < 20; ++i)
		{
			retainList.add(testInput[rand.nextInt(testInput.length)]);
		}
		set2.retainAll(retainList);
		tester.validateSkipSet(set2);

		assertTrue(retainList.containsAll(set2));
		assertTrue(set2.size() <= retainList.size());
		
		assertTrue(set2.addAll(hashSet));
		assertFalse(set2.addAll(set2));
		tester.validateSkipSet(set2);
		
		set2.clear();
		assertTrue(set2.isEmpty());
		tester.validateSkipSet(set2);
		
		assertTrue(set2.addAll(hashSet));
		
		assertTrue(set2.removeAll(retainList));
		for (Integer i : retainList)
		{
			assertFalse(set2.contains(i));
		}
		assertFalse(set2.removeAll(retainList));
		
		assertTrue(set1.addAll(set2));
		assertTrue(set2.removeAll(set1));
		assertTrue(set2.isEmpty());
		
		for (int i = 100; i > 0; --i)
		{
			assertTrue(set2.add(new Integer(i)));
		}
		tester.validateSkipSet(set2);
	}
}
