package com.analog.lyric.dimple.factorfunctions.core;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
final class SparseFactorTableIterator extends FactorTableIteratorBase
{
	/*-------
	 * State
	 */
	
	private final SparseFactorTable _table;
	private int[] _jointIndices;
	
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
