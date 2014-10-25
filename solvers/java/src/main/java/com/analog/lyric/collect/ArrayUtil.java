/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.collect;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.list.IntArrayList;

import com.analog.lyric.util.misc.Internal;
import com.google.common.math.DoubleMath;

/**
 * Contains static utility methods pertaining to arrays.
 */
public abstract class ArrayUtil
{
	/**
	 * Canonical empty boolean array.
	 */
	public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
	
	/**
	 * Canonical empty Class array.
	 * @since 0.07
	 */
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	
	/**
	 * Canonical empty double array.
	 */
	public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	
	/**
	 * Canonical empty double[][].
	 * @since 0.06
	 */
	public static final double[][] EMPTY_DOUBLE_ARRAY_ARRAY = new double[0][];
	
	/**
	 * Canonical empty Object array.
	 * 
	 * @since 0.06
	 */
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	
	/**
	 * Canonical empty Object array.
	 * 
	 * @since 0.06
	 */
	public static final Object[][] EMPTY_OBJECT_ARRAY_ARRAY = new Object[0][];
	
	/**
	 * Canonical empty int array.
	 */
	public static final int[] EMPTY_INT_ARRAY = new int[0];

	/**
	 * Canonical empty int[][].
	 */
	public static final int[][] EMPTY_INT_ARRAY_ARRAY = new int[0][];

	/**
	 * Canonical empty String[]
	 * @since 0.07
	 */
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	
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
	public static <T> T[] allocateArrayOfType(Class<?> type, @Nullable T[] array, int minSize)
	{
		if (array != null)
		{
			Class<?> componentType = array.getClass().getComponentType();
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
		
		@SuppressWarnings("unchecked")
		T[] result = (T[])Array.newInstance(type, minSize);
		return result;
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
	 * Given an array of non-negative array indices in ascending order, and another sorted list
	 * of indices of entries to be removed, this returns a new array containing the new values of
	 * the remaining indices
	 * <p>
	 * For example, given the original list [0, 1, 3] and the removed list [1,2], this will
	 * return [0,1].
	 * <p>
	 * @param list is the starting list of non-negative indices in ascending order.
	 * @param remove is a non-empty list of indexes to be removed also in ascending order.
	 * @return a newly allocated array.
	 * @since 0.05
	 */
	public static int[] contractSortedIndexList(int[] list, int[] remove)
	{
		final int originalLength = list.length;
		final int excludeLength = remove.length;

		final IntArrayList result = new IntArrayList(originalLength);
		int iConst = 0;
		int iList = 0;
		int listIndex;
		int constantIndex = remove[iConst];
		while (iList < originalLength)
		{
			listIndex = list[iList];
			if (iConst < excludeLength)
				constantIndex = remove[iConst];
			if (listIndex == constantIndex)
			{
				// Skip this list index entry
				iList++;
			}
			else if (listIndex < constantIndex || iConst >= excludeLength)
			{
				// Add this entry
				result.add(listIndex - iConst);
				iList++;
			}
			else if (listIndex > constantIndex)
			{
				// Move to the next constant if there is one
				iConst++;
			}
		}
		
		// Convert contracted list back to an int[]
		result.trimToSize();
		return result.elements();
	}
	
	/**
	 * Copies contents of collection into a new array with given component type.
	 * @since 0.05
	 */
	public static <T> T[] copy(Class<T> componentType, Collection<T> collection)
	{
		@SuppressWarnings("unchecked")
		final T[] array = (T[]) Array.newInstance(componentType, collection.size());
		return collection.toArray(array);
	}
	
	/**
	 * Copies entries from {@code source} array at specified {@code sourceIndices} to new array.
	 * 
	 * @param source is the array from which entries will be copied.
	 * @param sourceIndices specifies which entries to copy into new array in which order.
	 * Each index value must be in the range [0,{@code source.length}-1] and indicates which entry is to be
	 * copied into the corresponding destination.
	 * @return new array
	 * 
	 * @see #copyFromIndices(Object[], int[], Object[])
	 * @since 0.05
	 */
	public static <T> T[] copyFromIndices(T[] source, int[] sourceIndices)
	{
		return copyFromIndices(source, sourceIndices, null);
	}

	/**
	 * Copies entries from {@code source} array at specified {@code sourceIndices} to {@code destination} array.
	 * 
	 * @param source is the array from which entries will be copied.
	 * @param sourceIndices must be the same length as {@code destination} array (if not null). Each index value
	 * must be in the range [0,{@code source.length}-1] and indicates which entry is to be copied into the
	 * corresponding destination.
	 * @param destination is the array into which the entries are to be copied. If null, then a new one
	 * will be allocated using same type as {@code source} and length matching that of {@code sourceIndices}.
	 * @return array into which values have been copied. Will be same as {@code destination} if not null.
	 * 
	 * @see #copyFromIndices(Object[], int[])
	 * @since 0.05
	 */
	public static <T> T[] copyFromIndices(T[] source, int[] sourceIndices,
		@Nullable T[] destination)
	{
		final int destSize = sourceIndices.length;
		
		@SuppressWarnings("unchecked")
		@NonNull T[] result = destination != null ? destination :
			(T[])Array.newInstance(source.getClass().getComponentType(), destSize);
		
		for (int to = 0; to < destSize; ++to)
		{
			result[to] = source[sourceIndices[to]];
		}
		
		return result;
	}
	
	/**
	 * Searches array in linear order for first element with matching key.
	 * 
	 * @param array the array to be searched
	 * @param key the value to be searched for
	 * @param fromIndex the index of the first element (inclusive) to be searched
	 * @param toIndex the index of the last element (exclusive) to be searched
	 * 
	 * @return index of first array element in range for which {@code key.equals(array[i].getKey())}; otherwise -1.
	 * 
	 * @since 0.06
	 */
	public static <K> int linearSearch(IKeyed<K>[] array, K key, int fromIndex, int toIndex)
	{
		for (int i = fromIndex; i < toIndex; ++i)
		{
			if (key.equals(array[i].getKey()))
			{
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * Searches array in linear order for first element with matching key.
	 * 
	 * @param array the array to be searched
	 * @param key the value to be searched for
	 * 
	 * @return index of first array element for which {@code key.equals(array[i].getKey())}; otherwise -1.
	 * 
	 * @since 0.06
	 */
	public static <K> int linearSearch(IKeyed<K>[] array, K key)
	{
		return linearSearch(array, key, 0, array.length);
	}

	/**
	 * Determines if array is ordered according to provided comparator.
	 * @since 0.05
	 */
	public static <T> boolean isSorted(T[] array, Comparator<T> comparator)
	{
		final int size = array.length;
		
		if (size > 1)
		{
			T prev = array[0];
			for (int i = 1; i < size; ++i)
			{
				final T next = array[i];
				if (0 < comparator.compare(prev, next))
				{
					return false;
				}
				prev = next;
			}
		}
		
		return true;
	}
	
	/**
	 * Determines if array is ordered according to elements' {@link Comparable#compareTo(Object)} method.
	 * @since 0.05
	 */
	public static <T extends Comparable<T>> boolean isSorted(T[] array)
	{
		final int size = array.length;
		
		if (size > 1)
		{
			T prev = array[0];
			for (int i = 1; i < size; ++i)
			{
				final T next = array[i];
				if (0 < prev.compareTo(next))
				{
					return false;
				}
				prev = next;
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
	public static @Nullable double[] cloneArray(@Nullable double[] array)
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
	public static @Nullable int[] cloneArray(@Nullable int[] array)
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
	public static @Nullable int[][] cloneArray(@Nullable int[][] array)
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
	public static double[] copyArrayForInsert(@Nullable double[] array, int insertionPoint, int insertLength)
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
	public static int[] copyArrayForInsert(@Nullable int[] array, int insertionPoint, int insertLength)
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
	public static int[][] copyArrayForInsert(@Nullable int[][] array, int insertionPoint, int insertLength)
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

	public static @Nullable Object[] toArray(@Nullable Object value)
	{
		if (value instanceof Object[])
		{
			return (Object[])value;
		}
		
		if (value != null && value.getClass().isArray())
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
	public static @Nullable int[] toIntArray(@Nullable Object value)
	{
		if (value instanceof int[])
		{
			return (int[])value;
		}
		
		if (value != null && value.getClass().isArray())
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

	/**
	 * Returns a copy of an int array, without the element at the specified index.
	 * 
	 * @param array The original array.
	 * @param index The zero-based index of the element to exclude from the copy.
	 * @since 0.06
	 */
	@Internal
	public static int[] removeIntArrayEntry(final int[] array, final int index)
	{
		final int[] result = new int[array.length - 1];
		System.arraycopy(array, 0, result, 0, index);
		System.arraycopy(array, index + 1, result, index, result.length - index);
		return result;
	}
}
