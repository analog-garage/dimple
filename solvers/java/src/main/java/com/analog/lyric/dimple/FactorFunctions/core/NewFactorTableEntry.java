package com.analog.lyric.dimple.FactorFunctions.core;

import net.jcip.annotations.Immutable;

/**
 * Holds the information for one factor table entry from a {@link NewFactorTableIterator}
 */
@Immutable
public final class NewFactorTableEntry
{
	private final INewFactorTableBase _table;
	private final int _location;
	private final int _jointIndex;
	private final double _energy;
	
	NewFactorTableEntry(INewFactorTableBase table, int location, int jointIndex, double energy)
	{
		_table = table;
		_location = location;
		_jointIndex = jointIndex;
		_energy = energy;
	}
	
	/**
	 * The energy value for this table entry.
	 * <p>
	 * Same as the negative log of the {@link #weight}.
	 */
	public double energy()
	{
		return _energy;
	}

	/**
	 * Returns domain indices corresponding to this table entry in newly allocated array.
	 * @see #indices(int[])
	 * @see #values()
	 */
	public int[] indices()
	{
		return indices(null);
	}
	
	/**
	 * Returns domain indices corresponding to this table entry, using provided
	 * {@code indices} array if it is non-null and of the correct length.
	 * 
	 * @see #indices()
	 * @see #values(Object[])
	 */
	public int[] indices(int[] indices)
	{
		return _table.getDomainList().jointIndexToIndices(_jointIndex, indices);
	}
	
	public int jointIndex()
	{
		return _jointIndex;
	}

	public int location()
	{
		return _location;
	}

	public INewFactorTableBase table()
	{
		return _table;
	}
	
	public Object[] values()
	{
		return values(null);
	}
	
	public Object[] values(Object[] arguments)
	{
		return _table.getDomainList().jointIndexToElements(_jointIndex, arguments);
	}
	
	/**
	 * The weight value for this entry.
	 * @see #energy
	 */
	public double weight()
	{
		return NewFactorTableBase.energyToWeight(_energy);
	}
}