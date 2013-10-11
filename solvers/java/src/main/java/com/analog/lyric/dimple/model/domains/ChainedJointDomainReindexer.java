package com.analog.lyric.dimple.model.domains;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.exceptions.DimpleException;

public final class ChainedJointDomainReindexer extends JointDomainReindexer
{
	/*-------
	 * State
	 */
	
	private int _hashCode;
	private final JointDomainReindexer[] _converters;
	private volatile ChainedJointDomainReindexer _inverse;
	private final boolean _hasFastJointIndexConversion;
	private final boolean _maintainsJointIndexOrder;
	
	/*--------------
	 * Construction
	 */
	
	private ChainedJointDomainReindexer(
		JointDomainIndexer fromDomains,
		JointDomainIndexer addedDomains,
		JointDomainIndexer toDomains,
		JointDomainIndexer removedDomains,
		JointDomainReindexer ... converters
		)
	{
		super(fromDomains, addedDomains, toDomains, removedDomains);
		_converters = converters.clone();
		_hashCode = computeHashCode();
		
		boolean fastJointIndex = true;
		boolean maintainsOrder = true;
		for (JointDomainReindexer converter : converters)
		{
			fastJointIndex &= converter.hasFastJointIndexConversion();
			maintainsOrder &= converter.maintainsJointIndexOrder();
		}
		_hasFastJointIndexConversion = fastJointIndex;
		_maintainsJointIndexOrder = maintainsOrder;
	}
		
	static JointDomainReindexer create(JointDomainReindexer ... converters)
	{
		switch (converters.length)
		{
		case 0:
			return null;
			
		case 1:
			return converters[0];
		}
		
		// TODO: eliminate consecutive converters that cancel each other out, i.e. a converter
		// followed by its inverse.
		
		// Make sure that domains form a valid from->to/from->to chain.
		for (int i = converters.length; --i>=1;)
		{
			if (!converters[i]._fromDomains.equals(converters[i-1]._toDomains))
			{
				throw new DimpleException("Cannot combine converters with mismatched domains.");
			}
		}
		
		// Flatten out nested chains
		int size = 0;
		for (JointDomainReindexer converter : converters)
		{
			if (converter instanceof ChainedJointDomainReindexer)
			{
				ChainedJointDomainReindexer subchain = (ChainedJointDomainReindexer)converter;
				size += subchain._converters.length;
			}
			else
			{
				++size;
			}
		}
		if (converters.length != size)
		{
			JointDomainReindexer[] flattenedConverters = new JointDomainReindexer[size];
			for (int to = 0, from = 0; from < converters.length; ++from)
			{
				JointDomainReindexer converter = converters[from];
				if (converter instanceof ChainedJointDomainReindexer)
				{
					ChainedJointDomainReindexer subchain = (ChainedJointDomainReindexer)converter;
					for (JointDomainReindexer subconverter : subchain._converters)
					{
						flattenedConverters[to++] = subconverter;
					}
				}
				else
				{
					flattenedConverters[to++] = converter;
				}
			}
			converters = flattenedConverters;
		}
		
		JointDomainIndexer addedDomains = null;
		for (JointDomainReindexer converter : converters)
		{
			addedDomains = JointDomainIndexer.concat(addedDomains, converter._addedDomains);
		}
		
		JointDomainIndexer removedDomains = null;
		for (int i = converters.length; --i>=0;)
		{
			removedDomains = JointDomainIndexer.concat(removedDomains, converters[i]._removedDomains);
		}
		
		return new ChainedJointDomainReindexer(
			converters[0]._fromDomains,
			addedDomains,
			converters[size-1]._toDomains,
			removedDomains,
			converters);
	}
	
	@Override
	protected int computeHashCode()
	{
		return super.computeHashCode() * 23 + Arrays.hashCode(_converters);
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
		
		if (other instanceof ChainedJointDomainReindexer)
		{
			ChainedJointDomainReindexer that = (ChainedJointDomainReindexer)other;
			return Arrays.equals(_converters, that._converters);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _hashCode;
	}

	/*--------------------------------------
	 * JointDomainReindexer methods
	 */
	
	@Override
	public ChainedJointDomainReindexer getInverse()
	{
		ChainedJointDomainReindexer inverse = _inverse;
		
		if (inverse == null)
		{
			final int size = _converters.length;
			JointDomainReindexer[] converters = new JointDomainReindexer[size];
			for (int from = 0, to = size - 1; from < size; ++from, --to)
			{
				converters[to] = _converters[from].getInverse();
			}
			inverse = _inverse = new ChainedJointDomainReindexer(
				_toDomains, _removedDomains, _fromDomains, _addedDomains, converters);
		}
		
		return inverse;
	}

	@Override
	public void convertIndices(Indices indices)
	{
		int addOffset = 0, removeOffset = indices.removedIndices.length;
		Indices prev = null;
		for (JointDomainReindexer converter : _converters)
		{
			final Indices scratch = converter.getScratch();
			
			if (prev == null)
			{
				System.arraycopy(indices.fromIndices, 0, scratch.fromIndices, 0, scratch.fromIndices.length);
			}
			else
			{
				System.arraycopy(prev.toIndices, 0, scratch.fromIndices, 0, scratch.fromIndices.length);
			}
			
			final int addSize = scratch.addedIndices.length;
			if (addSize > 0)
			{
				System.arraycopy(indices.addedIndices, addOffset,  scratch.addedIndices, 0, addSize);
				addOffset += addSize;
			}
			
			converter.convertIndices(scratch);
			
			final int removeSize = scratch.removedIndices.length;
			if (removeSize > 0)
			{
				removeOffset -= removeSize;
				System.arraycopy(scratch.removedIndices, 0, indices.removedIndices, removeOffset, removeSize);
			}
			
			if (prev != null)
			{
				prev.release();
			}
			prev = scratch;
		}
		
		System.arraycopy(prev.toIndices, 0, indices.toIndices, 0, indices.toIndices.length);
		
		prev.release();
	}

	@Override
	public int convertJointIndex(int jointIndex, int addedJointIndex)
	{
		if (_addedDomains == null)
		{
			for (JointDomainReindexer converter : _converters)
			{
				jointIndex = converter.convertJointIndex(jointIndex, addedJointIndex);
			}
		}
		else
		{
			for (JointDomainReindexer converter : _converters)
			{
				int localAddedJointIndex = 0;
				JointDomainIndexer localAddedDomains = converter._addedDomains;
				if (localAddedDomains != null)
				{
					int card = localAddedDomains.getCardinality();
					localAddedJointIndex = addedJointIndex;
					addedJointIndex /= card;
					localAddedJointIndex -= card * addedJointIndex;
				}
				jointIndex = converter.convertJointIndex(jointIndex, localAddedJointIndex);
			}
		}
		
		return jointIndex;
	}
	
	@Override
	public int convertJointIndex(int jointIndex, int addedJointIndex, AtomicInteger removedJointIndexRef)
	{
		if (_removedDomains == null || removedJointIndexRef == null)
		{
			return convertJointIndex(jointIndex, addedJointIndex);
		}
		
		int removedJointIndex = 0;
			
		for (JointDomainReindexer converter : _converters)
		{
			int localAddedJointIndex = 0;
			if (converter._addedDomains != null)
			{
				int card = converter._addedDomains.getCardinality();
				localAddedJointIndex = addedJointIndex;
				addedJointIndex /= card;
				localAddedJointIndex -= card * addedJointIndex;
			}
			jointIndex = converter.convertJointIndex(jointIndex, localAddedJointIndex, removedJointIndexRef);

			if (converter._removedDomains != null)
			{
				removedJointIndex *= converter._removedDomains.getCardinality();
				removedJointIndex += removedJointIndexRef.get();
			}
		}

		removedJointIndexRef.set(removedJointIndex);
		
		return jointIndex;
	}

	@Override
	public boolean hasFastJointIndexConversion()
	{
		return _hasFastJointIndexConversion;
	}

	@Override
	protected boolean maintainsJointIndexOrder()
	{
		return _maintainsJointIndexOrder;
	}
}
