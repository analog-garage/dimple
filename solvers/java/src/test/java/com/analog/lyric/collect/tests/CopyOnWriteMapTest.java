package com.analog.lyric.collect.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Test;

import com.analog.lyric.collect.CopyOnWriteConcurrentMap;
import com.analog.lyric.collect.CopyOnWriteMap;

public class CopyOnWriteMapTest
{
	private final CopyOnWriteMapTester<String,Object> tester = new CopyOnWriteMapTester<String,Object>();

	@Test
	public void test()
	{
		testCopyOf(new HashMap<String,Object>());
		testCopyOf(new TreeMap<String,Object>());
		testCopyOfConcurrent(new ConcurrentHashMap<String,Object>());
		testCopyOfConcurrent(new ConcurrentSkipListMap<String,Object>());
	}
	
	private void testCopyOf(Map<String,Object> map)
	{
		testCopyOf(map, new CopyOnWriteMap<String,Object>(map));
	}
	
	private void testCopyOf(Map<String,Object> map, CopyOnWriteMap<String,Object> copyMap)
	{
		tester.validateCopyOnWriteMap(copyMap);
		
		assertSame(map, copyMap.originalMap());
		assertTrue(copyMap.isEmpty());
		assertFalse(copyMap.wasCopied());
		
		map.put("foo", 42);
		assertEquals(42, copyMap.get("foo"));
		
		assertNull(copyMap.put("bar", 23));
		assertEquals(23, copyMap.get("bar"));
		assertNull(map.get("bar"));
		assertEquals(42, copyMap.get("foo"));
		assertTrue(copyMap.wasCopied());
		
		assertEquals(42, copyMap.put("foo",  24));
		assertEquals(24, copyMap.get("foo"));
		assertEquals(42, map.get("foo"));
		
		tester.validateCopyOnWriteMap(copyMap);

		assertEquals(24, copyMap.remove("foo"));
		assertNull(copyMap.get("foo"));
		assertEquals(42, map.get("foo"));
		
		copyMap.clear();
		assertEquals(0, copyMap.size());
		assertEquals(1, map.size());
		assertNull(copyMap.get("bar"));
		
		copyMap.revertToOriginalMap();
		assertFalse(copyMap.wasCopied());
		tester.assertMapEquals(map, copyMap);
		
		map.put("baz", 12);
		assertEquals(12, copyMap.get("baz"));
		
		assertEquals(42, copyMap.remove("foo"));
		assertTrue(copyMap.wasCopied());
		assertNull(copyMap.get("foo"));
		assertEquals(42, map.get("foo"));
		
		copyMap.revertToOriginalMap();
		
		assertFalse(map.isEmpty());
		copyMap.clear();
		assertTrue(copyMap.isEmpty());
		assertFalse(map.isEmpty());
		
		copyMap.revertToOriginalMap();
		assertFalse(copyMap.isEmpty());
		
		Map<String,Object> map2 = new HashMap<String,Object>();
		map2.put("a", 1);
		map2.put("b", 2);
		map2.put("c", 3);
		
		copyMap.putAll(map2);
		for (Map.Entry<String,Object> entry : map2.entrySet())
		{
			assertEquals(entry.getValue(), copyMap.get(entry.getKey()));
			assertFalse(map.containsKey(entry.getKey()));
		}
	}
	
	void testCopyOfConcurrent(ConcurrentMap<String, Object> map)
	{
		testCopyOfConcurrent(map, new CopyOnWriteConcurrentMap<String,Object>(map));
	}
	
	void testCopyOfConcurrent(ConcurrentMap<String,Object> map, CopyOnWriteConcurrentMap<String, Object> copyMap)
	{
		testCopyOf(map, copyMap);
		
		map.clear();
		
		copyMap.revertToOriginalMap();
		assertFalse(copyMap.wasCopied());
		
		map.put("foo", 23);
		assertEquals(23, copyMap.putIfAbsent("foo", 42));
		assertTrue(copyMap.wasCopied());
		assertEquals(23, copyMap.get("foo"));
		copyMap.revertToOriginalMap();
		
		assertEquals(null, copyMap.putIfAbsent("bar", 42));
		assertTrue(copyMap.wasCopied());
		assertEquals(42, copyMap.get("bar"));
		assertFalse(map.containsKey("bar"));
		assertEquals(42, copyMap.putIfAbsent("bar", 1000));
		assertEquals(42, copyMap.get("bar"));
		
		assertFalse(copyMap.remove("bar", 12));
		assertEquals(42, copyMap.get("bar"));
		assertTrue(copyMap.remove("bar", 42));
		assertNull(copyMap.get("bar"));
		assertTrue(copyMap.remove("foo", 23));
		assertNull(copyMap.get("foo"));
		copyMap.revertToOriginalMap();
		
		assertNull(copyMap.replace("baz", 10));
		assertTrue(copyMap.wasCopied());
		assertEquals(23, copyMap.replace("foo", 42));
		assertEquals(42, copyMap.get("foo"));
		assertEquals(23, map.get("foo"));
		
		assertFalse(copyMap.replace("foo", 10, 11));
		assertEquals(42, copyMap.get("foo"));
		assertTrue(copyMap.replace("foo", 42, 11));
		assertEquals(11, copyMap.get("foo"));
		copyMap.revertToOriginalMap();
		assertFalse(copyMap.replace("foo", 10, 11));
		assertTrue(copyMap.wasCopied());
		assertEquals(23, map.get("foo"));
	}
}
