package com.analog.lyric.collect;

import java.lang.reflect.Array;

import com.google.common.math.DoubleMath;

/**
 * Contains static utility methods pertaining to arrays.
 */
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
	 * Canonical empty int[][].
	 */
	public static final int[][] EMPTY_INT_ARRAY_ARRAY = new int[0][];
	
	/**
	 * Returns an array with length at least {@code minSize} and with component type
	 * compatible with {@code type}.
	 * <p>
	 * If {@code array} fits the above description, it will simply be returned.
	 * If {@code array} is too short but has a compatible component type, this will
	 * return a new array with length equal to {@code minSize} and component type
	 * the same as {@code array}. Otherwise returns a new array with component
	 * type same as {@code type}.
	 */
	public static <T> T[] allocateArrayOfType(Class<?> type, T[] array, int minSize)
	{
		if (array != null)
		{
			Class<? extends T> componentType = (Class<? extends T>) array.getClass().getComponentType();
			if (componentType.isAssignableFrom(type))
			{
				if (array.length >= minSize)
				{
					return array;
				}
				else
				{
					type = componentType;
				}
			}
		}
		
		return (T[])Array.newInstance(type, minSize);
	}
	
	/**
	 * True if all values in {@code array} are {@link DoubleMath#fuzzyEquals}
	 * to each other with given {@code tolerance}. Returns true for arrays
	 * of less than length 2.
	 * 
	 * @see #subsetFuzzyEqual(double[], int[], double)
	 */
	public static boolean allFuzzyEqual(double[] array, double tolerance)
	{
		if (array.length > 1)
		{
			double min , max;
			min = max = array[0];

			for (int i = 1, end = array.length; i < end ; ++i)
			{
				final double d = array[i];
				min = Math.min(min, d);
				max = Math.max(max, d);
			}
			if (!DoubleMath.fuzzyEquals(min, max, tolerance))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * True if all values in {@code array} whose indices are in {@code arraySubindices}
	 * are {@link DoubleMath#fuzzyEquals} to each other with given {@code tolerance}. Returns true if
	 * length of {@code arraySubindices} is less than length 2.
	 * 
	 * @param arraySubindices must be no larger than {@code array} and should contain indexes in the
	 * range [0,array.length-1].
	 * 
	 * @see #allFuzzyEqual(double[], double)
	 */
	public static boolean subsetFuzzyEqual(double[] array, int[] arraySubindices, double tolerance)
	{
		if (arraySubindices.length > 1)
		{
			double min , max;
			min = max = array[arraySubindices[0]];

			for (int i = 1, end = arraySubindices.length; i < end ; ++i)
			{
				final double d = array[arraySubindices[i]];
				min = Math.min(min, d);
				max = Math.max(max, d);
			}
			if (!DoubleMath.fuzzyEquals(min, max, tolerance))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns a copy of {@code array} returning null if {@code array}
	 * is null and returns {@link #EMPTY_DOUBLE_ARRAY} if empty.
	 */
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

	/**
	 * Returns a copy of {@code array} returning null if {@code array}
	 * is null and returns {@link #EMPTY_INT_ARRAY} if empty.
	 */
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
	
	/**
	 * Returns a copy of {@code array} returning null if {@code array}
	 * is null and returns {@link #EMPTY_INT_ARRAY_ARRAY} if empty.
	 * Note that this does not make a deep copy of the array elements.
	 */
	public static int[][] cloneArray(int[][] array)
	{
		if (array == null)
		{
			return null;
		}
		else if (array.length == 0)
		{
			return EMPTY_INT_ARRAY_ARRAY;
		}
		else
		{
			return array.clone();
		}
	}

	/**
	 * Returns a copy of {@code array} but with space for insertion of {@code insertLength}
	 * values at offset {@code insertionPoint}.
	 * @throws ArrayIndexOutOfBoundsException if {@code insertionPoint} is not in the range
	 * [0, array.length].
	 */
	public static double[] copyArrayForInsert(double[] array, int insertionPoint, int insertLength)
	{
		if (array == null)
		{
			return new double[insertLength];
		}
		
		int curSize = array.length;
		
		double[] newArray = new double[curSize + insertLength];
		
		System.arraycopy(array, 0, newArray, 0, insertionPoint);
		System.arraycopy(array, insertionPoint, newArray, insertionPoint + insertLength, curSize - insertionPoint);
		
		return newArray;
	}

	/**
	 * Returns a copy of {@code array} but with space for insertion of {@code insertLength}
	 * values at offset {@code insertionPoint}.
	 * @throws ArrayIndexOutOfBoundsException if {@code insertionPoint} is not in the range
	 * [0, array.length].
	 */
	public static int[] copyArrayForInsert(int[] array, int insertionPoint, int insertLength)
	{
		if (array == null)
		{
			return new int[insertLength];
		}
		int curSize = array.length;
		
		int[] newArray = new int[curSize + insertLength];
		
		System.arraycopy(array, 0, newArray, 0, insertionPoint);
		System.arraycopy(array, insertionPoint, newArray, insertionPoint + insertLength, curSize - insertionPoint);
		
		return newArray;
	}
	
	/**
	 * Returns a copy of {@code array} but with space for insertion of {@code insertLength}
	 * values at offset {@code insertionPoint}. Note that this does not make a deep copy of the array elements.
	 * @throws ArrayIndexOutOfBoundsException if {@code insertionPoint} is not in the range
	 * [0, array.length].
	 */
	public static int[][] copyArrayForInsert(int[][] array, int insertionPoint, int insertLength)
	{
		if (array == null)
		{
			return new int[insertLength][];
		}
		int curSize = array.length;
		
		int[][] newArray = new int[curSize + insertLength][];
		
		System.arraycopy(array, 0, newArray, 0, insertionPoint);
		System.arraycopy(array, insertionPoint, newArray, insertionPoint + insertLength, curSize - insertionPoint);
		
		return newArray;
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
