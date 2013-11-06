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

import java.util.Collection;
import java.util.HashMap;
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
		assertTrue(map.equals(map));
		
		Set<K> keySet = map.keySet();
		this._keySetTester.validateSet(keySet);
		assertEquals(keySet.size(), map.size());
		assertFalse(map.equals(keySet));
		
		Set<Map.Entry<K,V>> entrySet = map.entrySet();
		this._entrySetTester .validateSet(entrySet);
		assertEquals(entrySet.size(), map.size());
		
		Collection<V> values = map.values();
		this._valueCollectionTester.validateCollection(values);
		assertEquals(values.size(), map.size());
	
		int expectedHash = 0;
		K key = null;
		V value = null;
		for (Map.Entry<K, V> entry : entrySet)
		{
			key = entry.getKey();
			value = entry.getValue();
			
			assertEquals(key.hashCode() ^ value.hashCode(), entry.hashCode());
			
			expectedHash += entry.hashCode();
			
			assertEquals( value, map.get(key) );
			assertTrue( map.containsKey(key) );
			assertTrue( map.containsValue(value) );
			assertTrue( keySet.contains(key) );
			assertTrue( values.contains(value) );
		}
		
		assertEquals(expectedHash, map.hashCode());
		
		HashMap<K,V> map2 = new HashMap<K,V>(map);
		assertTrue(map.equals(map2));
		if (!map2.isEmpty())
		{
			if (value != null)
			{
				map2.put(key, null);
				assertFalse(map.equals(map2));
			}
			map2.remove(key);
			assertFalse(map.equals(map2));
		}
	}
	
	public void assertMapEquals(Map<K,V> map1, Map<K,V> map2)
	{
		assertEquals(map1.isEmpty(), map2.isEmpty());
		assertEquals(map1.size(), map2.size());
		assertTrue(map1.equals(map2));
		
		_entrySetTester.assertSetEquals(map1.entrySet(), map2.entrySet());
		_keySetTester.assertSetEquals(map1.keySet(), map2.keySet());
		_valueCollectionTester.assertCollectionEquals(map1.values(), map2.values());
	}
}
