package com.analog.lyric.collect.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.collect.NonNull;

public class NonNullTest
{

	@Test
	public void test()
	{
		// NonNull.list
		List<Integer> list = NonNull.list(null);
		assertNotNull(list);
		assertTrue(list.isEmpty());
		
		ArrayList<Integer> list2 = new ArrayList<Integer>();
		list2.add(42);
		list = NonNull.list(list2);
		assertNotNull(list);
		assertEquals(list,  list2);
		
		// NonNull.map
		Map<Integer, Integer> map = NonNull.map(null);
		assertNotNull(map);
		assertTrue(map.isEmpty());
		
		HashMap<Integer,Integer> map2 = new HashMap<Integer,Integer>();
		map2.put(42, 23);
		map = NonNull.map(map2);
		assertNotNull(map);
		assertEquals(map,  map2);
		
		// NonNull.list
		Set<Integer> set = NonNull.set(null);
		assertNotNull(set);
		assertTrue(set.isEmpty());
		
		HashSet<Integer> set2 = new HashSet<Integer>();
		set2.add(42);
		set = NonNull.set(set2);
		assertNotNull(set);
		assertEquals(set,  set2);
	}

}
