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

package com.analog.lyric.collect;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

// TODO: implement NavigableMap - requires implementing various submap/keyset views.

public class SkipMap<K, V> extends AbstractSkipList<K> implements Map<K, V>
{

	public static class Entry<K,V> implements Map.Entry<K, V>
	{
		private final Object[] node;
		
		protected Entry(Object[] node)
		{
			this.node = node;
		}

		@Override
		public boolean equals(@Nullable Object other)
		{
			if (this == other)
			{
				return true;
			}
			
			if (other instanceof Entry)
			{
				Entry<?,?> that = (Entry<?,?>)other;
				return node[0].equals(that.node[0]) && Objects.equals(node[1], that.node[1]);
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			Object value = node[1];
			return node[0].hashCode() ^ (value == null ? 0 : value.hashCode());
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public K getKey()
		{
			return (K)node[0];
		}

		@SuppressWarnings("unchecked")
		@Override
		public V getValue()
		{
			return (V)node[1];
		}

		@Override
		public @Nullable V setValue(@Nullable V value)
		{
			V oldValue = this.getValue();
			node[1] = value;
			return oldValue;
		}
	}
	
	private @Nullable Entry<K,V> makeEntry(@Nullable Object[] node)
	{
		return node == null ? null : new Entry<K,V>(node);
	}
	
	@SuppressWarnings("unchecked")
	protected final @Nullable V getNodeValue(Object[] node)
	{
		return (V)node[1];
	}
	
	protected final @Nullable V setNodeValue(Object[] node, @Nullable V value)
	{
		@SuppressWarnings("unchecked")
		V oldValue = (V)node[1];
		node[1] = value;
		return oldValue;
	}
	
	/*--------------
	 * Construction
	 */

	public SkipMap(Comparator<? super K> comparator)
	{
		super(comparator, (short)2);
	}
	
	/**
	 * @since 0.05
	 */
	public static <K,V> SkipMap<K,V> create(Comparator<? super K> comparator)
	{
		return new SkipMap<K,V>(comparator);
	}

	// TODO: add following constructors: (), (Map), (SortedMap)
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof Map)
		{
			Map<?,?> otherMap = (Map<?,?>)other;
			if (otherMap.size() == size())
			{
				// TODO: special case if other is a SkipMap or a SortedMap with the same comparator

				for (Object[] node = this.getNextNode(this.head); node != null; node = this.getNextNode(node))
				{
					Object key = node[0], value = node[1];
					if (!Objects.equals(otherMap.get(key), value))
					{
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		// Implements computation specified by Map.hashCode()
		int hash = 0;
		for (Object[] node = this.getNextNode(this.head); node != null; node = this.getNextNode(node))
		{
			Object key = node[0], value = node[1];
			hash += key.hashCode() ^ (value == null ? 0 : value.hashCode());
		}
		return hash;
	}
	
	/*
	 * Map methods
	 */
	
	@Override
	public boolean containsKey(@Nullable Object key)
	{
		try
		{
			@SuppressWarnings("unchecked")
			K k = (K)key;
			return k != null && this.containsNode(k);
		}
		catch (ClassCastException ex)
		{
			return false;
		}
	}
	
	@Override
	public boolean containsValue(@Nullable Object value)
	{
		for (Object[] node = this.getNextNode(this.head); node != null; node = this.getNextNode(node))
		{
			if (Objects.equals(this.getNodeValue(node), value))
			{
				return true;
			}
		}
		
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public EntrySet entrySet()
	{
		return new EntrySet(this);
	}

	@Override
	public @Nullable V get(@Nullable Object key)
	{
		V result = null;
		
		if (key != null)
		{
			try
			{
				@SuppressWarnings("unchecked")
				K k = (K)key;
				result = this.get2(k);
			}
			catch (ClassCastException ex)
			{
			}
		}
		
		return result;
	}
	
	@Override
	public KeySet<K,V> keySet()
	{
		return new KeySet<K,V>(this);
	}

	@Override
	public @Nullable V put(@Nullable K key, @Nullable V value)
	{
		return this.setNodeValue(this.addNode(key), value);
	}

	@Override
	@NonNullByDefault(false)
	public void putAll(Map<? extends K, ? extends V> m)
	{
		for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
		{
			this.put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public @Nullable V remove(@Nullable Object key)
	{
		V result = null;
		
		if (key != null)
		{
			try
			{
				@SuppressWarnings("unchecked")
				K k = (K)key;
				result = this.remove2(k);
			}
			catch (ClassCastException ex)
			{
		}
		}
		
		return result;
	}
	
	@Override
	public ValueCollection<K, V> values()
	{
		return new ValueCollection<K,V>(this);
	}

	/*
	 * SortedMap methods
	 */

	public K firstKey()
	{
		final Object[] node = this.firstNode();
		if (node == null)
		{
			throw new NoSuchElementException();
		}
		return this.getNodeKey(node);
	}

	public K lastKey()
	{
		final Object[] node = this.lastNode();
		if (node == null)
		{
			throw new NoSuchElementException();
		}
		return this.getNodeKey(node);

	}

	/*
	 * NavigableMap methods
	 */
	
	public @Nullable Map.Entry<K,V> ceilingEntry(K key)
	{
		return this.makeEntry(this.findCeilingNode(key));
	}

	public @Nullable K ceilingKey(K key)
	{
		Object[] node = this.findCeilingNode(key);
		return node == null ? null : this.getNodeKey(node);
	}

	public @Nullable Map.Entry<K,V> firstEntry()
	{
		return this.makeEntry(this.getNextNode(this.head));
	}
	
	/**
	 * Returns greatest value in set that is less than or equal to {@code value} or null.
	 */
	public @Nullable Map.Entry<K,V> floorEntry(K key)
	{
		return this.makeEntry(this.findFloorNode(key));
	}
	
	public @Nullable K floorKey(K key)
	{
		Object[] node = this.findFloorNode(key);
		return node == null ? null : this.getNodeKey(node);
	}

	public @Nullable Map.Entry<K,V> higherEntry(K key)
	{
		return this.makeEntry(this.findHigherNode(key));
	}

	public @Nullable K higherKey(K key)
	{
		Object[] node = this.findHigherNode(key);
		return node == null ? null : this.getNodeKey(node);
	}
	public @Nullable Map.Entry<K, V> lastEntry()
	{
		return this.makeEntry(this.lastNode());
	}
	
	public @Nullable Map.Entry<K,V> lowerEntry(K key)
	{
		Object[] node = this.findLowerNode(key);
		return node == this.head ? null : this.makeEntry(node);
	}

	public K lowerKey(K key)
	{
		return this.getNodeKey(this.findLowerNode(key));
	}

	public @Nullable Map.Entry<K,V> pollFirstEntry()
	{
		return this.makeEntry(this.pollFirstNode());
	}

	public @Nullable Map.Entry<K,V> pollLastEntry()
	{
		return this.makeEntry(this.pollLastNode());
	}
	
	/*
	 * SkipMap methods
	 */

	/** Like {@link #containsKey} but {@code key} must be of type {@code K}. */
	public boolean containsKey2(K key)
	{
		return this.containsNode(key);
	}
	
	/** Like {@link #get} but {@code key} must be of type {@code K}. */
	public @Nullable V get2(K key)
	{
		Object[] node = this.getNode(key);
		return node != null ? this.getNodeValue(node) : null;
	}

	/** Like {@link #remove} but {@code key} must be of type {@code K}. */
	public @Nullable V remove2(K key)
	{
		Object[] node = this.removeNode(key);
		return node == null ? null : this.getNodeValue(node);
	}

	/*
	 * Views
	 */
	
	public static class EntrySet<K,V> extends AbstractSet<Entry<K,V>>
		implements ReleasableIterableCollection<Entry<K,V>>
	{
		private final SkipMap<K,V> map;
		
		private EntrySet(SkipMap<K,V> map)
		{
			this.map = map;
		}
		
		@Override
		public void clear()
		{
			this.map.clear();
		}

		@Override
		public boolean contains(@Nullable Object obj)
		{
			if (obj != null)
			{
				try
				{
					@SuppressWarnings("unchecked")
					Entry<K,V> entry = (Entry<K, V>)obj;
					K key = entry.getKey();

					return entry.node == this.map.getNode(key);
				}
				catch (ClassCastException ex)
				{
				}
			}

			return false;
		}

		@Override
		public Iterator<K,V> iterator()
		{
			return Iterator.make(this.map);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean remove(@Nullable Object obj)
		{
			if (obj instanceof Map.Entry)
			{
				return this.map.remove(((Map.Entry)obj).getKey()) != null;
			}
			
			return false;
		}

		@Override
		public int size()
		{
			return this.map.size();
		}
	}

	public static class KeySet<K,V> extends AbstractSet<K> implements ReleasableIterableCollection<K>
	{
		private final SkipMap<K,V> map;
		
		private KeySet(SkipMap<K,V> map)
		{
			this.map = map;
		}
		
		@Override
		public void clear()
		{
			this.map.clear();
		}

		@Override
		public boolean contains(@Nullable Object key)
		{
			return this.map.containsKey(key);
		}

		@Override
		public KeyIterator<K,V> iterator()
		{
			return KeyIterator.make(this.map);
		}

		@Override
		public boolean remove(@Nullable Object key)
		{
			return key != null && this.map.remove(key) != null;
		}

		@Override
		public int size()
		{
			return this.map.size();
		}
	}
	
	public static class ValueCollection<K,V> extends AbstractCollection<V>
		implements ReleasableIterableCollection<V>
	{
		private final SkipMap<K,V> map;
		
		private ValueCollection(SkipMap<K,V> map)
		{
			this.map = map;
		}
		
		@Override
		public void clear()
		{
			this.map.clear();
		}
		
		@Override
		public boolean contains(@Nullable Object value)
		{
			return this.map.containsValue(value);
		}
		
		@Override
		public ValueIterator<K,V> iterator()
		{
			return ValueIterator.make(this.map);
		}

		@Override
		public int size()
		{
			return this.map.size();
		}
	}
	
	/*
	 * Iterators
	 */
	
	/**
	 * A reusable iterator for fast iteration over {@link SkipMap} entries.
	 * <p>
	 * This iterator is optimized for visiting elements in set without removal.
	 * Iterating over the entire set costs O(n) in the size of the map. The
	 * {@link #remove} method is supported but costs O(log(n)).
	 */
	public static class Iterator<K,V> implements ReleasableIterator<Entry<K,V>>
	{
		/*
		 * State
		 */

		/** The underlying map to be iterated over. This may be null. */
		protected @Nullable SkipMap<K,V> map;

		/** The next node to be returned by {@link #next}. Null when there are no more nodes. */
		private @Nullable Object[] nextNode;

		/** The last value returned by {@link #next}. Could be null. */
		private @Nullable K lastKey;

		private static final ThreadLocal<Iterator<?,?>> reusableInstance = new ThreadLocal<Iterator<?,?>>();
		
		/*
		 * Construction/initialization methods
		 */

		/**
		 * Constructs iterator over given {@code map}, which may be null.
		 */
		public Iterator(@Nullable SkipMap<K,V> map)
		{
			this.reset(map);
		}

		protected static <K,V> Iterator<K,V> make(@Nullable SkipMap<K,V> map)
		{
			@SuppressWarnings("unchecked")
			Iterator<K,V> iter = (Iterator<K, V>)Iterator.reusableInstance.get();
			
			if (iter != null)
			{
				Iterator.reusableInstance.set(null);
				iter.reset(map);
			}
			else
			{
				iter = new Iterator<K,V>(map);
			}
				
			return iter;
		}
		
		@Override
		public void release()
		{
			if (Iterator.reusableInstance.get() == null)
			{
				this.reset(null);
				Iterator.reusableInstance.set(this);
			}
		}
		
		/**
		 * Resets iterator back to beginning of map.
		 */
		public void reset()
		{
			final @Nullable SkipMap<K,V> map2 = this.map;
			this.nextNode = map2 == null ? null : map2.getNextNode(map2.head);
		}

		/**
		 * Resets iterator to beginning of {@code newMap}, which may be null.
		 */
		public void reset(@Nullable SkipMap<K,V> newMap)
		{
			this.map = newMap;
			this.reset();
		}

		/*
		 * java.util.Iterator methods
		 */

		/**
		 * Returns true if {@link #next} method will return a non-null value.
		 */
		@Override
		public boolean hasNext()
		{
			return this.nextNode != null;
		}

		/**
		 * Returns the next element in the iteration or null if at the end of the list.
		 * It is not necessary to invoke {@link #hasNext} before calling this method.
		 */
		@Override
		public @Nullable Entry<K,V> next()
		{
			Object[] node = this.advance();
			return node == null ? null : new Entry<K,V>(node);
		}

		@Override
		public void remove()
		{
			final SkipMap<K,V> map2 = map;
			if (map2 == null)
			{
				throw new IllegalStateException();
			}
			map2.removeNode(this.lastKey);
		}
		
		/*
		 * Local Iterator methods
		 */
		
		/**
		 * Returns the next value in the iteration or null if at the end of the list.
		 * It is not necessary to invoke {@link #hasNext} before calling this method.
		 * You can obtain the key to which this value was mapped by calling
		 * {@link #getLastKey} before the next call to {@link #next} or {@link #nextValue}.
		 */
		public @Nullable V nextValue()
		{
			final SkipMap<K,V> map2 = this.map;
			@Nullable V result = null;
			if (map2 != null)
			{
				@Nullable Object[] node = this.advance();
				result =  node == null ? null : map2.getNodeValue(node);
			}
			return result;
		}
		
		/**
		 * Returns the key associated with the last value returned by {@link #nextValue}
		 * or with the last entry returned by {@link #next}. Returns null if at start
		 * or end of iteration.
		 */
		public @Nullable K getLastKey()
		{
			return this.lastKey;
		}
		
		private @Nullable Object[] advance()
		{
			final SkipMap<K,V> map2 = this.map;
			@Nullable Object[] node = null;
			if (map2 != null)
			{
				node = this.nextNode;
				if (node != null)
				{
					this.lastKey = map2.getNodeKey(node);
					this.nextNode = map2.getNextNode(node);
					return node;
				}
				else
				{
					this.lastKey = null;
				}
			}
			return node;
		}
	}

	public static class KeyIterator<K,V> extends AbstractSkipList.KeyIterator<K>
		implements ReleasableIterator<K>
	{
		private static final ThreadLocal<KeyIterator<?,?>> reusableInstance= new ThreadLocal<KeyIterator<?,?>>();
		
		public KeyIterator(@Nullable SkipMap<K,V> map)
		{
			super(map);
		}
		
		public static <K,V> KeyIterator<K,V> make(@Nullable SkipMap<K,V> map)
		{
			@SuppressWarnings("unchecked")
			KeyIterator<K,V> iter = (KeyIterator<K, V>) KeyIterator.reusableInstance.get();
			
			if (iter != null)
			{
				KeyIterator.reusableInstance.set(null);
				iter.reset(map);
			}
			else
			{
				iter = new KeyIterator<K,V>(map);
			}
			
			return iter;
		}
		
		@Override
		public void release()
		{
			if (KeyIterator.reusableInstance.get() == null)
			{
				this.reset(null);
				KeyIterator.reusableInstance.set(this);
			}
		}
		
		public void reset(@Nullable SkipMap<K,V> map)
		{
			super.reset(map);
		}
	}
	
	public static class ValueIterator<K,V> implements ReleasableIterator<V>
	{
		/*
		 * State
		 */
		
		/** The underlying set to be iterated over. This may be null. */
		protected @Nullable SkipMap<K,V> map;
		
		/** The next node to be returned by {@link #next}. Null when there are no more nodes. */
		private @Nullable Object[] nextNode;
		
		/** The last key returned by {@link #next}. Could be null. */
		private @Nullable K lastKey;
		
		private static final ThreadLocal<ValueIterator<?,?>> reusableInstance = new ThreadLocal<ValueIterator<?,?>>();
		
		/*
		 * Construction/initialization methods
		 */
		
		public ValueIterator(@Nullable SkipMap<K,V> map)
		{
			this.reset(map);
		}
		
		public static <K,V> ValueIterator<K,V> make(@Nullable SkipMap<K,V> map)
		{
			@SuppressWarnings("unchecked")
			ValueIterator<K,V> iter = (ValueIterator<K, V>) ValueIterator.reusableInstance.get();
			
			if (iter != null)
			{
				ValueIterator.reusableInstance.set(null);
				iter.reset(map);
			}
			else
			{
				iter = new ValueIterator<K,V>(map);
			}
			
			return iter;
		}
		
		@Override
		public void release()
		{
			if (ValueIterator.reusableInstance.get() == null)
			{
				this.reset(null);
				ValueIterator.reusableInstance.set(this);
			}
		}
		
		/**
		 * Resets iterator back to beginning of set.
		 */
		public void reset()
		{
			final @Nullable SkipMap<K,V> map2 = this.map;
			this.nextNode = map2 == null ? null : map2.getNextNode(map2.head);
		}
		
		/**
		 * Resets iterator to beginning of {@code newList}, which may be null.
		 */
		public void reset(@Nullable SkipMap<K,V> newMap)
		{
			this.map = newMap;
			this.reset();
		}
		
		/*
		 * java.util.Iterator methods
		 */
		
		/**
		 * Returns true if {@link #next} method will return a non-null value.
		 */
		@Override
		public boolean hasNext()
		{
			return this.nextNode != null;
		}

		/**
		 * Returns the next element in the iteration or null if at the end of the list.
		 * It is not necessary to invoke {@link #hasNext} before calling this method.
		 */
		@Override
		public @Nullable V next()
		{
			V value = null;
			final SkipMap<K,V> map2 = map;
			if (map2 != null)
			{
				K key = null;
				Object[] n = this.nextNode;
				if (n != null)
				{
					value = map2.getNodeValue(n);
					key = map2.getNodeKey(n);
					this.nextNode = map2.getNextNode(n);
				}
				this.lastKey = key;
			}
			return value;
		}

		/**
		 * Removes the entry that was most recently returned by the {@link #next} method.
		 * Costs O(log n).
		 */
		@Override
		public void remove()
		{
			final SkipMap<K,V> map2 = map;
			if (map2 == null)
			{
				throw new IllegalStateException();
			}
			map2.removeNode(this.lastKey);
			this.lastKey = null;
		}

	}

}


