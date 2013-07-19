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
	private int _location;
	private int _jointIndex;
	private double _energy;
	
	/*--------------
	 * Construction
	 */
	
	NewFactorTableIterator(INewFactorTableBase table, boolean dense)
	{
		_dense = dense;
		_table = table;
		_location = -1;
		_jointIndex = -1;
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		if (_jointIndex + 1 >= _table.jointSize())
		{
			return false;
		}
		
		if (!_dense)
		{
			return fixedLocation() + 1 < _table.size();
		}
		
		return true;
	}
	
	@Override
	public NewFactorTableEntry next()
	{
		return advance() ? getEntry() : null;
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
		boolean advanced = false;
		
		if (_jointIndex + 1 < _table.jointSize())
		{
			if (_dense)
			{
				++_jointIndex;
				_location = _table.locationFromJointIndex(_jointIndex);
				advanced = true;
			}
			else
			{
				_location = fixedLocation();
				if (_location + 1 < _table.size())
				{
					++_location;
					_jointIndex = _table.locationToJointIndex(_location);
					advanced = true;
				}
				else
				{
					_jointIndex = _table.jointSize();
				}
			}
		}
		
		_energy = advanced ? _table.getEnergyForLocation(_location) : Double.POSITIVE_INFINITY;
		
		return advanced;
	}
	
	public NewFactorTableEntry getEntry()
	{
		return new NewFactorTableEntry(_table, _location, _jointIndex, _energy);
	}
	
	public double energy()
	{
		return _energy;
	}
	
	public int location()
	{
		return _location;
	}
	
	public int jointIndex()
	{
		return _jointIndex;
	}
	
	public double weight()
	{
		return NewFactorTableBase.energyToWeight(_energy);
	}

	/*-----------------
	 * Private methods
	 */
	
	private int fixedLocation()
	{
		int location = _location;
		if (_jointIndex >= 0 && _jointIndex != _table.locationToJointIndex(location))
		{
			// Reset location from joint location.
			location = _table.locationFromJointIndex(_jointIndex);
			if (location < 0)
			{
				location = -1-_location;
			}
		}
		return location;
	}
	
}
