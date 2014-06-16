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
public class Selection
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
	public static int [] quickfindFirstKindices(double [] array, int k)
	{
		Double [] newlist = new Double[array.length];
		for (int i = 0; i < newlist.length; i++)
			newlist[i] = array[i];
		
		return quickfindFirstKindices(newlist,k);
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
	public static <T extends Comparable<T>> int [] quickfindFirstKindices(T[] array, int k)
	 {
		 //select the kth
		 T obj = select(array,k-1);
		 
		 int [] result = new int[k];
		 
		 int index = 0;
		 
		 for (int i = 0, end = array.length; i < end; i++)
		 {
			 if (array[i].compareTo(obj) <= 0)
			 {
				 result[index] = i;
				 index++;
			 }
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
	public static int [] quickfindLastKindices(double [] array, int k)
	{
		Double [] newlist = new Double[array.length];
		for (int i = 0; i < newlist.length; i++)
			newlist[i] = array[i];
		
		return quickfindLastKindices(newlist,k);
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
	public static <T extends Comparable<T>> int [] quickfindLastKindices(T[] array, int k)
	{
		T obj = select(array,array.length-k);
		
		 int [] result = new int[k];
		 
		 int index = 0;
		 
		 for (int i = 0, end = array.length; i < end; i++)
		 {
			 if (array[i].compareTo(obj) >= 0)
			 {
				 result[index] = i;
				 index++;
			 }
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
		Double [] newlist = new Double[array.length];
		for (int i = 0; i < array.length; i++)
			newlist[i] = array[i];
		
		return select(newlist,0,array.length-1,k);
	}

	/**
	 * Returns the kth element in array in natural order.
	 * <p>
	 * Returns index of kth element in {@linkplain Comparable natural order}.
	 * <p>
	 * @param array a non-empty array in any order.
	 * @param k a number in the range [0, array.length-1]
	 */
	public static <T extends Comparable<T>> T select(T[] array,int k)
	{
		return select(array.clone(),0,array.length-1,k);
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
	
}
