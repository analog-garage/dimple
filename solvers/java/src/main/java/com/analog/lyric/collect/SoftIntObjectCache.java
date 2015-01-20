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

package com.analog.lyric.collect;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A cache that associates an integer with a cached value, where all cache state is held by a soft reference.
 * <p>
 * The entire underlying state of the cache is held by a {@link SoftReference} and may be discarded by
 * the Java garbage collector if memory is low. When that happens the {@link #remove(int)}
 * method will return null.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class SoftIntObjectCache<T>
{

	protected final AtomicReference<SoftReference<ConcurrentHashMap<Integer,T>>> _cacheRef;

	/**
	 * Constructs cache instance.
	 */
	public SoftIntObjectCache()
	{
		_cacheRef = new AtomicReference<>(new SoftReference<ConcurrentHashMap<Integer,T>>(null));
	}

	/*--------------------------------
	 * AbstractIntObjectCache methods
	 */

	/**
	 * Clear the cache's state.
	 * 
	 * @since 0.08
	 */
	public void clear()
	{
		_cacheRef.get().clear();
	}
	
	/**
	 * Removes and returns value in cache currently associated with key or else null.
	 * @since 0.08
	 */
	public @Nullable T remove(int key)
	{
		ConcurrentHashMap<Integer,T> cache = _cacheRef.get().get();
		return cache != null ? cache.remove(key) : null;
	}

	/**
	 * Associates value with given key, if key not already used in cache.
	 * <p>
	 * Note that values may be cleared from the cache implicitly by the garbage collector
	 * if the system is low on memory.
	 * 
	 * @returns True if value was added to the cache. False if cache already had an entry for that key.
	 * @since 0.08
	 */
	public boolean put(int key, T object)
	{
		return getCache().putIfAbsent(key, object) == null;
	}

	/*-----------------
	 * Private methods
	 */
	
	private ConcurrentHashMap<Integer,T> getCache()
	{
		final AtomicReference<SoftReference<ConcurrentHashMap<Integer,T>>> cacheRef = _cacheRef;
		
		while (true)
		{
			final SoftReference<ConcurrentHashMap<Integer,T>> ref = cacheRef.get();
			final ConcurrentHashMap<Integer,T> cache = ref.get();
		
			if (cache != null)
			{
				return cache;
			}
			
			final ConcurrentHashMap<Integer,T> newCache = new ConcurrentHashMap<>();
			if (_cacheRef.compareAndSet(ref, new SoftReference<>(newCache)))
			{
				return newCache;
			}
		}
	}

}