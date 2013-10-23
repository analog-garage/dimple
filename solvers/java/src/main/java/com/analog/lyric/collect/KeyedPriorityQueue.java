package com.analog.lyric.collect;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;

import net.jcip.annotations.NotThreadSafe;

/**
 * A priority queue that works on elements that have inherent keys and that does not allow
 * more than one entry with the same key.
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
	
	public KeyedPriorityQueue()
	{
		this(11, null);
	}
	
	public KeyedPriorityQueue(int initialCapacity)
	{
		this(initialCapacity, null);
	}
	
	public KeyedPriorityQueue(int initialCapacity, Comparator<? super E> comparator)
	{
		super(initialCapacity, comparator);
	}
	
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
	public boolean removeKey(Object key)
	{
		return super.removeKey(key);
	}
	
	
}
