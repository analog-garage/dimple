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

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import net.jcip.annotations.Immutable;

/**
 * 
 * @author Christopher Barber
 * @since 0.05
 */
public final class Comparators
{
	private Comparators() {} // Prevent instantiation
	
	@Immutable
	@NonNullByDefault(false)
	private static enum LexicalIntArrayComparator implements Comparator<int[]>, Serializable
	{
		INSTANCE;
		
		@Override
		public int compare(int[] array1, int[] array2)
		{
			int diff = array1.length - array2.length;
			if (diff == 0)
			{
				for (int i = 0, end = array1.length; i < end; ++i)
				{
					int val1 = array1[i], val2 = array2[i];
					if (val1 != val2)
					{
						diff = val1 < val2 ? -1 : 1;
						break;
					}
				}
			}
			return diff;
		}
	}
	
	@Immutable
	@NonNullByDefault(false)
	private static enum ReverseLexicalIntArrayComparator implements Comparator<int[]>, Serializable
	{
		INSTANCE;
		
		@Override
		public int compare(int[] array1, int[] array2)
		{
			int diff = array1.length - array2.length;
			if (diff == 0)
			{
				for (int i = array1.length; --i >= 0;)
				{
					int val1 = array1[i], val2 = array2[i];
					if (val1 != val2)
					{
						diff = val1 < val2 ? -1 : 1;
						break;
					}
				}
			}
			return diff;
		}
	}

	/**
	 * Returns a comparator for int arrays that compares arrays in reverse lexicographical order, which
	 * compares elements from the last index backward (e.g. {@code [1,2,3]} is after {@code [1,3,2]}).
	 * If arrays are of different length, the shorter array comes first.
	 * 
	 * @see #lexicalIntArray()
	 */
	public static Comparator<int[]> reverseLexicalIntArray()
	{
		return ReverseLexicalIntArrayComparator.INSTANCE;
	}
	
	/**
	 * Returns a comparator for int arrays that compares arrays in lexicographical (aka "dictionary") order
	 * (e.g. {@code [1,2,3]} is before {@code [1,3,2]}). If arrays are of different length, the shorter array comes
	 * first.
	 * 
	 * @see #reverseLexicalIntArray()
	 */
	public static Comparator<int[]> lexicalIntArray()
	{
		return LexicalIntArrayComparator.INSTANCE;
	}

	/**
	 * Returns comparator used by collection.
	 * 
	 * If collection is a {@link SortedSet} returns {@link SortedSet#comparator()}, else returns null.
	 */
	public static @Nullable <T> Comparator<? super T> fromCollection(Collection<? extends T> collection)
	{
		@SuppressWarnings("unchecked")
		Comparator<? super T> comparator = collection instanceof SortedSet ?
			(Comparator<? super T>) ((SortedSet<? extends T>)collection).comparator() : null ;
		return comparator;
	}
}
