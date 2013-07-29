package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.DimpleException;

/**
 * Iterator over entries in a {@link INewFactorTableBase}.
 */
@NotThreadSafe
public class NewFactorTableIterator implements Iterator<NewFactorTableEntry>
{
	/*-------
	 * State
	 */
	
	private final boolean _dense;
	private final INewFactorTableBase _table;
	private int _sparseIndex;
	private int _jointIndex;
	private double _energy;
	private double _weight;
	
	/*--------------
	 * Construction
	 */
	
	NewFactorTableIterator(INewFactorTableBase table, boolean dense)
	{
		_dense = dense;
		_table = table;
		_sparseIndex = 0;
		_jointIndex = 0;
		_energy = Double.POSITIVE_INFINITY;
		_weight = 0.0;
		
		if (dense)
		{
			if (table.hasSparseRepresentation())
			{
				if (0 < table.sparseSize() && table.sparseIndexToJointIndex(0) == 0)
				{
					_weight = table.getWeightForSparseIndex(0);
					_energy = table.getEnergyForSparseIndex(0);
				}
			}
			else
			{
				_weight = table.getWeightForJointIndex(0);
				_energy = table.getEnergyForJointIndex(0);
			}
		}
		else
		{
			if (table.hasSparseRepresentation())
			{
				if (0 < table.sparseSize())
				{
					_jointIndex = table.sparseIndexToJointIndex(0);
					_weight = table.getWeightForSparseIndex(0);
					_energy = table.getEnergyForSparseIndex(0);
				}
			}
			else
			{
				_sparseIndex = -1;
				for (int end = table.jointSize(); _jointIndex < end; ++_jointIndex)
				{
					_weight = table.getWeightForJointIndex(_jointIndex);
					if (_weight != 0.0)
					{
						_energy = table.getEnergyForJointIndex(_jointIndex);
						break;
					}
				}
			}
		}
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _dense ? _jointIndex < _table.jointSize() : _weight != 0.0;
	}
	
	@Override
	public NewFactorTableEntry next()
	{
		NewFactorTableEntry entry = getEntry();
		if (entry != null)
		{
			advance();
		}
		return entry;
	}

	@Override
	public void remove()
	{
		throw DimpleException.unsupportedMethod(getClass(), "remove");
	}
	
	/*---------
	 * Methods
	 */
	
	public boolean advance()
	{
		if (done())
		{
			return false;
		}
		
		fixSparseIndex();
		
		return _dense? denseAdvance() : sparseAdvance();
	}
	
	public boolean done()
	{
		return _jointIndex >= _table.jointSize();
	}
	
	public NewFactorTableEntry getEntry()
	{
		NewFactorTableEntry entry = null;
		
		if (_jointIndex < _table.jointSize())
		{
			entry = new NewFactorTableEntry(_table.getDomainList(), _sparseIndex, _jointIndex, _energy, _weight);
		}
		
		return entry;
	}
	
	public double energy()
	{
		return _energy;
	}
	
	public int jointIndex()
	{
		return _jointIndex;
	}
	
	public int sparseIndex()
	{
		return _sparseIndex;
	}
	
	public double weight()
	{
		return _weight;
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
			_sparseIndex = -1;
			return false;
		}
		
		if (_table.hasSparseRepresentation())
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
					_sparseIndex = -1;
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
			_sparseIndex = -1;
			_energy = _table.getEnergyForJointIndex(ji);
			_weight = _table.getWeightForJointIndex(ji);
		}
		
		return true;
	}
	
	private boolean sparseAdvance()
	{
		if (_table.hasSparseRepresentation())
		{
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
