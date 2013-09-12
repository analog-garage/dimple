package com.analog.lyric.collect;

public class DoubleArrayIterable
	extends AbstractPrimitiveArrayIterable<Double>
	implements PrimitiveIterable.OfDouble
{
	protected final double[] _array;
	
	public DoubleArrayIterable(double[] array, int start, int end)
	{
		super(start, end);
		assert(end <= array.length);
		_array = array;
	}
	
	public DoubleArrayIterable(double[] array)
	{
		this(array, 0, array.length);
	}

	@Override
	public DoubleArrayIterator iterator()
	{
		return new DoubleArrayIterator(_array, _start, _end);
	}
}
