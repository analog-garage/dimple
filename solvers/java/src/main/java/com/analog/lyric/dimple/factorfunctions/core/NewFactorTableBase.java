package com.analog.lyric.dimple.factorfunctions.core;

import java.util.BitSet;
import java.util.Random;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.JointDomainIndexer;

@NotThreadSafe
public abstract class NewFactorTableBase implements INewFactorTableBase, IFactorTable
{
	/*--------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private JointDomainIndexer _domains;
	
	/*--------------
	 * Construction
	 */
	
	protected NewFactorTableBase(JointDomainIndexer domains)
	{
		_domains = domains;
	}
	
	protected NewFactorTableBase(BitSet directedTo, DiscreteDomain ... domains)
	{
		_domains = JointDomainIndexer.create(directedTo, domains);
	}
	
	protected NewFactorTableBase(NewFactorTableBase that)
	{
		_domains = that._domains;
	}
	
	/*----------------
	 * Object methods
	 */

	@Override
	public abstract NewFactorTableBase clone();
	
	/*------------------
	 * Iterable methods
	 */
	
	@Override
	public NewFactorTableIterator iterator()
	{
		return new NewFactorTableIterator(this, false);
	}
	
	@Override
	public NewFactorTableIterator fullIterator()
	{
		return new NewFactorTableIterator(this, true);
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
	public final double getEnergyForArguments(Object ... arguments)
	{
		return getEnergyForJointIndex(_domains.jointIndexFromElements(arguments));
	}
	
	@Override
	public final double getWeightForArguments(Object ... arguments)
	{
		return getWeightForJointIndex(_domains.jointIndexFromElements(arguments));
	}
	
	@Override
	public final BitSet getInputSet()
	{
		return _domains.getInputSet();
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
	public final int sparseIndexFromArguments(Object ... arguments)
	{
		return sparseIndexFromJointIndex(_domains.jointIndexFromElements(arguments));
	}
	
	@Override
	public final int sparseIndexFromIndices(int ... indices)
	{
		_domains.validateIndices(indices);
		return sparseIndexFromJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public Object[] sparseIndexToArguments(int sparseIndex, Object[] arguments)
	{
		return _domains.jointIndexToElements(sparseIndexToJointIndex(sparseIndex), arguments);
	}
	
	@Override
	public int[] sparseIndexToIndices(int sparseIndex, int[] indices)
	{
		return _domains.jointIndexToIndices(sparseIndexToJointIndex(sparseIndex), indices);
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
	public final void setEnergyForArguments(double energy, Object ... arguments)
	{
		setEnergyForJointIndex(energy, _domains.jointIndexFromElements(arguments));
	}

	@Override
	public final void setEnergyForIndices(double energy, int ... indices)
	{
		_domains.validateIndices(indices);
		setEnergyForJointIndex(energy, _domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public final void setWeightForArguments(double weight, Object ... arguments)
	{
		setWeightForJointIndex(weight, _domains.jointIndexFromElements(arguments));
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
	public void changeIndices(int[][] indices)
	{
		change(indices, getWeights());
	}

	@Override
	public void changeWeight(int index, double weight)
	{
		setWeightForSparseIndex(weight, index);
	}

	@Override
	public double evalAsFactorFunction(Object... arguments)
	{
		final int sparseIndex = sparseIndexFromJointIndex(_domains.jointIndexFromElements(arguments));
		return sparseIndex >= 0 ? getWeightForSparseIndex(sparseIndex) : 0.0 ;
	}

	@Override
	public void evalDeterministicFunction(Object... arguments)
	{
		evalDeterministic(arguments);
	}

	@Override
	public double get(int[] indices)
	{
		return getWeightForIndices(indices);
	}

	@Override
	public final DiscreteDomain[] getDomains()
	{
		return _domains.toArray(new DiscreteDomain[getDimensions()]);
	}

	@Override
	public int[][] getIndices()
	{
		final int size = sparseSize();
		if (size == 0)
		{
			return ArrayUtil.EMPTY_INT_ARRAY_ARRAY;
		}
		
		int[][] indices = new int[size][];
		for (int i = 0; i < size; ++i)
		{
			indices[i] = getRow(i);
		}
		return indices;
	}

	@Override
	public int[] getColumnCopy(int column)
	{
		int[] result = new int[sparseSize()];
		int[] indices = new int[getDimensions()];
		
		for (int i = 0, end = sparseSize(); i < end; ++i)
		{
			sparseIndexToIndices(i, indices);
			result[i] = indices[column];
		}
	
		return result;
	}

	@Override
	public int getColumns()
	{
		return getDimensions();
	}

	@Override
	public int[] getDirectedFrom()
	{
		return _domains.getInputDomainIndices();
	}

	@Override
	public int[] getDirectedTo()
	{
		return _domains.getOutputDomainIndices();
	}

	@Override
	public int getEntry(int row, int column)
	{
		return _domains.jointIndexToElementIndex(sparseIndexToJointIndex(row), column);
	}

	@Override
	public int[] getRow(int row)
	{
		int[] indices = new int[getDimensions()];
		sparseIndexToIndices(row, indices);
		return indices;
	}

	@Override
	public int getRows()
	{
		return sparseSize();
	}

	@Override
	public final int getWeightIndexFromTableIndices(int[] indices)
	{
		_domains.validateIndices(indices);
		return sparseIndexFromJointIndex(_domains.jointIndexFromIndices(indices));
	}

	@Override
	public void randomizeWeights(Random rand)
	{
		for (int i = jointSize(); --i >= 0;)
		{
			setWeightForJointIndex(rand.nextDouble(), i);
		}
	}

	@Override
	public void serializeToXML(String serializeName, String targetDirectory)
	{
		throw DimpleException.unsupported("serializeToXML");
	}
	
	@Override
	public void set(int[] indices, double value)
	{
		setWeightForIndices(value, indices);
	}
}