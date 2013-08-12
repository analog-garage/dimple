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
	
	final BitSet _inputSet;
	final int _inputCardinality;
	final int[] _inputIndices;
	final int[] _inputProducts;
	final int _outputCardinality;
	final int[] _outputIndices;
	final int[] _outputProducts;
	final int[] _directedProducts;
	
	/*--------------
	 * Construction
	 */
	
	DirectedDiscreteDomainList(BitSet inputs, DiscreteDomain ... domains)
	{
		super(computeHashCode(inputs, domains), domains);
		_inputSet = inputs;
		
		final int nDomains = domains.length;
		final int nInputs = inputs.cardinality();
		final int nOutputs = nDomains - nInputs;
		
		if (nOutputs == 0 || inputs.length() > nDomains)
		{
			throw new DimpleException("Illegal input set for domain list");
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

			if (inputs.get(i))
			{
				inputIndices[curInput] = i;
				inputProducts[i] = inputProduct;
				inputProduct *= size;
				++curInput;
			}
			else
			{
				outputIndices[curOutput] = i;
				outputProducts[i] = outputProduct;
				outputProduct *= size;
				++curOutput;
			}
		}
		
		for (int i = 0; i < nOutputs; ++i)
		{
			int j = outputIndices[i];
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
			return Arrays.equals(_domains, thatDiscrete._domains) && _inputSet.equals(thatDiscrete._inputSet);
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
		return (BitSet) _inputSet.clone();
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
		BitSet set = getInputSet();
		set.flip(0, size());
		return set;
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
	public Object[] jointIndexToElements(int jointIndex, Object[] elements)
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
		assert(location >= 0);
		int product, index;
		for (int i = subindices.length; --i >= 0;)
		{
			int j = subindices[i];
			indices[j] = index = location / (product = products[j]);
			location -= index * product;
		}
	}

	
}
