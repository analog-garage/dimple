package com.analog.lyric.dimple.factorfunctions.core;

import java.io.Serializable;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.domains.JointDomainIndexer;

/**
 * Holds the information for one factor table entry from a {@link FactorTableIterator}
 */
@Immutable
public final class FactorTableEntry implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final JointDomainIndexer _domains;
	private final int _sparseIndex;
	private final int _jointIndex;
	private final double _energy;
	private final double _weight;
	
	/*--------------
	 * Construction
	 */
	
	public FactorTableEntry(JointDomainIndexer domains, int sparseIndex, int jointIndex, double energy, double weight)
	{
		_domains = domains;
		_sparseIndex = sparseIndex;
		_jointIndex = jointIndex;
		_energy = energy;
		_weight = weight;
	}
	
	/*---------
	 * Methods
	 */
	
	public JointDomainIndexer domains()
	{
		return _domains;
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
		return _domains.jointIndexToIndices(_jointIndex, indices);
	}
	
	public int jointIndex()
	{
		return _jointIndex;
	}

	public int sparseIndex()
	{
		return _sparseIndex;
	}

	public Object[] values()
	{
		return values(null);
	}
	
	public Object[] values(Object[] arguments)
	{
		return _domains.jointIndexToElements(_jointIndex, arguments);
	}
	
	/**
	 * The weight value for this entry.
	 * @see #energy
	 */
	public double weight()
	{
		return _weight;
	}
}