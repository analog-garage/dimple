package com.analog.lyric.collect.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class MapTester<K,V>
{
	protected SetTester<Map.Entry<K,V>> _entrySetTester;
	protected SetTester<K> _keySetTester;
	protected CollectionTester<V> _valueCollectionTester;
	
	public MapTester(
		SetTester<Map.Entry<K,V>> entrySetTester,
		SetTester<K> keySetTester,
		CollectionTester<V> valueCollectionTester)
	{
		this._entrySetTester = entrySetTester;
		this._keySetTester = keySetTester;
		this._valueCollectionTester = valueCollectionTester;
	}
	
	public MapTester()
	{
		this(new SetTester<Map.Entry<K, V>>(),
			new SetTester<K>(),
			new CollectionTester<V>());
	}
	
	public void validateMap(Map<K,V> map)
	{
		assertTrue(map.size() >= 0);
		assertEquals(map.size() == 0, map.isEmpty());
		
		Set<K> keySet = map.keySet();
		this._keySetTester.validateSet(keySet);
		assertEquals(keySet.size(), map.size());
		
		Set<Map.Entry<K,V>> entrySet = map.entrySet();
		this._entrySetTester .validateSet(entrySet);
		assertEquals(entrySet.size(), map.size());
		
		Collection<V> values = map.values();
		this._valueCollectionTester.validateCollection(values);
		assertEquals(values.size(), map.size());
	
		for (Map.Entry<K, V> entry : entrySet)
		{
			K key = entry.getKey();
			V value = entry.getValue();
			
			assertEquals( value, map.get(key) );
			assertTrue( map.containsKey(key) );
			assertTrue( map.containsValue(value) );
			assertTrue( keySet.contains(key) );
			assertTrue( values.contains(value) );
		}
	}
}
