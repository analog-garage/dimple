package com.analog.lyric.collect;

public class IntArrayIterable
	extends AbstractPrimitiveArrayIterable<Integer>
	implements PrimitiveIterable.OfInt
{
	protected final int[] _array;
	
	public IntArrayIterable(int[] array, int start, int end)
	{
		super(start, end);
		assert(end <= array.length);
		_array = array;
	}
	
	public IntArrayIterable(int[] array)
	{
		this(array, 0, array.length);
	}

	@Override
	public IntArrayIterator iterator()
	{
		return new IntArrayIterator(_array, _start, _end);
	}
}
