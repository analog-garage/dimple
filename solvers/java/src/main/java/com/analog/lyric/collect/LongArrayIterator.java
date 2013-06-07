package com.analog.lyric.collect;


import net.jcip.annotations.ThreadSafe;

/**
 * {@link PrimitiveIterator.OfLong} implementation based on an underlying
 * long array.
 * <p>
 * Thread safe but could return bad value if underlying long array
 * is modified in another thread during iteration because reading/writing
 * longs is not atomic in Java.
 */
@ThreadSafe
public final class LongArrayIterator extends AbstractPrimitiveArrayIterator<Long> implements PrimitiveIterator.OfLong
{
	/*-------
	 * State
	 */
	
	final long[] _array;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs iterator that will visit all the members of {@code array}
	 * in increasing order starting at array index {@code start} and ending at
	 * array index {@code end} - 1.
	 * <p>
	 * Does not make a copy of the array, so avoid modifying it in another
	 * thread while iterating.
	 * <p>
	 * @see #LongArrayIterator(long[])
	 */
	public LongArrayIterator(long[] array, int start, int end)
	{
		super(start, end);
		_array = array;
		assert(end <= array.length);
	}
	
	/**
	 * Invokes {@link #LongArrayIterator(long[],int,int)} with
	 * {@code start} set to zero, and {@code end} set to {@code array}'s
	 * length.
	 */
	public LongArrayIterator(long[] array)
	{
		this(array, 0, array.length);
	}
	
	/*---------------------------
	 * PrimitiveIterator methods
	 */
	
	@Override
	public Long next()
	{
		return nextLong();
	}

	@Override
	public long nextLong()
	{
		return _array[nextIndex()];
	}
}
