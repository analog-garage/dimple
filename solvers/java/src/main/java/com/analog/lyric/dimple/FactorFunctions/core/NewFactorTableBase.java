package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.BitSet;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.DiscreteDomain;

public abstract class NewFactorTableBase implements INewFactorTableBase, IFactorTable
{
	/*--------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	/**
	 * The domains defining the dimensions of the factor table in the order in
	 * which the corresponding arguments/indices are passed.
	 */
	protected final DiscreteDomain[] _domains;
	
	/**
	 * If non-null, specifies which arguments/indices/domains represent inputs
	 * for a directed table. The {@link BitSet#size()} should be the same as
	 * the {@link #getDomainCount()} and should have at least one set bit and one
	 * clear bit.
	 */
	protected final BitSet _inputSet;

	/**
	 * If this is a directed factor table (and {@link #_inputSet} is non-null),
	 * this will contain the indexes of the input arguments in increasing order.
	 * Otherwise this will be null.
	 */
	protected final int[] _inputIndices;
	
	/**
	 * If this is a directed factor table (and {@link #_inputSet} is non-null),
	 * this will contain the indexes of the output arguments in increasing order.
	 * Otherwise this will be null.
	 */
	protected final int[] _outputIndices;
	
	/**
	 * If this is a directed factor table {@link #_inputSet} is non-null,
	 * the first entry will contain one, and subsequent entries will contain
	 * the cumulative product of the domain sizes of the input arguments in
	 * the order specified by {@link #_inputIndices}.
	 * <p>
	 * If this is an undirected factor table this will contain the cumulative
	 * products of the domain sizes of all of the arguments.
	 */
	protected final int[] _inputDomainProducts;

	/**
	 * Similar to {@link #_inputDomainProducts} for input arguments, but when
	 * table is not directed this array will only contain a single element with
	 * the value 1.
	 */
	protected final int[] _outputDomainProducts;
	
	/**
	 * @see #jointSize()
	 */
	protected final int _jointSize;

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
		_domains = domains;
		_inputSet = directedFrom;

		final int nDomains = domains.length;
		
		if (directedFrom == null)
		{
			_outputIndices = EMPTY_INT_ARRAY;
			_inputIndices = EMPTY_INT_ARRAY;
			_inputDomainProducts = new int[nDomains + 1];
			_inputDomainProducts[0] = 1;
			_outputDomainProducts = new int[] { 1 };
			
			int product = 1;
			for (int i  = 0; i < nDomains; ++i)
			{
				product *= domains[i].size();
				_inputDomainProducts[i + 1] = product;
			}
			
			_jointSize = product;
		}
		else
		{
			final int indexMap[] = new int[nDomains];
			final int nInputs = bitsetToIndexMap(directedFrom, indexMap);
			final int nOutputs = nDomains - nInputs;
			int inputProduct = 1, outputProduct = 1;
			
			_inputIndices = new int[nInputs];
			_inputDomainProducts = new int[nInputs + 1];
			_inputDomainProducts[0] = 1;
			
			_outputIndices = new int[nOutputs];
			_outputDomainProducts = new int[nOutputs + 1];
			_outputDomainProducts[0] = 1;
			
			for (int i = 0; i < nDomains; ++i)
			{
				int size = domains[i].size();
				int j = indexMap[i];
				if (j >= 0)
				{
					_inputIndices[j] = i;
					inputProduct *= size;
					_inputDomainProducts[j+1] = inputProduct;
				}
				else
				{
					j = -1-j;
					_outputIndices[j] = i;
					outputProduct *= size;
					_outputDomainProducts[j+1] = outputProduct;
				}
			}
			
			_jointSize = inputProduct * outputProduct;
		}
	}
	
	public NewFactorTableBase(NewFactorTableBase that)
	{
		_domains = that._domains.clone();
		_inputSet = that._inputSet == null ? null : (BitSet)that._inputSet.clone();
		_inputDomainProducts = cloneArray(that._inputDomainProducts);
		_outputDomainProducts = cloneArray(that._outputDomainProducts);
		_inputIndices = cloneArray(that._inputIndices);
		_outputIndices = cloneArray(that._outputIndices);
		_jointSize = that._jointSize;
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
	public final int getDomainCount()
	{
		return _domains.length;
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
	public DiscreteDomain getDomain(int i)
	{
		return _domains[i];
	}

	@Override
	public BitSet getInputSet()
	{
		return _inputSet == null ? null : (BitSet)_inputSet.clone();
	}
	
	@Override
	public int getDomainSize(int i)
	{
		return _domains[i].size();
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
		return locationFromJointIndex(jointIndexFromArguments(arguments));
	}
	
	@Override
	public final int locationFromIndices(int ... indices)
	{
		return locationFromJointIndex(jointIndexFromIndices(indices));
	}
	
	@Override
	public Object[] locationToArguments(int location, Object[] arguments)
	{
		return jointIndexToArguments(locationToJointIndex(location), arguments);
	}
	
	@Override
	public final int[] locationToIndices(int location, int[] indices)
	{
		return jointIndexToIndices(locationToJointIndex(location), indices);
	}
	
	@Override
	public int jointIndexFromArguments(Object ... arguments)
	{
		return outputIndexFromArguments(arguments) + inputIndexFromArguments(arguments) * getOutputIndexSize();
	}
	
	@Override
	public int jointIndexFromIndices(int ... indices)
	{
		return outputIndexFromIndices(indices) + inputIndexFromIndices(indices) * getOutputIndexSize();
	}
	
	@Override
	public Object[] jointIndexToArguments(int joint, Object[] arguments)
	{
		if (arguments == null || arguments.length != _domains.length)
		{
			arguments = new Object[_domains.length];
		}
		final int outputSize = getOutputIndexSize();
		final int inputIndex = joint / outputSize;
		final int outputIndex = joint - inputIndex * outputSize;
		inputIndexToArguments(inputIndex, arguments);
		outputIndexToArguments(outputIndex, arguments);
		return arguments;
	}
	
	@Override
	public int[] jointIndexToIndices(int joint, int[] indices)
	{
		if (indices == null || indices.length != _domains.length)
		{
			indices = new int[_domains.length];
		}
		final int outputSize = getOutputIndexSize();
		final int inputIndex = joint / outputSize;
		final int outputIndex = joint - inputIndex * outputSize;
		inputIndexToIndices(inputIndex, indices);
		outputIndexToIndices(outputIndex, indices);
		return indices;
	}
	
	@Override
	public final int jointSize()
	{
		return _jointSize;
	}
	
	@Override
	public boolean isDirected()
	{
		return _inputSet != null;
	}

	/*-----------------------
	 * IFactorTable methods
	 */
	
	@Override
	public double get(int[] indices)
	{
		return getWeightForIndices(indices);
	}

	@Override
	public final DiscreteDomain[] getDomains()
	{
		return _domains;
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
		int[] indices = new int[getDomainCount()];
		
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
		return getDomainCount();
	}

	@Override
	public int[] getDirectedFrom()
	{
		return _inputIndices.clone();
	}

	@Override
	public int[] getDirectedTo()
	{
		return _outputIndices.clone();
	}

	@Override
	public int getEntry(int row, int column)
	{
		return getRow(row)[column];
	}

	@Override
	public int[] getRow(int row)
	{
		int[] indices = new int[getDomainCount()];
		locationToIndices(row, indices);
		return indices;
	}

	@Override
	public int getRows()
	{
		return size();
	}

	@Override
	public void serializeToXML(String serializeName, String targetDirectory)
	{
		throw DimpleException.unsupported("serializeToXML");
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

	protected int getInputIndexSize()
	{
		return _inputDomainProducts[_inputDomainProducts.length - 1];
	}
	
	protected int getOutputIndexSize()
	{
		return _outputDomainProducts[_outputDomainProducts.length - 1];
	}
	
	protected int inputIndexFromArguments(Object[] arguments)
	{
		return locationFromArguments(arguments, _domains, _inputIndices, _inputDomainProducts);
	}
	
	protected int inputIndexFromIndices(int[] indices)
	{
		return locationFromIndices(indices, _inputIndices, _inputDomainProducts);
	}

	protected void inputIndexToArguments(int inputIndex, Object[] arguments)
	{
		locationToArguments(inputIndex, arguments, _domains, _inputIndices, _inputDomainProducts);
	}

	protected void inputIndexToIndices(int inputIndex, int[] indices)
	{
		locationToIndices(inputIndex, indices, _inputIndices, _inputDomainProducts);
	}
	
	protected int outputIndexFromArguments(Object[] arguments)
	{
		return locationFromArguments(arguments, _domains, _outputIndices, _outputDomainProducts);
	}
	
	protected int outputIndexFromIndices(int[] indices)
	{
		return locationFromIndices(indices, _outputIndices, _outputDomainProducts);
	}
	
	protected void outputIndexToIndices(int outputIndex, int[] indices)
	{
		locationToIndices(outputIndex, indices, _outputIndices, _outputDomainProducts);
	}

	protected void outputIndexToArguments(int outputIndex, Object[] arguments)
	{
		locationToArguments(outputIndex, arguments, _domains, _outputIndices, _outputDomainProducts);
	}
		
	protected static int locationFromArguments(Object[] arguments, DiscreteDomain[] domains, int[] subindices, int[] products)
	{
		int location = 0;
		
		if (subindices.length == 0)
		{
			for (int i = 0, end = products.length - 1; i < end; ++i)
			{
				location += products[i] * domains[i].getIndex(arguments[i]);
			}
		}
		else
		{
			assert(subindices.length + 1 == products.length);
			for (int i = 0, end = subindices.length; i < end; ++i)
			{
				int j = subindices[i];
				location += products[i] * domains[j].getIndex(arguments[j]);
			}
		}
		
		return location;
	}
	
	protected static int locationFromIndices(int[] indices, int[] subindices, int[] products)
	{
		int location = 0;
		
		if (subindices.length == 0)
		{
			for (int i = 0, end = products.length - 1; i < end; ++i)
			{
				location += products[i] * indices[i];
			}
		}
		else
		{
			assert(products.length == subindices.length + 1);
			for (int i = 0, end = subindices.length; i < end; ++i)
			{
				location += products[i] * indices[subindices[i]];
			}
		}
		
		return location;
	}
	
	protected static void locationToArguments(int location, Object[] arguments, DiscreteDomain[] domains,
		int[] subindices, int[] products)
	{
		assert(location >= 0);
		int product, index;
		if (subindices.length == 0)
		{
			for (int i = products.length - 1; --i >= 0;)
			{
				index = location / (product = products[i]);
				arguments[i] = domains[i].getElement(index);
				location -= index * product;
			}
		}
		else
		{
			assert(products.length == subindices.length + 1);
			for (int i = subindices.length; --i >= 0;)
			{
				int j = subindices[i];
				index = location / (product = products[i]);
				arguments[j] = domains[j].getElement(index);
				location -= index * product;
			}
		}

	}

	protected static void locationToIndices(int location, int[] indices, int[] subindices, int[] products)
	{
		assert(location >= 0);
		int product, index;
		if (subindices.length == 0)
		{
			for (int i = products.length - 1; --i >= 0;)
			{
				indices[i] = index = location / (product = products[i]);
				location -= index * product;
			}
		}
		else
		{
			assert(products.length == subindices.length + 1);
			for (int i = subindices.length; --i >= 0;)
			{
				indices[subindices[i]] = index = location / (product = products[i]);
				location -= index * product;
			}
		}
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