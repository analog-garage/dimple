/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Extends {@link ArrayList} with additional methods for automatically growing list.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class ExtendedArrayList<T> extends ArrayList<T>
{
	private static final long serialVersionUID = 1L;
	
	/*--------------
	 * Construction
	 */

	public ExtendedArrayList()
	{
		this(10);
	}
	
	public ExtendedArrayList(Collection<? extends T> collection)
	{
		super(collection);
	}
	
	public ExtendedArrayList(int initialCapacity)
	{
		super(initialCapacity);
	}
	
	/*--------------
	 * List methods
	 */
	
	/**
	 * Inserts element at specified position in list, growing if necessary.
	 * <p>
	 * Unlike the superclass version of this method, this will grow the list
	 * using {@link #setSize(int)} instead of throwing an {@link IndexOutOfBoundsException}.
	 */
	@Override
	public void add(int index, @Nullable T element)
	{
		growSize(index);
		super.add(index, element);
	}
	
	/**
	 * Replaces element at specified position in list, growing if necessary.
	 * <p>
	 * Unlike the superclass version of this method, this will grow the list
	 * using {@link #setSize(int)} instead of throwing an {@link IndexOutOfBoundsException}.
	 */
	@Override
	public @Nullable T set(int index, @Nullable T element)
	{
		growSize(index + 1);
		return super.set(index, element);
	}

	/*---------------------------
	 * ExtendedArrayList methods
	 */

	/**
	 * Returns the element at the specified position, or null if index out of range.
	 * <p>
	 * This is similar to {@link #get(int)} but returns null instead of throwing
	 * an {@link IndexOutOfBoundsException} if {@code index} is not in range.
	 * @since 0.08
	 */
	public @Nullable T getOrNull(int index)
	{
		if (index >= 0 & index < size())
		{
			return super.get(index);
		}
		
		return null;
	}
	
	/**
	 * Sets {@link #size} of array to specified value if larger than current size.
	 * @param newSize is the new size of the array. Is ignored if less than current size.
	 * @since 0.08
	 * @see #setSize
	 */
	public void growSize(int newSize)
	{
		if (newSize > size())
		{
			setSize(newSize);
		}
	}
	
	/**
	 * Sets {@link #size} of array to specified value.
	 * <p>
	 * @param newSize the new size of the array. If less than the current size, elements will be removed
	 * from the end of the list; if greater than the current size, new elements with null values will
	 * be appended to the end of the list.
	 * 
	 * @since 0.08
	 * @see #growSize
	 */
	public void setSize(int newSize)
	{
		final int prevSize = size();
		
		if (newSize < prevSize)
		{
			for (int i = prevSize; --i>=newSize;)
			{
				remove(i);
			}
		}
		else if (newSize > prevSize)
		{
			ensureCapacity(newSize);
			for (int i = prevSize; i < newSize; ++i)
			{
				add(null);
			}
		}
	}
}
