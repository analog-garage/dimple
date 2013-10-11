package com.analog.lyric.dimple.factorfunctions.core;

import java.util.BitSet;
import java.util.Random;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;

@NotThreadSafe
public abstract class FactorTableBase implements IFactorTableBase, IFactorTable
{
	/*--------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private JointDomainIndexer _domains;
	
	/*--------------
	 * Construction
	 */
	
	protected FactorTableBase(JointDomainIndexer domains)
	{
		_domains = domains;
	}
	
	protected FactorTableBase(BitSet directedTo, DiscreteDomain ... domains)
	{
		_domains = JointDomainIndexer.create(directedTo, domains);
	}
	
	protected FactorTableBase(FactorTableBase that)
	{
		_domains = that._domains;
	}
	
	/*----------------
	 * Object methods
	 */

	@Override
	public abstract FactorTableBase clone();
	
	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public FactorTableIterator iterator()
	{
		return new FactorTableIterator(this, false);
	}
	
	@Override
	public FactorTableIterator fullIterator()
	{
		return new FactorTableIterator(this, true);
	}
	
	/*-----------------------------
	 * INewFactorTableBase methods
	 */

	@Override
	public final double density()
	{
		return (double)countNonZeroWeights() / (double)jointSize();
	}
	
	@Override
	public final int getDimensions()
	{
		return _domains.size();
	}
	
	@Override
	public final JointDomainIndexer getDomainIndexer()
	{
		return _domains;
	}
	
	protected final void setDomainIndexer(JointDomainIndexer newDomains)
	{
		assert(_domains.domainsEqual(newDomains));
		_domains = newDomains;
	}
	
	@Override
	public final double getEnergyForElements(Object ... elements)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public final double getWeightForElements(Object ... elements)
	{
		return getWeightForJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public final BitSet getInputSet()
	{
		return _domains.getInputSet();
	}
	
	@Override
	public final BitSet getOutputSet()
	{
		return _domains.getOutputSet();
	}
	
	@Override
	public final double getEnergyForIndices(int ... indices)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public final double getWeightForIndices(int ... indices)
	{
		return getWeightForJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public final int sparseIndexFromElements(Object ... elements)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public final int sparseIndexFromIndices(int ... indices)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public Object[] sparseIndexToElements(int sparseIndex, Object[] elements)
	{
		return _domains.jointIndexToElements(sparseIndexToJointIndex(sparseIndex), elements);
	}
	
	@Override
	public int[] sparseIndexToIndices(int sparseIndex, int[] indices)
	{
		return _domains.jointIndexToIndices(sparseIndexToJointIndex(sparseIndex), indices);
	}
	
	@Override
	public int[] sparseIndexToIndices(int sparseIndex)
	{
		return sparseIndexToIndices(sparseIndex, null);
	}
	
	@Override
	public boolean isDirected()
	{
		return _domains.isDirected();
	}

	@Override
	public final int jointSize()
	{
		return _domains.getCardinality();
	}
	
	@Override
	public final void setEnergyForElements(double energy, Object ... elements)
	{
		setEnergyForJointIndex(energy, _domains.jointIndexFromElements(elements));
	}

	@Override
	public final void setEnergyForIndices(double energy, int ... indices)
	{
		_domains.validateIndices(indices);
		setEnergyForJointIndex(energy, _domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public final void setWeightForElements(double weight, Object ... elements)
	{
		setWeightForJointIndex(weight, _domains.jointIndexFromElements(elements));
	}

	@Override
	public final void setWeightForIndices(double weight, int ... indices)
	{
		_domains.validateIndices(indices);
		setWeightForJointIndex(weight, _domains.jointIndexFromIndices(indices));
	}
	
	/*-----------------------
	 * IFactorTable methods
	 */
	
	@Override
	public void randomizeWeights(Random rand)
	{
		if (hasDenseRepresentation())
		{
			for (int i = jointSize(); --i >= 0;)
			{
				// nextDouble() produces range [0,1). Subtract that from 1.0 to get (0,1].
				setWeightForJointIndex(1.0 - rand.nextDouble(), i);
			}
		}
		else
		{
			for (int i = sparseSize(); --i >= 0;)
			{
				setWeightForSparseIndex(1.0 - rand.nextDouble(), i);
			}
		}
	}

}