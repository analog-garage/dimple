package com.analog.lyric.dimple.factorfunctions.core;

/**
 * Common implementation base for {@link IFactorTableIterator}s.
 */
abstract class FactorTableIteratorBase implements IFactorTableIterator
{

	int _sparseIndex = 0;
	double _energy = Double.POSITIVE_INFINITY;
	double _weight = 0.0;
	FactorTableEntry _entry = null;

	FactorTableIteratorBase()
	{
	}

	@Override
	public final FactorTableEntry next()
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
	public final FactorTableEntry getEntry()
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