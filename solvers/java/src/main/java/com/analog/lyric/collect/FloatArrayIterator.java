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
 * {@link PrimitiveIterator.OfFloat} implementation based on an underlying
 * float array.
 */
@ThreadSafe
public final class FloatArrayIterator extends AbstractPrimitiveArrayIterator<Float> implements PrimitiveIterator.OfFloat
{
	/*-------
	 * State
	 */
	
	final float[] _array;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs iterator that will visit all the members of {@code array}
	 * in increasing order starting at array index {@code start} and ending at
	 * array index {@code end} - 1.
	 * <p>
	 * Does not make a copy of the array, so changes to array in another
	 * thread may be reflected in iteration.
	 * <p>
	 * @see #FloatArrayIterator(float[])
	 */
	public FloatArrayIterator(float[] array, int start, int end)
	{
		super(start, end);
		assert(end <= array.length);
		_array = array;
	}
	
	/**
	 * Invokes {@link #FloatArrayIterator(float[],int,int)} with
	 * {@code start} set to zero, and {@code end} set to {@code array}'s
	 * length.
	 */
	public FloatArrayIterator(float[] array)
	{
		this(array, 0, array.length);
	}
	
	/*---------------------------
	 * PrimitiveIterator methods
	 */
	
	@Override
	public Float next()
	{
		return nextFloat();
	}

	@Override
	public float nextFloat()
	{
		return _array[nextIndex()];
	}
}
