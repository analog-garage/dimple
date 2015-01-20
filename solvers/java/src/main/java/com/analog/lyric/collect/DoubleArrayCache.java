/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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
 * A cache of double[] for temporary use.
 * <p>
 * Use this to manage double arrays to avoid allocating and gc'ing many temporary
 * arrays.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@ThreadSafe
public class DoubleArrayCache extends SoftIntObjectCache<double[]>
{
	/*--------------------------
	 * DoubleArrayCache methods
	 */
	
	/**
	 * Returns new array of given size from cache or returns a newly allocated one.
	 * <p>
	 * The caller must not assume that the array has been zeroed out!
	 * <p>
	 * @since 0.08
	 */
	public double[] allocate(int size)
	{
		final double[] array = remove(size);
		return array != null ? array : new double[size];
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * @throws IllegalArgumentException if {@code length} is not the same as the array's length.
	 * @see #release
	 */
	@Override
	public boolean put(int length, double[] array)
	{
		if (length != array.length)
		{
			throw new IllegalArgumentException(
				String.format("Length %d does not match array length %d", length, array.length));
		}
		return super.put(length, array);
	}
	
	/**
	 * Returns an array to the cache for reuse.
	 * <p>
	 * @return True if array was inserted, or false if another array of that size was already in the cache.
	 * @since 0.08
	 */
	public boolean release(double[] array)
	{
		return super.put(array.length, array);
	}
	
}
