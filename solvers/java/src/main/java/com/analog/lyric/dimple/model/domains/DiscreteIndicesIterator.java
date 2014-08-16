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

package com.analog.lyric.dimple.model.domains;

import java.util.Arrays;
import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * An iterator over any array of integer indices each with a specified bounds
 * iterating over the index at position n before that at n+1. This is the
 * same as dictionary order if the indices are read from back to front.
 * <p>
 * For example, the following code:
 * <pre>
 *     for (int[] indices : new DiscreteIndicesIterator(2, 3))
 *     {
 *        System.out.format("%s\n", indices);
 *     }
 * </pre>
 * will produce the output:
 * <pre>
 *     [0,0]
 *     [1,0]
 *     [0,1]
 *     [1,1]
 *     [0,2]
 *     [1,2]
 * </pre>
 * Unlike most iterators, this always returns the same int[] instance, so if you want
 * a fresh copy, you must make sure to clone it.
 * <p>
 * As a special case, when passed an empty domain list, the iterator will still iterate once!
 * 
 * @since 0.05
 */
@NotThreadSafe
public final class DiscreteIndicesIterator implements Iterator<int[]>
{
	private final int[] _limits;
	private final int[] _indices;
	private int _lastNotAtLimit;
	private boolean _doOnce;

	/*--------------
	 * Construction
	 */

	/**
	 * Constructs an iterator over the indices over the specified {@code domains}.
	 * If {@code domains} isa {@link JointDomainIndexer}, then this will produce
	 * the same order as the undirected joint index order.
	 * <p>
	 * @see #DiscreteIndicesIterator(DomainList, int[])
	 */
	public DiscreteIndicesIterator(DomainList<DiscreteDomain> domains)
	{
		this(domains, null);
	}
	
	/**
	 * Constructs an iterator over the indices over the specified {@code domains}
	 * using the specified {@code indices} array.

	 * @param indices must be null or have length equal to {@code domains} size.
	 * 
	 * @see #DiscreteIndicesIterator(DomainList)
	 */
	public DiscreteIndicesIterator(DomainList<DiscreteDomain> domains, @Nullable int[] indices)
	{
		this(limitsFromDomains(domains), indices, false);
	}
	
	/**
	 * Constructs an iterator over the indices each in the range [0, sizes[i] - 1].

	 * @see #DiscreteIndicesIterator(int[], int[])
	 */
	public DiscreteIndicesIterator(int ... sizes)
	{
		this(sizes, null);
	}
	
	/**
	 * Constructs an iterator over the indices each in the range [0, sizes[i] - 1].
	 * 
	 * @param indices must be null or have length equal to {@code domains} size.
	 * 
	 * @see #DiscreteIndicesIterator(int[], int[])
	 */
	public DiscreteIndicesIterator(int[] sizes, @Nullable int[] indices)
	{
		this(sizes, indices, true);
	}
	
	private DiscreteIndicesIterator(int[] sizesOrLimits, @Nullable int[] indices, boolean sizes)
	{
		final int dimensions = sizesOrLimits.length;
		
		if (sizes)
		{
			_limits = new int[dimensions];
			for (int i = 0; i < dimensions; ++i)
			{
				_limits[i] = sizesOrLimits[i] - 1;
			}
		}
		else
		{
			_limits = sizesOrLimits;
		}
		_indices = indices != null ? indices : new int[dimensions];
		reset();
	}
	
	private static int[] limitsFromDomains(DomainList<DiscreteDomain> domains)
	{
		final int dimensions = domains.size();
		final int[] limits = new int[dimensions];
		
		for (int i = 0; i < dimensions; ++i)
		{
			limits[i] = domains.get(i).size() - 1;
		}
		
		return limits;
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _lastNotAtLimit >= 0 || _doOnce;
	}

	@Override
	public int[] next()
	{
		_doOnce = false;
		
		for (int i = 0; i <= _lastNotAtLimit; ++i)
		{
			final int limit = _limits[i];
			int val = _indices[i] + 1;
			
			if (val <= limit)
			{
				_indices[i] = val;
				if (val == limit && i == _lastNotAtLimit)
				{
					--_lastNotAtLimit;
				}
				break;
			}
			else // val > limit
			{
				_indices[i] = 0;
			}
		}
			
		return _indices;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException(getClass().getSimpleName() + ".remove");
	}

	/*---------------------------------
	 * DiscreteIndicesIterator methods
	 */
	
	/**
	 * Reset position back to head of iterator.
	 */
	public void reset()
	{
		Arrays.fill(_indices, 0);
		if (_indices.length > 0)
		{
			_indices[0] = -1;
		}
		else
		{
			_doOnce = true;
		}
		_lastNotAtLimit = _indices.length - 1;
	}
}
