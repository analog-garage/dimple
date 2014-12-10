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

import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.collect.WeakIntHashMap;
import com.analog.lyric.collect.WeakLongHashMap;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestWeakPrimitiveHashMap
{
	@Test
	public void testInt()
	{
		WeakIntHashMap<Object> map = new WeakIntHashMap<>();
		assertTrue(map.isEmpty());
		assertInvariants(map);
		
		Object s = new int[400];
		assertTrue(map.put(42, s));
		assertEquals(s, map.get(42));
		assertTrue(map.containsKey(42));
		assertEquals(1, map.size());
		assertInvariants(map);
		
		assertFalse(map.removeKey(23));
		assertEquals(1, map.size());
		assertTrue(map.removeKey(42));
		assertTrue(map.isEmpty());
		
		map.put(23, s);
		map.put(42, s);
		assertEquals(2, map.size());
		assertInvariants(map);
		
		// GC and sleep to try to get jvm to put entries on the reference queue, but there is
		// no guarantee when that will happen.
		s = null;
		System.gc();
		try
		{
			Thread.sleep(10);
		}
		catch (InterruptedException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		System.gc();
		assertInvariants(map);
		assertNull(map.get(23));
	}
	
	@Test
	public void testLong()
	{
		WeakLongHashMap<Object> map = new WeakLongHashMap<>();
		assertTrue(map.isEmpty());
		assertInvariants(map);
		
		Object s = new long[400];
		assertTrue(map.put(42, s));
		assertEquals(s, map.get(42));
		assertTrue(map.containsKey(42));
		assertEquals(1, map.size());
		assertInvariants(map);
		
		assertFalse(map.removeKey(23));
		assertEquals(1, map.size());
		assertTrue(map.removeKey(42));
		assertTrue(map.isEmpty());
		
		map.put(23, s);
		map.put(42, s);
		assertEquals(2, map.size());
		assertInvariants(map);
		
		// GC and sleep to try to get jvm to put entries on the reference queue, but there is
		// no guarantee when that will happen.
		s = null;
		System.gc();
		try
		{
			Thread.sleep(10);
		}
		catch (InterruptedException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		System.gc();
		assertInvariants(map);
		assertNull(map.get(23));
	}

	private <T> void assertInvariants(WeakIntHashMap<T> map)
	{
		assertTrue(map.size() >= 0);
		assertEquals(map.isEmpty(), map.size() == 0);
		
		int[] keys = map.keys();
		assertEquals(map.size(), keys.length);
		
		for (int key : keys)
		{
			assertEquals(map.containsKey(key), map.get(key) != null);
		}
		
		if (keys.length > 0)
		{
			Arrays.sort(keys);
		
			assertFalse(map.containsKey(keys[keys.length - 1] + 1));
			assertNull(map.get(keys[0] - 1));
		}
		else
		{
			assertFalse(map.containsKey(0));
			assertNull(map.get(0));
		}
	}

	private <T> void assertInvariants(WeakLongHashMap<T> map)
	{
		assertTrue(map.size() >= 0);
		assertEquals(map.isEmpty(), map.size() == 0);
		
		long[] keys = map.keys();
		assertEquals(map.size(), keys.length);
		
		for (long key : keys)
		{
			assertEquals(map.containsKey(key), map.get(key) != null);
		}
		
		if (keys.length > 0)
		{
			Arrays.sort(keys);
		
			assertFalse(map.containsKey(keys[keys.length - 1] + 1));
			assertNull(map.get(keys[0] - 1));
		}
		else
		{
			assertFalse(map.containsKey(0));
			assertNull(map.get(0));
		}
	}
}
