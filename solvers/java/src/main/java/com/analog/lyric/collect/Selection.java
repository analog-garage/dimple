/*******************************************************************************
*   Copyright 2012-2014 Analog Devices, Inc.
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

import java.util.Comparator;


/**
 * Provides efficient implementations of variants of the <em>selection</em> problem.
 * <p>
 * The selection problem involves efficiently finding the nth element in a set of unordered
 * elements for some ordering relation. Obviously this can be done trivially by sorting,
 * which requires O(n log n) operations, but this class implements an algorithm that
 * takes O(n) time.
 * <p>
 * @since 0.06
 */
public abstract class Selection
{
	
	/**
	 * Returns the indices of the k lowest elements.
	 * <p>
	 * Returns a newly allocated array of the indices of the {@code k} lowest
	 * elements in {@linkplain Comparable natural order}.
	 * <p>
	 * @param array is a non-empty array in any order.
	 * @param k is in the range [1, array.length]
	 */
	public static int [] findFirstKIndices(double [] array, int k)
	{
		 //select the kth
		 double val = select(array,k-1);
		 
		 int [] result = new int[k];
		 
		 for (int index = 0, i = -1; index < k; ++index)
		 {
			 while (array[++i] > val) {}
			 result[index] = i;
		 }

		 return result;
	}

	/**
	 * Returns the indices of the k lowest elements.
	 * <p>
	 * Returns a newly allocated array of the indices of the {@code k} lowest
	 * elements in {@linkplain Comparable natural order}.
	 * <p>
	 * @param array is a non-empty array in any order.
	 * @param k is in the range [1, array.length]
	 */
	public static <T extends Comparable<T>> int [] findFirstKIndices(T[] array, int k)
	 {
		 //select the kth
		 T obj = select(array,k-1);
		 
		 int [] result = new int[k];
		 
		 for (int index = 0, i = -1; index < k; index++)
		 {
			 while (array[++i].compareTo(obj) > 0) {}
			 result[index] = i;
		 }
		 
		 return result;
	 }
	

	/**
	 * Returns the indices of the k highest elements.
	 * <p>
	 * Returns a newly allocated array of the indices of the {@code k} highest
	 * elements in {@linkplain Comparable natural order}.
	 * <p>
	 * @param array is a non-empty array in any order.
	 * @param k is in the range [1, array.length]
	 */
	public static int [] findLastKIndices(double [] array, int k)
	{
		double val = select(array,array.length-k);
		
		 int [] result = new int[k];
		 
		 for (int index = 0, i = -1; index < k; index++)
		 {
			 while (array[++i] < val) {}
			 result[index] = i;
		 }
		 
		 return result;
	}
	
	/**
	 * Returns the indices of the k highest elements.
	 * <p>
	 * Returns a newly allocated array of the indices of the {@code k} highest
	 * elements in {@linkplain Comparable natural order}.
	 * <p>
	 * @param array is a non-empty array in any order.
	 * @param k is in the range [1, array.length]
	 */
	public static <T extends Comparable<T>> int [] findLastKIndices(T[] array, int k)
	{
		T obj = select(array,array.length-k);
		
		 int [] result = new int[k];
		 
		 for (int index = 0, i = -1; index < k; index++)
		 {
			 while (array[++i].compareTo(obj) < 0) {}
			 result[index] = i;
		 }
		 
		 return result;
	}


	/**
	 * Returns the kth element in array in natural order.
	 * <p>
	 * Returns index of kth element in {@linkplain Comparable natural order}.
	 * <p>
	 * @param array a non-empty array in any order.
	 * @param k a number in the range [0, array.length-1]
	 */
	public static double select(double [] array, int k)
	{
		return selectMutably(array.clone(), k);
	}

	/**
	 * Returns the kth element in array in natural order.
	 * <p>
	 * Returns index of kth element in {@linkplain Comparable natural order}.
	 * <p>
	 * The {@code array} will be rearranged so that the kth element is at position
	 * k and all elements less than the kth element will be before it in the array.
	 * <p>
	 * @param array a non-empty array in any order.
	 * @param k a number in the range [0, array.length-1]
	 */
	public static double selectMutably(double [] array, int k)
	{
		return select(array,0,array.length-1,k);
	}

	/**
	 * Returns the kth element in array in natural order.
	 * <p>
	 * Returns index of kth element in {@linkplain Comparable natural order}.
	 * <p>
	 * @param array a non-empty array in any order.
	 * @param k a number in the range [0, array.length-1]
	 */
	public static <T extends Comparable<T>> T select(T[] array, int k)
	{
		return selectMutably(array.clone(), k);
	}
	
	/**
	 * Returns the kth element in array in natural order.
	 * <p>
	 * Returns index of kth element in {@linkplain Comparable natural order}.
	 * <p>
	 * The {@code array} will be rearranged so that the kth element is at position
	 * k and all elements less than the kth element will be before it in the array.
	 * <p>
	 * @param array a non-empty array in any order.
	 * @param k a number in the range [0, array.length-1]
	 */
	public static <T extends Comparable<T>> T selectMutably(T[] array, int k)
	{
		return select(array,0,array.length-1,k);
	}

	/**
	 * Returns the kth element in array using specified ordering..
	 * <p>
	 * Returns index of kth element in ordering defined by {@code comparator}.
	 * <p>
	 * @param array a non-empty array in any order.
	 * @param k a number in the range [0, array.length-1]
	 */
	public static <T> T select(Comparator<T> comparator, T[] array, int k)
	{
		return selectMutably(comparator, array.clone(), k);
	}
	
	/**
	 * Returns the kth element in array using specified ordering..
	 * <p>
	 * Returns index of kth element in ordering defined by {@code comparator}.
	 * <p>
	 * The {@code array} will be rearranged so that the kth element is at position
	 * k and all elements less than the kth element will be before it in the array.
	 * <p>
	 * @param array a non-empty array in any order.
	 * @param k a number in the range [0, array.length-1]
	 */
	public static <T> T selectMutably(Comparator<T> comparator, T[] array, int k)
	{
		return select(comparator, array, 0, array.length-1, k);
	}

	/*-----------------
	 * Private methods
	 */

	/**
	 * Given a list, a left index, a right index, and a pivot index, this method
	 * will create a partition between all elements less than the pivot value
	 * and all elements greater than the pivot value.  The operation is done in
	 * place and returns the new position of the original pivot index.
	 * 
	 * @param list is a non-empty unordered array
	 * @param left is in the range [0, list.length-1]
	 * @param right is in the range [0, list.length-1]
	 * @param pivotIndex is in the range [0, list.length-1]
	 */
	private static <T extends Comparable<T>> int partition(T[] list, int left, int right, int pivotIndex)
	{
		final T pivotValue = list[pivotIndex];
		
		//swap list[pivotIndex] and list[right]  // Move pivot to end
		list[pivotIndex] = list[right];
		list[right] = pivotValue;
		
		int storeIndex = left;
		
		for (int i = left; i <= right; i++)
		{
			final T cur = list[i];
			if (cur.compareTo(pivotValue) < 0)
			{
	             //swap list[storeIndex] and list[i]
				list[i] = list[storeIndex];
				list[storeIndex] = cur;
	                          
				storeIndex++;
			}
		}
	     
	    //swap list[right] and list[storeIndex]  // Move pivot to its final place
		final T tmp = list[right];
		list[right] = list[storeIndex];
		list[storeIndex] = tmp;
		
	    return storeIndex;
	}

	/**
	 * Given a list, a left index, a right index, and an index, k, this method
	 * will return the kth smallest object.
	 * 
	 * @param list is a non-empty unordered array
	 * @param left is in the range [0, list.length-1]
	 * @param right is in the range [0, list.length-1]
	 * @param k is in the range [0,list.length-1]
	 */
	private static <T extends Comparable<T>> T select(T[] list, int left, int right, int k)
	{
		++k; // Convert to one-based indexing
		
	     while (true)
	     {
	         //select pivotIndex between left and right
	    	 int pivotIndex = (right-left)/2+left;
	         int pivotNewIndex = partition(list, left, right, pivotIndex);
	         
	         int pivotDist = pivotNewIndex - left + 1;
	         
	         if (pivotDist == k)
	         {
	             return list[pivotNewIndex];
	         }
	         else if ( k < pivotDist)
	         {
	             right = pivotNewIndex - 1;
	         }
	         else
	         {
	             k = k - pivotDist;
	             left = pivotNewIndex + 1;
	         }
	     }
	}
	
	/**
	 * Given a list, a left index, a right index, and a pivot index, this method
	 * will create a partition between all elements less than the pivot value
	 * and all elements greater than the pivot value.  The operation is done in
	 * place and returns the new position of the original pivot index.
	 * 
	 * @param list is a non-empty unordered array
	 * @param left is in the range [0, list.length-1]
	 * @param right is in the range [0, list.length-1]
	 * @param pivotIndex is in the range [0, list.length-1]
	 */
	private static <T> int partition(Comparator<T> comparator, T[] list, int left, int right, int pivotIndex)
	{
		final T pivotValue = list[pivotIndex];
		
		//swap list[pivotIndex] and list[right]  // Move pivot to end
		list[pivotIndex] = list[right];
		list[right] = pivotValue;
		
		int storeIndex = left;
		
		for (int i = left; i <= right; i++)
		{
			final T cur = list[i];
			if (comparator.compare(cur, pivotValue) < 0)
			{
	             //swap list[storeIndex] and list[i]
				list[i] = list[storeIndex];
				list[storeIndex] = cur;
	                          
				storeIndex++;
			}
		}
	     
	    //swap list[right] and list[storeIndex]  // Move pivot to its final place
		final T tmp = list[right];
		list[right] = list[storeIndex];
		list[storeIndex] = tmp;
		
	    return storeIndex;
	}

	/**
	 * Given a list, a left index, a right index, and an index, k, this method
	 * will return the kth smallest object.
	 * 
	 * @param list is a non-empty unordered array
	 * @param left is in the range [0, list.length-1]
	 * @param right is in the range [0, list.length-1]
	 * @param k is in the range [0,list.length-1]
	 */
	private static <T> T select(Comparator<T> comparator, T[] list, int left, int right, int k)
	{
		++k; // Convert to one-based indexing
		
	     while (true)
	     {
	         //select pivotIndex between left and right
	    	 int pivotIndex = (right-left)/2+left;
	         int pivotNewIndex = partition(comparator, list, left, right, pivotIndex);
	         
	         int pivotDist = pivotNewIndex - left + 1;
	         
	         if (pivotDist == k)
	         {
	             return list[pivotNewIndex];
	         }
	         else if ( k < pivotDist)
	         {
	             right = pivotNewIndex - 1;
	         }
	         else
	         {
	             k = k - pivotDist;
	             left = pivotNewIndex + 1;
	         }
	     }
	}

	/**
	 * Given a list, a left index, a right index, and a pivot index, this method
	 * will create a partition between all elements less than the pivot value
	 * and all elements greater than the pivot value.  The operation is done in
	 * place and returns the new position of the original pivot index.
	 * 
	 * @param list is a non-empty unordered array
	 * @param left is in the range [0, list.length-1]
	 * @param right is in the range [0, list.length-1]
	 * @param pivotIndex is in the range [0, list.length-1]
	 */
	private static int partition(double[] list, int left, int right, int pivotIndex)
	{
		final double pivotValue = list[pivotIndex];
		
		//swap list[pivotIndex] and list[right]  // Move pivot to end
		list[pivotIndex] = list[right];
		list[right] = pivotValue;
		
		int storeIndex = left;
		
		for (int i = left; i <= right; i++)
		{
			final double cur = list[i];
			if (cur < pivotValue)
			{
	             //swap list[storeIndex] and list[i]
				list[i] = list[storeIndex];
				list[storeIndex] = cur;
	                          
				storeIndex++;
			}
		}
	     
	    //swap list[right] and list[storeIndex]  // Move pivot to its final place
		final double tmp = list[right];
		list[right] = list[storeIndex];
		list[storeIndex] = tmp;
		
	    return storeIndex;
	}

	/**
	 * Given a list, a left index, a right index, and an index, k, this method
	 * will return the kth smallest object.
	 * 
	 * @param list is a non-empty unordered array
	 * @param left is in the range [0, list.length-1]
	 * @param right is in the range [0, list.length-1]
	 * @param k is in the range [0,list.length-1]
	 */
	private static double select(double[] list, int left, int right, int k)
	{
		++k; // Convert to one-based indexing
		
	     while (true)
	     {
	         //select pivotIndex between left and right
	    	 int pivotIndex = (right-left)/2+left;
	         int pivotNewIndex = partition(list, left, right, pivotIndex);
	         
	         int pivotDist = pivotNewIndex - left + 1;
	         
	         if (pivotDist == k)
	         {
	             return list[pivotNewIndex];
	         }
	         else if ( k < pivotDist)
	         {
	             right = pivotNewIndex - 1;
	         }
	         else
	         {
	             k = k - pivotDist;
	             left = pivotNewIndex + 1;
	         }
	     }
	}
}
