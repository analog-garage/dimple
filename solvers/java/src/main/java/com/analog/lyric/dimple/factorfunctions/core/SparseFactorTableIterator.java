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

import java.util.Objects;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.domains.JointDomainIndexer;

@NotThreadSafe
final class SparseFactorTableIterator extends FactorTableIteratorBase
{
	/*-------
	 * State
	 */
	
	private final SparseFactorTable _table;
	
	/*--------------
	 * Construction
	 */

	SparseFactorTableIterator(SparseFactorTable table)
	{
		_table = table;
		_sparseIndex = -1;
	}
	
	/*-------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		int[] indices = _jointIndices;
		int si = _sparseIndex;
		double w = _weight, e = _energy;
		
		boolean result = advance();
		
		_jointIndices = indices;
		_sparseIndex = si;
		_weight = w;
		_energy = e;
		
		return result;
	}

	/*------------------------------
	 * IFactorTableIterator methods
	 */
	
	@Override
	public boolean advance()
	{
		if (done())
		{
			return false;
		}
		
		_entry = null;
		return sparseAdvance();
	}

	@Override
	public JointDomainIndexer domains()
	{
		return _table.getDomainIndexer();
	}
	
	@Override
	public int[] indicesUnsafe()
	{
		return Objects.requireNonNull(_jointIndices);
	}
	
	@Override
	public int jointIndex()
	{
		return -1;
	}
	
	@Override public boolean skipsZeroWeights()
	{
		return true;
	}

	/*-------------------------
	 * FactorTableIteratorBase
	 */
	
	@Override
	boolean done()
	{
		return _sparseIndex >= _table.sparseSize();
	}

	@Override
	void makeEntry()
	{
		if (!done())
		{
			_entry = new FactorTableEntry(_table.getDomainIndexer(), _sparseIndex, _jointIndices, _energy, _weight);
		}
	}

	/*---------
	 * Private
	 */
	
	private boolean sparseAdvance()
	{
		for (int si = _sparseIndex + 1, end = _table.sparseSize(); si < end; ++si)
		{
			final double weight = _table.getWeightForSparseIndex(si);
			if (weight != 0.0)
			{
				_sparseIndex = si;
				_jointIndices = _table.sparseIndexToIndicesUnsafe(si);
				_weight = weight;
				_energy = _table.getEnergyForSparseIndex(si);
				return true;
			}
		}
				
		_weight = 0.0;
		_energy = Double.POSITIVE_INFINITY;
		
		return false;
	}
}
