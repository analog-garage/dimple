package com.analog.lyric.dimple.factorfunctions.core;

/**
 * Enumerates internal representations for {@link FactorTable}.
 * <p>
 * The underlying representation can have (at least) the following elements:
 * <ul>
 * <li>A dense array of weights with entries for every joint index.
 * <li>A dense array of energies with entries for every joint index.
 * <li>A sparse array of weights with entries for every sparse index.
 * <li>A sparse array of energies with entries for every sparse index.
 * <li>A mapping of sparse indexes to joint indexes. This array is used
 * for all sparse representations including deterministic.
 * <li>An array of sparse element indices. This has the same information as the
 * sparse to joint index map but represents joint indexes as an array of element
 * indices.
 * </ul>
 * <p>
 * @see FactorTable#getRepresentation()
 * @see FactorTable#setRepresentation(FactorTableRepresentation)
 */
public enum FactorTableRepresentation
{
	DETERMINISTIC(FactorTable.DETERMINISTIC),
	
	DENSE_ENERGY(FactorTable.DENSE_ENERGY),
	DENSE_WEIGHT(FactorTable.DENSE_WEIGHT),
	ALL_DENSE(FactorTable.ALL_DENSE),
	SPARSE_ENERGY(FactorTable.SPARSE_ENERGY),
	ALL_ENERGY(FactorTable.ALL_ENERGY),
	SPARSE_ENERGY_DENSE_WEIGHT(FactorTable.SPARSE_ENERGY_DENSE_WEIGHT),
	NOT_SPARSE_WEIGHT(FactorTable.NOT_SPARSE_WEIGHT),
	SPARSE_WEIGHT(FactorTable.SPARSE_WEIGHT),
	DENSE_ENERGY_SPARSE_WEIGHT(FactorTable.DENSE_ENERGY_SPARSE_WEIGHT),
	ALL_WEIGHT(FactorTable.ALL_WEIGHT),
	NOT_SPARSE_ENERGY(FactorTable.NOT_SPARSE_ENERGY),
	ALL_SPARSE(FactorTable.ALL_SPARSE),
	NOT_DENSE_WEIGHT(FactorTable.NOT_DENSE_WEIGHT),
	NOT_DENSE_ENERGY(FactorTable.NOT_DENSE_ENERGY),
	ALL_VALUES(FactorTable.ALL_VALUES),
	DETERMINISTIC_WITH_INDICES(FactorTable.DETERMINISTIC_WITH_INDICES),
	DENSE_ENERGY_WITH_INDICES(FactorTable.DENSE_ENERGY_WITH_INDICES),
	DENSE_WEIGHT_WITH_INDICES(FactorTable.DENSE_WEIGHT_WITH_INDICES),
	ALL_DENSE_WITH_INDICES(FactorTable.ALL_DENSE_WITH_INDICES),
	SPARSE_ENERGY_WITH_INDICES(FactorTable.SPARSE_ENERGY_WITH_INDICES),
	ALL_ENERGY_WITH_INDICES(FactorTable.ALL_ENERGY_WITH_INDICES),
	SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES(FactorTable.SPARSE_ENERGY_DENSE_WEIGHT_WITH_INDICES),
	NOT_SPARSE_WEIGHT_WITH_INDICES(FactorTable.NOT_SPARSE_WEIGHT_WITH_INDICES),
	SPARSE_WEIGHT_WITH_INDICES(FactorTable.SPARSE_WEIGHT_WITH_INDICES),
	DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES(FactorTable.DENSE_ENERGY_SPARSE_WEIGHT_WITH_INDICES),
	ALL_WEIGHT_WITH_INDICES(FactorTable.ALL_WEIGHT_WITH_INDICES),
	NOT_SPARSE_ENERGY_WITH_INDICES(FactorTable.NOT_SPARSE_ENERGY_WITH_INDICES),
	ALL_SPARSE_WITH_INDICES(FactorTable.ALL_SPARSE_WITH_INDICES),
	NOT_DENSE_WEIGHT_WITH_INDICES(FactorTable.NOT_DENSE_WEIGHT_WITH_INDICES),
	NOT_DENSE_ENERGY_WITH_INDICES(FactorTable.NOT_DENSE_ENERGY_WITH_INDICES),
	ALL(FactorTable.ALL),
	;

	private final int _mask;
	private static final FactorTableRepresentation[] _values = FactorTableRepresentation.values();
	
	private FactorTableRepresentation(int mask)
	{
		_mask = mask;
	}
	
	private static FactorTableRepresentation forMask(int mask)
	{
		// NOTE: assumes ordinal() == _mask
		return _values[mask];
	}
	
	public static FactorTableRepresentation forOrdinal(int ordinal)
	{
		return _values[ordinal];
	}
	
	int mask()
	{
		return _mask;
	}

	FactorTableRepresentation difference(FactorTableRepresentation that)
	{
		return forMask(_mask & ~that._mask);
	}
	
	FactorTableRepresentation union(FactorTableRepresentation that)
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
		return (_mask & ALL_VALUES._mask) == 0;
	}
}