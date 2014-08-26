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

import static java.util.Objects.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.Comparators;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.values.Value;

public final class LargeDirectedJointDomainIndexer extends LargeJointDomainIndexer
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final BitSet _outputSet;
	private final int[] _inputIndices;
	private final int[] _outputIndices;
	private final int _outputCardinality;
	private final @Nullable int[] _outputProducts;
	
	private final Comparator<int[]> _indicesComparator;
	
	/*--------------
	 * Construction
	 */
	
	LargeDirectedJointDomainIndexer(BitSet outputs, DiscreteDomain[] domains)
	{
		super(computeHashCode(outputs, domains), domains);
		_outputSet = (BitSet)outputs.clone();
		
		final int nDomains = domains.length;
		final int nOutputs = outputs.cardinality();
		final int nInputs = nDomains - nOutputs;
		
		if (outputs.length() > nDomains)
		{
			throw new DimpleException("Illegal output set for domain list");
		}
		
		final int[] inputIndices = new int[nInputs];
		final int[] outputIndices = new int[nOutputs];

		int curInput = 0, curOutput = 0;

		for (int i = 0; i < nDomains; ++i)
		{
			if (outputs.get(i))
			{
				outputIndices[curOutput] = i;
				++curOutput;
			}
			else
			{
				inputIndices[curInput] = i;
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
		}
		
		_inputIndices = inputIndices;
		_outputIndices = outputIndices;
		_indicesComparator = canonicalOrder ? Comparators.reverseLexicalIntArray() :
			new DirectedArrayComparator(inputIndices, outputIndices);
		
		if (domainSubsetTooLargeForIntegerIndex(domains, outputIndices))
		{
			_outputCardinality = -1;
			_outputProducts = null;
		}
		else
		{
			int[] outputProducts = new int[nDomains];
			int outputProduct = 1;
			for (int i : outputIndices)
			{
				outputProducts[i] = outputProduct;
				outputProduct *= domains[i].size();
			}
			_outputCardinality = outputProduct;
			_outputProducts = outputProducts;
		}
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
		
		if (that instanceof LargeDirectedJointDomainIndexer)
		{
			LargeDirectedJointDomainIndexer thatDiscrete = (LargeDirectedJointDomainIndexer)that;
			return _hashCode == thatDiscrete._hashCode
				&& Arrays.equals(_domains, thatDiscrete._domains)
				&& _outputSet.equals(thatDiscrete._outputSet);
		}
		
		return false;
	}
	
	/*------------------
	 * Directed methods
	 */
	
	@Override
	public final Comparator<int[]> getIndicesComparator()
	{
		return _indicesComparator;
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
		assertSupportsOutputIndexing("getOutputCardinality");
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
	public boolean hasCanonicalDomainOrder()
	{
		return _indicesComparator == Comparators.reverseLexicalIntArray();
	}
	
	@Override
	public boolean hasSameInputs(int[] indices1, int[] indices2)
	{
		return hasSameInputsImpl(indices1, indices2, _inputIndices);
	}
	
	@Override
	public boolean isDirected()
	{
		return true;
	}
	
	@Override
	public int outputIndexFromElements(Object ... elements)
	{
		assertSupportsOutputIndexing("outputIndexFromElements");
		final DiscreteDomain[] domains = _domains;
		final int[] products = requireNonNull(_outputProducts);
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
		final int[] outputProducts = assertSupportsOutputIndexing("outputIndexFromIndices");
		final int length = indices.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += indices[i] * outputProducts[i];
		}
		return joint;
	}
	
	@Override
	public int outputIndexFromValues(Value ... values)
	{
		final int[] outputProducts = assertSupportsOutputIndexing("outputIndexFromValues");
		final int length = values.length;
		int joint = 0;
		for (int i = 0, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += values[i].getIndex() * outputProducts[i];
		}
		return joint;
	}

	@Override
	public void outputIndexToElements(int outputIndex, Object[] elements)
	{
		final int[] outputProducts = assertSupportsOutputIndexing("outputIndexToElements");
		locationToElements(outputIndex, elements, _outputIndices, outputProducts);
	}
	
	@Override
	public void outputIndexToIndices(int outputIndex, int[] indices)
	{
		final int[] outputProducts = assertSupportsOutputIndexing("outputIndexToIndices");
		locationToIndices(outputIndex, indices, _outputIndices, outputProducts);
	}
	
	@Override
	public void outputIndexToValues(int outputIndex, Value[] values)
	{
		final int[] outputProducts = assertSupportsOutputIndexing("outputIndexToValues");
		locationToValues(outputIndex, values, _outputIndices, outputProducts);
	}
	

	@Override
	public boolean supportsOutputIndexing()
	{
		return _outputCardinality > 0;
	}
	
	private int[] assertSupportsOutputIndexing(String method)
	{
		if (!supportsOutputIndexing())
		{
			throw new DimpleException("%s.%s not supported for very large joint output domain cardinality",
				getClass().getSimpleName(), method);
		}
		
		return Objects.requireNonNull(_outputProducts);
	}
}
