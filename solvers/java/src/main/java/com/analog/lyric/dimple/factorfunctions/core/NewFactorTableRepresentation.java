package com.analog.lyric.dimple.factorfunctions.core;

public enum NewFactorTableRepresentation
{
	DETERMINISTIC(NewFactorTable.DETERMINISTIC),
	
	DENSE_ENERGY(NewFactorTable.DENSE_ENERGY),
	DENSE_WEIGHT(NewFactorTable.DENSE_WEIGHT),
	ALL_DENSE(NewFactorTable.ALL_DENSE),
	SPARSE_ENERGY(NewFactorTable.SPARSE_ENERGY),
	ALL_ENERGY(NewFactorTable.ALL_ENERGY),
	SPARSE_ENERGY_DENSE_WEIGHT(NewFactorTable.SPARSE_ENERGY_DENSE_WEIGHT),
	NOT_SPARSE_WEIGHT(NewFactorTable.NOT_SPARSE_WEIGHT),
	SPARSE_WEIGHT(NewFactorTable.SPARSE_WEIGHT),
	DENSE_ENERGY_SPARSE_WEIGHT(NewFactorTable.DENSE_ENERGY_SPARSE_WEIGHT),
	ALL_WEIGHT(NewFactorTable.ALL_WEIGHT),
	NOT_SPARSE_ENERGY(NewFactorTable.NOT_SPARSE_ENERGY),
	ALL_SPARSE(NewFactorTable.ALL_SPARSE),
	NOT_DENSE_WEIGHT(NewFactorTable.NOT_DENSE_WEIGHT),
	NOT_DENSE_ENERGY(NewFactorTable.NOT_DENSE_ENERGY),
	ALL(NewFactorTable.ALL),
	DETERMINISTIC_WITH_INDICES(NewFactorTable.DETERMINISTIC_WITH_INDICES),
	DENSE_ENERGY_WITH_INDICES(NewFactorTable.DENSE_ENERGY_WITH_INDICES),
	DENSE_WEIGHT_WITH_INDICES(NewFactorTable.DENSE_WEIGHT_WITH_INDICES),
	ALL_DENSE_WITH_INDICES(NewFactorTable.ALL_DENSE_WITH_INDICES),
	SPARSE_ENERGY_WITH_INDICES(NewFactorTable.SPARSE_ENERGY_WITH_INDICES),
	ALL_ENERGY_WITH_INDICES(NewFactorTable.ALL_ENERGY_WITH_INDICES),
	SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES(NewFactorTable.SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES),
	NOT_SPARSE_WEIGHT_WITH_INDICES(NewFactorTable.NOT_SPARSE_WEIGHT_WITH_INDICES),
	SPARSE_WEIGHT_WITH_INDICES(NewFactorTable.SPARSE_WEIGHT_WITH_INDICES),
	DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES(NewFactorTable.DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES),
	ALL_WEIGHT_WITH_INDICES(NewFactorTable.ALL_WEIGHT_WITH_INDICES),
	NOT_SPARSE_ENERGY_WITH_INDICES(NewFactorTable.NOT_SPARSE_ENERGY_WITH_INDICES),
	ALL_SPARSE_WITH_INDICES(NewFactorTable.ALL_SPARSE_WITH_INDICES),
	NOT_DENSE_WEIGHT_WITH_INDICES(NewFactorTable.NOT_DENSE_WEIGHT_WITH_INDICES),
	NOT_DENSE_ENERGY_WITH_INDICES(NewFactorTable.NOT_DENSE_ENERGY_WITH_INDICES),
	ALL_WITH_INDICES(NewFactorTable.ALL_WITH_INDICES),
	;

	private final int _mask;
	private static final NewFactorTableRepresentation[] _values = NewFactorTableRepresentation.values();
	
	private NewFactorTableRepresentation(int mask)
	{
		_mask = mask;
	}
	
	private static NewFactorTableRepresentation forMask(int mask)
	{
		// NOTE: assumes ordinal() == _mask
		return _values[mask];
	}
	
	public static NewFactorTableRepresentation forOrdinal(int ordinal)
	{
		return _values[ordinal];
	}
	
	int mask()
	{
		return _mask;
	}

	NewFactorTableRepresentation difference(NewFactorTableRepresentation that)
	{
		return forMask(_mask & ~that._mask);
	}
	
	NewFactorTableRepresentation union(NewFactorTableRepresentation that)
	{
		return forMask(_mask | that._mask);
	}
	
	public boolean hasDenseEnergy()
	{
		return (_mask & DENSE_ENERGY._mask) != 0;
	}
	
	public boolean hasDenseWeight()
	{
		return (_mask & DENSE_WEIGHT._mask) != 0;
	}

	public boolean hasEnergy()
	{
		return (_mask & ALL_ENERGY._mask) != 0;
	}
	
	public boolean hasWeight()
	{
		return (_mask & ALL_WEIGHT._mask) != 0;
	}
	
	public boolean hasSparse()
	{
		return (_mask & ALL_SPARSE._mask) != 0;
	}
	
	public boolean hasSparseEnergy()
	{
		return (_mask & SPARSE_ENERGY._mask) != 0;
	}
	
	public boolean hasSparseWeight()
	{
		return (_mask & SPARSE_WEIGHT._mask) != 0;
	}
	
	public boolean hasDense()
	{
		return (_mask & ALL_DENSE._mask) != 0;
	}
	
	public boolean isDeterministic()
	{
		return (_mask & ALL._mask) == 0;
	}
}