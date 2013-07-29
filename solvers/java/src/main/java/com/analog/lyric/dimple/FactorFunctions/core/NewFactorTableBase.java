package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.BitSet;
import java.util.Random;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;

public abstract class NewFactorTableBase implements INewFactorTableBase, IFactorTable
{
	/*--------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	protected final DiscreteDomainList _domains;

	
	
	/*--------------
	 * Construction
	 */
	
	protected NewFactorTableBase(BitSet directedFrom, DiscreteDomain ... domains)
	{
		_domains = DiscreteDomainList.create(directedFrom, domains);
	}
	
	public NewFactorTableBase(NewFactorTableBase that)
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
	public final int getDimensions()
	{
		return _domains.size();
	}
	
	@Override
	public final DiscreteDomainList getDomainList()
	{
		return _domains;
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
		return sparseIndexFromJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public Object[] sparseIndexToArguments(int sparseIndex, Object[] arguments)
	{
		return _domains.jointIndexToElements(sparseIndexToJointIndex(sparseIndex), arguments);
	}
	
	@Override
	public final int[] sparseIndexToIndices(int sparseIndex, int[] indices)
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
		return getWeightForSparseIndex(sparseIndexFromJointIndex(_domains.jointIndexFromElements(arguments)));
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
		int[][] indices = new int[sparseSize()][];
		for (int i = 0, end = sparseSize(); i < end; ++i)
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
		return _domains.getInputIndices();
	}

	@Override
	public int[] getDirectedTo()
	{
		return _domains.getOutputIndices();
	}

	@Override
	public int getEntry(int row, int column)
	{
		return getRow(row)[column];
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

	@Override
	public void setDirected(int[] directedTo, int[] directedFrom)
	{
		throw DimpleException.unsupported("setDirected");
	}

	/*--------------------------
	 * Protected helper methods
	 */

	/**
	 * Constructs a new {@link BitSet} of given {@code size} whose set bits are specified
	 * by {@code indices}.
	 * 
	 * @throws IndexOutOfBoundsException if any index is negative or not less than {@code size}.
	 * @throws IllegalArgumentException if any index is specified more than once.
	 */
	protected static BitSet bitsetFromIndices(int size, int ... indices)
	{
		final BitSet bitset = new BitSet(size);
		
		for (int index : indices)
		{
			if (index < 0 || index >= size)
			{
				throw new IndexOutOfBoundsException(String.format("Index %d out of range [0, %d]", index, size - 1));
			}
		
			if (bitset.get(index))
			{
				throw new IllegalArgumentException(String.format("Duplicate index %d", index));
			}
		
			bitset.set(index);
		}
		
		return bitset;
	}
	
	/**
	 * Computes map of indexes into {@code bitset} that indicates the nth set or clear bit. If
	 * the value {@code n} of {@code indexMap[i]} is non-negative, then bit {@code i} is the {@code nth}
	 * set bit (counting from zero), and if negative then {@code i} is the {@code (-1-n)th} clear bit.
	 * 
	 * @param bitset
	 * @param indexMap is an array whose length must equal {@code bitset.size()} and whose values
	 * will computed by this function.
	 * @return the number of bits set in {@code bitset}.
	 */
	protected static int bitsetToIndexMap(BitSet bitset, int[] indexMap)
	{
		int nTrue = 0;
		for (int i = 0, end = indexMap.length; i < end; ++i)
		{
			indexMap[i] = bitset.get(i) ? nTrue++ : nTrue-i-1;
		}
		return nTrue;
	}
	
	protected static int locationFromIndices(int[] indices, int[] products)
	{
		int location = 0;
		
		for (int i = 0, end = products.length - 1; i < end; ++i)
		{
			location += products[i] * indices[i];
		}
		
		return location;
	}
	
	protected static double energyToWeight(double energy)
	{
		return Math.exp(-energy);
	}
	
	protected static double weightToEnergy(double weight)
	{
		return -Math.log(weight);
	}
}