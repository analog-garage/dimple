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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CollectionTester<T>
{
	/** Validates invariants on collection without calling any mutating methods. */
	public void validateCollection(Collection<T> collection)
	{
		assertTrue(collection.size() >= 0);
		assertEquals(collection.size() == 0, collection.isEmpty());
		
		Object obj = new Object() {};
		try
		{
			assertFalse(collection.contains(obj));
		}
		catch (ClassCastException ex)
		{
			// Ignore - collection may throw cast exception for mismatched type.
		}
		
		List<Object> all = new ArrayList<Object>(collection.size());
		int size = 0;
		for (T elt : collection)
		{
			++size;
			all.add(elt);
			assertTrue(collection.contains(elt));
		}
		assertEquals(size, collection.size());
		
		assertTrue(collection.containsAll(collection));
		assertTrue(collection.containsAll(all));
		
		all.add(obj);
		try
		{
			assertFalse(collection.containsAll(all));
		}
		catch (ClassCastException ex)
		{
			// Ignore - collection does not have to support wrong key type.
		}
		
		Object[] all2 = collection.toArray();
		Object[] all3 = new Object[all2.length];
		assertSame(all3, collection.toArray(all3));
		assertEquals(all2.length, collection.size());
		for (Object elt : all2)
		{
			assertTrue(collection.contains(elt));
		}
		
		assertArrayEquals(all2, all3);
		
		try
		{
			assertFalse(collection.remove(obj));
		}
		catch (UnsupportedOperationException ex)
		{
			// Ignore - ok for collection to not implement remove.
		}
	}
	
	public void assertCollectionEquals(Collection<T> c1, Collection<T> c2)
	{
		assertEquals(c1.size(), c2.size());
		assertTrue(c1.containsAll(c2));
		assertTrue(c2.containsAll(c1));
	}
}
