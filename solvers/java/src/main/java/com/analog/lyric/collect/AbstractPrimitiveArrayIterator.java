package com.analog.lyric.collect;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

/**
 * Partial implementation of {@link PrimitiveIterator} based on an
 * array of the primitive type.
 */
@ThreadSafe
public abstract class AbstractPrimitiveArrayIterator<T> implements PrimitiveIterator<T>
{
	/*-------
	 * State
	 */
	
	/**
	 * The one more than the index of the last element in the underlying
	 * array to be included in the iteration.
	 */
	protected final int _end;

	/**
	 * The array index of the next element to be returned by the iterator.
	 */
	private final AtomicInteger _index;

	/*--------------
	 * Construction
	 */
	
	/**
	 * @param start is the index of the first element in the array to
	 * be included in the iteration.
	 * @param end is one past the index of the last element in the array
	 * to be included in the iteration.
	 */
	protected AbstractPrimitiveArrayIterator(int start, int end)
	{
		assert(start >= 0);
		assert(start <= end);
		_end = end;
		_index = new AtomicInteger(start);
	}

	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _index.get() < _end;
	}

	/**
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("PrimitiveIterator.remove");
	}

	/*----------------------------------------
	 * AbstractPrimitiveArrayIterator methods
	 */
	
	/**
	 * @return array index of the next element to be returned by the iteration.
	 */
	protected int nextIndex()
	{
		int i = _index.getAndIncrement();
		if (i >= _end)
		{
			throw new NoSuchElementException();
		}
		return i;
	}

}