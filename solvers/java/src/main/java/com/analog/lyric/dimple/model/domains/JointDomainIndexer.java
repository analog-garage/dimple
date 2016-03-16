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

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.collect.Comparators;
import com.analog.lyric.collect.Supers;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.Value;

import net.jcip.annotations.Immutable;

/**
 * Provides a representation and canonical indexing operations for an ordered list of
 * {@link DiscreteDomain} for use in implementing discrete factor tables and messages.
 * <p>
 * Construct using one of the {@code create} methods listed below.
 * <p>
 * The methods of this class employs the following terminology:
 * <dl>
 * <dt>size</dt>
 * <dd>Refers to number of things in a single dimension: {@link #size()} is the number of
 * domains in the indexer, {@link #getInputSize()}/{@link #getOutputSize()} are the number of domains that are
 * designated as inputs/respectively. and {@link #getDomainSize(int)} is the size of the nth domain.
 * </dd>
 * 
 * <dt>cardinality</dt>
 * <dd>Is a combinatoric size across multiple dimensions: {@link #getCardinality()} is the number of
 * combinations of elements from all domains, and {@link #getInputCardinality()}/{@link #getOutputCardinality()}
 * are the combinations involving only domains designated as inputs/outputs respectively.
 * </dd>
 * 
 * <dt>elements</dt>
 * <dd>Refers to an array of elements of the domains in the indexer in their canonical order in the indexer,
 * where the ith element is a member of the domain returned by {@link #get}(i).
 * </dd>
 * 
 * <dt>indices</dt>
 * <dd>Refers to an array of indices of domain elements in their canonical order in the indexer. This is
 * an equivalent representation to elements but uses the element indexes rather than their actual values.
 * </dd>
 * 
 * <dt>values</dt>
 * <dd>Refers to a {@link Value} array presumed to contain non-null {@link DiscreteValue}s that contain
 * both the element and index.
 * </dd>
 * 
 * <dt>joint index</dt>
 * <dd>Is a single integral index representing a unique combination of indices/elements that maps each
 * combination of values to the range [0, {@link #getCardinality()}-1].
 * </dd>
 * 
 * <dt>domain index</dt>
 * <dd>Refers to the index of a domain in the indexer considered as a {@link DomainList}, e.g.
 * {@link #getInputDomainIndices} returns an array of indexes of domains designated as input domains.
 * </dd>
 * 
 * <dt>input index</dt>
 * <dd>Is a single integral index representing a unique combination of just input indices/elements that
 * maps each such combination to the range [0, {@link #getInputCardinality()}-1]. Only meaningful for
 * directed indexers.
 * </dd>
 * 
 * <dt>output index</dt>
 * <dd>Is the same as input index but for outputs.</dd>
 * 
 * </dl>
 * <p>
 * @see #create(DiscreteDomain...)
 * @see #create(Domain...)
 * @see #create(BitSet, DiscreteDomain...)
 * @see #create(BitSet, JointDomainIndexer)
 * @see #create(int[], DiscreteDomain[])
 * @see #create(int[], Domain...)
 */
@Immutable
public abstract class JointDomainIndexer extends DomainList<DiscreteDomain>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Precomputed {@link #hashCode()}.
	 */
	final int _hashCode;
	
	/**
	 * Nearest common base class across all domains.
	 */
	private final Class<?> _elementClass;

	/*--------------
	 * Construction
	 */
	
	JointDomainIndexer(int hashCode, DiscreteDomain[] domains)
	{
		super(domains);
		
		_hashCode = hashCode;
		if (domains.length == 0)
		{
			throw new DimpleException("Empty domain list");
		}
		
		Class<?> elementClass = domains[0].getElementClass();
		for (int i = domains.length; --i >= 1; )
		{
			assert(elementClass != null);
			elementClass = Supers.nearestCommonSuperClass(elementClass, domains[i].getElementClass());
		}
		// This could only be null if the getElementClass() could return null or a primitive type.
		_elementClass = Objects.requireNonNull(elementClass);
	}
	
	JointDomainIndexer(DiscreteDomain[] domains)
	{
		this(computeHashCode(domains), domains);
	}
	
	private static JointDomainIndexer lookupOrCreate(@Nullable BitSet outputs, DiscreteDomain[] domains, boolean cloneDomains)
	{
		if (cloneDomains)
		{
			domains = Arrays.copyOf(domains, domains.length, DiscreteDomain[].class);
		}
		
		if (domainsTooLargeForIntegerIndex(domains))
		{
			if (outputs != null)
			{
				return intern(new LargeDirectedJointDomainIndexer(outputs, domains));
			}
			else
			{
				return intern(new LargeJointDomainIndexer(domains));
			}
		}
		if (outputs != null)
		{
			return intern(new StandardDirectedJointDomainIndexer(outputs, domains));
		}
		else
		{
			return intern(new StandardJointDomainIndexer(domains));
		}
	}
	
	static JointDomainIndexer lookupOrCreate(@Nullable int[] outputIndices, DiscreteDomain[] domains, boolean cloneDomains)
	{
		BitSet outputs = null;
		
		if (outputIndices != null)
		{
			outputs = BitSetUtil.bitsetFromIndices(domains.length, outputIndices);
		}
		
		return lookupOrCreate(outputs, domains, cloneDomains);
	}

	public static JointDomainIndexer create(@Nullable BitSet outputs, DiscreteDomain ... domains)
	{
		return lookupOrCreate(outputs, domains, true);
	}
	
	/**
	 * Creates an undirected indexer consisting of the specified {@code domains} in the given order.
	 * <p>
	 * May return a previously cached value.
	 */
	public static JointDomainIndexer create(DiscreteDomain ... domains)
	{
		return lookupOrCreate((BitSet)null, domains, true);
	}
	
	/**
	 * Creates a directed indexer consisting of the specified {@code domains} in the given
	 * order and with the specified domains designated as outputs.
	 * <p>
	 * If {@code outputIndices} is null, this will instead return an undirected indexer.
	 */
	public static JointDomainIndexer create(@Nullable int[] outputDomainIndices, DiscreteDomain[] domains)
	{
		return lookupOrCreate(outputDomainIndices, domains, true);
	}
	
	/**
	 * Creates a directed indexer consisting of the specified {@code domains} in the given
	 * order and with the specified domains designated as outputs.
	 * <p>
	 * If {@code outputs} is null, this will instead return an undirected indexer.
	 */
	public static JointDomainIndexer create(@Nullable BitSet outputs, JointDomainIndexer domains)
	{
		return lookupOrCreate(outputs, domains._domains, false);
	}
	
	/**
	 * Returns a new domain list that concatenates the domains of this list
	 * with {@code that}. Only produces a directed list if both lists are directed.
	 */
	public static @Nullable JointDomainIndexer concat(
		@Nullable JointDomainIndexer domains1,
		@Nullable JointDomainIndexer domains2)
	{
		if (domains1 == null)
		{
			return domains2;
		}
		else if (domains2 == null)
		{
			return domains1;
		}
		
		return concatNonNull(domains1, domains2);
	}
	
	/**
	 * Returns a new domain list that concatenates the domains of this list
	 * with {@code that}. Only produces a directed list if both lists are directed.
	 */
	public static JointDomainIndexer concatNonNull(JointDomainIndexer domains1, JointDomainIndexer domains2)
	{
		final int size1 = domains1.size();
		final int size2 = domains2.size();
		final int size = size1 + domains2.size();
		final DiscreteDomain[] domains = Arrays.copyOf(domains1._domains, size);
		for (int i = 0; i < size2; ++ i)
		{
			domains[size1 + i] = domains2.get(i);
		}
		
		BitSet outputs = null;

		if (domains1.isDirected() & domains2.isDirected())
		{
			outputs = requireNonNull(domains1.getOutputSet());
			for (int i : requireNonNull(domains2.getOutputDomainIndices()))
			{
				outputs.set(size1 + i);
			}
		}
		
		return JointDomainIndexer.create(outputs, domains);
	}
	
	/**
	 * Creates a new indexer constructed from the {@code length} domains in this list starting
	 * at {@code offset}. Returns a directed indexer if this indexer is directed.
	 * 
	 * @since 0.05
	 */
	public JointDomainIndexer subindexer(int offset, int length)
	{
		if (offset < 0 || offset >= size() || length < 0 || length + offset > size())
		{
			throw new IllegalArgumentException(String.format("Bad offset/length %d/%d for subindexer", offset, length));
		}
		
		final DiscreteDomain[] domains = new DiscreteDomain[length];
		for (int i = 0; i < length; ++i)
		{
			domains[i] = get(i + offset);
		}
		
		BitSet outputs = null;
		
		if (isDirected())
		{
			final BitSet theseOutputs = requireNonNull(getOutputSet());
			outputs = new BitSet(length);
			for (int i = 0; i < length; ++i)
			{
				if (theseOutputs.get(i + offset))
				{
					outputs.set(i);
				}
			}
		}
		
		return JointDomainIndexer.create(outputs, domains);
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
		
		if (that instanceof JointDomainIndexer)
		{
			JointDomainIndexer thatDiscrete = (JointDomainIndexer)that;
			return thatDiscrete._hashCode == _hashCode &&
				!thatDiscrete.isDirected() &&
				Arrays.equals(_domains, thatDiscrete._domains);
		}
		
		return false;
	}
	
	@Override
	public final int hashCode()
	{
		return _hashCode;
	}
	
	/*--------------------
	 * DomainList methods
	 */
	
	@Override
	public JointDomainIndexer asJointDomainIndexer()
	{
		return this;
	}
	
	@Override
	public boolean isDiscrete()
	{
		return true;
	}
	
	/*----------------------------
	 * JointDomainIndexer methods
	 */
	
	/**
	 * Returns an array with length at least {@link #size()} and with component type
	 * compatible with elements of all domains in list (i.e. a superclass of type specified
	 * by {@link #getElementClass()}.
	 * <p>
	 * If {@code elements} fits the above description, it will simply be returned.
	 * If {@code elements} is too short but has a compatible component type, this will
	 * return a new array with length equal to {@link #size()} and component type
	 * the same as {@code elements}. Otherwise returns a new array with component
	 * type same as {@link #getElementClass()}.
	 */
	public final <T> T[] allocateElements(@Nullable T [] elements)
	{
		return ArrayUtil.allocateArrayOfType(_elementClass,  elements,  _domains.length);
	}
	
	/**
	 * Returns an array with length at least {@link #size()}.
	 * <p>
	 * If {@code indices} is non-null and is sufficiently long, it will be returned.
	 * Otherwise a newly allocated array will be returned.
	 */
	public final int[] allocateIndices(@Nullable int [] indices)
	{
		if (indices == null || indices.length < _domains.length)
		{
			indices = new int[_domains.length];
		}
		return indices;
	}
	
	public final int[] elementsToIndices(Object[] elements)
	{
		return elementsToIndices(elements, null);
	}
	
	public final int[] elementsToIndices(Object[] elements, @Nullable int indices[])
	{
		indices = allocateIndices(indices);
		for (int i = 0, end = _domains.length; i < end; ++i)
		{
			indices[i] = _domains[i].getIndexOrThrow(elements[i]);
		}
		return indices;
	}
	
	public final Object[] elementsFromIndices(int indices[])
	{
		return elementsFromIndices(indices, null);
	}
	
	public final Object[] elementsFromIndices(int indices[], @Nullable Object[] elements)
	{
		elements = allocateElements(elements);
		for (int i = 0, end = _domains.length; i < end; ++i)
		{
			elements[i] = _domains[i].getElement(indices[i]);
		}
		return elements;
	}
	
	/**
	 * The number of possible combinations of all domain elements. Equal to the product of
	 * all of the domain sizes.
	 * <p>
	 * @see #getInputCardinality()
	 * @see #getOutputCardinality()
	 */
	public abstract int getCardinality();
	
	/**
	 * Returns the size of the ith domain in the list.
	 */
	public final int getDomainSize(int i)
	{
		return _domains[i].size();
	}
	
	/**
	 * Returns nearest common superclass for elements in all domains.
	 */
	public final Class<?> getElementClass()
	{
		return _elementClass;
	}
	
	/**
	 * Returns a comparator that orders indices arrays.
	 * <p>
	 * If {@link #supportsJointIndexing()}, this is guaranteed to produce the same order as
	 * the natural order of the corresponding joint indexes. If not {@link #isDirected()} or
	 * {@link #hasCanonicalDomainOrder()}, then the comparator implements a reverse lexicographical
	 * ordering (see {@link Comparators#reverseLexicalIntArray()}).
	 * <p>
	 * The comparator is intended to be used with arrays of length {@link #size()}.
	 */
	public Comparator<int[]> getIndicesComparator()
	{
		return Comparators.reverseLexicalIntArray();
	}
	
	/**
	 * The number of possible combinations of input domain elements. Equal to the product of
	 * all of the input domain sizes. Will be one if not {@link #isDirected()}.
	 * @see #getCardinality()
	 * @see #getOutputCardinality()
	 */
	public int getInputCardinality()
	{
		return 1;
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
	public int getInputDomainIndex(int i)
	{
		throw new ArrayIndexOutOfBoundsException();
	}
	
	/**
	 * Returns a copy of the indexes of the input domains listed in increasing order or
	 * else null if not {@link #isDirected()}.
	 * <p>
	 * Use {@link #getInputDomainIndex(int)} to lookup an index without allocating a new array object.
	 */
	public @Nullable int[] getInputDomainIndices()
	{
		return null;
	}
	
	/**
	/**
	 * Returns a copy of the {@link BitSet} representing the indexes of the input domains or
	 * else null if not {@link #isDirected()}.
	 * 
	 * @see #getInputDomainIndices()
	 * @see #getInputDomainIndex(int)
	 * @see #getOutputSet()
	 */
	public @Nullable BitSet getInputSet()
	{
		return null;
	}
	
	/**
	 * The number of input domains if {@link #isDirected()}, otherwise zero.
	 * <p>
	 * If directed, this must be greater than zero and when combined with {@link #getOutputSize()} must add up to
	 * {@link #size()}.
	 */
	public int getInputSize()
	{
		return 0;
	}
	
	/**
	 * The number of possible combinations of output domain elements. Equal to the product of
	 * all of the output domain sizes. Will be the same as {@link #getCardinality()} if not {@link #isDirected()}.
	 * @see #getInputCardinality()
	 */
	public abstract int getOutputCardinality();
	
	/**
	 * Returns the index of the ith output domain. If not {@link #isDirected()} this treats
	 * all domains as output domains and will just return {@code i} if within range.
	 * @throws ArrayIndexOutOfBoundsException if {@code i} is negative or not less than
	 * {@link #getOutputSize()}.
	 */
	public int getOutputDomainIndex(int i)
	{
		if (i < 0 || i >= size())
		{
			throw new ArrayIndexOutOfBoundsException();
		}
		return i;
	}
	
	/**
	 * Returns a copy of the indexes of the input domains listed in increasing order or
	 * else null if not {@link #isDirected()}.
	 * <p>
	 * Use {@link #getOutputDomainIndex(int)} to lookup an index without allocating a new array object.
	 */
	public @Nullable int[] getOutputDomainIndices()
	{
		return null;
	}
	
	/**
	 * Returns a copy of the {@link BitSet} representing the indexes of the output domains or
	 * else null if not {@link #isDirected()}.
	 * 
	 * @see #getOutputDomainIndices()
	 * @see #getOutputDomainIndex(int)
	 * @see #getInputSet()
	 */
	public @Nullable BitSet getOutputSet()
	{
		return null;
	}
	
	/**
	 * The number of output domains if {@link #isDirected()}, otherwise the same as {@link #size()}.
	 * <p>
	 * If directed, this must be greater than zero and when combined with {@link #getInputSize()} must add up to
	 * {@link #size()}.
	 */
	public int getOutputSize()
	{
		return size();
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
	public abstract int getStride(int i);
	
	/**
	 * Returns amount by which joint index returned by {@link #undirectedJointIndexFromIndices(int...)} changes
	 * when ith element index changes by 1.
	 * <p>
	 * This can be used to iterate over the joint indexes for one dimension for fixed values of all of
	 * the other dimensions.
	 * <p>
	 * @see #getStride(int)
	 */
	public abstract int getUndirectedStride(int i);
	
	/**
	 * True if domain list is partitioned into inputs and outputs.
	 */
	public boolean isDirected()
	{
		return false;
	}
	
	/**
	 * True if not {@link #isDirected()}. Otherwise,
	 * if {@link #isDirected()}, then this is true if all the
	 * output domains in {@link #getOutputSet()} are at the front
	 * of the list.
	 */
	public boolean hasCanonicalDomainOrder()
	{
		return true;
	}
	
	/**
	 * Returns true if two indices arrays have the same values at the
	 * indexes specified by {@link #getInputDomainIndices()}. If
	 * not {@link #isDirected()}, this will always return true.
	 */
	public boolean hasSameInputs(int[] indices1, int[] indices2)
	{
		return true;
	}
	
	/**
	 * Computes a unique index for the subset of {@code elements} designated as inputs.
	 * <p>
	 * Similar to {@link #jointIndexFromElements(Object...)} but computes value only
	 * from those elements indexed by {@link #getInputSet()}.
	 * <p>
	 * Returns 0 if not {@link #isDirected()}.
	 * <p>
	 * @see #inputIndexFromIndices(int...)
	 * @see #outputIndexFromElements(Object...)
	 */
	public int inputIndexFromElements(Object ... elements)
	{
		return 0;
	}
	
	/**
	 * Computes a unique index for the subset of {@code indices} designated as inputs.
	 * <p>
	 * Similar to {@link #jointIndexFromIndices(int...)} but computes value only
	 * from those element indices designated by {@link #getInputSet()}.
	 * <p>
	 * Returns 0 if not {@link #isDirected()}.
	 * <p>
	 * @see #inputIndexFromElements(Object...)
	 * @see #outputIndexFromIndices(int...)
	 */
	public int inputIndexFromIndices(int ... indices)
	{
		return 0;
	}
	
	/**
	 * Computes a unique index for the subset of {@code values} designated as inputs.
	 * <p>
	 * Similar to {@link #jointIndexFromValues(Value...)} but computes value only
	 * from those element indices designated by {@link #getInputSet()}.
	 * <p>
	 * Returns 0 if not {@link #isDirected()}.
	 * <p>
	 * @see #inputIndexFromElements(Object...)
	 * @see #inputIndexFromIndices(int...)
	 * @see #outputIndexFromValues(Value...)
	 */
	public int inputIndexFromValues(Value ... values)
	{
		return 0;
	}
	
	/**
	 * Converts a joint index to an input index.
	 * <p>
	 * Returns 0 if not {@link #isDirected()}.
	 * <p>
	 * @see #outputIndexFromJointIndex(int)
	 * @see #jointIndexFromInputOutputIndices(int, int)
	 */
	public int inputIndexFromJointIndex(int jointIndex)
	{
		return 0;
	}
	
	/**
	 * Writes elements corresponding to {@code inputIndex} into corresponding {@code elements} array.
	 * <p>
	 * Only updates members of {@code elements} designated as inputs, and therefore will do nothing
	 * if not {@link #isDirected()}.
	 * <p>
	 * @param inputIndex must be in range [0, {@link #getInputCardinality()}-1].
	 * @param elements must have length equal to {@link #size()}.
	 * <p>
	 * @see #inputIndexFromElements(Object...)
	 * @see #outputIndexToElements(int, Object[])
	 */
	public void inputIndexToElements(int inputIndex, Object[] elements)
	{
	}
	
	/**
	 * Writes elements corresponding to {@code inputIndex} into corresponding {@code elements} array.
	 * <p>
	 * Only updates members of {@code elements} designated as inputs, and therefore will do nothing
	 * if not {@link #isDirected()}.
	 * <p>
	 * @param inputIndex must be in range [0, {@link #getInputCardinality()}-1].
	 * @param values must have length equal to {@link #size()} and must be fully populated (i.e. no null entries)
	 *   with {@link Value} objects with domain compatible with corresponding indexer domains.
	 * 
	 * @since 0.07
	 */
	public void inputIndexToValues(int inputIndex, Value[] values)
	{
	}
	
	/**
	 * Writes element indices corresponding to {@code inputIndex} into corresponding {@code indices} array.
	 * <p>
	 * Only updates members of {@code indices} for domains designated as inputs, and therefore will do nothing
	 * if not {@link #isDirected()}.
	 * <p>
	 * @param inputIndex must be in range [0, {@link #getInputCardinality()}-1].
	 * @param indices must have length equal to {@link #size()}.
	 * <p>
	 * @see #inputIndexFromIndices(int...)
	 * @see #outputIndexToIndices(int, int[])
	 */
	public void inputIndexToIndices(int inputIndex, int[] indices)
	{
	}
	
	/**
	 * Computes a unique joint index associated with the specified domain elements.
	 * <p>
	 * @param elements must have length equal to {@link #size()} and each elements must
	 * be an element of the corresponding domain.
	 * @see #jointIndexFromIndices(int[])
	 * @see #jointIndexToElements(int, Object[])
	 */
	public int jointIndexFromElements(Object ... elements)
	{
		return undirectedJointIndexFromElements(elements);
	}

	/**
	 * Computes a unique joint index associated with the specified {@code indices}.
	 * <p>
	 * The joint index is equivalent the inner product of the vector of stride values and {@code indices}.
	 * That is:
	 * <pre>
	 *       int jointIndex = 0;
	 *       for (int i = 0; i &lt; getDimensions(); ++i)
	 *          jointIndex += getStride(i) * indices[i];
	 * </pre>
	 * <p>
	 * @param indices must have length equal to {@link #size()} and each index must be a non-negative
	 * value less than the size of the corresponding domain otherwise the function could return an
	 * incorrect result.
	 * @see #jointIndexFromElements
	 * @see #jointIndexToIndices
	 * @see #validateIndices(int...)
	 */
	public int jointIndexFromIndices(int ... indices)
	{
		return undirectedJointIndexFromIndices(indices);
	}
	
	public int jointIndexFromValues(Value ... values)
	{
		return undirectedJointIndexFromValues(values);
	}
	
	/**
	 * Converts input and output indexes to a joint index.
	 * <p>
	 * Returns {@code outputIndex} if not {@link #isDirected()}.
	 * <p>
	 * @param inputIndex must be in range [0, {@link #getInputCardinality()}-1]
	 * @param outputIndex must be in range [0, {@link #getOutputCardinality()}-1]
	 * <p>
	 * @see #inputIndexFromJointIndex(int)
	 * @see #outputIndexFromJointIndex(int)
	 */
	public int jointIndexFromInputOutputIndices(int inputIndex, int outputIndex)
	{
		return outputIndex;
	}

	/**
	 * Computes domain values corresponding to given joint index.
	 * <p>
	 * @param jointIndex a unique joint table index in the range [0,{@link #getCardinality()}).
	 * @param elements if this is an array of length {@link #size()}, the computed values will
	 * be placed in this array, otherwise a new array will be allocated.
	 * @see #jointIndexToIndices(int, int[])
	 * @see #jointIndexFromElements(Object...)
	 */
	public <T> T[] jointIndexToElements(int jointIndex, @Nullable T[] elements)
	{
		return undirectedJointIndexToElements(jointIndex, elements);
	}
	
	/**
	 * Computes domain values corresponding to given joint index.
	 * <p>
	 * @param jointIndex a unique joint table index in the range [0,{@link #getCardinality()}).
	 * @param values must have length equal to {@link #size()} and must be fully populated (i.e. no null entries)
	 *   with {@link Value} objects with domain compatible with corresponding indexer domains.
	 * @since 0.07
	 */
	public Value[] jointIndexToValues(int jointIndex, Value[] values)
	{
		return undirectedJointIndexToValues(jointIndex, values);
	}
	
	/**
	 * Computes domain values corresponding to given joint index.
	 * <p>
	 * Same as {@link #jointIndexToElements(int, Object[])} with null second argument.
	 */
	public final Object[] jointIndexToElements(int jointIndex)
	{
		return jointIndexToElements(jointIndex, null);
	}
	
	/**
	 * Computes domain values corresponding to given joint index.
	 * <p>
	 * @see #jointIndexToValues(int, Value[])
	 * @since 0.07
	 */
	public final Value[] jointIndexToValues(int jointIndex)
	{
		return jointIndexToValues(jointIndex, Value.createFromDomains(_domains));
	}
	
	/**
	 * Computes element index for a single domain from a joint index.
	 * <p>
	 * This is like {@link #jointIndexToIndices} but only computes one element index.
	 * <p>
	 * @param jointIndex must be in range [0, {@link #getCardinality()}-1].
	 * @param domainIndex must be in range [0, {@link #size()}-1].
	 * <p>
	 * @see #undirectedJointIndexToElementIndex(int, int)
	 */
	public int jointIndexToElementIndex(int jointIndex, int domainIndex)
	{
		return undirectedJointIndexToElementIndex(jointIndex, domainIndex);
	}
	
	/**
	 * Computes domain indices corresponding to given joint index.
	 * <p>
	 * @param jointIndex a unique joint table index in the range [0,{@link #getCardinality()}).
	 * @param indices if this is an array of length {@link #size()}, the computed values will
	 * be placed in this array, otherwise a new array will be allocated.
	 * @see #jointIndexToElements(int, Object[])
	 * @see #jointIndexFromIndices(int...)
	 * @see #jointIndexToElementIndex(int, int)
	 */
	public int[] jointIndexToIndices(int jointIndex, @Nullable int[] indices)
	{
		return undirectedJointIndexToIndices(jointIndex, indices);
	}
	
	/**
	 * Computes domain indices corresponding to given joint index.
	 * <p>
	 * Same as {@link #jointIndexToIndices(int, int[])} with null second argument.
	 */
	public final int[] jointIndexToIndices(int jointIndex)
	{
		return jointIndexToIndices(jointIndex, null);
	}
	
	/**
	 * Computes a unique index for the subset of {@code elements} designated as outputs.
	 * <p>
	 * Similar to {@link #jointIndexFromElements(Object...)} but computes value only
	 * from those elements indexed by {@link #getOutputSet()}.
	 * <p>
	 * Same as {@link #jointIndexFromElements(Object...)} if not {@link #isDirected()}.
	 * <p>
	 * @see #outputIndexFromIndices(int...)
	 * @see #inputIndexFromElements(Object...)
	 */
	public int outputIndexFromElements(Object ... elements)
	{
		return undirectedJointIndexFromElements(elements);
	}
	
	/**
	 * Computes a unique index for the subset of {@code indices} designated as outputs.
	 * <p>
	 * Similar to {@link #jointIndexFromIndices(int...)} but computes value only
	 * from those element indices designated by {@link #getOutputSet()}.
	 * <p>
	 * Same as {@link #jointIndexFromIndices(int...)} if not {@link #isDirected()}.
	 * <p>
	 * @see #outputIndexFromElements(Object...)
	 * @see #inputIndexFromIndices(int...)
	 */
	public int outputIndexFromIndices(int ... indices)
	{
		return undirectedJointIndexFromIndices(indices);
	}
	
	public int outputIndexFromValues(Value ... values)
	{
		return undirectedJointIndexFromValues(values);
	}
	
	/**
	 * Converts a joint index to an output index.
	 * <p>
	 * Returns {@code jointIndex} if not {@link #isDirected()}.
	 * <p>
	 * @see #inputIndexFromJointIndex(int)
	 * @see #jointIndexFromInputOutputIndices(int, int)
	 */
	public int outputIndexFromJointIndex(int jointIndex)
	{
		return jointIndex;
	}
	
	/**
	 * Writes elements corresponding to {@code outputIndex} into corresponding {@code elements} array.
	 * <p>
	 * Only updates members of {@code elements} designated as outputs, so this will only update all
	 * of the elements if not {@link #isDirected()}.
	 * <p>
	 * @param outputIndex must be in range [0, {@link #getOutputCardinality()}-1].
	 * @param elements must have length equal to {@link #size()}.
	 * <p>
	 * @see #outputIndexFromElements(Object...)
	 * @see #inputIndexToElements(int, Object[])
	 */
	public void outputIndexToElements(int outputIndex, Object[] elements)
	{
		undirectedJointIndexToElements(outputIndex, elements);
	}
	
	/**
	 * Writes elements corresponding to {@code outputIndex} into corresponding {@code elements} array.
	 * <p>
	 * Only updates members of {@code elements} designated as outputs, so this will only update all
	 * of the elements if not {@link #isDirected()}.
	 * <p>
	 * @param outputIndex must be in range [0, {@link #getOutputCardinality()}-1].
	 * @param values must have length equal to {@link #size()} and must be fully populated (i.e. no null entries)
	 *   with {@link Value} objects with domain compatible with corresponding indexer domains.
	 * @since 0.07
	 */
	public void outputIndexToValues(int outputIndex, Value[] values)
	{
		undirectedJointIndexToValues(outputIndex, values);
	}
	
	/**
	 * Writes element indices corresponding to {@code outputIndex} into corresponding {@code indices} array.
	 * <p>
	 * Only updates members of {@code indices} for domains designated as inputs, and therefore will do nothing
	 * if not {@link #isDirected()}.
	 * <p>
	 * @param outputIndex must be in range [0, {@link #getOutputCardinality()}-1].
	 * @param indices must have length equal to {@link #size()}.
	 * <p>
	 * @see #outputIndexFromIndices(int...)
	 */
	public void outputIndexToIndices(int outputIndex, int[] indices)
	{
		undirectedJointIndexToIndices(outputIndex, indices);
	}
	
	public abstract int undirectedJointIndexFromElements(Object ... elements);

	public abstract int undirectedJointIndexFromIndices(int ... indices);
	
	public abstract int undirectedJointIndexFromValues(Value ... values);
	
	public abstract <T> T[] undirectedJointIndexToElements(int jointIndex, @Nullable T[] elements);
	
	/**
	 * @since 0.07
	 */
	public abstract Value[] undirectedJointIndexToValues(int jointIndex, Value[] elements);
	
	public final Object[] undirectedJointIndexToElements(int jointIndex)
	{
		return undirectedJointIndexToElements(jointIndex, null);
	}

	/**
	 * @since 0.07
	 */
	public final Value[] undirectedJointIndexToValues(int jointIndex)
	{
		return undirectedJointIndexToValues(jointIndex, Value.createFromDomains(_domains));
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
	public abstract int undirectedJointIndexToElementIndex(int jointIndex, int domainIndex);
	
	public abstract int[] undirectedJointIndexToIndices(int jointIndex, @Nullable int[] indices);
	
	public final int[] undirectedJointIndexToIndices(int jointIndex)
	{
		return undirectedJointIndexToIndices(jointIndex, null);
	}

	/**
	 * Generates a random joint index value in the range [0, {@link #getCardinality()} - 1] using provided
	 * random number generator. Will throw an exception if not {@link #supportsJointIndexing()}.
	 * <p>
	 * @see #randomIndices(Random, int[])
	 */
	public int randomJointIndex(Random rand)
	{
		return rand.nextInt(getCardinality());
	}
	
	/**
	 * Generates a random set of indices for the domains using the supplied random number generator.
	 * 
	 * @param indices if non-null and of length at least {@link #size()}, this indices will be written
	 * into this array. Otherwise a new one will be allocated.
	 * 
	 * @see #randomJointIndex(Random)
	 */
	public int[] randomIndices(Random rand, @Nullable int[] indices)
	{
		indices = allocateIndices(indices);
		for (int i = 0; i < size(); ++i)
		{
			indices[i] = rand.nextInt(getDomainSize(i));
		}
		return indices;
	}
	
	/**
	 * Indicates whether class supports operations involving single integer jointIndex representation.
	 * This will be false when the joint cardinality of the component domains is larger than 2<sup>31</sup>.
	 */
	public abstract boolean supportsJointIndexing();
	
	/**
	 * Indicates whether class supports operations involving single integer output index representation
	 * of the subset of domains identified by {@link #getOutputSet()}. This will be false when the
	 * joint cardinality of the component output domains is larger than 2<sup>31</sup>.
	 * Note that this can be true when {@link #supportsJointIndexing} is false.
	 */
	public abstract boolean supportsOutputIndexing();
	
	/**
	 * Verifies that the provided {@code indices} are in the correct range for
	 * this domain list, namely that:
	 * <ul>
	 * <li>{@code indices} has {@link #size()} elements
	 * <li>all values are non-negative
	 * <li>{@code indices[i]} less than {@link #getDomainSize}{@code (i)}
	 * </ul>
	 * @throws IllegalArgumentException if wrong number of indices
	 * @throws IndexOutOfBoundsException if any index is out of range for its domain.
	 */
	public int[] validateIndices(int ... indices)
	{
		final DiscreteDomain[] domains = _domains;
		final int length = domains.length;
	
		if (indices.length != length)
		{
			throw new IllegalArgumentException(
				String.format("Wrong number of indices: %d instead of %d", indices.length, length));
		}
		
		for (int i = 0; i < length; ++i)
		{
			final int index = indices[i];
			if (index < 0 || index >= domains[i].size())
			{
				throw new IndexOutOfBoundsException(
					String.format("Index %d out of bounds for domain %d with size %d", index, i, domains[i].size()));
			}
		}
		
		return indices;
	}
	
	public Value[] validateValues(Value ... values)
	{
		final DiscreteDomain[] domains = _domains;
		final int length = domains.length;
	
		if (values.length != length)
		{
			throw new IllegalArgumentException(
				String.format("Wrong number of values: %d instead of %d", values.length, length));
		}
		
		// TODO: should this check that value is in the appropriate domain?
		
		for (int i = 0; i < length; ++i)
		{
			final int index = values[i].getIndex();
			if (index < 0 || index >= domains[i].size())
			{
				throw new IndexOutOfBoundsException(
					String.format("Index %d out of bounds for domain %d with size %d", index, i, domains[i].size()));
			}
		}
		
		return values;
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	protected static int computeHashCode(DiscreteDomain[] domains)
	{
		return Arrays.hashCode(domains);
	}

	static int computeHashCode(BitSet inputs, DiscreteDomain[] domains)
	{
		return Arrays.hashCode(domains) * 13 + inputs.hashCode();
	}
	
	/**
	 * Indicate whether the joint cardinality of the specified discrete domains
	 * is too large to be represented in an {@code int}.
	 * @see #domainSubsetTooLargeForIntegerIndex(DiscreteDomain[], int[])
	 */
	static boolean domainsTooLargeForIntegerIndex(DiscreteDomain[] domains)
	{
		if (domains.length > 31)
		{
			return true;
		}
		
		double logProduct = 0.0;
		
		for (DiscreteDomain domain : domains)
		{
			logProduct += Math.log(domain.size());
		}
		
		double approxBits = logProduct / Math.log(2);
		
		if (approxBits >= 32)
		{
			return true;
		}
		else if (approxBits <= 30)
		{
			return false;
		}
		
		// Compute product with longs to get exact answer when close to threshold.
		long product = 1;
		
		for (DiscreteDomain domain : domains)
		{
			product *= domain.size();
		}
		
		return product > Integer.MAX_VALUE;
	}
	
	/**
	 * Indicate whether the joint cardinality of a subset of the specified discrete domains
	 * is too large to be represented in an {@code int}.
	 * 
	 * @param subindexes specifies a unique set of indices into {@code domains}.
	 * @see #domainSubsetTooLargeForIntegerIndex(DiscreteDomain[], int[])
	 */
	static boolean domainSubsetTooLargeForIntegerIndex(DiscreteDomain[] domains, int[] subindexes)
	{
		if (subindexes.length > 31)
		{
			return true;
		}
		
		double logProduct = 0.0;
		
		for (int i : subindexes)
		{
			logProduct += Math.log(domains[i].size());
		}
		
		double approxBits = logProduct / Math.log(2);
		
		if (approxBits >= 32)
		{
			return true;
		}
		else if (approxBits <= 30)
		{
			return false;
		}
		
		// Compute product with longs to get exact answer when close to threshold.
		long product = 1;
		
		for (int i : subindexes)
		{
			product *= domains[i].size();
		}
		
		return product > Integer.MAX_VALUE;
	}
	
	protected static boolean hasSameInputsImpl(int[] array1, int[] array2, int[] inputIndices)
	{
		for (int i : inputIndices)
		{
			if (array1[i] != array2[i])
			{
				return false;
			}
		}

		return true;
	}
	
	void locationToElements(int location, Object[] elements, int[] subindices, int[] products)
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
	
	void locationToValues(int location, Value[] elements, int[] subindices, int[] products)
	{
		final DiscreteDomain[] domains = _domains;
		int product, index;
		for (int i = subindices.length; --i >= 0;)
		{
			int j = subindices[i];
			index = location / (product = products[j]);
			final DiscreteDomain domain = domains[j];
			final Value value = elements[j];
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
			location -= index * product;
		}
	}
	
	static void locationToIndices(int location, int[] indices, int[] subindices, int[] products)
	{
		int product, index;
		for (int i = subindices.length; --i >= 0;)
		{
			int j = subindices[i];
			indices[j] = index = location / (product = products[j]);
			location -= index * product;
		}
	}
	
	/*---------------
	 * Inner classes
	 */
	
	/**
	 * A comparator for integer arrays that compares them first by their input subindexes
	 * in reverse lexicographical order (from back to front) and then their output subindexes.
	 * This will order all index arrays such that the ones representing the same input will be adjacent.
	 */
	@Immutable
	static class DirectedArrayComparator implements Comparator<int[]>, Serializable
	{
		private static final long serialVersionUID = 1L;
		private final int[] _inputIndices;
		private final int[] _outputIndices;
		
		DirectedArrayComparator(int[] inputIndices, int[] outputIndices)
		{
			_inputIndices = inputIndices;
			_outputIndices = outputIndices;
		}
		
		@Override
		@NonNullByDefault(false)
		public int compare(int[] array1, int[] array2)
		{
			int diff = array1.length - array2.length;
			if (diff == 0)
			{
				for (int i = _inputIndices.length; --i>=0;)
				{
					int j = _inputIndices[i];
					int val1 = array1[j], val2 = array2[j];
					if (val1 != val2)
					{
						return val1 < val2 ? -1 : 1;
					}
				}
				for (int i = _outputIndices.length; --i>=0;)
				{
					int j = _outputIndices[i];
					int val1 = array1[j], val2 = array2[j];
					if (val1 != val2)
					{
						return val1 < val2 ? -1 : 1;
					}
				}
			}
			
			return diff;
		}
	}
	
}
