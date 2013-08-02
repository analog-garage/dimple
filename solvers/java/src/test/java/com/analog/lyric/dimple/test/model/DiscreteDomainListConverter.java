package com.analog.lyric.dimple.test.model;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.analog.lyric.dimple.model.JointDiscreteDomain;
import com.google.common.base.Objects;

/**
 * Supports conversion of indexes between two {@link DiscreteDomainList}s.
 */
@ThreadSafe
public abstract class DiscreteDomainListConverter
{
	/*----------------
	 * Nested classes
	 */
	
	/**
	 * A set of domain indexes sized appropriately for a single {@link #converter} for use
	 * in conversion computation.
	 * 
	 * @see DiscreteDomainListConverter#getScratch()
	 * @see DiscreteDomainListConverter#convertIndices(Indices)
	 */
	@NotThreadSafe
	public static class Indices
	{
		public final DiscreteDomainListConverter converter;
		public final int[] fromIndices;
		public final int[] toIndices;
		public final int[] addedIndices;
		public final int[] removedIndices;
		
		private Indices(DiscreteDomainListConverter converter)
		{
			this.converter = converter;
			fromIndices = new int[converter._fromDomains.size()];
			toIndices = new int[converter._toDomains.size()];
			addedIndices =
				converter._addedDomains == null ? ArrayUtil.EMPTY_INT_ARRAY : new int[converter._addedDomains.size()];
			removedIndices =
				converter._removedDomains == null ? ArrayUtil.EMPTY_INT_ARRAY : new int[converter._removedDomains.size()];
		}
		
		/**
		 * Fills in {@link #fromIndices} from {@code fromJointIndex} and
		 * {@link #addedIndices} from {@code addedJointIndex}. If {@code #converter}
		 * has null for {@link DiscreteDomainListConverter#getAddedDomains()}, then
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
		 * has non-null for {@link DiscreteDomainListConverter#getRemovedDomains()},
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
	
	protected final DiscreteDomainList _addedDomains;
	protected final DiscreteDomainList _fromDomains;
	protected final DiscreteDomainList _removedDomains;
	protected final DiscreteDomainList _toDomains;
	
	private final AtomicReference<Indices> _scratch = new AtomicReference<Indices>();
	
	/*--------------
	 * Construction
	 */
	
	protected DiscreteDomainListConverter(
		DiscreteDomainList fromDomains,
		DiscreteDomainList addedDomains,
		DiscreteDomainList toDomains,
		DiscreteDomainList removedDomains)
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
	 * {@code toDomains} is in the range [fromSize, totalSize-1]. Likewise the domains in {@code toDomains} and
	 * {@code removeDomains} are in the range [0, toSize-1] and [toSize, totalSize - 1]. The mapping must not
	 * omit or repeat elements and may not map domains of different sizes.
	 */
	public static DiscreteDomainListConverter createPermuter(
		DiscreteDomainList fromDomains,
		DiscreteDomainList addedDomains,
		DiscreteDomainList toDomains,
		DiscreteDomainList removedDomains,
		int[] oldToNewIndex)
	{
		return new DiscreteDomainListPermuter(fromDomains, addedDomains, toDomains, removedDomains, oldToNewIndex);
	}
	
	/**
	 * Creates a converter that inserts a number of {@code addedDomains} at the given {@code offset}
	 * within {@code fromDomains}.
	 */
	public static DiscreteDomainListConverter createAdder(
		DiscreteDomainList fromDomains,
		int offset,
		DiscreteDomain ... addedDomains)
	{
		return createAdder(fromDomains, offset, DiscreteDomainList.create(addedDomains));
	}
	
	/**
	 * Creates a converter that inserts the {@code addedDomains} at the given {@code offset}
	 * within {@code fromDomains}.
	 */
	public static DiscreteDomainListConverter createAdder(
		DiscreteDomainList fromDomains,
		int offset,
		DiscreteDomainList addedDomains)
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
		
		return createPermuter(fromDomains, addedDomains, DiscreteDomainList.create(toDomains), null, oldToNewIndex);
	}
	
	/**
	 * Creates a converter that joins {@code length} adjacent domains at given {@code offset} in
	 * {@code fromDomains} into a single {@link JointDiscreteDomain}.
	 * <p>
	 * @see #createSplitter(DiscreteDomainList, int)
	 */
	public static DiscreteDomainListConverter createJoiner(DiscreteDomainList fromDomains, int offset, int length)
	{
		return DiscreteDomainListJoiner.createJoiner(fromDomains, offset, length);
	}
	
	/**
	 * Creates a converter that removes the domains specified by {@code removedIndices}
	 * from {@code fromDomains}.
	 */
	public static DiscreteDomainListConverter createRemover(DiscreteDomainList fromDomains, BitSet removedIndices)
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
		
		return createPermuter(fromDomains, null, DiscreteDomainList.create(toDomains),
			DiscreteDomainList.create(removedDomains), oldToNewIndex);
	}
		
	/**
	 * Creates a converter that removes the domains specified by {@code removedIndices}
	 * from {@code fromDomains}.
	 */
	public static DiscreteDomainListConverter createRemover(DiscreteDomainList fromDomains, int ... removedIndices)
	{
		return createRemover(fromDomains, BitSetUtil.bitsetFromIndices(fromDomains.size(), removedIndices));
	}
	
	/**
	 * Creates a converter that splits a {@link JointDiscreteDomain} at given {@code offset} in
	 * {@code fromDomains} into its constituent subdomains.
	 * <p>
	 * @see #createJoiner(DiscreteDomainList, int, int)
	 */
	public static DiscreteDomainListJoiner createSplitter(DiscreteDomainList fromDomains, int offset)
	{
		return DiscreteDomainListJoiner.createSplitter(fromDomains, offset);
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
		
		if (other instanceof DiscreteDomainListConverter)
		{
			DiscreteDomainListConverter that = (DiscreteDomainListConverter)other;
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
	 * DiscreteDomainListConverter methods
	 */
	
	// FIXME: remove this?
	public final int getAddedCardinality()
	{
		return _addedDomains == null ? 1 : _addedDomains.getCardinality();
	}
	
	public final DiscreteDomainList getAddedDomains()
	{
		return _addedDomains;
	}
	
	public abstract DiscreteDomainListConverter getInverse();
	
	public final DiscreteDomainList getRemovedDomains()
	{
		return _removedDomains;
	}
	
	public final DiscreteDomainList getFromDomains()
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
	
	public final DiscreteDomainList getToDomains()
	{
		return _toDomains;
	}
	
	public abstract void convertIndices(Indices indices);
	
	public double[] convertDenseWeights(double[] oldWeights)
	{
		final double[] weights = new double[_toDomains.getCardinality()];
	
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
	
	/*-----------------
	 * Private methods
	 */
	
	private final void releaseScratch(Indices scratch)
	{
		_scratch.lazySet(scratch);
	}
}
