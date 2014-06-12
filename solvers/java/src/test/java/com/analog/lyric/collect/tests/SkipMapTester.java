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

import java.util.Map;
import java.util.NoSuchElementException;

import com.analog.lyric.collect.SkipMap;

public class SkipMapTester<K, V> extends MapTester<K, V>
{
	public SkipMapTester()
	{
		super();
	}
	
	@Override
	public void validateMap(Map<K,V> map)
	{
		if (map instanceof SkipMap)
		{
			this.validateSkipMap((SkipMap<K, V>) map);
		}
		else
		{
			super.validateMap(map);
		}
	}
	
	public void validateSkipMap(SkipMap<K,V> map)
	{
		super.validateMap(map);
		
		Map.Entry<K,V> firstEntry = map.firstEntry();
		Map.Entry<K,V> lastEntry = map.lastEntry();
		
		assertEquals(map.isEmpty(), firstEntry == null);
		assertEquals(map.isEmpty(), lastEntry == null);

		if (map.isEmpty())
		{
			try
			{
				map.firstKey();
				fail("Expected NoSuchElementException");
			}
			catch (NoSuchElementException ex) {}
			
			try
			{
				map.lastKey();
				fail("Expected NoSuchElementException");
			}
			catch (NoSuchElementException ex) {}

			assertNull(map.pollFirstEntry());
			assertNull(map.pollLastEntry());
		}
		else
		{
			assertEquals(map.firstKey(), requireNonNull(firstEntry).getKey());
			assertEquals(map.lastKey(), requireNonNull(lastEntry).getKey());
		}
		
		Map.Entry<K, V> prevEntry = null;
		for (K key : map.keySet())
		{
			assertTrue(map.containsKey2(key));
			
			V value = map.get(key);
			assertEquals(value, map.get2(key));
			
			Map.Entry<K, V> entry = map.floorEntry(key);
			assertNotNull(entry);
			assertEquals(key, entry.getKey());
			assertEquals(value, entry.getValue());
			assertEquals(key, map.floorKey(key));
			
			entry = map.ceilingEntry(key);
			requireNonNull(entry);
			assertEquals(key, entry.getKey());
			assertEquals(value, entry.getValue());
			assertEquals(key, map.ceilingKey(key));
			
			if (prevEntry == null)
			{
				assertEquals(key, map.firstKey());
				assertNull(map.lowerEntry(key));
				assertNull(map.lowerKey(key));
			}
			else
			{
				K prevKey = prevEntry.getKey();
				assertEquals(prevKey, map.lowerKey(key));
				assertEquals(key, map.higherKey(prevKey));
				
				Map.Entry<K,V> lowerEntry = map.lowerEntry(key);
				requireNonNull(lowerEntry);
				assertEquals(prevKey, lowerEntry.getKey());
				assertEquals(prevEntry.getValue(), lowerEntry.getValue());
				
				Map.Entry<K,V> higherEntry = map.higherEntry(prevKey);
				requireNonNull(higherEntry);
				assertEquals(key, higherEntry.getKey());
				assertEquals(value, higherEntry.getValue());
			}
			
			prevEntry = entry;
		}
		
		if (prevEntry != null)
		{
			K lastKey = prevEntry.getKey();
			
			assertNull(map.higherKey(lastKey));
		}
	}
}
