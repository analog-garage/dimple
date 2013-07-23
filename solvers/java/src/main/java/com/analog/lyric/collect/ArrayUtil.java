package com.analog.lyric.collect;

import java.lang.reflect.Array;

public abstract class ArrayUtil
{
	/**
	 * Converts value to an int array or else returns null.
	 * <p>
	 * If {@code value} is an int array it will simply be returned.
	 * If it is another type of array, whose elements are
	 * all integral values, then a new array with those values will
	 * be returned. Otherwise returns null.
	 */
	public static int[] toIntArray(Object value)
	{
		if (value instanceof int[])
		{
			return (int[])value;
		}
		
		if (value.getClass().isArray())
		{
			final int size = Array.getLength(value);
			final int[] array = new int[size];
			for (int i = 0; i < size; ++i)
			{
				Object obj = Array.get(value, i);
				if (obj instanceof Number)
				{
					Number number = (Number)obj;
					int index = number.intValue();
					if (index == number.doubleValue())
					{
						array[i] = index;
						continue;
					}
				}
				return null;
			}
			
			return array;
		}

		return null;
	}
	
	public static Object[] toArray(Object value)
	{
		if (value instanceof Object[])
		{
			return (Object[])value;
		}
		
		if (value.getClass().isArray())
		{
			final int size = Array.getLength(value);
			final Object[] array = new Object[size];
			for (int i = 0; i < size; ++i)
			{
				array[i] = Array.get(value, i);
			}
			return array;
		}

		return null;
	}
}
