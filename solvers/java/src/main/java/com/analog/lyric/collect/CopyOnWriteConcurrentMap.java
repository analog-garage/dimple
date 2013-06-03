/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.collect;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author cbarber
 *
 */
public class CopyOnWriteConcurrentMap<K, V> implements ConcurrentMap<K, V>
{
	/*-------
	 * State
	 */
	
	private final ConcurrentMap<K,V> _originalMap;
	private AtomicReference<ConcurrentMap<K,V>> _map;

	/*--------------
	 * Construction
	 */
	
	public CopyOnWriteConcurrentMap(ConcurrentMap<K,V> underlyingMap)
	{
		_originalMap = underlyingMap;
		_map = new AtomicReference<ConcurrentMap<K,V>>(underlyingMap);
	}
	
	/*-----------------------
	 * ConcurrentMap methods
	 */
	
	@Override
	public void clear()
	{
		if (_originalMap.isEmpty())
		{
			_map.set(_originalMap);
		}
		else
		{
			mutableMap().clear();
		}
	}
	
	/*
	 * 
	 */
	@Override
	public boolean containsKey(Object key)
	{
		return readOnlyMap().containsKey(key);
	}

	/*
	 * 
	 */
	@Override
	public boolean containsValue(Object value)
	{
		return readOnlyMap().containsValue(value);
	}

	/*
	 * 
	 */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 */
	@Override
	public V get(Object key)
	{
		return readOnlyMap().get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return readOnlyMap().isEmpty();
	}

	/*
	 * 
	 */
	@Override
	public Set<K> keySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 */
	@Override
	public V put(K key, V value)
	{
		return mutableMap().put(key, value);
	}

	/*
	 * 
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		mutableMap().putAll(map);
	}

	@Override
	public V remove(Object key)
	{
		V prevValue = null;
		if (readOnlyMap().containsKey(key))
		{
			prevValue = mutableMap().remove(key);
		}
		return prevValue;
	}

	@Override
	public int size()
	{
		return readOnlyMap().size();
	}

	/*
	 * 
	 */
	@Override
	public Collection<V> values()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * 
	 */
	@Override
	public V putIfAbsent(K key, V value)
	{
		return mutableMap().putIfAbsent(key, value);
	}

	/*
	 * 
	 */
	@Override
	public boolean remove(Object key, Object value)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * 
	 */
	@Override
	public V replace(K key, V value)
	{
		return mutableMap().replace(key,  value);
	}

	/*
	 * 
	 */
	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		return mutableMap().replace(key, oldValue, newValue);
	}
	
	/*----------------------------------
	 * CopyOnWriteConcurrentMap methods
	 */
	
	public ConcurrentMap<K,V> originalMap()
	{
		return _originalMap;
	}
	
	public void reset()
	{
		_map.set(_originalMap);
	}
	
	public boolean wasCopied()
	{
		return _map.get() != _originalMap;
	}

	/*-----------------
	 * Private methods
	 */
	
	private ConcurrentMap<K,V> copyOriginalMap()
	{
		ConcurrentMap<K,V> map = new ConcurrentHashMap<K,V>();
		
		map.putAll(_originalMap);
		
		return map;
	}
	
	private ConcurrentMap<K,V> readOnlyMap()
	{
		return _map.get();
	}
	
	private ConcurrentMap<K,V> mutableMap()
	{
		ConcurrentMap<K,V> map = _map.get();
		
		if (map == _originalMap)
		{
			synchronized (_map)
			{
				if (_map.get() == _originalMap)
				{
					_map.set(copyOriginalMap());
				}
			}
		}
		
		return map;
	}
}
