package com.analog.lyric.dimple.factorfunctions.core;

import java.util.BitSet;
import java.util.Random;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer.Indices;
import com.analog.lyric.dimple.model.values.Value;

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
	public IFactorTableIterator iterator()
	{
		return new FactorTableIterator(this, false);
	}
	
	@Override
	public FactorTableIterator fullIterator()
	{
		return new FactorTableIterator(this, true);
	}
	
	/*--------------------------
	 * IFactorTableBase methods
	 */

	@Override
	public double density()
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
	public double getEnergyForElements(Object ... elements)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public double getWeightForElements(Object ... elements)
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
	public double getEnergyForIndices(int ... indices)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public double getWeightForIndices(int ... indices)
	{
		return getWeightForJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public double getEnergyForValues(Value ... values)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromValues(values));
	}
	
	@Override
	public double getWeightForValues(Value ... values)
	{
		return getWeightForJointIndex(_domains.jointIndexFromValues(values));
	}
	
	@Override
	public IFactorTable createTableConditionedOn(int[] valueIndices)
	{
		final JointDomainIndexer fromDomains = getDomainIndexer();

		if (valueIndices.length != fromDomains.size())
		{
			throw new ArrayIndexOutOfBoundsException(); // FIXME add message
		}
		
		BitSet removedIndices = new BitSet(valueIndices.length);
		for (int i = 0; i < valueIndices.length; ++i)
		{
			int valueIndex = valueIndices[i];
			if (valueIndex >= 0)
			{
				if (valueIndex >= fromDomains.getDomainSize(i))
				{
					throw new IndexOutOfBoundsException(); // FIXME add message
				}
				removedIndices.set(i);
			}
		}
		
		final JointDomainReindexer remover = JointDomainReindexer.createRemover(fromDomains, removedIndices);
		
		Indices indices = remover.getScratch();
		for (int i = 0; i < valueIndices.length; ++i)
		{
			indices.fromIndices[i] = Math.max(0, valueIndices[i]);
		}
		remover.convertIndices(indices);

		final int[] removedValueIndices = indices.removedIndices.clone();
		indices.release();
		
		final IFactorTable newTable = FactorTable.create(remover.getToDomains());
		
		FactorTableRepresentation representation = getRepresentation();
		if (representation.isDeterministic())
		{
			representation = FactorTableRepresentation.SPARSE_ENERGY;
		}
		final boolean useWeight = !representation.hasEnergy();

		if (useWeight)
		{
			newTable.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);
		}
		else
		{
			newTable.setRepresentation(FactorTableRepresentation.SPARSE_ENERGY);
		}

		initTableConditionedOn(newTable, remover, removedValueIndices, useWeight);
		
		newTable.setRepresentation(representation);
		
		return newTable;
	}
	
	@Override
	public int sparseIndexFromElements(Object ... elements)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromElements(elements));
	}
	
	@Override
	public int sparseIndexFromIndices(int ... indices)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public int sparseIndexFromValues(Value ... values)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromValues(values));
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
	public final int[] sparseIndexToIndices(int sparseIndex)
	{
		return sparseIndexToIndices(sparseIndex, null);
	}
	
	@Override
	public boolean isDirected()
	{
		return _domains.isDirected();
	}

	@Override
	public int jointSize()
	{
		return _domains.getCardinality();
	}
	
	@Override
	public void setEnergyForElements(double energy, Object ... elements)
	{
		setEnergyForJointIndex(energy, _domains.jointIndexFromElements(elements));
	}

	@Override
	public void setEnergyForIndices(double energy, int ... indices)
	{
		_domains.validateIndices(indices);
		setEnergyForJointIndex(energy, _domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public void setEnergyForValues(double energy, Value ... values)
	{
		_domains.validateValues(values);
		setEnergyForJointIndex(energy, _domains.jointIndexFromValues(values));
	}
	
	@Override
	public void setWeightForElements(double weight, Object ... elements)
	{
		setWeightForJointIndex(weight, _domains.jointIndexFromElements(elements));
	}

	@Override
	public void setWeightForIndices(double weight, int ... indices)
	{
		_domains.validateIndices(indices);
		setWeightForJointIndex(weight, _domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public void setWeightForValues(double energy, Value ... values)
	{
		_domains.validateValues(values);
		setWeightForJointIndex(energy, _domains.jointIndexFromValues(values));
	}
	
	@Override
	public final boolean supportsJointIndexing()
	{
		return _domains.supportsJointIndexing();
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

	/*-------------------------
	 * FactorTableBase methods
	 */

	/**
	 * Invoked by {@link #createTableConditionedOn(int[])} to copy values into new table.
	 * 
	 * @param newTable is the empty table to be filled in. Initial representation is sparse
	 * with weight or energy as specified by {@code useWeight} argument.
	 * @param remover is the index converter from this table's index format to the new table.
	 * @param removedValueIndices are the domain value indices for the dimensions that are being
	 * conditioned out.
	 * @param useWeight indicates whether to fill table in using weights or energies.
	 */
	protected abstract void initTableConditionedOn(
		IFactorTable newTable, JointDomainReindexer remover, int[] removedValueIndices, boolean useWeight);

}