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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link Map} implementation that delegates to another map
 * until the first time a method is called that will add or
 * remove entries from the map. After the {@link #originalMap()}
 * has been copied (as indicated by {@link #wasCopied()}), changes
 * to the original map will not be reflected in this map.
 * <p>
 * The map can be reverted back to its original uncopied state
 * by invoking {@link #revertToOriginalMap()}.
 * <p>
 * Until the first mutating operation has been performed, the
 * map is only thread safe to the extent that the underlying map
 * is. The implementation synchronizes against this object when
 * explicit synchronization is needed.
 */
@ThreadSafe
@NonNullByDefault(false)
public class CopyOnWriteMap<K, V> implements Map<K, V>
{
	/*-------
	 * State
	 */
	
	private final @NonNull Map<K,V> _originalMap;
	private @NonNull AtomicReference<Map<K,V>> _map;

	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct map delegating to {@code underlyingMap} until
	 * the first mutating operation, which will trigger a copy
	 * to be made.
	 */
	public CopyOnWriteMap(@NonNull Map<K,V> underlyingMap)
	{
		_originalMap = underlyingMap;
		_map = new AtomicReference<Map<K,V>>(underlyingMap);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		return readOnlyMap().equals(other);
	}
	
	@Override
	public int hashCode()
	{
		return readOnlyMap().hashCode();
	}
	
	/*-----------------------
	 * ConcurrentMap methods
	 */
	
	/**
	 * {@link #wasCopied()} will be true after invoking this unless
	 * {@link #isEmpty()} and not already copied.
	 */
	@Override
	public void clear()
	{
		if (!isEmpty())
		{
			synchronized (this)
			{
				if (_map.get() == _originalMap)
				{
					_map.set(createEmptyMap(0));
				}
				else
				{
					_map.get().clear();
				}
			}
		}
	}
	
	@Override
	public boolean containsKey(Object key)
	{
		return readOnlyMap().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return readOnlyMap().containsValue(value);
	}

	/**
	 * This implementation does not support any mutating operations on the set.
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet()
	{
		return Collections.unmodifiableSet(readOnlyMap().entrySet());
	}

	@Override
	public @Nullable V get(Object key)
	{
		return readOnlyMap().get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return readOnlyMap().isEmpty();
	}

	/**
	 * This implementation does not support any mutating operations on the set.
	 */
	@Override
	public Set<K> keySet()
	{
		return Collections.unmodifiableSet(readOnlyMap().keySet());
	}

	/**
	 * {@link #wasCopied()} will be true after invoking this.
	 */
	@Override
	public @Nullable V put(K key, @Nullable V value)
	{
		return mutableMap().put(key, value);
	}

	/**
	 * {@link #wasCopied()} will be true after invoking this.
	 */
	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		mutableMap().putAll(map);
	}

	/**
	 * {@link #wasCopied()} will be true after invoking this unless not
	 * already copied and {@link #originalMap()} does not contain {@code key}.
	 */
	@Override
	public @Nullable V remove(Object key)
	{
		Map<K,V> map = _map.get();
		
		if (map == _originalMap)
		{
			if (!map.containsKey(key))
			{
				return null;
			}
			map = mutableMap();
		}
		
		return map.remove(key);
	}

	@Override
	public int size()
	{
		return readOnlyMap().size();
	}

	/**
	 * This implementation does not support any mutating operations on the set.
	 */
	@Override
	public Collection<V> values()
	{
		return Collections.unmodifiableCollection(readOnlyMap().values());
	}

	/*------------------------
	 * CopyOnWriteMap methods
	 */
	
	/**
	 * The underlying map to which this one delegates until the first
	 * time a mutating operation is called on this map. Set in the constructor.
	 */
	public @NonNull Map<K,V> originalMap()
	{
		return _originalMap;
	}
	
	/**
	 * Resets the state of the map back to simple delegation of {@link #originalMap()},
	 * after which {@link #wasCopied()} will be false.
	 */
	public void revertToOriginalMap()
	{
		_map.set(_originalMap);
	}
	
	/**
	 * Indicates whether {@link #originalMap()} has been copied. When true, mutating operations
	 * will not trigger additional copies and non-mutating operations will return the contents
	 * of the internal copy, not the contents of the original map. Changes made to the original
	 * map after the copy was made will not be noticed.
	 */
	public boolean wasCopied()
	{
		return _map.get() != _originalMap;
	}

	/**
	 * Returns a copy of {@link #originalMap()} used the first time
	 * a mutating method is called on this class.
	 * <p>
	 * The default implementation creates the new map using {@link #createEmptyMap}
	 * and adds members using {@link Map#putAll}.
	 * <p>
	 * Subclasses may override this method to return other map types or use a different
	 * copying technique.
	 */
	protected @NonNull Map<K,V> copyOriginalMap()
	{
		Map<K,V> map = createEmptyMap(_originalMap.size());
		map.putAll(_originalMap);
		return map;
	}

	/**
	 * Creates an empty map with specified initial capacity.
	 * <p>
	 * The default implementation returns a new {@link TreeMap} if the
	 * {@link #originalMap()} is a {@link SortedMap} and otherwise returns
	 * a new {@link HashMap}.
	 * <p>
	 * Subclasses may override this method to return other map types.
	 */
	protected @NonNull Map<K,V> createEmptyMap(int capacity)
	{
		if (_originalMap instanceof SortedMap)
		{
			return new TreeMap<K,V>(((SortedMap<K,V>)_originalMap).comparator());
		}
		else
		{
			return new HashMap<K,V>(capacity);
		}
	}
	
	/*-----------------
	 * Private methods
	 */
	
	/**
	 * Returns map for use in implementing read-only operations. This will not
	 * trigger a copy. Use {@link #mutableMap()} for mutating operations.
	 * <p>
	 * Note that if {@link #wasCopied()} is true, this will return the same
	 * object as {@link #mutableMap()}.
	 */
	protected @NonNull Map<K,V> readOnlyMap()
	{
		return _map.get();
	}
	
	/**
	 * Returns map for use in implementing mutating operations. This will trigger
	 * a copy of the {@link #originalMap()} (synchronized against this object) the
	 * first time it is invoked after construction or after call to {@link #revertToOriginalMap()}.
	 * Use {@link #readOnlyMap()} for read-only operations.
	 */
	protected @NonNull Map<K,V> mutableMap()
	{
		Map<K,V> map = _map.get();
		
		if (map == _originalMap)
		{
			synchronized (this)
			{
				if (_map.get() == _originalMap)
				{
					_map.set(map = copyOriginalMap());
				}
			}
		}
		
		return map;
	}
}
