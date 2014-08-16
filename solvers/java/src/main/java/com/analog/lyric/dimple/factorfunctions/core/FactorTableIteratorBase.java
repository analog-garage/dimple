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

package com.analog.lyric.dimple.factorfunctions.core;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Common implementation base for {@link IFactorTableIterator}s.
 */
abstract class FactorTableIteratorBase implements IFactorTableIterator
{

	int _sparseIndex = 0;
	double _energy = Double.POSITIVE_INFINITY;
	double _weight = 0.0;
	@Nullable FactorTableEntry _entry = null;
	@Nullable int[] _jointIndices = null;

	FactorTableIteratorBase()
	{
	}

	@Override
	public final @Nullable FactorTableEntry next()
	{
		advance();
		return getEntry();
	}

	@Override
	public final void remove()
	{
		throw new UnsupportedOperationException("IFactorTableIterator.remove");
	}

	@Override
	public final @Nullable FactorTableEntry getEntry()
	{
		if (_entry == null && !done())
		{
			makeEntry();
		}
		
		return _entry;
	}

	@Override
	public final double energy()
	{
		return _energy;
	}

	@Override
	public final int[] indices()
	{
		return indices(null);
	}
	
	@Override
	public final int[] indices(@Nullable int[] array)
	{
		final int[] unsafeResult = indicesUnsafe();
		final int length = unsafeResult.length;
		
		if (array == null || array.length < length)
		{
			array = new int[length];
		}
		
		System.arraycopy(unsafeResult, 0, array, 0, length);
		
		return array;
	}
	
	@Override
	public final int sparseIndex()
	{
		return _sparseIndex;
	}

	@Override
	public final double weight()
	{
		return _weight;
	}

	abstract boolean done();
	
	abstract void makeEntry();
}