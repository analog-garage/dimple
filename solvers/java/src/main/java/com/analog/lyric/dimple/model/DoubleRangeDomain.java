package com.analog.lyric.dimple.model;

import com.google.common.math.DoubleMath;
import com.google.common.primitives.Doubles;


public class DoubleRangeDomain extends TypedDiscreteDomain<Double>
{
	private final double _first;
	private final double _interval;
	private final int _size;
	private final double _tolerance;
	
	/*--------------
	 * Construction
	 */
	
	DoubleRangeDomain(int size, double first, double interval)
	{
		super(computeHashCode(size, first, interval));
		_first = first;
		_size = size;
		_interval = interval;
		_tolerance = defaultToleranceForInterval(interval);
	}
		
	DoubleRangeDomain(int size, double first)
	{
		this(size, first, 1.0);
	}
	
	DoubleRangeDomain(int size)
	{
		this(size, 0.0, 1.0);
	}
	
	private static int computeHashCode(int size, double first, double interval)
	{
		return Doubles.hashCode(first) + size * 11 + Doubles.hashCode(interval) * 13;
	}
	

	@Override
	public Double getElement(int i)
	{
		return getDoubleElement(i);
	}

	@Override
	public Double[] getElements()
	{
		Double[] elements = new Double[_size];
		
		for (int i = 0; i < _size; ++i)
		{
			elements[i] = getDoubleElement(i);
		}
		
		return elements;
	}

	@Override
	public int size()
	{
		return _size;
	}

	@Override
	public int getIndex(Object value)
	{
		if (value instanceof Number)
		{
			Number number = (Number)value;
			double d = number.doubleValue();
			d -= _first;
			d /= _interval;
			int i = (int)d;
			if (i < _size && DoubleMath.fuzzyEquals(i, d, _tolerance))
			{
				return i;
			}
		}
		
		return -1;
	}

	/*------------------------
	 * IntRangeDomain methods
	 */
	
	public double getDoubleElement(int i)
	{
		return _first + i * _interval;
	}

	static double defaultToleranceForInterval(double interval)
	{
		return Math.abs(interval * 1e-6);
	}
}
