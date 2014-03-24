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

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.domains.JointDomainIndexer;

/**
 * Iterator over entries in a {@link IFactorTableBase}.
 */
@NotThreadSafe
class FactorTableIterator extends FactorTableIteratorBase
{
	/*-------
	 * State
	 */
	
	private final boolean _dense;
	private final IFactorTableBase _table;
	private int _jointIndex;
	
	/*--------------
	 * Construction
	 */
	
	FactorTableIterator(IFactorTableBase table, boolean dense)
	{
		_dense = dense;
		_table = table;
		_jointIndex = -1;
	}

	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		int ji = _jointIndex, si = _sparseIndex;
		double w = _weight, e = _energy;
		
		boolean result = advance();
		
		_jointIndex = ji;
		_sparseIndex = si;
		_weight = w;
		_energy = e;
		
		return result;
	}
	
	/*---------
	 * Methods
	 */
	
	@Override
	public boolean advance()
	{
		if (done())
		{
			return false;
		}
		
		_entry = null;
		
		if (_jointIndex >= 0)
		{
			fixSparseIndex();
		}
		
		return _dense? denseAdvance() : sparseAdvance();
	}
	
	@Override
	public JointDomainIndexer domains()
	{
		return _table.getDomainIndexer();
	}
	
	@Override
	public int[] indicesUnsafe()
	{
		if (! _dense && _table instanceof IFactorTable)
		{
			final IFactorTable table = (IFactorTable)_table;
			if (table.hasSparseIndices())
			{
				return table.getIndicesSparseUnsafe()[_sparseIndex];
			}
		}
		return _jointIndices = domains().jointIndexToIndices(_jointIndex, _jointIndices);
	}
	
	@Override
	public int jointIndex()
	{
		return _jointIndex;
	}
	
	@Override
	public boolean skipsZeroWeights()
	{
		return !_dense;
	}
	
	/*---------------------------------
	 * FactorTableIteratorBase methods
	 */
	
	@Override
	boolean done()
	{
		return _jointIndex >= _table.jointSize();
	}
	
	@Override
	void makeEntry()
	{
		if (_jointIndex < _table.jointSize())
		{
			_entry = new FactorTableEntry(_table.getDomainIndexer(), _sparseIndex, _jointIndex, _energy, _weight);
		}
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private boolean denseAdvance()
	{
		_energy = Double.POSITIVE_INFINITY;
		_weight = 0.0;

		final int ji = ++_jointIndex;
		if (ji >= _table.jointSize())
		{
			_sparseIndex = ji;
			return false;
		}
		
		if (_table.sparseSize() > 0)
		{
			if (_sparseIndex >= _table.sparseSize())
			{
				return true;
			}
			
			int sji = _table.sparseIndexToJointIndex(_sparseIndex);
			if (sji < ji)
			{
				++_sparseIndex;
				if (_sparseIndex >= _table.sparseSize())
				{
					_sparseIndex = _table.sparseSize();
					return true;
				}
				sji = _table.sparseIndexToJointIndex(_sparseIndex);
			}
			
			if (sji == ji)
			{
				_energy = _table.getEnergyForSparseIndex(_sparseIndex);
				_weight = _table.getWeightForSparseIndex(_sparseIndex);
			}
		}
		else
		{
			_sparseIndex = _table.hasSparseRepresentation() ? 0 : -1;
			_energy = _table.getEnergyForJointIndex(ji);
			_weight = _table.getWeightForJointIndex(ji);
		}
		
		return true;
	}
	
	private boolean sparseAdvance()
	{
		if (_table.hasSparseRepresentation())
		{
			if (_jointIndex < 0)
			{
				_sparseIndex = -1;
			}
			for (int si = _sparseIndex + 1, end = _table.sparseSize(); si < end; ++si)
			{
				final double weight = _table.getWeightForSparseIndex(si);
				if (weight != 0.0)
				{
					_sparseIndex = si;
					_jointIndex = _table.sparseIndexToJointIndex(si);
					_weight = weight;
					_energy = _table.getEnergyForSparseIndex(si);
					return true;
				}
			}
		}
		else
		{
			for (int ji = _jointIndex + 1, end = _table.jointSize(); ji < end; ++ji)
			{
				final double weight = _table.getWeightForJointIndex(ji);
				if (weight != 0.0)
				{
					_sparseIndex = -1;
					_jointIndex = ji;
					_weight = weight;
					_energy = _table.getEnergyForJointIndex(ji);
					return true;
				}
			}
		}
		
		_weight = 0.0;
		_energy = Double.POSITIVE_INFINITY;
		_jointIndex = _table.jointSize();
		
		return false;
	}

	private void fixSparseIndex()
	{
		final int ji = _jointIndex;
		
		if (_table.hasSparseRepresentation())
		{
			int si = _sparseIndex;
			boolean broken = si < 0 || si >= _table.sparseSize();
 
			if (!broken)
			{
				final int sji = _table.sparseIndexToJointIndex(si);
				if (_dense)
				{
					broken = ji < sji || si+1 < _table.sparseSize() && ji >= _table.sparseIndexToJointIndex(si+1);
				}
				else
				{
					broken = ji != sji;
				}
			}

			if (broken)
			{
				// Sparse index does not correspond to joint index. Table must have been modified
				// during iteration.
				si = _table.sparseIndexFromJointIndex(ji);
				_sparseIndex = si < 0 ? -1-si : si;
			}
			
			assert(_sparseIndex >=0);
		}
		else
		{
			_sparseIndex = -1;
		}
	}
}
