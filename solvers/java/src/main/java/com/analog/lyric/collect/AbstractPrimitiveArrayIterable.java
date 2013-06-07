package com.analog.lyric.collect;

public abstract class AbstractPrimitiveArrayIterable<T> implements PrimitiveIterable<T>
{
	protected final int _start;
	protected final int _end;

	protected AbstractPrimitiveArrayIterable(int start, int end)
	{
		assert(start <= end);
		_start = start;
		_end = end;
	}
}
