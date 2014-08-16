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

import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An extension of {@link CopyOnWriteMap} for delegating to a {@link ConcurrentMap}.
 */
@ThreadSafe
@NonNullByDefault(false)
public class CopyOnWriteConcurrentMap<K, V> extends CopyOnWriteMap<K,V> implements ConcurrentMap<K,V>
{
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct map delegating to {@code underlyingMap} until
	 * the first mutating operation, which will trigger a copy
	 * to be made.
	 */
	public CopyOnWriteConcurrentMap(@NonNull ConcurrentMap<K,V> underlyingMap)
	{
		super(underlyingMap);
	}
	
	/*-----------------------
	 * ConcurrentMap methods
	 */
	
	/**
	 * {@link #wasCopied()} will be true after invoking this.
	 */
	@Override
	public V putIfAbsent(K key, V value)
	{
		return mutableMap().putIfAbsent(key, value);
	}

	/**
	 * {@link #wasCopied()} will be true after invoking this.
	 */
	@Override
	public boolean remove(Object key, Object value)
	{
		return mutableMap().remove(key, value);
	}

	/**
	 * {@link #wasCopied()} will be true after invoking this.
	 */
	@Override
	public @Nullable V replace(K key, V value)
	{
		return mutableMap().replace(key,  value);
	}

	/**
	 * {@link #wasCopied()} will be true after invoking this.
	 */
	@Override
	public boolean replace(K key, V oldValue, V newValue)
	{
		return mutableMap().replace(key, oldValue, newValue);
	}
	
	/*----------------------------------
	 * CopyOnWriteConcurrentMap methods
	 */
	
	@Override
	public @NonNull ConcurrentMap<K,V> originalMap()
	{
		return (ConcurrentMap<K, V>)super.originalMap();
	}
	
	@Override
	protected @NonNull ConcurrentMap<K,V> copyOriginalMap()
	{
		return (ConcurrentMap<K, V>) super.copyOriginalMap();
	}

	/**
	 * Creates an empty map with specified initial capacity.
	 * <p>
	 * This implementation returns a new {@link ConcurrentSkipListMap} if the
	 * {@link #originalMap()} is a {@link SortedMap} and otherwise returns
	 * a new {@link ConcurrentHashMap}.
	 * <p>
	 * Subclasses may override this method to return other map types.
	 */
	@Override
	protected @NonNull ConcurrentMap<K,V> createEmptyMap(int capacity)
	{
		Map<K,V> original = originalMap();
		if (original instanceof SortedMap)
		{
			return new ConcurrentSkipListMap<K, V>(((SortedMap<K,V>)original).comparator());
		}
		else
		{
			return new ConcurrentHashMap<K,V>(original.size());
		}
	}
	
	/*-----------------
	 * Private methods
	 */
	
	@Override
	protected @NonNull ConcurrentMap<K,V> readOnlyMap()
	{
		return (ConcurrentMap<K, V>) super.readOnlyMap();
	}
	
	@Override
	protected @NonNull ConcurrentMap<K,V> mutableMap()
	{
		return (ConcurrentMap<K, V>) super.mutableMap();
	}
}
