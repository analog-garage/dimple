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
	
	/*
	 * Given a list of doubles and an index, k, this method
	 * will return indices for the k smallest values
	 */
	public static int [] quickfindFirstKindices(double [] list, int k)
	{
		Double [] newlist = new Double[list.length];
		for (int i = 0; i < newlist.length; i++)
			newlist[i] = list[i];
		
		return quickfindFirstKindices(newlist,k);
	}

	
	/*
	 * Given a list of objects and an index, k, this method
	 * will return indices for the k smallest values.  The objects
	 * must implement Comparable
	 */
	public static <T extends Comparable<T>> int [] quickfindFirstKindices(T [] list, int k)
	 {
		 //select the kth
		 T obj = select(list,k);
		 
		 int [] result = new int[k];
		 
		 int index = 0;
		 
		 for (int i = 0, end = list.length; i < end; i++)
		 {
			 if (list[i].compareTo(obj) <= 0)
			 {
				 result[index] = i;
				 index++;
			 }
		 }
		 
		 return result;
	 }
	

	public static int [] quickfindLastKindices(double [] list, int k)
	{
		Double [] newlist = new Double[list.length];
		for (int i = 0; i < newlist.length; i++)
			newlist[i] = list[i];
		
		return quickfindLastKindices(newlist,k);
	}
	
	public static <T extends Comparable<T>> int [] quickfindLastKindices(T[] list, int k)
	{
		T obj = select(list,1+list.length-k);
		
		 int [] result = new int[k];
		 
		 int index = 0;
		 
		 for (int i = 0, end = list.length; i < end; i++)
		 {
			 if (list[i].compareTo(obj) >= 0)
			 {
				 result[index] = i;
				 index++;
			 }
		 }
		 
		 return result;
	}


	/*
	 * Given a list of doubles and an index, k, this method
	 * will return the kth smallest double.
	 */
	public static double select(double [] list, int k)
	{
		Double [] newlist = new Double[list.length];
		for (int i = 0; i < list.length; i++)
			newlist[i] = list[i];
		
		return select(newlist,0,list.length-1,k);
	}

	/*
	 * Given a list and an index, k, this method
	 * will return the kth smallest object.  The objects must implement the
	 * Comparable interface.
	 */
	public static <T extends Comparable<T>> T select(T[] list,int k)
	{
		return select(list.clone(),0,list.length-1,k);
	}
	
	/*-----------------
	 * Private methods
	 */

	/*
	 * Given a list, a left index, a right index, and a pivot index, this method
	 * will create a partition between all elements less than the pivot value
	 * and all elements greater than the pivot value.  The operation is done in
	 * place and returns the new position of the original pivot index.
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

	/*
	 * Given a list, a left index, a right index, and an index, k, this method
	 * will return the kth smallest object.  The objects must implement the
	 * Comparable interface.
	 */
	private static <T extends Comparable<T>> T select(T[] list, int left, int right, int k)
	{
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
