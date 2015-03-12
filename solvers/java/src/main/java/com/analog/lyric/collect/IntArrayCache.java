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

import java.util.concurrent.Semaphore;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A cache of int[] for temporary use.
 * <p>
 * Use this to manage int arrays to avoid allocating and gc'ing many temporary
 * arrays.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@ThreadSafe
public final class IntArrayCache
{
	private static int N_SLOTS = 31;
	private static int MAX_INSTANCES = 3;
	
	private static class Arrays
	{
		// Use semaphore with spin locking to avoid overhead of implicit try/finally block
		// for synchronized.
		private final Semaphore _lock;
		private final int _arrayLength;
		private int _size;
		private int _maxSize;
		private int[][] _arrays;
		
		private Arrays(int arrayLength, int maxSize)
		{
			_lock = new Semaphore(1);
			_arrayLength = arrayLength;
			_maxSize = maxSize;
			_size = 0;
			_arrays = new int[maxSize][];
		}
		
		private @Nullable int[] pop()
		{
			int[] array = null;
			
			_lock.acquireUninterruptibly();
			
			int size = _size;
			if (size > 0)
			{
				_size = --size;
				array = _arrays[size];
				_arrays[size] = null;
			}
			
			_lock.release();
			
			return array;
		}
		
		private void push(int[] array)
		{
			if (array.length == _arrayLength)
			{
				_lock.acquireUninterruptibly();
				
				int size = _size;
				if (size < _maxSize)
				{
					_arrays[size] = array;
					++_size;
				}
				
				_lock.release();
			}
		}
	}
	
	private final Arrays[] _arrays;
	
	/*--------------
	 * Construction
	 */
	
	public IntArrayCache(int maxInstances)
	{
		_arrays = new Arrays[N_SLOTS];
		for (int i = 0; i < N_SLOTS; ++i)
		{
			_arrays[i] = new Arrays(1<<i, maxInstances);
		}
	}
	
	public IntArrayCache()
	{
		this(MAX_INSTANCES);
	}
	
	/*--------------------------
	 * DoubleArrayCache methods
	 */
	
	/**
	 * Returns new array of at least {@code minSize} from cache or returns a newly allocated one.
	 * <p>
	 * The caller must not assume that the array has been zeroed out!
	 * <p>
	 * @since 0.08
	 */
	public int[] allocateAtLeast(int minSize)
	{
		if (minSize <= 0)
		{
			return ArrayUtil.EMPTY_INT_ARRAY;
		}
		
		int[] array = _arrays[slotForLength(minSize)].pop();
		return array != null ? array : new int[minSize];
	}

	/**
	 * Returns an array to the cache for reuse.
	 * @since 0.08
	 */
	public void release(int[] array)
	{
		final int size = array.length;
		_arrays[slotForLength(size)].push(array);
	}
	
	private int slotForLength(int minLength)
	{
		return 32 - Integer.numberOfLeadingZeros(minLength - 1);
	}
}
