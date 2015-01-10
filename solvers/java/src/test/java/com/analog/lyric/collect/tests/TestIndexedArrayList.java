/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.collect.IndexedArrayList;
import com.analog.lyric.util.test.SerializationTester;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestIndexedArrayList
{
	@Test
	public void test()
	{
		IndexedArrayList<String> list = new IndexedArrayList<>();
		assertInvariants(list);
		assertTrue(list.isEmpty());
		
		assertTrue(list.add("a"));
		assertEquals(0, list.indexOf("a"));
		assertEquals(1, list.size());
		
		assertTrue(list.add("b"));
		assertEquals(1, list.indexOf("b"));
		assertEquals(2, list.size());
		
		assertFalse(list.add("a"));
		assertEquals(2, list.size());
		
		assertInvariants(list);
		
		assertEquals(-1, list.indexOf("c"));
		assertEquals(-1, list.lastIndexOf("c"));
		
		assertTrue(list.add("c"));
		assertEquals(2, list.indexOf("c"));
		
		assertEquals("a", list.set(0,  "d"));
		assertEquals(-1, list.indexOf("a"));
		assertEquals(3, list.size());
		assertEquals("d", list.get(0));
		
		assertInvariants(list);

		assertEquals("c", list.set(2, "d"));
		assertEquals(2, list.size());
		assertEquals("b", list.get(0));
		assertEquals("d", list.get(1));
		
		assertInvariants(list);

		list.clear();
		assertTrue(list.isEmpty());
		
		for (char c = 'a'; c <= 'z'; ++c)
		{
			list.add(c + "");
		}
		
		assertInvariants(list);
		
		for (int i = 0; i < 26; ++i)
		{
			assertEquals((char)('a' + i) + "", list.get(i));
		}
		
		assertTrue(list.remove("d"));
		assertFalse(list.remove("d"));
		assertEquals(25, list.size());
		
		assertInvariants(list);
		
		Set<String> set = new HashSet<>();
		set.add("b");
		set.add("e");
		set.add("g");
		set.add("barf");
		
		assertTrue(list.removeAll(set));
		assertFalse(list.removeAll(set));
		assertInvariants(list);
		assertEquals(22, list.size());
		for (String s : list)
		{
			assertFalse(set.contains(s));
		}
		
		set.add("w");
		set.add("x");
		set.add("y");
		set.add("z");
		assertTrue(list.retainAll(set));
		assertFalse(list.retainAll(set));
		assertEquals(4, list.size());
		assertInvariants(list);
		assertEquals("w", list.get(0));
		assertEquals("z", list.get(3));
		
	}
	
	private <T> void assertInvariants(IndexedArrayList<T> list)
	{
		CollectionTester<T> tester = new CollectionTester<>();
		tester.validateCollection(list);

		int i = 0;
		for (T value : list)
		{
			assertEquals(value, list.set(i, value));
			assertEquals(value, list.get(i));
			assertEquals(i, list.indexOf(value));
			assertEquals(i, list.lastIndexOf(value));
			++i;
		}
		
		ArrayList<T> copy = new ArrayList<>(list);
		assertEquals(copy, list);
		
		IndexedArrayList<T> list2 = new IndexedArrayList<>(copy);
		assertEquals(list, list2);
		
		IndexedArrayList<T> list3 = SerializationTester.clone(list);
		assertEquals(list, list3);
	}
}
