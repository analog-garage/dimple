package com.analog.lyric.dimple.model;

/**
 * A discrete domain defined by a range of integers separated by a constant positive interval.
 * <p>
 * @see DiscreteDomain#range(int, int)
 * @see DiscreteDomain#range(int, int, int)
 */
public class IntRangeDomain extends TypedDiscreteDomain<Integer>
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
		_size = (interval + upperBound - lowerBound) / interval;
		_interval = interval;
	}
		
	private static int computeHashCode(int lowerBound, int upperBound, int interval)
	{
		return lowerBound + (upperBound * 11 + interval) * 13;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object that)
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
	 * {@inheritDoc}
	 * <p>
	 * Use {@link #getIntElement(int)} instead of this method to avoid allocating an {@link Integer} object.
	 */
	@Override
	public Integer getElement(int i)
	{
		return getIntElement(i);
	}

	@Override
	public Integer[] getElements()
	{
		Integer[] elements = new Integer[_size];
		
		for (int i = 0; i < _size; ++i)
		{
			elements[i] = getIntElement(i);
		}
		
		return elements;
	}

	/**
	 * {@inheritDoc}
	 * @see #getIndex(int)
	 */
	@Override
	public int getIndex(Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			int i = number.intValue();
			if (i == number.doubleValue())
			{
				return getIndex(i);
			}
		}
		
		return -1;
	}
	
	/**
	 * Same as {@link #getIndex(Object)} but taking an int.
	 */
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
