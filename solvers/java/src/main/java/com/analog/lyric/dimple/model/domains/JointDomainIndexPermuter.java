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
import java.util.concurrent.atomic.AtomicInteger;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link JointDomainReindexer} implementation that permutes the domain order
 * including added and removed domains in the permutation.
 */
@ThreadSafe
public final class JointDomainIndexPermuter extends JointDomainReindexer
{

	/*-------
	 * State
	 */
	
	private static final int APPEND = 1;
	private static final int PREPEND = 2;
	private static final int REMOVE_FROM_FRONT = 3;
	private static final int REMOVE_FROM_BACK = 4;
	
	final private int[] _oldToNewIndex;
	final private int _hashCode;
	final private JointDomainIndexPermuter _inverse;
	final private boolean _maintainsOrder;
	final private int _jointIndexConversionType;
	
	/*---------------
	 * Construction
	 */
	
	private JointDomainIndexPermuter(
		JointDomainIndexer fromDomains,
		@Nullable JointDomainIndexer addedDomains,
		JointDomainIndexer toDomains,
		@Nullable JointDomainIndexer removedDomains,
		int[] oldToNewIndex,
		@Nullable JointDomainIndexPermuter inverse)
	{
		super(fromDomains, addedDomains, toDomains, removedDomains);
		
		final int fromSize = fromDomains.size();
		final int addedSize = addedDomains == null ? 0 : addedDomains.size();
		final int toSize = toDomains.size();
		final int removedSize = removedDomains == null ? 0 : removedDomains.size();
		
		final int size = fromSize + addedSize;
		
		if (inverse == null)
		{
			
			if (size != toSize + removedSize)
			{
				throw new IllegalArgumentException(
					"Combined size of 'fromDomains' and 'addedDomains' does not equal " +
					"'toDomains' and 'removedDomains'");
			}
			
			if (size != oldToNewIndex.length)
			{
				throw new IllegalArgumentException("Length of 'oldToNewIndex' does not match domain sizes.");
			}
			
			final int[] newToOldIndex = new int[size];
			Arrays.fill(newToOldIndex, -1);
			
			for (int from = 0; from < size; ++from)
			{
				final int to = oldToNewIndex[from];
				if (to < 0 || to >= size)
				{
					throw new IllegalArgumentException(
						String.format("'oldToNewIndex' contains out-of-range value %d", to));
				}
				if (newToOldIndex[to] >= 0)
				{
					throw new IllegalArgumentException(
						String.format("'oldToNewIndex' contains two entries mapping to %d", to));
				}
				int fromDomainSize =
					from < fromSize ? fromDomains.getDomainSize(from) :
						requireNonNull(addedDomains).getDomainSize(from - fromSize);
				int toDomainSize =
					to < toSize ? toDomains.getDomainSize(to) :
						requireNonNull(removedDomains).getDomainSize(to - toSize);
				if (fromDomainSize != toDomainSize)
				{
					throw new IllegalArgumentException(
						String.format("'oldToNewIndex' domain size mismatch at index %d", from));
				}
				newToOldIndex[to] = from;
			}
			
			inverse = new JointDomainIndexPermuter(toDomains, removedDomains,
				fromDomains, addedDomains, newToOldIndex, this);
		}

		_hashCode = computeHashCode();
		_oldToNewIndex = oldToNewIndex;
		_inverse = inverse;
		
		// In theory, we could perform these optimization on directed domains with non-canonical
		// domain order, but it is not worth the trouble at this time.
		
		boolean maintainsOrder = false;
		int jointIndexType = 0;

		if (_fromDomains.hasCanonicalDomainOrder() && _toDomains.hasCanonicalDomainOrder())
		{
			maintainsOrderCheck:
			do
			{
				// Order is only maintained if domains are only removed from front of
				// list, added to the end of the list, and relative order of domains is maintained.
				for (int i = 0; i < removedSize; ++i)
				{
					if (oldToNewIndex[i] != (toSize + i))
					{
						break maintainsOrderCheck;
					}
				}
				for (int i = removedSize; i < size; ++i)
				{
					if (oldToNewIndex[i] != i - removedSize)
					{
						break maintainsOrderCheck;
					}
				}
				maintainsOrder = true;
			} while (false);
		
			if (maintainsOrder)
			{
				if (_addedDomains != null && _removedDomains == null)
				{
					jointIndexType = APPEND;
				}
				else if (_addedDomains == null && _removedDomains != null)
				{
					jointIndexType = REMOVE_FROM_FRONT;
				}
			}
			else
			{
				if (_addedDomains != null && _removedDomains == null)
				{
					jointIndexType = PREPEND;
					for (int i = 0; i < fromSize; ++i)
					{
						if (oldToNewIndex[i] != addedSize + i)
						{
							jointIndexType = 0;
							break;
						}
					}
					for (int i = 0; i < addedSize; ++i)
					{
						if (oldToNewIndex[fromSize + i] != i)
						{
							jointIndexType = 0;
							break;
						}
					}
				}
				else if (_addedDomains == null && _removedDomains != null)
				{
					jointIndexType = REMOVE_FROM_BACK;
					for (int i = 0; i < size; ++i)
					{
						if (i != oldToNewIndex[i])
						{
							jointIndexType = 0;
							break;
						}
					}
				}
			}
		}
		
		_maintainsOrder = maintainsOrder;
		_jointIndexConversionType = jointIndexType;
	}

	JointDomainIndexPermuter(
		JointDomainIndexer fromDomains,
		@Nullable JointDomainIndexer addedDomains,
		JointDomainIndexer toDomains,
		@Nullable JointDomainIndexer removedDomains,
		int[] oldToNewIndex)
	{
		this(fromDomains, addedDomains, toDomains, removedDomains, oldToNewIndex, null);
	}
	
	@Override
	protected int computeHashCode()
	{
		return super.computeHashCode() * 19 + Arrays.hashCode(_oldToNewIndex);
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
		
		if (other instanceof JointDomainIndexPermuter && super.equals(other))
		{
			return Arrays.equals(_oldToNewIndex, ((JointDomainIndexPermuter)other)._oldToNewIndex);
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return _hashCode;
	}
	
	/*-------------------------------------
	 * JointDomainReindexer methods
	 */
	
	@Override
	public void convertIndices(Indices indices)
	{
		assert(indices.converter == this);
		
		final int[] oldToNew = _oldToNewIndex;
		final int fromSize = _fromDomains.size();
		final int toSize = _toDomains.size();
		
		final int[] fromIndices = indices.fromIndices;
		final int[] toIndices = indices.toIndices;
		final int[] addedIndices = indices.addedIndices;
		final int[] removedIndices = indices.removedIndices;
		
		for (int from = fromIndices.length; --from >=0; )
		{
			final int index = fromIndices[from];
			final int to = oldToNew[from];
			if (to < toSize)
			{
				toIndices[to] = index;
			}
			else
			{
				removedIndices[to-toSize] = index;
			}
		}
		for (int added = addedIndices.length; --added >=0; )
		{
			final int index = addedIndices[added];
			final int to = oldToNew[added+fromSize];
			if (to < toSize)
			{
				toIndices[to] = index;
			}
			else
			{
				// This case is unusual, since there is no point in moving
				// a domain from added list immediately to the removed list.
				removedIndices[to] = index;
			}
		}
	}

	@SuppressWarnings("null")
	@Override
	public int convertJointIndex(int jointIndex, int addedJointIndex)
	{
		switch (_jointIndexConversionType)
		{
		case PREPEND:
			return addedJointIndex + _addedDomains.getCardinality() * jointIndex;
			
		case APPEND:
			return jointIndex + _fromDomains.getCardinality() * addedJointIndex;
			
		case REMOVE_FROM_FRONT:
			return jointIndex / _removedDomains.getCardinality();
			
		case REMOVE_FROM_BACK:
			return jointIndex % _toDomains.getCardinality();
			
			// TODO:
			// insert domains at position k:
			//
			//   oldJointIndex - oldJointIndex/kCardinality +
			//   kCardinality * addedJointIndex +
			//   kCardinality * addedCardinality * oldJointIndex/kCardinality
			//
			// remove domains at position k
			
		default:
			return super.convertJointIndex(jointIndex, addedJointIndex);
		}
	}
	
	@SuppressWarnings("null")
	@Override
	public int convertJointIndex(int jointIndex, int addedJointIndex, @Nullable AtomicInteger removedJointIndexRef)
	{
		switch (_jointIndexConversionType)
		{
		case PREPEND:
			return addedJointIndex + _addedDomains.getCardinality() * jointIndex;
			
		case APPEND:
			return jointIndex + _fromDomains.getCardinality() * addedJointIndex;
			
		case REMOVE_FROM_FRONT:
			if (removedJointIndexRef == null)
			{
				return jointIndex / _removedDomains.getCardinality();
			}
			else
			{
				final int removedCardinality =  _removedDomains.getCardinality();
				final int newJointIndex = jointIndex / removedCardinality;
				removedJointIndexRef.set(jointIndex - newJointIndex * removedCardinality);
				return newJointIndex;
			}
			
		case REMOVE_FROM_BACK:
			if (removedJointIndexRef == null)
			{
				return jointIndex % _toDomains.getCardinality();
			}
			else
			{
				final int toCardinality =  _toDomains.getCardinality();
				final int removedJointIndex = jointIndex / toCardinality;
				removedJointIndexRef.set(removedJointIndex);
				return jointIndex - removedJointIndex * toCardinality;
			}
			
		default:
			return super.convertJointIndex(jointIndex, addedJointIndex, removedJointIndexRef);
		}
	}
	
	@Override
	public JointDomainIndexPermuter getInverse()
	{
		return _inverse;
	}

	@Override
	public boolean hasFastJointIndexConversion()
	{
		return _jointIndexConversionType != 0;
	}

	@Override
	protected boolean maintainsJointIndexOrder()
	{
		return _maintainsOrder;
	}
}
