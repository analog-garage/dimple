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

import org.eclipse.jdt.annotation.Nullable;

/**
 * A priority queue that only holds one instance of an element.
 * <p>
 * This is similar to {@link java.util.PriorityQueue} except that:
 * <ul>
 * <li>Calls to {@link #offer(Object)} and {@link #add(Object)} will return false when element is
 * already in the queue.
 * <li>Calls to {@link #contains(Object)} are O(1) instead of O(n).
 * <li>When two elements have the same priority, the first element added will be returned by
 * {@link #remove()} et al.
 * </ul>
 * 
 * @since 0.05
 * @author Christopher Barber
 * @see KeyedPriorityQueue
 */
@NotThreadSafe
public class UniquePriorityQueue<E> extends AbstractKeyedPriorityQueue<E,E> implements Serializable
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
	 * Invokes {@link #UniquePriorityQueue(int, Comparator)} with a small default capacity
	 * and no comparator.
	 */
	public UniquePriorityQueue()
	{
		this(11);
	}
	
	/**
	 * Constructs empty queue
	 * <p>
	 * Invokes {@link #UniquePriorityQueue(int, Comparator)} with given {@code initialCapacity}
	 * and no comparator.
	 */
	public UniquePriorityQueue(int initialCapacity)
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
	public UniquePriorityQueue(int initialCapacity, @Nullable Comparator<? super E> priorityComparator)
	{
		super(initialCapacity, priorityComparator);
	}
	
	/**
	 * Constructs queue with elements from collection.
	 * 
	 * @param collection specifies the elements to add to the queue which will be added in the
	 * same order as the collection's iterator. If the collection is a {@link java.util.SortedSet}, then
	 * its comparator will be used to determine priorities. Otherwise the natural order will be used
	 * (see {@link #UniquePriorityQueue(int, Comparator)}).
	 */
	public UniquePriorityQueue(Collection<? extends E> collection)
	{
		super(collection);
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public boolean contains(@Nullable Object object)
	{
		return containsKey(object);
	}

	@Override
	public boolean remove(@Nullable Object object)
	{
		return removeKey(object);
	}

	/*------------------------------------
	 * AbstractKeyedPriorityQueue methods
	 */
	
	@Override
	protected E getKeyFromElement(E element)
	{
		return element;
	}
}
