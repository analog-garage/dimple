/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

import static java.util.Objects.*;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.map.OpenIntObjectHashMap;

/**
 * A simple int to weak Object map.
 * <p>
 * This maps integers to a non-null value of a reference type T, but only holds a
 * weak reference so that the value may be garbage collected if no one else has
 * a reference.
 * <p>
 * This does not implement the {@link java.util.Map} interface because the keys
 * are primitive ints, but has a similar API.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public class WeakIntHashMap<T>
{
	private static class Ref<T> extends WeakReference<T>
	{
		private final int _key;
		private volatile boolean _removed = false;
		
		private Ref(int key, T value, ReferenceQueue<? super T> q)
		{
			super(value, q);
			_key = key;
		}
	}

	/*-------
	 * State
	 */

	private final ReferenceQueue<T> _collected;
	private final OpenIntObjectHashMap _map;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs an empty map.
	 * 
	 * @since 0.08
	 */
	public WeakIntHashMap()
	{
		this(256);
	}
	
	/**
	 * Constructs an empty map with given initial capacity.
	 * @since 0.08
	 */
	public WeakIntHashMap(int capacity)
	{
		_collected = new ReferenceQueue<>();
		_map = new OpenIntObjectHashMap(capacity);
	}
	
	/*---------
	 * Methods
	 */

	/**
	 * Cleans up internal storage for entries that were garbage collected.
	 * @return the number of entries that were collected.
	 * @since 0.08
	 */
	@SuppressWarnings("unchecked")
	public int clean()
	{
		int count = 0;
		for (Ref<T> ref; (ref = (Ref<T>)_collected.poll()) != null;)
		{
			if (!ref._removed && _map.get(ref._key) == ref)
			{
				_map.removeKey(ref._key);
			}
			ref._removed = true;
			return ++count;
		}
		return count;
	}

	/**
	 * Removes all entries from the table.
	 * 
	 * @since 0.08
	 */
	public void clear()
	{
		_map.clear();
		while (_collected.poll() != null) {} // drain queue
	}
	
	/**
	 * True if map contains an entry with given key (that has not yet been garbage collected)
	 */
	public boolean containsKey(int key)
	{
		return get(key) != null;
	}

	/**
	 * Expand capacity to fit specify number of entries.
	 * @since 0.08
	 */
	public void ensureCapacity(int capacity)
	{
		clean();
		_map.ensureCapacity(capacity);
	}

	/**
	 * Returns value mapped to given key.
	 * @param key
	 * @return value or null if there is no such mapping (including if the entry was
	 * garbage collected since first added)
	 * @since 0.08
	 */
	@SuppressWarnings("unchecked")
	public @Nullable T get(int key)
	{
		T result = null;
		Ref<T> ref = (Ref<T>)_map.get(key);
		if (ref != null)
		{
			result = ref.get();
			if (result == null)
			{
				clean();
			}
		}
		
		return result;
	}
	
	/**
	 * Returns array of current key values in arbitrary order.
	 * <p>
	 * Note that there is no guarantee that the entries will for these keys will not be garbage-collected
	 * away
	 * <p>
	 * @since 0.08
	 */
	public int[] keys()
	{
		clean();
		return _map.keys().elements();
	}
	
	/**
	 * True if map has no entries.
	 * @since 0.08
	 */
	public boolean isEmpty()
	{
		return size() == 0;
	}
	
	/**
	 * Add an entry to the map.
	 * @return True if map did not already have an entry for this key.
	 * @since 0.08
	 */
	public boolean put(int key, T value)
	{
		return _map.put(key, new Ref<>(key, requireNonNull(value), _collected));
	}

	/**
	 * Removes value mapped to given key.
	 * @return True if map contained the key.
	 * @since 0.08
	 */
	public boolean removeKey(int key)
	{
		@SuppressWarnings("unchecked")
		Ref<T> ref = (Ref<T>)_map.get(key);
		if (ref != null)
		{
			ref._removed = true;
			_map.removeKey(key);
			return true;
		}
		return false;
	}

	/**
	 * The current number of entries in the map.
	 * @since 0.08
	 */
	public int size()
	{
		clean();
		return _map.size();
	}
	
}
