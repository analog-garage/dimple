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
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.exceptions.DimpleException;
import org.eclipse.jdt.annotation.Nullable;

public final class ChainedJointDomainReindexer extends JointDomainReindexer
{
	/*-------
	 * State
	 */
	
	private int _hashCode;
	private final JointDomainReindexer[] _converters;
	private volatile @Nullable ChainedJointDomainReindexer _inverse;
	private final boolean _hasFastJointIndexConversion;
	private final boolean _maintainsJointIndexOrder;
	
	/*--------------
	 * Construction
	 */
	
	private ChainedJointDomainReindexer(
		JointDomainIndexer fromDomains,
		@Nullable JointDomainIndexer addedDomains,
		JointDomainIndexer toDomains,
		@Nullable JointDomainIndexer removedDomains,
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
		
	static JointDomainReindexer create(JointDomainReindexer firstConverter, JointDomainReindexer ... moreConverters)
	{
		if (moreConverters.length == 0)
		{
			return firstConverter;
		}

		JointDomainReindexer[] converters = new JointDomainReindexer[moreConverters.length + 1];
		converters[0] = firstConverter;
		System.arraycopy(moreConverters, 0, converters,  1, moreConverters.length);
		
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
	public boolean equals(@Nullable Object other)
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

			if (scratch.toIndices[0] < 0)
			{
				Arrays.fill(indices.toIndices, -1);
				return;
			}
		}
		
		if (prev != null)
		{
			System.arraycopy(prev.toIndices, 0, indices.toIndices, 0, indices.toIndices.length);

			prev.release();
		}
	}

	@Override
	public int convertJointIndex(int jointIndex, int addedJointIndex)
	{
		if (_addedDomains == null)
		{
			for (JointDomainReindexer converter : _converters)
			{
				jointIndex = converter.convertJointIndex(jointIndex, addedJointIndex);
				if (jointIndex < 0)
				{
					break;
				}
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
				if (jointIndex < 0)
				{
					break;
				}
			}
		}
		
		return jointIndex;
	}
	
	@Override
	public int convertJointIndex(int jointIndex, int addedJointIndex, @Nullable AtomicInteger removedJointIndexRef)
	{
		if (_removedDomains == null || removedJointIndexRef == null)
		{
			return convertJointIndex(jointIndex, addedJointIndex);
		}
		
		int removedJointIndex = 0;
			
		for (JointDomainReindexer converter : _converters)
		{
			int localAddedJointIndex = 0;
			final JointDomainIndexer addedDomains = converter._addedDomains;
			if (addedDomains != null)
			{
				int card = addedDomains.getCardinality();
				localAddedJointIndex = addedJointIndex;
				addedJointIndex /= card;
				localAddedJointIndex -= card * addedJointIndex;
			}
			jointIndex = converter.convertJointIndex(jointIndex, localAddedJointIndex, removedJointIndexRef);

			final JointDomainIndexer removedDomains = converter._removedDomains;
			if (removedDomains != null)
			{
				removedJointIndex *= removedDomains.getCardinality();
				removedJointIndex += removedJointIndexRef.get();
			}
			
			if (jointIndex < 0)
			{
				break;
			}
		}

		removedJointIndexRef.set(removedJointIndex);
		
		return jointIndex;
	}

	@Override
	public int[] convertSparseToJointIndex(int[] oldSparseToJointIndex)
	{
		int[] sparseToJoint = oldSparseToJointIndex;
		for (JointDomainReindexer converter : _converters)
		{
			sparseToJoint = converter.convertSparseToJointIndex(sparseToJoint);
		}
		return sparseToJoint;
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
