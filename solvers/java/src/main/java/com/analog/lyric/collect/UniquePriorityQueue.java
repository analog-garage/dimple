package com.analog.lyric.collect;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;

import net.jcip.annotations.NotThreadSafe;

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
	
	public UniquePriorityQueue()
	{
		this(11);
	}
	
	public UniquePriorityQueue(int initialCapacity)
	{
		this(initialCapacity, null);
	}
	
	public UniquePriorityQueue(int initialCapacity, Comparator<? super E> comparator)
	{
		super(initialCapacity, comparator);
	}
	
	public UniquePriorityQueue(Collection<? extends E> collection)
	{
		super(collection);
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public boolean contains(Object object)
	{
		return containsKey(object);
	}

	@Override
	public boolean remove(Object object)
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
