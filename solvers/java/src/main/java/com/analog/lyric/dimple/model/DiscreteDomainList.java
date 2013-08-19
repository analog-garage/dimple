package com.analog.lyric.dimple.model;

import java.util.Arrays;
import java.util.BitSet;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.BitSetUtil;

/**
 * Provides a representation and canonical indexing operations for an ordered list of
 * {@link DiscreteDomain} for use in implementing discrete factor tables and messages.
 */
@Immutable
public class DiscreteDomainList extends DomainList<DiscreteDomain>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final int[] _products;
	private final int _cardinality;
	private final int _hashCode;
	
	/*--------------
	 * Construction
	 */
	
	DiscreteDomainList(int hashCode, DiscreteDomain[] domains)
	{
		super(domains);
		
		_hashCode = hashCode;
		if (domains.length == 0)
		{
			throw new DimpleException("Empty domain list");
		}
		
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
	
	private DiscreteDomainList(DiscreteDomain[] domains)
	{
		this(Arrays.hashCode(domains), domains);
	}
	
	private static DiscreteDomainList lookupOrCreate(BitSet outputs, DiscreteDomain[] domains, boolean cloneDomains)
	{
		// TODO: implement cache...
		
		if (cloneDomains)
		{
			domains = Arrays.copyOf(domains, domains.length, DiscreteDomain[].class);
		}
		
		if (outputs != null)
		{
			return new DirectedDiscreteDomainList(outputs, domains);
		}
		else
		{
			return new DiscreteDomainList(domains);
		}
	}
	
	static DiscreteDomainList lookupOrCreate(int[] outputIndices, DiscreteDomain[] domains, boolean cloneDomains)
	{
		BitSet outputs = null;
		
		if (outputIndices != null)
		{
			outputs = BitSetUtil.bitsetFromIndices(domains.length, outputIndices);
		}
		
		return lookupOrCreate(outputs, domains, cloneDomains);
	}

	public static DiscreteDomainList create(BitSet outputs, DiscreteDomain ... domains)
	{
		return lookupOrCreate(outputs, domains, true);
	}
	
	public static DiscreteDomainList create(DiscreteDomain ... domains)
	{
		return lookupOrCreate((BitSet)null, domains, true);
	}
	
	public static DiscreteDomainList create(int[] outputIndices, DiscreteDomain[] domains)
	{
		return lookupOrCreate(outputIndices, domains, true);
	}
	
	/**
	 * Returns a new domain list that concatenates the domains of this list
	 * with {@code that}. Only produces a directed list if both lists are directed.
	 */
	public static DiscreteDomainList concat(DiscreteDomainList domains1, DiscreteDomainList domains2)
	{
		if (domains1 == null)
		{
			return domains2;
		}
		else if (domains2 == null)
		{
			return domains1;
		}
		
		final int size1 = domains1.size();
		final int size2 = domains2.size();
		final int size = size1 + domains2.size();
		final DiscreteDomain[] domains = Arrays.copyOf(domains1._domains, size);
		for (int i = 0; i < size2; ++ i)
		{
			domains[size1 + i] = domains2.get(i);
		}
		
		BitSet outputs = null;

		if (domains1.isDirected() && domains2.isDirected())
		{
			outputs = domains1.getOutputSet();
			for (int i : domains2.getOutputIndices())
			{
				outputs.set(size1 + i);
			}
		}
		
		return DiscreteDomainList.create(outputs, domains);
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
		
		if (that instanceof DiscreteDomainList)
		{
			DiscreteDomainList thatDiscrete = (DiscreteDomainList)that;
			return !thatDiscrete.isDirected() && Arrays.equals(_domains, thatDiscrete._domains);
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
	public DiscreteDomainList asDiscreteDomainList()
	{
		return this;
	}
	
	@Override
	public boolean isDiscrete()
	{
		return true;
	}
	
	/*----------------------------
	 * DiscreteDomainList methods
	 */
	
	/**
	 * The number of possible combinations of all domain elements. Equal to the product of
	 * all of the domain sizes.
	 * <p>
	 * @see #getInputCardinality()
	 * @see #getOutputCardinality()
	 */
	public final int getCardinality()
	{
		return _cardinality;
	}
	
	/**
	 * Returns the size of the ith domain in the list.
	 */
	public final int getDomainSize(int i)
	{
		return _domains[i].size();
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
	
	public int getInputIndex(int i)
	{
		throw new ArrayIndexOutOfBoundsException();
	}
	
	/**
	 * Returns a copy of the indexes of the input domains listed in increasing order or
	 * else null if not {@link #isDirected()}.
	 * <p>
	 * Use {@link #getInputIndex(int)} to lookup an index without allocating a new array object.
	 */
	public int[] getInputIndices()
	{
		return null;
	}
	
	/**
	 * Returns a copy of the {@link BitSet} representing the indexes of the input domains or
	 * else null if not {@link #isDirected()}.
	 * 
	 * @see #getInputIndices()
	 * @see #getInputIndex(int)
	 * @see #getOutputSet()
	 */
	public BitSet getInputSet()
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
	public int getOutputCardinality()
	{
		return _cardinality;
	}
	
	/**
	 * Returns the index of the ith output domain. If not {@link #isDirected()} this treats
	 * all domains as output domains and will just return {@code i} if within range.
	 * @throws ArrayIndexOutOfBoundsException if {@code i} is negative or not less than
	 * {@link #getOutputSize()}.
	 */
	public int getOutputIndex(int i)
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
	 * Use {@link #getOutputIndex(int)} to lookup an index without allocating a new array object.
	 */
	public int[] getOutputIndices()
	{
		return null;
	}
	
	/**
	 * Returns a copy of the {@link BitSet} representing the indexes of the output domains or
	 * else null if not {@link #isDirected()}.
	 * 
	 * @see #getOutputIndices()
	 * @see #getOutputIndex(int)
	 * @see #getInputSet()
	 */
	public BitSet getOutputSet()
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
	
	public int inputIndexFromElements(Object ... elements)
	{
		return 0;
	}
	
	public int inputIndexFromIndices(int ... indices)
	{
		return 0;
	}
	
	public int inputIndexFromJointIndex(int jointIndex)
	{
		return 0;
	}
	
	public void inputIndexToElements(int inputIndex, Object[] elements)
	{
	}
	
	public void inputIndexToIndices(int inputIndex, int[] indices)
	{
	}
	
	/**
	 * Computes a unique joint index associated with the specified domain elements.
	 * <p>
	 * @param elements must have length equal to {@link #size()} and each elements must
	 * be an element of the corresponding domain.
	 * @see #jointIndexFromIndices(int ... )
	 * @see #jointIndexToElements(int, Object[])
	 */
	public int jointIndexFromElements(Object ... elements)
	{
		return undirectedJointIndexFromElements(elements);
	}

	/**
	 * Computes a unique joint index associated with the specified {@code indices}.
	 * 
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
	public Object[] jointIndexToElements(int jointIndex, Object[] elements)
	{
		return undirectedJointIndexToElements(jointIndex, elements);
	}
	
	/**
	 * Computes domain indices corresponding to given joint index.
	 * <p>
	 * @param jointIndex a unique joint table index in the range [0,{@link #getCardinality()}).
	 * @param indices if this is an array of length {@link #size()}, the computed values will
	 * be placed in this array, otherwise a new array will be allocated.
	 * @see #jointIndexToElements(int, Object[])
	 * @see #jointIndexFromIndices(int...)
	 */
	public int[] jointIndexToIndices(int jointIndex, int[] indices)
	{
		return undirectedJointIndexToIndices(jointIndex, indices);
	}
	
	public int outputIndexFromElements(Object ... elements)
	{
		return undirectedJointIndexFromElements(elements);
	}
	
	public int outputIndexFromIndices(int ... indices)
	{
		return undirectedJointIndexFromIndices(indices);
	}
	
	public int outputIndexFromJointIndex(int jointIndex)
	{
		return jointIndex;
	}
	
	public void outputIndexToElements(int outputIndex, Object[] elements)
	{
		undirectedJointIndexToElements(outputIndex, elements);
	}
	
	public void outputIndexToIndices(int outputIndex, int[] indices)
	{
		undirectedJointIndexToIndices(outputIndex, indices);
	}
	
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
	
	public final Object[] undirectedJointIndexToElements(int jointIndex, Object[] elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _products;

		elements = allocateElements(elements);
		
		int product;
		for (int i = products.length; --i >= 0;)
		{
			final int index = jointIndex / (product = products[i]);
			elements[i] = domains[i].getElement(index);
			jointIndex -= index * product;
		}
		return elements;
	}

	public final int[] undirectedJointIndexToIndices(int jointIndex, int[] indices)
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
	
	/**
	 * Verifies that the provided {@code indices} are in the correct range for
	 * this domain list, namely that:
	 * <ul>
	 * <li>{@code indices} has {@link #size()} elements
	 * <li>all values are non-negative
	 * <li>{@code indices[i]} < {@link #getDomainSize}{@code (i)}
	 * </ul>
	 * @throws IllegalArgumentException if wrong number of indices
	 * @throws IndexOutOfBoundsException if any index is out of range for its domain.
	 */
	public void validateIndices(int ... indices)
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
	}
	
	/*-----------------
	 * Package methods
	 */
	
	final Object[] allocateElements(Object [] elements)
	{
		if (elements == null || elements.length < _domains.length)
		{
			elements = new Object[_domains.length];
		}
		return elements;
	}

	final int[] allocateIndices(int [] indices)
	{
		if (indices == null || indices.length < _domains.length)
		{
			indices = new int[_domains.length];
		}
		return indices;
	}
}
