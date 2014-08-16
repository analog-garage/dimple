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

package com.analog.lyric.dimple.model.domains;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A discrete domain defined by a range of integers separated by a constant positive interval.
 * <p>
 * @see DiscreteDomain#range(int, int)
 * @see DiscreteDomain#range(int, int, int)
 */
public class IntRangeDomain extends IntDiscreteDomain
{
	private static final long serialVersionUID = 1L;
	
	private final int _lowerBound;
	private final int _upperBound;
	private final int _size;
	private final int _interval;
	
	/*--------------
	 * Construction
	 */
	
	IntRangeDomain(int lowerBound, int upperBound, int interval)
	{
		super(computeHashCode(lowerBound, upperBound, interval));
		
		if (interval < 1)
		{
			throw new IllegalArgumentException(
				String.format("Non-positive interval '%d' for integer range domain", interval));
		}
		
		if (upperBound < lowerBound)
		{
			throw new IllegalArgumentException(
				String.format("Bad integer range [%d,%d]: upper bound lower than lower bound", lowerBound, upperBound));
		}
		
		_lowerBound = lowerBound;
		_upperBound = upperBound;
		_interval = interval;
		
		long longInterval = interval;
		long size = (longInterval + upperBound - lowerBound) / longInterval;
		
		if (size > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException("Bad integer range: more than MAX_INTEGER (2^32) values in range");
		}
		
		_size = (int)size;
	}
		
	private static int computeHashCode(int lowerBound, int upperBound, int interval)
	{
		return lowerBound + (upperBound * 11 + interval) * 13;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object that)
	{
		if (this == that)
		{
			return true;
		}
		
		if (that instanceof IntRangeDomain)
		{
			IntRangeDomain thatRange = (IntRangeDomain)that;
			return
				_lowerBound == thatRange._lowerBound &&
				_upperBound == thatRange._upperBound &&
				_interval == thatRange._interval;
		}
		
		return false;
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */
	
	/**
	 * Same as {@link #getIndex(Object)} but taking an int.
	 */
	@Override
	public int getIndex(int value)
	{
		value -= _lowerBound;
		value /= _interval;
		return value < _size ? value : -1;
	}

	@Override
	public int size()
	{
		return _size;
	}

	/*------------------------
	 * IntRangeDomain methods
	 */
	
	/**
	 * Same as {@link #getElement(int)} but returning an unboxed int.
	 */
	@Override
	public int getIntElement(int i)
	{
		assertIndexInBounds(i, _size);
		return _lowerBound + i * _interval;
	}
	
	/**
	 * The interval separating consecutive elements of the domain.
	 * <p>
	 * Guaranteed to be positive.
	 */
	public int getInterval()
	{
		return _interval;
	}
	
	/**
	 * Returns the lower bound for the domain which is also the same as the first element.
	 * <p>
	 * @see #getUpperBound()
	 */
	public int getLowerBound()
	{
		return _lowerBound;
	}
	
	/**
	 * Returns the upper bound for the domain. This will only be a member of the domain if it can be expressed
	 * as an exact multiple of the interval added to the lower bound.
	 * <p>
	 * @see #getInterval()
	 * @see #getLowerBound()
	 */
	public int getUpperBound()
	{
		return _upperBound;
	}
}
