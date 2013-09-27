package com.analog.lyric.dimple.model;

import java.util.Arrays;
import java.util.BitSet;

import net.jcip.annotations.Immutable;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.collect.Supers;

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
public class JointDomainIndexer extends DomainList<DiscreteDomain>
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
	
	/**
	 * Precomputed {@link #hashCode()}.
	 */
	private final int _hashCode;
	
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
		
		final int nDomains = domains.length;
		
		_products = new int[nDomains];
		int product = 1;
		for (int i = 0; i < nDomains; ++i)
		{
			_products[i] = product;
			product *= domains[i].size();
		}
		_cardinality = product;
		
		Class<?> elementClass = domains[0].getElementClass();
		for (int i = domains.length; --i >= 1; )
		{
			elementClass = Supers.nearestCommonSuperClass(elementClass, domains[i].getElementClass());
		}
		_elementClass = elementClass;
	}
	
	private JointDomainIndexer(DiscreteDomain[] domains)
	{
		this(Arrays.hashCode(domains), domains);
	}
	
	private static JointDomainIndexer lookupOrCreate(BitSet outputs, DiscreteDomain[] domains, boolean cloneDomains)
	{
		// TODO: implement cache...
		
		if (cloneDomains)
		{
			domains = Arrays.copyOf(domains, domains.length, DiscreteDomain[].class);
		}
		
		if (outputs != null)
		{
			return new DirectedJointDomainIndexer(outputs, domains);
		}
		else
		{
			return new JointDomainIndexer(domains);
		}
	}
	
	static JointDomainIndexer lookupOrCreate(int[] outputIndices, DiscreteDomain[] domains, boolean cloneDomains)
	{
		BitSet outputs = null;
		
		if (outputIndices != null)
		{
			outputs = BitSetUtil.bitsetFromIndices(domains.length, outputIndices);
		}
		
		return lookupOrCreate(outputs, domains, cloneDomains);
	}

	public static JointDomainIndexer create(BitSet outputs, DiscreteDomain ... domains)
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
	public static JointDomainIndexer create(int[] outputDomainIndices, DiscreteDomain[] domains)
	{
		return lookupOrCreate(outputDomainIndices, domains, true);
	}
	
	/**
	 * Creates a directed indexer consisting of the specified {@code domains} in the given
	 * order and with the specified domains designated as outputs.
	 * <p>
	 * If {@code outputs} is null, this will instead return an undirected indexer.
	 */
	public static JointDomainIndexer create(BitSet outputs, JointDomainIndexer domains)
	{
		return lookupOrCreate(outputs, domains._domains, false);
	}
	
	/**
	 * Returns a new domain list that concatenates the domains of this list
	 * with {@code that}. Only produces a directed list if both lists are directed.
	 */
	public static JointDomainIndexer concat(JointDomainIndexer domains1, JointDomainIndexer domains2)
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

		if (domains1.isDirected() & domains2.isDirected())
		{
			outputs = domains1.getOutputSet();
			for (int i : domains2.getOutputDomainIndices())
			{
				outputs.set(size1 + i);
			}
		}
		
		return JointDomainIndexer.create(outputs, domains);
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
		
		if (that instanceof JointDomainIndexer)
		{
			JointDomainIndexer thatDiscrete = (JointDomainIndexer)that;
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
	public final <T> T[] allocateElements(T [] elements)
	{
		return ArrayUtil.allocateArrayOfType(_elementClass,  elements,  _domains.length);
	}

	public final int[] allocateIndices(int [] indices)
	{
		if (indices == null || indices.length < _domains.length)
		{
			indices = new int[_domains.length];
		}
		return indices;
	}

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
	 * Returns nearest common superclass for elements in all domains.
	 */
	public final Class<?> getElementClass()
	{
		return _elementClass;
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
	public int[] getInputDomainIndices()
	{
		return null;
	}
	
	/**
	 * Returns a copy of the {@link BitSet} representing the indexes of the input domains or
	 * else null if not {@link #isDirected()}.
	 * 
	 * @see #getInputDomainIndices()
	 * @see #getInputDomainIndex(int)
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
	public int[] getOutputDomainIndices()
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
	 * Returns amount by which joint index returned by {@link #jointIndexFromIndices(int...)} changes
	 * when ith element index changes by 1.
	 * <p>
	 * This can be used to iterate over the joint indexes for one dimension for fixed values of all of
	 * the other dimensions.
	 * <p>
	 * @see #getUndirectedStride(int)
	 */
	public int getStride(int i)
	{
		return getUndirectedStride(i);
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
	public final int getUndirectedStride(int i)
	{
		return _products[i];
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
	public <T> T[] jointIndexToElements(int jointIndex, T[] elements)
	{
		return undirectedJointIndexToElements(jointIndex, elements);
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
	public int[] jointIndexToIndices(int jointIndex, int[] indices)
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
	
	public final <T> T[] undirectedJointIndexToElements(int jointIndex, T[] elements)
	{
		final DiscreteDomain[] domains = _domains;
		final int[] products = _products;

		elements = allocateElements(elements);
		
		int product;
		for (int i = products.length; --i >= 0;)
		{
			final int index = jointIndex / (product = products[i]);
			elements[i] = (T) domains[i].getElement(index);
			jointIndex -= index * product;
		}
		return elements;
	}
	
	public final Object[] undirectedJointIndexToElements(int jointIndex)
	{
		return undirectedJointIndexToElements(jointIndex, null);
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
	public int undirectedJointIndexToElementIndex(int jointIndex, int domainIndex)
	{
		return (jointIndex / _products[domainIndex]) % _domains[domainIndex].size();
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
	
	public final int[] undirectedJointIndexToIndices(int jointIndex)
	{
		return undirectedJointIndexToIndices(jointIndex, null);
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
	
}
