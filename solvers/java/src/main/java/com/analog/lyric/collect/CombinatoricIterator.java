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

import java.lang.reflect.Array;
import java.util.Arrays;

import com.google.common.collect.UnmodifiableIterator;

/**
 * Iterates through all combinations of arrays of elements.
 * <p>
 * Given an array of arrays defining the possible elements for position of the output array,
 * this will iterate over every combination of the elements.
 * <p>
 * For example, the following code:
 * <pre>
 *     String[] first = new String[] { "a", "b" };
 *     String[] second = new String[] { "x", "y", "z" };
 *     CombinatoricIterator<String> iter = new CombinatoricIterator<>(String.class, first, second);
 *     while (iter.hasNext())
 *     {
 *        System.out.format("%s\n", Arrays.toString(iter.next());
 *     }
 * </pre>
 * will produce the output:
 * <pre>
 *    [a, x]
 *    [b, x]
 *    [a, y]
 *    [b, y]
 *    [a, z]
 *    [b, z]
 * </pre>
 * Unlike most iterators, this always returns the same int[] instance, so if you want
 * a fresh copy, you must make sure to clone it.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class CombinatoricIterator<T> extends UnmodifiableIterator<T[]>
{
	/*-------
	 * State
	 */
	
	/**
	 * The elements that will be combinatorially explored.
	 */
	private final T[][] _elements;
	private final int[] _indices;
	private final T[] _values;
	private final int[] _limits;
	private int _lastNotAtLimit;
	
	/*--------------
	 * Construction
	 */
	
	@SuppressWarnings("unchecked")
	public CombinatoricIterator(Class<T> elementType, T[] ... elements)
	{
		final int dimensions = elements.length;
		_elements = elements;
		_indices = new int[dimensions];
		_limits = new int[dimensions];
		_values = (T[]) Array.newInstance(elementType, dimensions);
		for (int i = 0; i < dimensions; ++i)
		{
			_limits[i] = elements[i].length - 1;
		}
		reset();
	}

	/*------------------
	 * Iterator methods
	 */

	@Override
	public boolean hasNext()
	{
		return _lastNotAtLimit >= 0;
	}

	/**
	 * Returns the next combination of values.
	 * <p>
	 * Note that this will always return the same array object to avoid
	 * allocation. So if you need a fresh copy, you must explicitly clone it!
	 */
	@Override
	public T[] next()
	{
		final int[] indices = _indices;
		final int[] limits = _limits;
		final T[] values = _values;
		final T[][] elements = _elements;
		
		final int lastNotAtLimit = _lastNotAtLimit;
		for (int i = 0; i <= lastNotAtLimit; ++i)
		{
			final int limit = limits[i];
			int val = indices[i] + 1;
			
			if (val <= limit)
			{
				indices[i] = val;
				values[i] = elements[i][val];
				if (val == limit && i == lastNotAtLimit)
				{
					while (--i >= 0 && indices[i] == limits[i]) {}
					_lastNotAtLimit = i;
				}
				break;
			}
			else // val > limit
			{
				indices[i] = 0;
				values[i] = elements[i][0];
			}
		}
	
		return values;
	}
	
	/*------------------------------
	 * CombinatoricIterator methods
	 */

	/**
	 * Returns the underlying elements passed to the constructor.
	 * @since 0.08
	 */
	public T[][] elements()
	{
		return _elements;
	}
	
	/**
	 * Returns the last set of indices for the values returned by {@link #next}.
	 * <p>
	 * This returns the actual underlying array used by this object.
	 * @since 0.08
	 */
	public int[] indices()
	{
		return _indices;
	}
	
	/**
	 * Reset position of iterator back to beginning.
	 */
	public void reset()
	{
		Arrays.fill(_indices, 0);
		_indices[0] = -1;
		_lastNotAtLimit = _indices.length - 1;
		while (_lastNotAtLimit > 0 && _limits[_lastNotAtLimit] == 0) --_lastNotAtLimit;
		for (int i = _values.length; --i >=0; )
		{
			_values[i] = _elements[i][0];
		}
	}
}
