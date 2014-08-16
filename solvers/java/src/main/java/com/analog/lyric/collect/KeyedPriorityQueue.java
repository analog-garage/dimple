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
import java.util.Collection;
import java.util.Comparator;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A priority queue that works on elements that have inherent keys and that does not allow
 * more than one entry with the same key.
 * <p>
 * This is similar to {@link java.util.PriorityQueue} except that:
 * <ul>
 * <li>Elements must implement the {@link IKeyed} interface.
 * <li>Methods {@link #containsKey(Object)} and {@link #removeKey(Object)} provide key-based methods
 * for manipulating the queue.
 * <li>Calls to {@link #offer(Object)} and {@link #add(Object)} will return false when an element with
 * the same key is already in the queue.
 * <li>Calls to {@link #contains(Object)} are O(1) instead of O(n).
 * <li>When two elements have the same priority, the first element added will be returned by
 * {@link #remove()} et al.
 * </ul>
 * @since 0.05
 * @see UniquePriorityQueue
 */
@NotThreadSafe
@NonNullByDefault(false)
public class KeyedPriorityQueue<K,E extends IKeyed<K>> extends AbstractKeyedPriorityQueue<K,E> implements Serializable
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs empty queue
	 * <p>
	 * Invokes {@link #KeyedPriorityQueue(int, Comparator)} with a small default capacity
	 * and no comparator.
	 */
	public KeyedPriorityQueue()
	{
		this(11, null);
	}
	
	/**
	 * Constructs empty queue
	 * <p>
	 * Invokes {@link #KeyedPriorityQueue(int, Comparator)} with given {@code initialCapacity}
	 * and no comparator.
	 */
	public KeyedPriorityQueue(int initialCapacity)
	{
		this(initialCapacity, null);
	}
	
	/**
	 * Constructs empty queue.
	 * 
	 * @param initialCapacity provides a hint as to the intended size of the queue.
	 * @param priorityComparator defines the priority ordering among elements. If null,
	 * then natural ordering will be used and all elements must implement {@link Comparable}
	 * and must be comparable to existing elements in the queue.
	 */
	public KeyedPriorityQueue(int initialCapacity, @Nullable Comparator<? super E> priorityComparator)
	{
		super(initialCapacity, priorityComparator);
	}
	
	/**
	 * Constructs queue with elements from collection.
	 * 
	 * @param collection specifies the elements to add to the queue which will be added in the
	 * same order as the collection's iterator. If the collection is a {@link java.util.SortedSet}, then
	 * its comparator will be used to determine priorities. Otherwise the natural order will be used
	 * (see {@link #KeyedPriorityQueue(int, Comparator)}).
	 */
	public KeyedPriorityQueue(@NonNull Collection<? extends E> collection)
	{
		super(collection);
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public boolean contains(Object object)
	{
		if (object instanceof IKeyed)
		{
			return _keyToEntry.containsKey(((IKeyed<?>)object).getKey());
		}

		return false;
	}

	@Override
	public boolean remove(Object object)
	{
		if (object instanceof IKeyed)
		{
			return removeKey(((IKeyed<?>)object).getKey());
		}

		return false;
	}

	/*----------------------------
	 * KeyedPriorityQueue methods
	 */

	@Override
	protected @Nullable K getKeyFromElement(@NonNull E element)
	{
		return element.getKey();
	}
	
	/**
	 * Indicates whether queue contains element with the given key.
	 */
	@Override
	public boolean containsKey(@Nullable Object key)
	{
		return super.containsKey(key);
	}
	
	@Override
	public @Nullable E get(@Nullable Object key)
	{
		return super.get(key);
	}
	
	@Override
	public boolean removeKey(@Nullable Object key)
	{
		return super.removeKey(key);
	}
	
	
}
