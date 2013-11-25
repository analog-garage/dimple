package com.analog.lyric.collect;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;

import net.jcip.annotations.NotThreadSafe;

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
	public KeyedPriorityQueue(int initialCapacity, Comparator<? super E> priorityComparator)
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
	public KeyedPriorityQueue(Collection<? extends E> collection)
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
	protected K getKeyFromElement(E element)
	{
		return element.getKey();
	}
	
	/**
	 * Indicates whether queue contains element with the given key.
	 */
	@Override
	public boolean containsKey(Object key)
	{
		return super.containsKey(key);
	}
	
	@Override
	public E get(Object key)
	{
		return super.get(key);
	}
	
	@Override
	public boolean removeKey(Object key)
	{
		return super.removeKey(key);
	}
	
	
}
