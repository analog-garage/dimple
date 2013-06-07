package com.analog.lyric.collect;

public interface PrimitiveIterable<T> extends Iterable<T>
{
	@Override
	public PrimitiveIterator<T> iterator();
	
	public static interface OfDouble extends PrimitiveIterable<Double>
	{
		@Override
		public PrimitiveIterator.OfDouble iterator();
	}

	public static interface OfFloat extends PrimitiveIterable<Float>
	{
		@Override
		public PrimitiveIterator.OfFloat iterator();
	}
	
	public static interface OfInt extends PrimitiveIterable<Integer>
	{
		@Override
		public PrimitiveIterator.OfInt iterator();
	}
	
	public static interface OfLong extends PrimitiveIterable<Long>
	{
		@Override
		public PrimitiveIterator.OfLong iterator();
	}
}
