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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;

import net.jcip.annotations.Immutable;
import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import com.google.common.collect.Ordering;

/**
 * Abstract base class for priority queue with unique keys which can be computed
 * from the elements themselves.
 * 
 * @since 0.05
 * @author Christopher Barber
 */
@NotThreadSafe
@NonNullByDefault(false)
public abstract class AbstractKeyedPriorityQueue<K,E> extends AbstractQueue<E> implements Serializable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	protected final PriorityQueue<Entry<E>> _queue;
	protected final Map<K,Entry<E>> _keyToEntry;
	protected final @Nullable Comparator<? super E> _priorityComparator;
	protected int _insertOrder = 0;

	/*--------------
	 * Construction
	 */
	
	public AbstractKeyedPriorityQueue(int initialCapacity, @Nullable Comparator<? super E> comparator)
	{
		_priorityComparator = comparator;
		if (comparator == null)
		{
			@SuppressWarnings("unchecked")
			final Comparator<E> natural = (Comparator<E>)Ordering.natural();
			comparator = natural;
		}
		_queue = new PriorityQueue<Entry<E>>(initialCapacity, new EntryComparator<E>(comparator));
		_keyToEntry = new HashMap<K,Entry<E>>(initialCapacity);
	}
	
	public AbstractKeyedPriorityQueue(@NonNull Collection<? extends E> collection)
	{
		this(collection.size(), Comparators.fromCollection(collection));
		addAll(collection);
	}

	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public boolean add(E element)
	{
		return offer(element);
	}
	
	@Override
	public void clear()
	{
		_queue.clear();
		_keyToEntry.clear();
		_insertOrder = 0;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new IteratorImpl();
	}

	@Override
	public boolean removeAll(Collection<?> collection)
	{
		boolean changed = false;
		for (Object object : collection)
		{
			changed |= remove(object);
		}
		return changed;
	}

	@Override
	public int size()
	{
		return _queue.size();
	}

	@Override
	public Object[] toArray()
	{
		return toArray(new Object[size()]);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] array)
	{
		if (array.length < size())
		{
			array = (T[]) Array.newInstance(array.getClass().getComponentType(), size());
		}
		
		int i = 0;
		for (Entry<E> entry : _queue)
		{
			array[i++] = (T) entry._element;
		}
		
		return array;
	}

	/*---------------
	 * Queue methods
	 */
	
	@Override
	public E element()
	{
		return _queue.element()._element;
	}

	/**
	 * Adds element to the queue if it does not already contain an element with the same key.
	 * Unlike {@link #add} this will return false instead of throwing an exception if the
	 * element was not added because its key was found.
	 * <p>
	 * If the element {@link #comparator()} is null, indicating that natural ordering should be
	 * used, then this method will attempt to cast the element to {@link Comparable}.
	 * <p>
	 * @throws ClassCastException if queue has no explicit comparator and element is not an instance of
	 * {@link Comparable}
	 * @throws NullPointerException if element is null
	 */
	@Override
	public boolean offer(E element)
	{
		K key = getKeyFromElement(element);
		if (!_keyToEntry.containsKey(key))
		{
			if (_priorityComparator == null)
			{
				// If there is no explicit comparator, then the natural ordering will be used, which
				// requires that the elements must implement Comparable.
				Comparable.class.cast(element);
			}
			Entry<E> entry = new Entry<E>(element, _insertOrder++);
			_keyToEntry.put(key, entry);
			_queue.add(entry);
			return true;
		}
		
		return false;
	}

	@Override
	public @Nullable E peek()
	{
		Entry<E> entry = _queue.peek();
		return entry != null ? entry._element : null;
	}

	@Override
	public @Nullable E poll()
	{
		Entry<E> entry = _queue.poll();
		if (entry == null)
		{
			_insertOrder = 0;
			return null;
		}
		else
		{
			E element = entry._element;
			_keyToEntry.remove(getKeyFromElement(element));
			return element;
		}
	}

	/*------------------------------------
	 * AbstractKeyedPriorityQueue methods
	 */
	
	/**
	 * The comparator used to determine priority order among elements in the queue.
	 * If null, then natural ordering of elements will be used and methods that
	 * add elements to the queue will only accept {@link Comparable} objects.
	 */
	public final @Nullable Comparator<? super E> comparator()
	{
		return _priorityComparator;
	}
	
	/**
	 * Returns the key for a given element.
	 */
	protected abstract @Nullable K getKeyFromElement(@NonNull E element);
	
	/**
	 * Indicates whether queue contains element with the given key.
	 */
	protected boolean containsKey(@Nullable Object key)
	{
		return _keyToEntry.containsKey(key);
	}
	
	/**
	 * Returns element in queue with given key or null.
	 */
	protected @Nullable E get(@Nullable Object key)
	{
		Entry<E> entry = _keyToEntry.get(key);
		return entry != null ? entry._element : null;
	}
	
	/**
	 * Remove entry with given key from the queue.
	 * @return false if queue did not contain element for the key.
	 */
	protected boolean removeKey(@Nullable Object key)
	{
		Entry<E> entry = _keyToEntry.remove(key);
		if (entry != null)
		{
			_queue.remove(entry);
			if (_queue.isEmpty())
			{
				_insertOrder = 0;
			}
			return true;
		}
		return false;
	}
	
	/*---------------
	 * Inner classes
	 */
	
	@Immutable
	protected static class Entry<E> implements Serializable
	{
		private static final long serialVersionUID = 1L;
		
		protected final E _element;
		protected final long _insertOrder;
	
		protected Entry(E element, long insertOrder)
		{
			_element = element;
			_insertOrder = insertOrder;
		}
	}
	
	@Immutable
	private static class EntryComparator<E> implements Comparator<Entry<E>>, Serializable
	{
		private static final long serialVersionUID = 1L;
		
		private final Comparator<? super E> _comparator;
		
		private EntryComparator(Comparator<? super E> comparator)
		{
			_comparator = comparator;
		}
		
		@Override
		public int compare(Entry<E> entry1, Entry<E> entry2)
		{
			int diff = _comparator.compare(entry1._element, entry2._element);
			if (diff == 0)
			{
				diff = Long.signum(entry1._insertOrder - entry2._insertOrder);
			}
			return diff;
		}
	}
	
	/*---------------
	 * Inner classes
	 */
	
	@NotThreadSafe
	private class IteratorImpl implements Iterator<E>
	{
		private @NonNull final Iterator<Entry<E>> _iterator;
		private @Nullable E _last = null;
		
		private IteratorImpl()
		{
			_iterator = _queue.iterator();
		}
		
		@Override
		public boolean hasNext()
		{
			return _iterator.hasNext();
		}

		@Override
		public @Nullable E next()
		{
			return _last = _iterator.next()._element;
		}

		@Override
		public void remove()
		{
			final E last = _last;
			
			if (last == null)
			{
				throw new IllegalStateException("Iterator.remove() invoked not called after next()");
			}
			
			_iterator.remove();
			// If that didn't throw an exception, remove from the map as well.
			_keyToEntry.remove(getKeyFromElement(last));
			_last = null;
		}
	}
}
