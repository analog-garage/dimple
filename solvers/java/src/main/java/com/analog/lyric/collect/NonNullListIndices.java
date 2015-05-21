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

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Iterable over the indices of non-null entries in a list.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class NonNullListIndices implements PrimitiveIterable.OfInt
{
	private final List<?> _list;
	
	/*--------------
	 * Construction
	 */
	
	public NonNullListIndices(List<?> list)
	{
		_list = list;
	}
	
	/*---------------------------
	 * PrimitiveIterable methods
	 */
	
	/**
	 * Returns iterator that will visit the indices of the {@linkplain #getList() contained list} that
	 * have non-null values. Indices are returned in increasing order.
	 */
	@Override
	public PrimitiveIterator.OfInt iterator()
	{
		return new PrimitiveIterator.OfInt()
		{
			private final ListIterator<?> _iter = _list.listIterator();
			private int _nextIndex = -1;
			
			private void advance()
			{
				while (_nextIndex < 0 && _iter.hasNext())
				{
					if (_iter.next() != null)
					{
						_nextIndex = _iter.previousIndex();
					}
				}
			}
			
			@Override
			public boolean hasNext()
			{
				advance();
				return _nextIndex >= 0;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException("remove");
			}

			@Override
			public Integer next()
			{
				return nextInt();
			}

			@Override
			public int nextInt()
			{
				advance();
				if (_nextIndex < 0)
				{
					throw new NoSuchElementException();
				}
				final int index = _nextIndex;
				_nextIndex = -1;
				return index;
			}
		};
	}
	
	/*--------------------
	 * NonNullListIndices
	 */
	
	/**
	 * The underlying list.
	 */
	public List<?> getList()
	{
		return _list;
	}
}
