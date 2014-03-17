package com.analog.lyric.dimple.model.domains;

import static com.analog.lyric.math.Utilities.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;
import cern.colt.map.OpenIntIntHashMap;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.google.common.base.Objects;

/**
 * Supports conversion of indexes between two {@link JointDomainIndexer}s.
 */
@ThreadSafe
public abstract class JointDomainReindexer
{
	/*----------------
	 * Nested classes
	 */
	
	/**
	 * A set of domain indexes sized appropriately for a single {@link #converter} for use
	 * in conversion computation.
	 * 
	 * @see JointDomainReindexer#getScratch()
	 * @see JointDomainReindexer#convertIndices(Indices)
	 */
	@NotThreadSafe
	public static class Indices
	{
		public final JointDomainReindexer converter;
		public final int[] fromIndices;
		public final int[] toIndices;
		public final int[] addedIndices;
		public final int[] removedIndices;
		public final int[] joinedIndices;
		
		private Indices(JointDomainReindexer converter)
		{
			this.converter = converter;
			fromIndices = new int[converter._fromDomains.size()];
			toIndices = new int[converter._toDomains.size()];
			addedIndices =
				converter._addedDomains == null ? ArrayUtil.EMPTY_INT_ARRAY : new int[converter._addedDomains.size()];
			removedIndices =
				converter._removedDomains == null ? ArrayUtil.EMPTY_INT_ARRAY : new int[converter._removedDomains.size()];
			int joinedSize =
				Math.abs(Math.abs(fromIndices.length - toIndices.length) -
					Math.abs(addedIndices.length - removedIndices.length));
			joinedIndices = joinedSize == 0 ? ArrayUtil.EMPTY_INT_ARRAY : new int[joinedSize];
		}
		
		/**
		 * Fills in {@link #fromIndices} from {@code fromJointIndex} and
		 * {@link #addedIndices} from {@code addedJointIndex}. If {@code #converter}
		 * has null for {@link JointDomainReindexer#getAddedDomains()}, then
		 * {@code addedJointIndex} will be ignored.
		 */
		public void writeIndices(int fromJointIndex, int addedJointIndex)
		{
			converter._fromDomains.jointIndexToIndices(fromJointIndex, fromIndices);
			if (addedIndices.length > 0)
			{
				converter._addedDomains.jointIndexToIndices(addedJointIndex, addedIndices);
			}
		}
		
		/**
		 * Returns joint index computed from {@code #toIndices} and if
		 * {@code removedJointIndexRef} is non-null and {@code #converter}
		 * has non-null for {@link JointDomainReindexer#getRemovedDomains()},
		 * this will compute a joint index from {@code #removedIndices} and set
		 * it in that {@code removedJiontIndexRef}.
		 */
		public int readIndices(AtomicInteger removedJointIndexRef)
		{
			int toJointIndex = converter._toDomains.jointIndexFromIndices(toIndices);
			if (removedJointIndexRef != null && removedIndices.length > 0)
			{
				removedJointIndexRef.set(converter._removedDomains.jointIndexFromIndices(removedIndices));
			}
			return toJointIndex;
		}
		
		/**
		 * Release object back to its {@link #converter} so that it may be reused.
		 * The caller must not use the object after calling this method.
		 */
		public final void release()
		{
			converter.releaseScratch(this);
		}
	}
	
	/*-------
	 * State
	 */
	
	protected final JointDomainIndexer _addedDomains;
	protected final JointDomainIndexer _fromDomains;
	protected final JointDomainIndexer _removedDomains;
	protected final JointDomainIndexer _toDomains;
	
	private final AtomicReference<Indices> _scratch = new AtomicReference<Indices>();
	
	/*--------------
	 * Construction
	 */
	
	protected JointDomainReindexer(
		JointDomainIndexer fromDomains,
		JointDomainIndexer addedDomains,
		JointDomainIndexer toDomains,
		JointDomainIndexer removedDomains)
	{
		_fromDomains = fromDomains;
		_addedDomains = addedDomains;
		_toDomains = toDomains;
		_removedDomains = removedDomains;
	}

	/**
	 * Creates a converter that implements a permutation of the domains in
	 * {@code fromDomains} and {@code addedDomains} into {@code ToDomains} and
	 * {@code removedDomains}.
	 * <p>
	 * The total number of domains in the from+added domains must equal those in
	 * to+removed domains.
	 * <p>
	 * @param fromDomains must be non-null
	 * @param addedDomains may be null
	 * @param toDomains must be non-null
	 * @param removedDomains may be null
	 * @param oldToNewIndex maps the index of the old domain to the index of the new domain where the index of domains
	 * in {@code fromDomains} are in the range [0,fromSize-1] and the index of domains in
	 * {@code addedDomains} is in the range [fromSize, totalSize-1]. Likewise the domains in {@code toDomains} and
	 * {@code removeDomains} are in the range [0, toSize-1] and [toSize, totalSize - 1]. The mapping must not
	 * omit or repeat elements and may not map domains of different sizes.
	 */
	public static JointDomainReindexer createPermuter(
		JointDomainIndexer fromDomains,
		JointDomainIndexer addedDomains,
		JointDomainIndexer toDomains,
		JointDomainIndexer removedDomains,
		int[] oldToNewIndex)
	{
		return new JointDomainIndexPermuter(fromDomains, addedDomains, toDomains, removedDomains, oldToNewIndex);
	}
	
	/**
	 * Creates a converter that implements a permutation of the domains
	 * in {@code fromDomains} to those in {@code toDomains}.
	 * <p>
	 * This implementation will invoke
	 * {@link #createPermuter(JointDomainIndexer, JointDomainIndexer, JointDomainIndexer, JointDomainIndexer, int[])}
	 * after calculating the {@code addedDomains} and {@code removedDomains} arguments.
	 * <p>
	 * There are three cases:
	 * <dl>
	 *   <dt>{@code fromDomains} and {@code toDomains} have the same size.</dt>
	 *   <dd>The {@code oldToNewIndex} must also be the same size. The {@code addedDomains} and {@code removedDomains}
	 *   arguments will be null.
	 *   </dd>
	 * 
	 *   <dt>{@code fromDomains} is smaller than {@code toDomains}</dt>
	 *   <dd>It is necessary to deduce the {@code addedDomains}. If {@code oldToNewIndex} is the same
	 *   length as {@code toDomains} it specifies how the added domains should be chosen from {@code toDomains}.
	 *   If it is the same length as {@code fromDomains} then the {@code addedDomains} will be chosen from
	 *   the remaining {@code toDomains} that are not in the index in order.
	 *   </dd>
	 * 
	 *   <dt>{@code fromDomains} is larger than {@code toDomains}</dt>
	 *   <dd>The {@code oldToNewIndex} must be the same size as {@code fromDomains}.
	 *   It is necessary to deduce the {@code removedDomains}.
	 *   </dd>
	 * </ol>
	 */
	public static JointDomainReindexer createPermuter(
		JointDomainIndexer fromDomains,
		JointDomainIndexer toDomains,
		int[] oldToNewIndex)
	{
		final int fromSize = fromDomains.size();
		final int toSize = toDomains.size();
		final int diff = fromSize - toSize;
		
		if (diff == 0)
		{
			return createPermuter(fromDomains, null, toDomains, null, oldToNewIndex);
		}
		else if (diff < 0)
		{
			// Need to compute added domains
			final int addedSize = -diff;
			
			if (oldToNewIndex.length < toSize)
			{
				// index map is too short. Deduce rest of it from missing entries.
				final BitSet toSet = BitSetUtil.bitsetFromIndices(toSize, oldToNewIndex);
				oldToNewIndex = Arrays.copyOf(oldToNewIndex, toSize);
				for (int from = fromSize, to = -1; from < toSize; ++from)
				{
					to = toSet.nextClearBit(to + 1);
					oldToNewIndex[from] = to;
				}
			}
			
			final DiscreteDomain[] addedDomains = new DiscreteDomain[addedSize];
			for (int i = 0; i < addedSize; ++i)
			{
				addedDomains[i] = toDomains.get(oldToNewIndex[i + fromSize]);
			}
			
			return createPermuter(fromDomains, JointDomainIndexer.create(addedDomains), toDomains, null, oldToNewIndex);
		}
		else
		{
			// From is longer than to - need to compute removed domains
			final int removedSize = diff;
			
			final DiscreteDomain[] removedDomains = new DiscreteDomain[removedSize];
			for (int from = 0; from < fromSize; ++from)
			{
				final int to = oldToNewIndex[from];
				if (to >= toSize)
				{
					removedDomains[to - toSize] = fromDomains.get(from);
				}
			}
			
			return createPermuter(fromDomains, null, toDomains, JointDomainIndexer.create(removedDomains), oldToNewIndex);
		}
	}
	
	/**
	 * Creates a converter to convert {@code fromDomains} to {@code toDomains}
	 * while maintaining the same domain order. This can be used to convert between
	 * domain lists with different input/output domain sets.
	 */
	public static JointDomainReindexer createPermuter(
		JointDomainIndexer fromDomains,
		JointDomainIndexer toDomains)
	{
		int[] oldToNew = new int[fromDomains.size()];
		for (int i = oldToNew.length; --i>=0;)
		{
			oldToNew[i] = i;
		}
		return createPermuter(fromDomains, toDomains, oldToNew);
	}
	
	/**
	 * Creates a converter that inserts a number of {@code addedDomains} at the given {@code offset}
	 * within {@code fromDomains}.
	 */
	public static JointDomainReindexer createAdder(
		JointDomainIndexer fromDomains,
		int offset,
		DiscreteDomain ... addedDomains)
	{
		return createAdder(fromDomains, offset, JointDomainIndexer.create(addedDomains));
	}
	
	/**
	 * Creates a converter that inserts the {@code addedDomains} at the given {@code offset}
	 * within {@code fromDomains}.
	 */
	public static JointDomainReindexer createAdder(
		JointDomainIndexer fromDomains,
		int offset,
		JointDomainIndexer addedDomains)
	{
		final int fromSize = fromDomains.size();
		final int addedSize = addedDomains.size();
		final int toSize = fromSize + addedSize;
		
		final int[] oldToNewIndex = new int[toSize];
		final DiscreteDomain[] toDomains = new DiscreteDomain[toSize];
		
		for (int i = 0; i < offset; ++i)
		{
			toDomains[i] = fromDomains.get(i);
			oldToNewIndex[i] = i;
		}
		
		for (int i = 0; i < addedSize; ++i)
		{
			int to = offset + i;
			toDomains[to] = addedDomains.get(i);
			oldToNewIndex[fromSize + i] = to;
		}
		
		for (int i = offset; i < fromSize; ++i)
		{
			int to = offset + addedSize;
			toDomains[to] = fromDomains.get(i);
			oldToNewIndex[i] = to;
		}
		
		return createPermuter(fromDomains, addedDomains, JointDomainIndexer.create(toDomains), null, oldToNewIndex);
	}
	
	/**
	 * Creates a converter that joins {@code length} adjacent domains at given {@code offset} in
	 * {@code fromDomains} into a single {@link JointDiscreteDomain}.
	 * <p>
	 * @see #createSplitter(JointDomainIndexer, int)
	 */
	public static JointDomainReindexer createJoiner(JointDomainIndexer fromDomains, int offset, int length)
	{
		return JointDomainIndexJoiner.createJoiner(fromDomains, offset, length);
	}
	
	/**
	 * Creates a converter that removes the domains specified by {@code removedIndices}
	 * from {@code fromDomains}.
	 */
	public static JointDomainReindexer createRemover(JointDomainIndexer fromDomains, BitSet removedIndices)
	{
		final int fromSize = fromDomains.size();
		final int removedSize = removedIndices.cardinality();
		final int toSize = fromSize - removedSize;
		
		final DiscreteDomain[] removedDomains = new DiscreteDomain[removedSize];
		final DiscreteDomain[] toDomains = new DiscreteDomain[fromSize - removedSize];
		final int[] oldToNewIndex = new int[fromSize];
		
		for (int i = 0, to = 0, removed = 0; i < fromSize; ++i)
		{
			DiscreteDomain domain = fromDomains.get(i);
			if (removedIndices.get(i))
			{
				removedDomains[removed] = domain;
				oldToNewIndex[i] = toSize + removed;
				++removed;
			}
			else
			{
				toDomains[to] = domain;
				oldToNewIndex[i] = to;
				++to;
			}
		}
		
		return createPermuter(fromDomains, null, JointDomainIndexer.create(toDomains),
			JointDomainIndexer.create(removedDomains), oldToNewIndex);
	}
		
	/**
	 * Creates a converter that removes the domains specified by {@code removedIndices}
	 * from {@code fromDomains}.
	 */
	public static JointDomainReindexer createRemover(JointDomainIndexer fromDomains, int ... removedIndices)
	{
		return createRemover(fromDomains, BitSetUtil.bitsetFromIndices(fromDomains.size(), removedIndices));
	}
	
	/**
	 * Creates a converter that splits a {@link JointDiscreteDomain} at given {@code offset} in
	 * {@code fromDomains} into its constituent subdomains.
	 * <p>
	 * @see #createSplitter(JointDomainIndexer, int...)
	 * @see #createJoiner(JointDomainIndexer, int, int)
	 */
	public static JointDomainIndexJoiner createSplitter(JointDomainIndexer fromDomains, int offset)
	{
		return JointDomainIndexJoiner.createSplitter(fromDomains, offset);
	}
	
	/**
	 * Creates a converter that splits a {@link JointDiscreteDomain} at given {@code offsets} in
	 * {@code fromDomains} into its constituent subdomains.
	 * <p>
	 * @see #createSplitter(JointDomainIndexer, int)
	 * @see #createJoiner(JointDomainIndexer, int, int)
	 */
	public static JointDomainReindexer createSplitter(JointDomainIndexer fromDomains, int ... offsets)
	{
		offsets = offsets.clone();
		Arrays.sort(offsets);
		
		JointDomainReindexer indexer = null;
		
		for (int i = offsets.length; --i>=0;)
		{
			final int offset = offsets[i];
			
			final JointDomainIndexJoiner splitter = createSplitter(fromDomains, offset);
			indexer = indexer != null ? indexer.combineWith(splitter) : splitter;
			fromDomains = indexer.getToDomains();
		}
		
		return indexer;
	}

	/**
	 * Creates a new converter that combines this one with {@code that} by first
	 * applying this conversion and passing the result to {@code that}.
	 */
	public JointDomainReindexer combineWith(JointDomainReindexer that)
	{
		return ChainedJointDomainReindexer.create(this, that);
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}
		
		if (other instanceof JointDomainReindexer)
		{
			JointDomainReindexer that = (JointDomainReindexer)other;
			return _fromDomains.equals(that._fromDomains) &&
				_toDomains.equals(that._toDomains) &&
				Objects.equal(_addedDomains, that._addedDomains) &&
				Objects.equal(_removedDomains, that._removedDomains);
		}
		
		return false;
	}
	
	@Override
	public abstract int hashCode();

	/*-------------------------------------
	 * JointDomainReindexer methods
	 */

	/**
	 * Returns {@link JointDomainIndexer} representing the subdomains to be added to
	 * the factor table. This may be null if no dimensions are to be added.
	 */
	public final JointDomainIndexer getAddedDomains()
	{
		return _addedDomains;
	}
	
	public abstract JointDomainReindexer getInverse();
	
	/**
	 * Returns {@link JointDomainIndexer} representing the subdomains to be removed from
	 * the factor table. This may be null if no dimensions are to be removed.
	 */
	public final JointDomainIndexer getRemovedDomains()
	{
		return _removedDomains;
	}
	
	/**
	 * Returns {@link JointDomainIndexer} for factor table to be converted from. Will never be null.
	 */
	public final JointDomainIndexer getFromDomains()
	{
		return _fromDomains;
	}
	
	/**
	 * Returns an "scratch" instance of {@link Indices} for use in conversion computations.
	 * <p>
	 * If the instance is later released via {@link Indices#release()} then that instance
	 * may be returned by a future invocation of this method.
	 */
	public final Indices getScratch()
	{
		Indices scratch = _scratch.getAndSet(null);
		return scratch != null ? scratch : new Indices(this);
	}
	
	/**
	 * Returns {@link JointDomainIndexer} for factor table to be converted to. Will never be null.
	 */
	public final JointDomainIndexer getToDomains()
	{
		return _toDomains;
	}
	
	/**
	 * Computes {@link Indices#toIndices} and {@link Indices#removedIndices} fields of {@code indices}
	 * assuming that {@link Indices#fromIndices}, {@link Indices#toIndices} and {@link Indices#joinedIndices}
	 * have already been set.
	 */
	public abstract void convertIndices(Indices indices);
	
	public double[] convertDenseEnergies(double[] oldEnergies)
	{
		if (_removedDomains == null)
		{
			// No domains removed, so we don't need to add together any weights and can
			// do a simple copy.
			return denseCopy(oldEnergies);
		}
		
		final double[] values = new double[_toDomains.getCardinality()];
		// values start out in weight domain and are converted to log domain at bottom.
		
		if (hasFastJointIndexConversion())
		{
			for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
			{
				for (int added = getAddedCardinality(); --added >= 0;)
				{
					values[convertJointIndex(oldJoint, added)] += energyToWeight(oldEnergies[oldJoint]);
				}
			}
		}
		else
		{
			Indices scratch = getScratch();

			for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
			{
				_fromDomains.jointIndexToIndices(oldJoint, scratch.fromIndices);
				for (int added = getAddedCardinality(); --added >= 0;)
				{
					if (_addedDomains != null)
					{
						_addedDomains.jointIndexToIndices(added, scratch.addedIndices);
					}
					convertIndices(scratch);
					values[_toDomains.jointIndexFromIndices(scratch.toIndices)]
						+= energyToWeight(oldEnergies[oldJoint]);
				}
			}

			scratch.release();
		}
		
		for (int i = values.length; --i>=0;)
		{
			values[i] = weightToEnergy(values[i]);
		}
		
		return values;
	}
	
	
	public double[] convertDenseWeights(double[] oldWeights)
	{
		if (_removedDomains == null)
		{
			// No domains removed, so we don't need to add together any weights and can
			// do a simple copy.
			return denseCopy(oldWeights);
		}
		
		final double[] weights = new double[_toDomains.getCardinality()];
		
		if (hasFastJointIndexConversion())
		{
			if (_addedDomains == null)
			{
				for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
				{
					weights[convertJointIndex(oldJoint, 0)] += oldWeights[oldJoint];
				}
			}
			else
			{
				for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
				{
					for (int added = _addedDomains.getCardinality(); --added >= 0;)
					{
						weights[convertJointIndex(oldJoint, added)] += oldWeights[oldJoint];
					}
				}
			}
		}
		else
		{
			Indices scratch = getScratch();

			if (_addedDomains == null)
			{
				for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
				{
					_fromDomains.jointIndexToIndices(oldJoint, scratch.fromIndices);
					convertIndices(scratch);
					weights[_toDomains.jointIndexFromIndices(scratch.toIndices)] += oldWeights[oldJoint];
				}
			}
			else
			{
				for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
				{
					_fromDomains.jointIndexToIndices(oldJoint, scratch.fromIndices);
					for (int added = _addedDomains.getCardinality(); --added >= 0;)
					{
						_addedDomains.jointIndexToIndices(added, scratch.addedIndices);
						convertIndices(scratch);
						weights[_toDomains.jointIndexFromIndices(scratch.toIndices)] += oldWeights[oldJoint];
					}
				}
			}

			scratch.release();
		}
		
		return weights;
	}
	
	public int convertJointIndex(int oldJointIndex, int addedJointIndex, AtomicInteger removedJointIndex)
	{
		Indices scratch = getScratch();

		scratch.writeIndices(oldJointIndex, addedJointIndex);
		convertIndices(scratch);
		int newJointIndex = scratch.readIndices(removedJointIndex);
		
		scratch.release();

		return newJointIndex;
	}
	
	public int convertJointIndex(int oldJointIndex, int addedJointIndex)
	{
		return convertJointIndex(oldJointIndex, addedJointIndex, null);
	}

	public double[] convertSparseEnergies(double[] oldEnergies,
		int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		if (sparseIndexToJointIndex.length == oldSparseIndexToJointIndex.length * getAddedCardinality())
		{
			// No entries need to be merged, so we can use a simple copy and avoid energy/weight conversions.
			return sparseCopy(oldEnergies, oldSparseIndexToJointIndex, sparseIndexToJointIndex);
		}
		
		final int oldSize = oldSparseIndexToJointIndex.length;
		final int size = sparseIndexToJointIndex.length;
		final double[] values = new double[size];
		
		final OpenIntIntHashMap jointIndexToSparseIndex = new OpenIntIntHashMap(sparseIndexToJointIndex.length);
		for (int si = sparseIndexToJointIndex.length; --si>=0;)
		{
			jointIndexToSparseIndex.put(sparseIndexToJointIndex[si], si);
		}

		final int maxAdded = getAddedCardinality();

		for (int oldSparse = 0; oldSparse < oldSize; ++oldSparse)
		{
			final double oldWeight = energyToWeight(oldEnergies[oldSparse]);
			final int oldJoint = oldSparseIndexToJointIndex[oldSparse];
			for (int added = 0; added < maxAdded; ++added)
			{
				final int newJoint = convertJointIndex(oldJoint, added);
				final int newSparse = jointIndexToSparseIndex.get(newJoint);
				values[newSparse] += oldWeight;
			}
		}
		
		for (int i = 0; i < size; ++i)
		{
			values[i] = weightToEnergy(values[i]);
		}
		
		return values;
	}
	
	public int[][] convertSparseIndices(
		int[][] oldSparseIndices, int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		final int sparseSize = sparseIndexToJointIndex.length;
		final int[][] indices = new int[sparseSize][];
		for (int si = 0; si < sparseSize; ++si)
		{
			indices[si] = _toDomains.jointIndexToIndices(sparseIndexToJointIndex[si]);
		}
		return indices;
	}
	
	public int[] convertSparseToJointIndex(int[] oldSparseToJointIndex)
	{
		final int[] sparseToJoint = new int[oldSparseToJointIndex.length * getAddedCardinality()];
		
		int i = 0;
		for (int added = 0, maxAdded = getAddedCardinality(); added < maxAdded; ++added)
		{
			for (int oldJoint : oldSparseToJointIndex)
			{
				sparseToJoint[i++] = convertJointIndex(oldJoint, added);
			}
		}

		if (!maintainsJointIndexOrder())
		{
			Arrays.sort(sparseToJoint);
		}

		if (_removedDomains != null)
		{
			// If domains are removed then we may end up with duplicates.
			// First determine new size.
			int count = 0, prev = -1;
			for (int joint : sparseToJoint)
			{
				if (joint != prev)
				{
					++count;
				}
				prev = joint;
			}
			
			if (count != sparseToJoint.length)
			{
				final int[] sparseToJoint2 = new int[count];
				i = 0;
				prev = -1;
				for (int joint : sparseToJoint)
				{
					if (joint != prev)
					{
						sparseToJoint2[i++] = joint;
					}
					prev = joint;
				}
				return sparseToJoint2;
			}
		}
		
		return sparseToJoint;
	}
	
	public double[] convertSparseWeights(
		double[] oldWeights, int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		if (_removedDomains == null)
		{
			return sparseCopy(oldWeights, oldSparseIndexToJointIndex, sparseIndexToJointIndex);
		}
		
		final int oldSize = oldSparseIndexToJointIndex.length;
		final int size = sparseIndexToJointIndex.length;
		final double[] weights = new double[size];
		
		final OpenIntIntHashMap jointIndexToSparseIndex = new OpenIntIntHashMap(sparseIndexToJointIndex.length);
		for (int si = sparseIndexToJointIndex.length; --si>=0;)
		{
			jointIndexToSparseIndex.put(sparseIndexToJointIndex[si], si);
		}

		final int maxAdded = getAddedCardinality();
		
		for (int oldSparse = 0; oldSparse < oldSize; ++oldSparse)
		{
			final double oldWeight = oldWeights[oldSparse];
			final int oldJoint = oldSparseIndexToJointIndex[oldSparse];
			for (int added = 0; added < maxAdded; ++added)
			{
				final int newJoint = convertJointIndex(oldJoint, added);
				final int newSparse = jointIndexToSparseIndex.get(newJoint);
				weights[newSparse] += oldWeight;
			}
		}
		
		return weights;
	}
	
	/**
	 * The number of different possible combinations of values in {@link #getAddedDomains()}
	 * or else returns 1 if no added domains.
	 */
	public final int getAddedCardinality()
	{
		return _addedDomains == null ? 1 : _addedDomains.getCardinality();
	}
	
	/**
	 * The number of different possible combinations of values in {@link #getRemovedDomains()}
	 * or else returns 1 if no removed domains.
	 */
	public final int getRemovedCardinality()
	{
		return _removedDomains == null ? 1 : _removedDomains.getCardinality();
	}

	public abstract boolean hasFastJointIndexConversion();
	
	/*-------------------
	 * Protected methods
	 */
	
	/**
	 * Default computation of {@link #hashCode()}
	 */
	protected int computeHashCode()
	{
		int hash = _fromDomains.hashCode() * 7 + _toDomains.hashCode();
		if (_addedDomains != null)
		{
			hash *= 11;
			hash += _addedDomains.hashCode();
		}
		if (_removedDomains != null)
		{
			hash *= 13;
			hash += _removedDomains.hashCode();
		}
		
		return hash;
	}
	
	/**
	 * True if converter maintains the same order of joint indexes such that if
	 * oldA <= oldB, then newA <= newB. Used to avoid sorting when converting
	 * sparse indices.
	 * <p>
	 * True only if all removals are at front of list, additions are at end of
	 * list and relative order of domains is otherwise maintained.
	 */
	protected abstract boolean maintainsJointIndexOrder();
	
	/*-----------------
	 * Private methods
	 */
	
	private double[] denseCopy(double[] oldValues)
	{
		final double[] values = new double[_toDomains.getCardinality()];
		
		if (hasFastJointIndexConversion())
		{
			for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
			{
				for (int added = getAddedCardinality(); --added >= 0;)
				{
					values[convertJointIndex(oldJoint, added)] = oldValues[oldJoint];
				}
			}
		}
		else
		{
			Indices scratch = getScratch();

			for (int oldJoint = _fromDomains.getCardinality(); --oldJoint >= 0;)
			{
				_fromDomains.jointIndexToIndices(oldJoint, scratch.fromIndices);
				for (int added = getAddedCardinality(); --added >= 0;)
				{
					if (_addedDomains != null)
					{
						_addedDomains.jointIndexToIndices(added, scratch.addedIndices);
					}
					convertIndices(scratch);
					values[_toDomains.jointIndexFromIndices(scratch.toIndices)] = oldValues[oldJoint];
				}
			}

			scratch.release();
		}

		return values;
	}
	
	private final void releaseScratch(Indices scratch)
	{
		_scratch.lazySet(scratch);
	}
	
	public double[] sparseCopy(double[] oldValues, int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		final int oldSize = oldSparseIndexToJointIndex.length;
		final int size = sparseIndexToJointIndex.length;
		final double[] values = new double[size];
		
		final OpenIntIntHashMap jointIndexToSparseIndex = new OpenIntIntHashMap(sparseIndexToJointIndex.length);
		for (int si = sparseIndexToJointIndex.length; --si>=0;)
		{
			jointIndexToSparseIndex.put(sparseIndexToJointIndex[si], si);
		}

		for (int oldSparse = 0; oldSparse < oldSize; ++oldSparse)
		{
			final int oldJoint = oldSparseIndexToJointIndex[oldSparse];
			for (int added = getAddedCardinality(); --added >=0; )
			{
				final int newJoint = convertJointIndex(oldJoint, added);
				final int newSparse = jointIndexToSparseIndex.get(newJoint);
				values[newSparse] = oldValues[oldSparse];
			}
		}
		
		return values;
	}

}
