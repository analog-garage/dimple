package com.analog.lyric.dimple.model;

import java.util.Arrays;
import java.util.BitSet;

import net.jcip.annotations.Immutable;

@Immutable
public final class DirectedDiscreteDomainList extends DiscreteDomainList
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	final BitSet _outputSet;
	final int _inputCardinality;
	final int[] _inputIndices;
	final int[] _inputProducts;
	final int _outputCardinality;
	final int[] _outputIndices;
	final int[] _outputProducts;
	final int[] _directedProducts;
	final private boolean _canonicalOrder;
	
	/*--------------
	 * Construction
	 */
	
	DirectedDiscreteDomainList(BitSet outputs, DiscreteDomain ... domains)
	{
		super(computeHashCode(outputs, domains), domains);
		_outputSet = outputs;
		
		final int nDomains = domains.length;
		final int nOutputs = outputs.cardinality();
		final int nInputs = nDomains - nOutputs;
		
		if (outputs.length() > nDomains)
		{
			throw new DimpleException("Illegal output set for domain list");
		}
		
		final int[] inputIndices = new int[nInputs];
		final int[] inputProducts = new int[nDomains];
		final int[] outputIndices = new int[nOutputs];
		final int[] outputProducts = new int[nDomains];
		final int[] directedProducts = new int[nDomains];
			
		int curInput = 0, curOutput = 0;
		int inputProduct = 1, outputProduct = 1;
		
		for (int i = 0; i < nDomains; ++i)
		{
			final int size = domains[i].size();

			if (outputs.get(i))
			{
				outputIndices[curOutput] = i;
				outputProducts[i] = outputProduct;
				outputProduct *= size;
				++curOutput;
			}
			else
			{
				inputIndices[curInput] = i;
				inputProducts[i] = inputProduct;
				inputProduct *= size;
				++curInput;
			}
		}
		
		boolean canonicalOrder = true;
		for (int i = 0; i < nOutputs; ++i)
		{
			int j = outputIndices[i];
			if (i != j)
			{
				canonicalOrder = false;
			}
			directedProducts[j] = outputProducts[j];
		}
		for (int i = 0; i < nInputs; ++i)
		{
			int j = inputIndices[i];
			directedProducts[j] = inputProducts[j] * outputProduct;
		}
		
		_inputCardinality = inputProduct;
		_outputCardinality = outputProduct;
		
		_inputIndices = inputIndices;
		_inputProducts = inputProducts;
		_outputIndices = outputIndices;
		_outputProducts = outputProducts;
		_directedProducts = directedProducts;
		_canonicalOrder = canonicalOrder;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object that)
	{
		if (this == that)
		{
			return true;
		}
		
		if (that instanceof DirectedDiscreteDomainList)
		{
			DirectedDiscreteDomainList thatDiscrete = (DirectedDiscreteDomainList)that;
			return Arrays.equals(_domains, thatDiscrete._domains)
				&& _outputSet.equals(thatDiscrete._outputSet);
		}
		
		return false;
	}
	
	/*----------------------------
	 * DiscreteDomainList methods
	 */
	
	@Override
	public int getInputCardinality()
	{
		return _inputCardinality;
	}
	
	@Override
	public int[] getInputIndices()
	{
		return _inputIndices.clone();
	}
	
	@Override
	public int getInputIndex(int i)
	{
		return _inputIndices[i];
	}
	
	@Override
	public BitSet getInputSet()
	{
		BitSet set = getOutputSet();
		set.flip(0, size());
		return set;
	}
	
	@Override
	public int getInputSize()
	{
		return _inputIndices.length;
	}
	
	@Override
	public int getOutputCardinality()
	{
		return _outputCardinality;
	}
	
	@Override
	public int getOutputIndex(int i)
	{
		return _outputIndices[i];
	}
	
	@Override
	public int[] getOutputIndices()
	{
		return _outputIndices.clone();
	}
	
	@Override
	public BitSet getOutputSet()
	{
		return (BitSet) _outputSet.clone();
	}

	@Override
	public int getOutputSize()
	{
		return _outputIndices.length;
	}
	
	@Override
	public boolean isDirected()
	{
		return true;
	}
	
	@Override
	public boolean hasCanonicalDomainOrder()
	{
		return _canonicalOrder;
	}
	
	@Override
	public int inputIndexFromElements(Object ... elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _inputProducts;
		int joint = 0;
		for (int i = 0, end = products.length; i < end; ++i)
		{
			int product = products[i];
			if (product != 0)
			{
				joint += product * domains[i].getIndexOrThrow(elements[i]);
			}
		}
		return joint;
	}
	
	@Override
	public int inputIndexFromIndices(int ... indices)
	{
		final int length = indices.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += indices[i] * _inputProducts[i];
		}
		return joint;
	}
	
	@Override
	public int inputIndexFromJointIndex(int jointIndex)
	{
		return jointIndex / _outputCardinality;
	}
	
	@Override
	public void inputIndexToElements(int inputIndex, Object[] elements)
	{
		locationToElements(inputIndex, elements, _inputIndices, _inputProducts);
	}
	
	@Override
	public void inputIndexToIndices(int inputIndex, int[] indices)
	{
		locationToIndices(inputIndex, indices, _inputIndices, _inputProducts);
	}
	
	@Override
	public int jointIndexFromElements(Object ... elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _directedProducts;
		int joint = 0;
		for (int i = 0, end = products.length; i < end; ++i)
		{
			joint += products[i] * domains[i].getIndexOrThrow(elements[i]);
		}
		return joint;
	}

	@Override
	public int jointIndexFromIndices(int ... indices)
	{
		final int length = indices.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += indices[i] * _directedProducts[i];
		}
		return joint;
	}
	
	@Override
	public int jointIndexFromInputOutputIndices(int inputIndex, int outputIndex)
	{
		return outputIndex + inputIndex * _outputCardinality;
	}
	
	@Override
	public <T> T[] jointIndexToElements(int jointIndex, T[] elements)
	{
		elements = allocateElements(elements);
		final int inputIndex = jointIndex / _outputCardinality;
		final int outputIndex = jointIndex - inputIndex * _outputCardinality;
		inputIndexToElements(inputIndex, elements);
		outputIndexToElements(outputIndex, elements);
		return elements;
	}
	
	@Override
	public int[] jointIndexToIndices(int jointIndex, int[] indices)
	{
		indices = allocateIndices(indices);
		final int inputIndex = jointIndex / _outputCardinality;
		final int outputIndex = jointIndex - inputIndex * _outputCardinality;
		inputIndexToIndices(inputIndex, indices);
		outputIndexToIndices(outputIndex, indices);
		return indices;
	}
	
	@Override
	public int outputIndexFromElements(Object ... elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _outputProducts;
		int joint = 0;
		for (int i = 0, end = products.length; i < end; ++i)
		{
			int product = products[i];
			if (product != 0)
			{
				joint += product * domains[i].getIndexOrThrow(elements[i]);
			}
		}
		return joint;
	}
	
	@Override
	public int outputIndexFromIndices(int ... indices)
	{
		final int length = indices.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += indices[i] * _outputProducts[i];
		}
		return joint;
	}
	
	@Override
	public int outputIndexFromJointIndex(int jointIndex)
	{
		return jointIndex % _outputCardinality;
	}
	
	@Override
	public void outputIndexToElements(int outputIndex, Object[] elements)
	{
		locationToElements(outputIndex, elements, _outputIndices, _outputProducts);
	}
	
	@Override
	public void outputIndexToIndices(int outputIndex, int[] indices)
	{
		locationToIndices(outputIndex, indices, _outputIndices, _outputProducts);
	}

	/*-----------------
	 * Private methods
	 */
	
	private static int computeHashCode(BitSet inputs, DiscreteDomain[] domains)
	{
		return Arrays.hashCode(domains) * 13 + inputs.hashCode();
	}
	
	private void locationToElements(int location, Object[] elements, int[] subindices, int[] products)
	{
		final DiscreteDomain[] domains = _domains;
		int product, index;
		for (int i = subindices.length; --i >= 0;)
		{
			int j = subindices[i];
			index = location / (product = products[j]);
			elements[j] = domains[j].getElement(index);
			location -= index * product;
		}
	}
	private static void locationToIndices(int location, int[] indices, int[] subindices, int[] products)
	{
		int product, index;
		for (int i = subindices.length; --i >= 0;)
		{
			int j = subindices[i];
			indices[j] = index = location / (product = products[j]);
			location -= index * product;
		}
	}

	
}
