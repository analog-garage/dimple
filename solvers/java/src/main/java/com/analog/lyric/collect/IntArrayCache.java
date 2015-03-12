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

import java.util.Arrays;
import java.util.concurrent.Semaphore;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A cache of int[] for temporary use.
 * <p>
 * Use this to manage int arrays to avoid allocating and gc'ing many temporary
 * arrays.
 * <p>
 * Currently, this cache only supports allocating arrays with a minimum size, not an exact size.
 * This allows it to simply return the appropriate power-of-two sized array, which it can look
 * up very quickly.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see DoubleArrayCache
 */
@ThreadSafe
public final class IntArrayCache
{
	private static final int[] SENTINEL = new int[0];
	
	/**
	 * The number of power-of-two slots supported by the cache.
	 */
	private static int N_SLOTS = 31;
	
	/**
	 * The default maximum number of instances of each array size held in the cache.
	 */
	public static int DEFAULT_MAX_INSTANCES = 3;
	
	/**
	 * Holds cached array instances of a single size.
	 */
	@ThreadSafe
	private static class CacheSlot
	{
		/**
		 * Lock for synchronization.
		 * <p>
		 * We use semaphore with spin locking instead of Java synchronization to avoid overhead of reentrant
		 * locking and implicit try/finally block.
		 */
		private final Semaphore _lock;
		
		/**
		 * The length of arrays held by this object.
		 */
		private final int _arrayLength;
		
		/**
		 * The maximum number of cached arrays that can be held by this object.
		 */
		private final int _maxSize;

		/**
		 * The cached array instances.
		 */
		@GuardedBy("_lock")
		private final int[][] _arrays;

		@GuardedBy("_lock")
		private int _size;
		
		private CacheSlot(int arrayLength, int maxSize)
		{
			_lock = new Semaphore(1);
			_arrayLength = arrayLength;
			_maxSize = maxSize;
			// Set initial size to max size but create array instances lazily using empty array as sentinel.
			_size = maxSize;
			_arrays = new int[maxSize][];
			Arrays.fill(_arrays, SENTINEL);
		}
		
		/**
		 * Grab an array from the cache slot, if one is available.
		 */
		private @Nullable int[] pop()
		{
			final Semaphore lock = _lock;
			final int[][] arrays = _arrays;
			int[] array = null;
			
			{
				lock.acquireUninterruptibly();

				int size = _size;
				if (size > 0)
				{
					_size = --size;
					array = arrays[size];
					arrays[size] = null;
				}

				lock.release();
			}
			
			if (array == SENTINEL)
			{
				// Lazily create array instance if necessary.
				array = new int[_arrayLength];
			}
			
			return array;
		}
		
		/**
		 * Return array to cache slot if there is room and it has the right size.
		 */
		private void push(int[] array)
		{
			if (array.length == _arrayLength)
			{
				final Semaphore lock = _lock;
				lock.acquireUninterruptibly();
				
				int size = _size;
				if (size < _maxSize)
				{
					_arrays[size] = array;
					_size = size + 1;
				}
				
				lock.release();
			}
		}
	}
	
	/**
	 * Holds cached array instances indexed by power-of-two.
	 */
	private final CacheSlot[] _arrays;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct new cache with specified max number of instances per each size.
	 * @param maxInstances a positive number specifying the maximum number of arrays of a single
	 * size that may be held in the cache at the same time.
	 * @since 0.08
	 */
	public IntArrayCache(int maxInstances)
	{
		_arrays = new CacheSlot[N_SLOTS];
		for (int i = 0; i < N_SLOTS; ++i)
		{
			_arrays[i] = new CacheSlot(1<<i, maxInstances);
		}
	}
	
	/**
	 * Construct a new cache with default number of instances per size.
	 * <p>
	 * Same as {@link #IntArrayCache(int)} with {@code maxInstances} set to {@link #DEFAULT_MAX_INSTANCES}.
	 * @since 0.08
	 */
	public IntArrayCache()
	{
		this(DEFAULT_MAX_INSTANCES);
	}
	
	/*--------------------------
	 * IntArrayCache methods
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
	
	/*-----------------
	 * Private methods
	 */
	
	private int slotForLength(int minLength)
	{
		return 32 - Integer.numberOfLeadingZeros(minLength - 1);
	}
}
