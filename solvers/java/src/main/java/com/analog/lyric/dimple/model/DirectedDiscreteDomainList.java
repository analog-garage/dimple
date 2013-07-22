package com.analog.lyric.dimple.model;

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
	
	/*--------------
	 * Construction
	 */
	
	DirectedDiscreteDomainList(BitSet inputs, DiscreteDomain ... domains)
	{
		super(domains);
		_inputSet = inputs;
		
		final int nDomains = domains.length;
		final int nInputs = inputs.cardinality();
		final int nOutputs = nDomains - nInputs;
		
		if (nInputs == 0 || nOutputs == 0 || inputs.length() > nDomains)
		{
			throw new DimpleException("Illegal input set for domain list");
		}
		
		final int[] inputIndices = new int[nInputs];
		final int[] inputProducts = new int[nInputs];
		final int[] outputIndices = new int[nOutputs];
		final int[] outputProducts = new int[nOutputs];

		int curInput = 0, curOutput = 0;
		int inputProduct = 1, outputProduct = 1;
		
		for (int i = 0; i < nDomains; ++i)
		{
			final int size = domains[i].size();

			if (inputs.get(i))
			{
				inputIndices[curInput] = i;
				inputProducts[curInput] = inputProduct;
				inputProduct *= size;
				++curInput;
			}
			else
			{
				outputIndices[curOutput] = i;
				outputProducts[curOutput] = outputProduct;
				outputProduct *= size;
				++curOutput;
			}
		}
		
		_inputCardinality = inputProduct;
		_outputCardinality = outputProduct;
		
		_inputIndices = inputIndices;
		_inputProducts = inputProducts;
		_outputIndices = outputIndices;
		_outputProducts = outputProducts;
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
		return locationFromElements(elements, _inputIndices, _inputProducts);
	}
	
	@Override
	public int inputIndexFromIndices(int ... indices)
	{
		return locationFromIndices(indices, _inputIndices, _inputProducts);
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
		return outputIndexFromElements(elements) + inputIndexFromElements(elements) * _outputCardinality;
	}

	@Override
	public int jointIndexFromIndices(int ... indices)
	{
		return outputIndexFromIndices(indices) + inputIndexFromIndices(indices) * _outputCardinality;
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
		return locationFromElements(elements, _outputIndices, _outputProducts);
	}
	
	@Override
	public int outputIndexFromIndices(int ... indices)
	{
		return locationFromIndices(indices, _outputIndices, _outputProducts);
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
	
	private int locationFromElements(Object[] elements, int[] subindices, int[] products)
	{
		final DiscreteDomain[] domains = _domains;
		int location = 0;
		
		for (int i = 0, end = subindices.length; i < end; ++i)
		{
			int j = subindices[i];
			location += products[i] * domains[j].getIndex(elements[j]);
		}
		
		return location;
	}
	
	private static int locationFromIndices(int[] indices, int[] subindices, int[] products)
	{
		int location = 0;
		
		for (int i = 0, end = subindices.length; i < end; ++i)
		{
			location += products[i] * indices[subindices[i]];
		}
		
		return location;
	}
	
	private void locationToElements(int location, Object[] elements, int[] subindices, int[] products)
	{
		final DiscreteDomain[] domains = _domains;
		int product, index;
		for (int i = subindices.length; --i >= 0;)
		{
			int j = subindices[i];
			index = location / (product = products[i]);
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
			indices[subindices[i]] = index = location / (product = products[i]);
			location -= index * product;
		}
	}

	
}
