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

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.values.Value;

@Immutable
public class StandardJointDomainIndexer extends JointDomainIndexer
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Contains cumulative products of domain sizes, such that _products[0] == 1
	 * and otherwise _products[i] == getDomainSize(i-1) * _products[i-1].
	 */
	private final int[] _products;
	
	/**
	 * The joint cardinality of all of the domains: the product of all of the domain
	 * sizes.
	 */
	private final int _cardinality;
	
	/*--------------
	 * Construction
	 */
	
	StandardJointDomainIndexer(int hashCode, DiscreteDomain[] domains)
	{
		super(hashCode, domains);
		
		final int nDomains = domains.length;
		
		_products = new int[nDomains];
		int product = 1;
		for (int i = 0; i < nDomains; ++i)
		{
			_products[i] = product;
			product *= domains[i].size();
		}
		_cardinality = product;
	}
	
	StandardJointDomainIndexer(DiscreteDomain[] domains)
	{
		this(computeHashCode(domains), domains);
	}
	
	/*----------------------------
	 * JointDomainIndexer methods
	 */
	
	/**
	 * The number of possible combinations of all domain elements. Equal to the product of
	 * all of the domain sizes.
	 * <p>
	 * @see #getInputCardinality()
	 * @see #getOutputCardinality()
	 */
	@Override
	public final int getCardinality()
	{
		return _cardinality;
	}
	
	/**
	 * Returns the index of the ith domain designated as an input domain.
	 * <p>
	 * This is equivalent to returning the ith element of {@link #getInputDomainIndices()} but
	 * without having to allocate and copy an array.
	 * <p>
	 * @throws ArrayIndexOutOfBoundsException if i is not in range [0, {@link #getInputSize()}-1]
	 * (will always throw if not {@link #isDirected()}.
	 */
	@Override
	public int getInputDomainIndex(int i)
	{
		throw new ArrayIndexOutOfBoundsException();
	}
	
	/**
	 * The number of possible combinations of output domain elements. Equal to the product of
	 * all of the output domain sizes. Will be the same as {@link #getCardinality()} if not {@link #isDirected()}.
	 * @see #getInputCardinality()
	 */
	@Override
	public int getOutputCardinality()
	{
		return _cardinality;
	}
	
	/**
	 * Returns amount by which joint index returned by {@link #jointIndexFromIndices(int...)} changes
	 * when ith element index changes by 1.
	 * <p>
	 * This can be used to iterate over the joint indexes for one dimension for fixed values of all of
	 * the other dimensions.
	 * <p>
	 * @see #getUndirectedStride(int)
	 */
	@Override
	public int getStride(int i)
	{
		return _products[i];
	}
	
	/**
	 * Returns amount by which joint index returned by {@link #undirectedJointIndexFromIndices(int...)} changes
	 * when ith element index changes by 1.
	 * <p>
	 * This can be used to iterate over the joint indexes for one dimension for fixed values of all of
	 * the other dimensions.
	 * <p>
	 * @see #getStride(int)
	 */
	@Override
	public final int getUndirectedStride(int i)
	{
		return _products[i];
	}
	
	@Override
	public final int undirectedJointIndexFromElements(Object ... elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _products;
		int joint = domains[0].getIndexOrThrow(elements[0]);
		for (int i = 1, end = products.length; i < end; ++i)
		{
			joint += products[i] * domains[i].getIndexOrThrow(elements[i]);
		}
		return joint;
	}

	@Override
	public final int undirectedJointIndexFromIndices(int ... indices)
	{
		final int length = indices.length;
		int joint = indices[0]; // _products[0] is 1, so we can skip the multiply
		for (int i = 1, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += indices[i] * _products[i];
		}
		return joint;
	}
	
	@Override
	public final int undirectedJointIndexFromValues(Value ... values)
	{
		final int length = values.length;
		int joint = values[0].getIndex(); // _products[0] is 1, so we can skip the multiply
		for (int i = 1, end = length; i != end; ++i) // != is slightly faster than < comparison
		{
			joint += values[i].getIndex() * _products[i];
		}
		return joint;
	}

	@Override
	public final <T> T[] undirectedJointIndexToElements(int jointIndex, @Nullable T[] elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _products;

		elements = allocateElements(elements);
		
		int product;
		for (int i = products.length; --i >= 0;)
		{
			final int index = jointIndex / (product = products[i]);
			@SuppressWarnings("unchecked")
			T element = (T) domains[i].getElement(index);
			elements[i] = element;
			jointIndex -= index * product;
		}
		return elements;
	}
	
	@Override
	public final Value[] undirectedJointIndexToValues(int jointIndex, Value[] values)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _products;
		
		int product;
		for (int i = products.length; --i >= 0;)
		{
			final Value value = values[i];
			final DiscreteDomain domain = domains[i];
			final int index = jointIndex / (product = products[i]);
			if (value.getDomain() == domain)
			{
				// If domain matches, then use the faster setIndex method.
				//
				// Because domains are interned, the == check should be sufficient the vast
				// majority of the time, and in the unlikely event it is not, setObject will
				// still do the right thing.
				value.setIndex(index);
			}
			else
			{
				value.setObject(domain.getElement(index));
			}
			jointIndex -= index * product;
		}
		return values;
	}
	

	/**
	 * Computes element index for a single domain from a joint index using undirected ordering
	 * of domains.
	 * <p>
	 * This is like {@link #undirectedJointIndexToIndices} but only computes one element index.
	 * <p>
	 * @param jointIndex must be in range [0, {@link #getCardinality()}-1].
	 * @param domainIndex must be in range [0, {@link #size()}-1].
	 * <p>
	 * @see #jointIndexToElementIndex(int, int)
	 */
	@Override
	public int undirectedJointIndexToElementIndex(int jointIndex, int domainIndex)
	{
		return (jointIndex / _products[domainIndex]) % _domains[domainIndex].size();
	}
	
	@Override
	public final int[] undirectedJointIndexToIndices(int jointIndex, @Nullable int[] indices)
	{
		final int[] products = _products;
		
		indices = allocateIndices(indices);
		
		int product;
		for (int i = products.length; --i >= 0;)
		{
			final int index = jointIndex / (product = products[i]);
			indices[i] = index;
			jointIndex -= index * product;
		}
		return indices;
	}

	@Override
	public boolean supportsJointIndexing()
	{
		return true;
	}
	
	@Override
	public boolean supportsOutputIndexing()
	{
		return true;
	}
}
