package com.analog.lyric.collect;

public class FloatArrayIterable
	extends AbstractPrimitiveArrayIterable<Float>
	implements PrimitiveIterable.OfFloat
{
	protected final float[] _array;
	
	public FloatArrayIterable(float[] array, int start, int end)
	{
		super(start, end);
		assert(end <= array.length);
		_array = array;
	}
	
	public FloatArrayIterable(float[] array)
	{
		this(array, 0, array.length);
	}

	@Override
	public FloatArrayIterator iterator()
	{
		return new FloatArrayIterator(_array, _start, _end);
	}
}
