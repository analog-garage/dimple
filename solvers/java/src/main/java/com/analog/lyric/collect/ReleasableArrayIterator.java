package com.analog.lyric.collect;

import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;

/**
 * {@link ReleasableIterator} that visits all of the elements of an array.
 * 
 * @since 0.05
 */
@NotThreadSafe
public final class ReleasableArrayIterator<T> implements ReleasableIterator<T>
{
	private T[] _array;
	private int _size;
	private int _index;

	/*---------------
	 * Construction
	 */
	
	private static final AtomicReference<ReleasableArrayIterator<?>> _reusableInstance =
		new AtomicReference<ReleasableArrayIterator<?>>();
	
	/**
	 * Allocates a new instance for given {@code array}.
	 * <p>
	 * This method is thread safe.
	 */
	static public <T2> ReleasableArrayIterator<T2> create(T2[] array)
	{
		ReleasableArrayIterator<T2> iter = (ReleasableArrayIterator<T2>) _reusableInstance.getAndSet(null);
		if (iter == null)
		{
			iter = new ReleasableArrayIterator<T2>();
		}
		iter.reset(array);
		return iter;
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _index < _size;
	}

	@Override
	public T next()
	{
		return _array[_index++];
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	/*----------------------------
	 * ReleasableIterator methods
	 */
	
	@Override
	public void release()
	{
		_array = null;
		_reusableInstance.lazySet(this);
	}
	
	void reset(T[] array)
	{
		_array = array;
		_size = array.length;
		_index = 0;
	}
}
