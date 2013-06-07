package com.analog.lyric.collect;

public class LongArrayIterable
	extends AbstractPrimitiveArrayIterable<Long>
	implements PrimitiveIterable.OfLong
{
	protected final long[] _array;
	
	public LongArrayIterable(long[] array, int start, int end)
	{
		super(start, end);
		assert(end <= array.length);
		_array = array;
	}
	
	public LongArrayIterable(long[] array)
	{
		this(array, 0, array.length);
	}

	@Override
	public LongArrayIterator iterator()
	{
		return new LongArrayIterator(_array, _start, _end);
	}
}
