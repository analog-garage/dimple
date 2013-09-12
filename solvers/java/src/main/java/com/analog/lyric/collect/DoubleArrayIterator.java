package com.analog.lyric.collect;

import net.jcip.annotations.ThreadSafe;

/**
 * {@link PrimitiveIterator.OfDouble} implementation based on an underlying
 * double array.
 * <p>
 * Thread safe but could return bad value if underlying double array
 * is modified in another thread during iteration because reading/writing
 * doubles is not atomic in Java.
 */
@ThreadSafe
public class DoubleArrayIterator extends AbstractPrimitiveArrayIterator<Double>
	implements PrimitiveIterator.OfDouble
{
	/*-------
	 * State
	 */
	
	protected final double[] _array;
	
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
	 * @see #DoubleArrayIterator(double[])
	 */
	public DoubleArrayIterator(double[] array, int start, int end)
	{
		super(start, end);
		assert(end <= array.length);
		_array = array;
	}

	/**
	 * Invokes {@link #DoubleArrayIterator(double[],int,int)} with
	 * {@code start} set to zero, and {@code end} set to {@code array}'s
	 * length.
	 */
	public DoubleArrayIterator(double[] array)
	{
		this(array, 0, array.length);
	}

	/*---------------------------
	 * PrimitiveIterator methods
	 */
	
	@Override
	public final Double next()
	{
		return nextDouble();
	}

	@Override
	public final double nextDouble()
	{
		return _array[nextIndex()];
	}
}
