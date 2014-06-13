/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.collect.SkipMap;
import com.analog.lyric.util.misc.NonNullByDefault;

public class SkipMapTest
{

	@Test
	public void test()
	{
		Comparator<Integer> naturalOrder = new Comparator<Integer>() {
			@Override
			@NonNullByDefault(false)
			public int compare(Integer i1, Integer i2)
			{
				return i1.compareTo(i2);
			}
		};

		SkipMapTester<Integer,String> tester = new SkipMapTester<Integer,String>();
		
		SkipMap<Integer,String> map1 = SkipMap.create(naturalOrder);
		assertTrue(map1.isEmpty());
		tester.validateSkipMap(map1);
		
		map1.clear();
		assertTrue(map1.isEmpty());
		
		assertNull(map1.ceilingKey(42));
		assertNull(map1.floorKey(42));
		assertNull(map1.get(42));
		assertFalse(map1.keySet().contains(42));
		assertFalse(map1.values().contains("42"));
		
		Integer k = new Integer(42);
		assertEquals(null, map1.put(k, "43"));
		assertEquals(k, map1.firstKey());
		assertEquals(k, map1.lastKey());
		tester.validateSkipMap(map1);
		
		assertEquals("43", map1.put(k, "42"));
		
		k = 23;
		assertNull(map1.put(k,  "23"));
		
		k = 151;
		assertNull(map1.put(k, "151"));
		assertEquals(3, map1.size());
		tester.validateSkipMap(map1);
		
		assertNull(map1.get(1234));
		assertNull(map1.get2(1234));
		assertNull(map1.get("not a valid key"));
		
		Map.Entry<Integer, String> firstEntry = map1.firstEntry();
		requireNonNull(firstEntry);
		assertEquals("23", firstEntry.getValue());
		assertEquals("23", map1.get(map1.firstKey()));
		firstEntry.setValue("xxx");
		assertEquals("xxx", firstEntry.getValue());
		assertEquals("xxx", map1.get(map1.firstKey()));
		
		
		map1.clear();
		assertTrue(map1.isEmpty());
		
		Random rand = new Random(54321);
		int[] testInput = new int[200];
		for (int i = 0; i < testInput.length; ++i)
		{
			testInput[i] = rand.nextInt();
		}
		
		HashMap<Integer,String> testInputMap = new HashMap<Integer,String>(testInput.length);
		for (int i : testInput)
		{
			String value = Integer.toString(i);
			assertEquals(testInputMap.put(i, value), map1.put(i,  value));
		}
		tester.validateSkipMap(map1);
		
		map1.entrySet().clear();
		assertTrue(map1.isEmpty());
		tester.validateSkipMap(map1);
		
		map1.putAll(testInputMap);
		tester.validateSkipMap(map1);
		
		map1.keySet().clear();
		assertTrue(map1.isEmpty());
		tester.validateSkipMap(map1);
		
		map1.putAll(testInputMap);
		
		map1.values().clear();
		assertTrue(map1.isEmpty());
		tester.validateSkipMap(map1);
		
		map1.putAll(testInputMap);
		
		// Test keySet iterator
		SkipMap.KeySet<Integer, String> keySet = map1.keySet();
		SkipMap.KeyIterator<Integer,String> keyIter = keySet.iterator();
		while (keyIter.next() != null) {}
		keyIter.reset();
		
		while (keyIter.next() != null)
		{
			keyIter.remove();
		}
		assertTrue(map1.isEmpty());
		
		assertNotSame(keyIter, keySet.iterator());
		keyIter.release();
		assertSame(keyIter, keySet.iterator());
		
		// Test values iterator
		map1.putAll(testInputMap);
		SkipMap.ValueCollection<Integer,String> values = map1.values();
		SkipMap.ValueIterator<Integer,String> valueIter = values.iterator();
		while (valueIter.next() != null) {}
		valueIter.reset();
		
		while (valueIter.next() != null)
		{
			valueIter.remove();
		}
		assertTrue(map1.isEmpty());
		
		assertNotSame(valueIter, values.iterator());
		valueIter.release();
		assertSame(valueIter, values.iterator());
	
		// Test entries iterator
		map1.putAll(testInputMap);
		SkipMap.EntrySet<Integer,String> entries = map1.entrySet();
		SkipMap.Iterator<Integer,String> iter = entries.iterator();
		while (iter.next() != null) {}
		iter.reset();
		
		assertNull(iter.getLastKey());
		while (true)
		{
			Map.Entry<Integer,String> entry = iter.next();
			if (entry == null)
			{
				break;
			}
			
			assertEquals(entry.getKey(), iter.getLastKey());
			iter.remove();
		}
		assertNull(iter.getLastKey());
		assertTrue(map1.isEmpty());
		
		assertNotSame(iter, entries.iterator());
		iter.release();
		assertSame(iter, entries.iterator());
		
		map1.putAll(testInputMap);
		iter = entries.iterator();
		SkipMap.Iterator<Integer,String> iter2 = entries.iterator();
		assertNotSame(iter, iter2);
		
		for (int i = map1.size(); i > 0; --i)
		{
			Map.Entry<Integer,String> entry = iter.next();
			requireNonNull(entry);
			String value = iter2.nextValue();
			Integer key = iter2.getLastKey();
			assertEquals(value, entry.getValue());
			assertEquals(key, entry.getKey());
		}
		
	}

}
