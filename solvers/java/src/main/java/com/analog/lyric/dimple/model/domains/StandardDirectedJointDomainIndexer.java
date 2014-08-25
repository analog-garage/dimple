/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.model.domains;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.Comparators;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.values.Value;

/**
 * Directed implementation of {@link JointDomainIndexer}.
 */
@Immutable
final class StandardDirectedJointDomainIndexer extends StandardJointDomainIndexer
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final BitSet _outputSet;
	private final int _inputCardinality;
	private final int[] _inputIndices;
	private final int[] _inputProducts;
	private final int _outputCardinality;
	private final int[] _outputIndices;
	private final int[] _outputProducts;
	private final int[] _directedProducts;
	private final boolean _canonicalOrder;
	
	private final Comparator<int[]> _indicesComparator;

	/*--------------
	 * Construction
	 */
	
	StandardDirectedJointDomainIndexer(BitSet outputs, DiscreteDomain ... domains)
	
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
		_indicesComparator = canonicalOrder ? Comparators.reverseLexicalIntArray() :
			new DirectedArrayComparator(inputIndices, outputIndices);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(@Nullable Object that)
	{
		if (this == that)
		{
			return true;
		}
		
		if (that instanceof StandardDirectedJointDomainIndexer)
		{
			StandardDirectedJointDomainIndexer thatDiscrete = (StandardDirectedJointDomainIndexer)that;
			return
				_hashCode == thatDiscrete._hashCode
				&& Arrays.equals(_domains, thatDiscrete._domains)
				&& _outputSet.equals(thatDiscrete._outputSet);
		}
		
		return false;
	}
	
	/*----------------------------
	 * JointDomainIndexer methods
	 */
	
	@Override
	public final Comparator<int[]> getIndicesComparator()
	{
		return _indicesComparator;
	}
	
	@Override
	public int getInputCardinality()
	{
		return _inputCardinality;
	}
	
	@Override
	public int[] getInputDomainIndices()
	{
		return _inputIndices.clone();
	}
	
	@Override
	public int getInputDomainIndex(int i)
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
	public int getOutputDomainIndex(int i)
	{
		return _outputIndices[i];
	}
	
	@Override
	public int[] getOutputDomainIndices()
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
	public int getStride(int i)
	{
		return _directedProducts[i];
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
	public boolean hasSameInputs(int[] indices1, int[] indices2)
	{
		return hasSameInputsImpl(indices1, indices2, _inputIndices);
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
	public int inputIndexFromValues(Value ... values)
	{
		final int length = values.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += values[i].getIndex() * _inputProducts[i];
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
	public void inputIndexToValues(int inputIndex, Value[] elements)
	{
		locationToValues(inputIndex, elements, _inputIndices, _inputProducts);
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
	public int jointIndexFromValues(Value ... values)
	{
		final int length = values.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += values[i].getIndex() * _directedProducts[i];
		}
		return joint;
	}
	
	@Override
	public int jointIndexFromInputOutputIndices(int inputIndex, int outputIndex)
	{
		return outputIndex + inputIndex * _outputCardinality;
	}
	
	@Override
	public <T> T[] jointIndexToElements(int jointIndex, @Nullable T[] elements)
	{
		elements = allocateElements(elements);
		final int inputIndex = jointIndex / _outputCardinality;
		final int outputIndex = jointIndex - inputIndex * _outputCardinality;
		inputIndexToElements(inputIndex, elements);
		outputIndexToElements(outputIndex, elements);
		return elements;
	}
	
	@Override
	public Value[] jointIndexToValues(int jointIndex, Value[] elements)
	{
		final int inputIndex = jointIndex / _outputCardinality;
		final int outputIndex = jointIndex - inputIndex * _outputCardinality;
		inputIndexToValues(inputIndex, elements);
		outputIndexToValues(outputIndex, elements);
		return elements;
	}
	
	@Override
	public int jointIndexToElementIndex(int jointIndex, int domainIndex)
	{
		return (jointIndex / _directedProducts[domainIndex]) % _domains[domainIndex].size();
	}

	@Override
	public int[] jointIndexToIndices(int jointIndex, @Nullable int[] indices)
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
	public int outputIndexFromValues(Value ... values)
	{
		final int length = values.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += values[i].getIndex() * _outputProducts[i];
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

	@Override
	public void outputIndexToValues(int outputIndex, Value[] elements)
	{
		locationToValues(outputIndex, elements, _outputIndices, _outputProducts);
	}
	

}
