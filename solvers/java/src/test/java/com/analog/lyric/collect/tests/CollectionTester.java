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
		assertFalse(collection.contains(obj));
		
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
		assertFalse(collection.containsAll(all));
		
		Object[] all2 = collection.toArray();
		assertEquals(all2.length, collection.size());
		for (Object elt : all2)
		{
			assertTrue(collection.contains(elt));
		}
		
		assertFalse(collection.remove(obj));
	}
}
