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

	/**
	 * Canonical empty double array.
	 */
	protected static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
	
	/**
	 * Canonical empty int array.
	 */
	protected static final int[] EMPTY_INT_ARRAY = new int[0];
	
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
	public NewFactorTableIterator jointIndexIterator()
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
		return getEnergyForLocation(locationFromArguments(arguments));
	}
	
	@Override
	public final double getWeightForArguments(Object ... arguments)
	{
		return getWeightForLocation(locationFromArguments(arguments));
	}
	
	@Override
	public final BitSet getInputSet()
	{
		return _domains.getInputSet();
	}
	
	@Override
	public final double getEnergyForIndices(int ... indices)
	{
		return getEnergyForLocation(locationFromIndices(indices));
	}
	
	@Override
	public final double getEnergyForJointIndex(int jointIndex)
	{
		return getEnergyForLocation(locationFromJointIndex(jointIndex));
	}
	
	@Override
	public final double getWeightForIndices(int ... indices)
	{
		return getWeightForLocation(locationFromIndices(indices));
	}
	
	@Override
	public final double getWeightForJointIndex(int jointIndex)
	{
		return getWeightForLocation(locationFromJointIndex(jointIndex));
	}
	
	@Override
	public final int locationFromArguments(Object ... arguments)
	{
		return locationFromJointIndex(_domains.jointIndexFromElements(arguments));
	}
	
	@Override
	public final int locationFromIndices(int ... indices)
	{
		return locationFromJointIndex(_domains.jointIndexFromIndices(indices));
	}
	
	@Override
	public Object[] locationToArguments(int location, Object[] arguments)
	{
		return _domains.jointIndexToElements(locationToJointIndex(location), arguments);
	}
	
	@Override
	public final int[] locationToIndices(int location, int[] indices)
	{
		return _domains.jointIndexToIndices(locationToJointIndex(location), indices);
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
	public final void setEnergyForJointIndex(double energy, int jointIndex)
	{
		setEnergyForLocation(energy, allocateLocationForJointIndex(jointIndex));
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
	
	@Override
	public final void setWeightForJointIndex(double weight, int jointIndex)
	{
		setWeightForLocation(weight, allocateLocationForJointIndex(jointIndex));
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
		setWeightForLocation(weight, index);
	}

	@Override
	public double evalAsFactorFunction(Object... arguments)
	{
		return getWeightForLocation(locationFromJointIndex(_domains.jointIndexFromElements(arguments)));
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
		int[][] indices = new int[size()][];
		for (int i = 0, end = size(); i < end; ++i)
		{
			indices[i] = getRow(i);
		}
		return indices;
	}

	@Override
	public int[] getColumnCopy(int column)
	{
		int[] result = new int[size()];
		int[] indices = new int[getDimensions()];
		
		for (int i = 0, end = size(); i < end; ++i)
		{
			locationToIndices(i, indices);
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
		locationToIndices(row, indices);
		return indices;
	}

	@Override
	public int getRows()
	{
		return size();
	}

	@Override
	public int getWeightIndexFromTableIndices(int[] indices)
	{
		return locationFromIndices(indices);
	}

	@Override
	public void randomizeWeights(Random rand)
	{
		for (int i = size(); --i >= 0;)
		{
			setWeightForLocation(rand.nextDouble(), i);
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
	
	protected abstract int allocateLocationForJointIndex(int jointIndex);

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
	
	protected static int[] cloneArray(int[] array)
	{
		if (array == null)
		{
			return null;
		}
		else if (array.length == 0)
		{
			return EMPTY_INT_ARRAY;
		}
		else
		{
			return array.clone();
		}
	}
	
	protected static double[] cloneArray(double[] array)
	{
		if (array == null)
		{
			return null;
		}
		else if (array.length == 0)
		{
			return EMPTY_DOUBLE_ARRAY;
		}
		else
		{
			return array.clone();
		}
	}
	
	protected static int[] copyArrayForInsert(int[] array, int insertionPoint, int insertLength)
	{
		int curSize = array == null ? 0 : array.length;
		
		assert(insertionPoint >= 0 && insertionPoint <= curSize);
		assert(insertLength >= 0);
		
		
		int[] newArray = new int[curSize + insertLength];
		
		for (int i = 0; i < insertionPoint; ++i)
		{
			newArray[i] = array[i];
		}
		for (int i = insertionPoint, j = insertionPoint + insertLength; i < curSize; ++i, ++j)
		{
			newArray[j] = array[i];
		}
		
		return newArray;
	}

	protected static double[] copyArrayForInsert(double[] array, int insertionPoint, int insertLength)
	{
		int curSize = array == null ? 0 : array.length;
		
		assert(insertionPoint >= 0 && insertionPoint <= curSize);
		assert(insertLength >= 0);
		
		
		double[] newArray = new double[curSize + insertLength];
		
		for (int i = 0; i < insertionPoint; ++i)
		{
			newArray[i] = array[i];
		}
		for (int i = insertionPoint, j = insertionPoint + insertLength; i < curSize; ++i, ++j)
		{
			newArray[j] = array[i];
		}
		
		return newArray;
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