/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.util.misc;

import java.util.Arrays;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.math.Utilities;
import com.google.common.primitives.Ints;

public class FlaggedMapList<T extends IGetId> extends MapList<T> implements IFlaggedMapList<T>
{
	/*-------
	 * State
	 */
	
	// TODO: this uses a byte per boolean. Either make this an explicit array of bytes and
	// allow use of all 8 bits per element, or convert to a long[] and use shifts for removal.
	private boolean[] _flags;
	
	// TODO: defer O(n) removal cost until a method that requires access by index is called.
	// This would amortize the cost when multiple removals are made.
	
	/*--------------
	 * Construction
	 */
	
	public FlaggedMapList()
	{
		this(16);
	}
	
	public FlaggedMapList(int initialCapacity)
	{
		super(initialCapacity);
		_flags = ArrayUtil.EMPTY_BOOLEAN_ARRAY;
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public void clear()
	{
		super.clear();
		_flags = ArrayUtil.EMPTY_BOOLEAN_ARRAY;
	}

	@Override
	public boolean remove(@Nullable Object value)
	{
		if (!(value instanceof IGetId))
		{
			return false;
		}
		
		final int id = ((IGetId)value).getId();
		if (!_hashMap.removeKey(id))
		{
			return false;
		}
		
		// The maximum number of instances that can be left to find.
		final int listSize = _arrayList.size();
		final int flagSize = _flags.length;

		int i = _arrayList.indexOf(value);
		
		for (int j = i + 1; j < listSize; ++j)
		{
			T next = _arrayList.get(j);
			if (next != value)
			{
				_arrayList.set(i, next);
				if (i < flagSize)
				{
					_flags[i] = _flags[j];
				}
				++i;
			}
		}

		for (int j = listSize; --j>=i;)
		{
			_arrayList.remove(j);
			if (j < flagSize)
			{
				_flags[j] = false;
			}
		}
		
		return true;
	}

	/*------------------
	 * IMapList methods
	 */
	
	@Override
	public @Nullable T removeByIndex(int index)
	{
		T value = super.removeByIndex(index);
		
		final int nFlags = _flags.length;
		if (index < nFlags)
		{
			System.arraycopy(_flags, index + 1, _flags, index, nFlags - index -1);
			_flags[nFlags - 1] = false;
		}
		
		return value;
	}
	
	/*-------------------------
	 * IFlaggedMapList methods
	 */

	@Override
	public void clearFlags()
	{
		_flags = ArrayUtil.EMPTY_BOOLEAN_ARRAY;
	}

	@Override
	public boolean isFlagged(int index)
	{
		return _flags.length > index && _flags[index];
	}
	
	@Override
	public void setFlag(int index, boolean flag)
	{
		getByIndex(index); // Call for bounds check on index
		if (_flags.length <= index)
		{
			_flags = Arrays.copyOf(_flags, Utilities.nextPow2(index + 1));
		}
		_flags[index] = flag;
	}

	public void setFlags(boolean flag, int ... indices)
	{
		if (indices.length > 0)
		{
			int maxIndex = Ints.max(indices);
			if (maxIndex >= _flags.length)
			{
				_flags = Arrays.copyOf(_flags, Utilities.nextPow2(maxIndex + 1));
			}
			for (int index : indices)
			{
				_flags[index] = flag;
			}
		}
	}
}
