package com.analog.lyric.dimple.model;

public class IntRangeDomain extends TypedDiscreteDomain<Integer>
{
	private static final long serialVersionUID = 1L;
	
	private final int _first;
	private final int _interval;
	private final int _size;
	
	/*--------------
	 * Construction
	 */
	
	IntRangeDomain(int size, int first, int interval)
	{
		super(computeHashCode(size, first, interval));
		_first = first;
		_size = size;
		_interval = interval;
	}
		
	IntRangeDomain(int size, int first)
	{
		this(size, first, 1);
	}
	
	IntRangeDomain(int size)
	{
		this(size, 0, 1);
	}
	
	private static int computeHashCode(int size, int first, int interval)
	{
		return first + size * 11 + interval * 13;
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
			return _size == thatRange._size && _first == thatRange._first && _interval == thatRange._interval;
		}
		
		return false;
	}
	
	/*------------------------
	 * DiscreteDomain methods
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

	@Override
	public int getIndex(Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			int i = number.intValue();
			if (i == number.doubleValue())
			{
				i -= _first;
				i /= _interval;
				if (i < _size)
				{
					return i;
				}
			}
		}
		
		return -1;
	}

	@Override
	public int size()
	{
		return _size;
	}

	/*------------------------
	 * IntRangeDomain methods
	 */
	
	public int getIntElement(int i)
	{
		return _first + i * _interval;
	}
}
