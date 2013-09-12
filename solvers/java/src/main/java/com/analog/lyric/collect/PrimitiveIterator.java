package com.analog.lyric.collect;

import java.util.Iterator;

/**
 * Iterators supporting unboxed iteration of primitive types.
 */
public interface PrimitiveIterator<T> extends Iterator<T>
{
	// JAVA8: This is a partial copy of part of a new interface proposed for java.util package in Java 8.
	 
	public static interface OfDouble extends PrimitiveIterator<Double>
	{
		@Override
		public Double next();
		
		/**
		 * @return next double in iteration.
		 */
		public double nextDouble();
	}

	public static interface OfFloat extends PrimitiveIterator<Float>
	{
		@Override
		public Float next();
		
		/**
		 * @return next float in iteration.
		 */
		public float nextFloat();
	}

	public static interface OfInt extends PrimitiveIterator<Integer>
	{
		@Override
		public Integer next();
		
		/**
		 * @return next int in iteration.
		 */
		public int nextInt();
	}

	public static interface OfLong extends PrimitiveIterator<Long>
	{
		@Override
		public Long next();
		
		/**
		 * @return next long in iteration.
		 */
		public long nextLong();
	}
}
