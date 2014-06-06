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
