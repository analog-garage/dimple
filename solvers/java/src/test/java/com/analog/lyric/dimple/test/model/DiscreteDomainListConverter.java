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

/**
 * Supports conversion of indexes between two {@link DiscreteDomainList}s.
 */
@ThreadSafe
public abstract class DiscreteDomainListConverter
{
	/*-------
	 * State
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
		
		public final void release()
		{
			converter.releaseScratch(this);
		}
	}
	
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

	public static DiscreteDomainListConverter createPermuter(
		DiscreteDomainList fromDomains,
		DiscreteDomainList addedDomains,
		DiscreteDomainList toDomains,
		DiscreteDomainList removedDomains,
		int[] oldToNewIndex)
	{
		return new DiscreteDomainListPermuter(fromDomains, addedDomains, toDomains, removedDomains, oldToNewIndex);
	}
	
	public static DiscreteDomainListConverter createAdder(
		DiscreteDomainList fromDomains,
		int offset,
		DiscreteDomain ... addedDomains)
	{
		return createAdder(fromDomains, offset, DiscreteDomainList.create(addedDomains));
	}
	
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
	
	public static DiscreteDomainListConverter createJoiner(DiscreteDomainList fromDomains, int offset, int length)
	{
		return DiscreteDomainListJoiner.createJoiner(fromDomains, offset, length);
	}
	
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
		
	public static DiscreteDomainListConverter createRemover(DiscreteDomainList fromDomains, int ... removedIndices)
	{
		return createRemover(fromDomains, BitSetUtil.bitsetFromIndices(fromDomains.size(), removedIndices));
	}
	
	public static DiscreteDomainListJoiner createSplitter(DiscreteDomainList fromDomains, int offset)
	{
		return DiscreteDomainListJoiner.createSplitter(fromDomains, offset);
	}

	/*-------------------------------------
	 * DiscreteDomainListConverter methods
	 */
	
	public final int getAddedCardinality()
	{
		return _addedDomains == null ? 1 : _addedDomains.getCardinality();
	}
	
	public final DiscreteDomainList getAddedDomains()
	{
		return _addedDomains;
	}
	
	public abstract DiscreteDomainListConverter getInverse();
	
	public final int getRemovedCardinality()
	{
		return _removedDomains == null ? 1 : _removedDomains.getCardinality();
	}
	
	public final DiscreteDomainList getRemovedDomains()
	{
		return _removedDomains;
	}
	
	public final DiscreteDomainList getFromDomains()
	{
		return _fromDomains;
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

		_fromDomains.jointIndexToIndices(oldJointIndex, scratch.fromIndices);
		if (_addedDomains != null)
		{
			_addedDomains.jointIndexToIndices(addedJointIndex, scratch.addedIndices);
		}
		
		convertIndices(scratch);
		
		int newJointIndex = _toDomains.jointIndexFromIndices(scratch.toIndices);
		if (_removedDomains != null && removedJointIndex != null)
		{
			removedJointIndex.set(_removedDomains.jointIndexFromIndices(scratch.removedIndices));
		}
		
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
	
	public final Indices getScratch()
	{
		Indices scratch = _scratch.getAndSet(null);
		return scratch != null ? scratch : new Indices(this);
	}
	
	private final void releaseScratch(Indices scratch)
	{
		_scratch.lazySet(scratch);
	}
}
