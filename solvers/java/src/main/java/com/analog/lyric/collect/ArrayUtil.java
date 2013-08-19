package com.analog.lyric.collect;

import java.lang.reflect.Array;

import com.google.common.math.DoubleMath;

public abstract class ArrayUtil
{
	/**
	 * Canonical empty double array.
	 */
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	
	/**
	 * Canonical empty int array.
	 */
	public static final int[] EMPTY_INT_ARRAY = new int[0];

	/**
	 * True if all values in {@code array} are {@link DoubleMath#fuzzyEquals}
	 * to each other with given {@code tolerance}. Returns true for arrays
	 * of less than length 2.
	 */
	public static boolean allFuzzyEqual(double[] array, double tolerance)
	{
		if (array.length > 1)
		{
			final double first = array[0];

			for (int i = 1, end = array.length; i < end ; ++i)
			{
				if (!DoubleMath.fuzzyEquals(first,  array[i], tolerance))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static double[] cloneArray(double[] array)
	{
		if (array == null)
		{
			return null;
		}
		else if (array.length == 0)
		{
			return EMPTY_DOUBLE_ARRAY;
		}
		else
		{
			return array.clone();
		}
	}

	public static int[] cloneArray(int[] array)
	{
		if (array == null)
		{
			return null;
		}
		else if (array.length == 0)
		{
			return EMPTY_INT_ARRAY;
		}
		else
		{
			return array.clone();
		}
	}
	public static double[] copyArrayForInsert(double[] array, int insertionPoint, int insertLength)
	{
		int curSize = array == null ? 0 : array.length;
		
		assert(insertionPoint >= 0 && insertionPoint <= curSize);
		assert(insertLength >= 0);
		
		
		double[] newArray = new double[curSize + insertLength];
		
		for (int i = 0; i < insertionPoint; ++i)
		{
			newArray[i] = array[i];
		}
		for (int i = insertionPoint, j = insertionPoint + insertLength; i < curSize; ++i, ++j)
		{
			newArray[j] = array[i];
		}
		
		return newArray;
	}
	public static int[] copyArrayForInsert(int[] array, int insertionPoint, int insertLength)
	{
		int curSize = array == null ? 0 : array.length;
		
		assert(insertionPoint >= 0 && insertionPoint <= curSize);
		assert(insertLength >= 0);
		
		
		int[] newArray = new int[curSize + insertLength];
		
		for (int i = 0; i < insertionPoint; ++i)
		{
			newArray[i] = array[i];
		}
		for (int i = insertionPoint, j = insertionPoint + insertLength; i < curSize; ++i, ++j)
		{
			newArray[j] = array[i];
		}
		
		return newArray;
	}
	
	public static double[] repeat(double[] array, int multiplier)
	{
		if (array.length > 0)
		{
			final double[] newArray = new double[array.length * multiplier];
			for (int i = 0, m = 0; m < multiplier; ++m)
			{
				for (double value : array)
				{
					newArray[i++] = value;
				}
			}
			array = newArray;
		}
		return array;
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
}
