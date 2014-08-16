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

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@link JointDomainReindexer} that supports the splitting/joining of adjacent subdomains.
 */
public final class JointDomainIndexJoiner extends JointDomainReindexer
{
	/*-------
	 * State
	 */
	
	private final int _hashCode;
	private final int _offset;
	private final int _length;
	private final JointDomainIndexJoiner _inverse;
	private final boolean _supportsJointIndexing;
	
	/*--------------
	 * Construction
	 */
	
	public static JointDomainIndexJoiner createJoiner(JointDomainIndexer fromDomains, int offset, int length)
	{
		return new JointDomainIndexJoiner(fromDomains, offset, length);
	}
	
	public static JointDomainIndexJoiner createSplitter(JointDomainIndexer fromDomains, int offset)
	{
		return new JointDomainIndexJoiner(fromDomains, offset);
	}
	
	private JointDomainIndexJoiner(
		JointDomainIndexer fromDomains,
		JointDomainIndexer toDomains,
		int offset,
		int length,
		JointDomainIndexJoiner inverse)
	{
		super(fromDomains, null, toDomains, null);
		_hashCode = computeHashCode();
		_offset = offset;
		_length = length;
		_inverse = inverse;
		_supportsJointIndexing = fromDomains.supportsJointIndexing() & toDomains.supportsJointIndexing();
	}
	
	private JointDomainIndexJoiner(JointDomainIndexer fromDomains, int offset)
	{
		super(fromDomains, null, makeToDomains(fromDomains, offset), null);
		_hashCode = computeHashCode();
		_offset = offset;
		_length = _fromDomains.size() - _toDomains.size() - 1;
		_inverse = new JointDomainIndexJoiner(_toDomains, fromDomains, offset, -_length, this);
		_supportsJointIndexing = fromDomains.supportsJointIndexing() & _toDomains.supportsJointIndexing();
	}
		
	private JointDomainIndexJoiner(JointDomainIndexer fromDomains, int offset, int length)
	{
		super(fromDomains, null, makeToDomains(fromDomains, offset, length), null);
		_hashCode = computeHashCode();
		_offset = offset;
		_length = length;
		_inverse = new JointDomainIndexJoiner(_toDomains, fromDomains, offset, -length, this);
		_supportsJointIndexing = fromDomains.supportsJointIndexing() & _toDomains.supportsJointIndexing();
	}
	
	private static JointDomainIndexer makeToDomains(JointDomainIndexer fromDomains, int offset, int length)
	{
		final int fromSize = fromDomains.size();
		final int toSize = fromSize + 1 - length;
		
		final DiscreteDomain[] toDomains = new DiscreteDomain[toSize];
		
		
		int from = 0, to = 0;
		for (; from < offset; ++from, ++to)
		{
			toDomains[to] = fromDomains.get(from);
		}

		final DiscreteDomain[] joinedDomains = new DiscreteDomain[length];
		for (int j = 0; j < length; ++j, ++from)
		{
			joinedDomains[j] = fromDomains.get(from);
		}
		toDomains[to++] = DiscreteDomain.joint(joinedDomains);

		for (; from < fromSize; ++from, ++to)
		{
			toDomains[to] = fromDomains.get(from);
		}
		
		return JointDomainIndexer.create(toDomains);
	}

	private static JointDomainIndexer makeToDomains(JointDomainIndexer fromDomains, int offset)
	{
		JointDomainIndexer joinedDomainList = ((JointDiscreteDomain<?>)fromDomains.get(offset)).getDomainIndexer();
		final int length = joinedDomainList.size();
		final int fromSize = fromDomains.size();
		final int toSize = fromSize - 1 + length;
		
		final DiscreteDomain[] toDomains = new DiscreteDomain[toSize];
		
		
		int from = 0, to = 0;
		for (; from < offset; ++from, ++to)
		{
			toDomains[to] = fromDomains.get(from);
		}

		for (int j = 0; j < length; ++j, ++to)
		{
			toDomains[to] = joinedDomainList.get(j);
		}
		++from;

		for (; from < fromSize; ++from, ++to)
		{
			toDomains[to] = fromDomains.get(from);
		}
		
		return JointDomainIndexer.create(toDomains);
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
		
		if (other instanceof JointDomainIndexJoiner)
		{
			JointDomainIndexJoiner that = (JointDomainIndexJoiner)other;
			return _fromDomains.equals(that._fromDomains) && _toDomains.equals(that._toDomains);
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
	public JointDomainIndexJoiner getInverse()
	{
		return _inverse;
	}
	
	@Override
	public double[] convertDenseEnergies(double[] oldEnergies)
	{
		return oldEnergies.clone();
	}
	
	@Override
	public double[] convertDenseWeights(double[] oldWeights)
	{
		return oldWeights.clone();
	}
	
	@Override
	public void convertIndices(Indices indices)
	{
		if (_supportsJointIndexing)
		{
			// When joint indexing is supported, conversion is simply mapping from the domain indices to
			// the joint index and back again for the new domains.
			int index = _fromDomains.jointIndexFromIndices(indices.fromIndices);
			_toDomains.jointIndexToIndices(index, indices.toIndices);
		}
		else
		{
			// Otherwise direct subindex copying is required.
			if (_length > 0)
			{
				System.arraycopy(indices.fromIndices, 0, indices.toIndices, 0, _length);
			}
			
			int fromi, toi;
			
			if (_length > 0)
			{
				// Joining
				JointDiscreteDomain<?> joinedDomains = (JointDiscreteDomain<?>)_toDomains.get(_offset);
				System.arraycopy(indices.fromIndices, _offset, indices.joinedIndices, 0, _length);
				indices.toIndices[_offset] = joinedDomains.getIndexFromIndices(indices.joinedIndices);
				
				fromi = _offset + _length;
				toi = _offset + 1;
			}
			else
			{
				// Splitting
				JointDiscreteDomain<?> joinedDomains = (JointDiscreteDomain<?>)_fromDomains.get(_offset);
				joinedDomains.getElementIndices(indices.fromIndices[_offset], indices.joinedIndices);
				System.arraycopy(indices.joinedIndices, 0, indices.toIndices, _offset, -_length);
				
				fromi = _offset + 1;
				toi = _offset - _length;
			}
			
			System.arraycopy(indices.fromIndices, fromi, indices.toIndices, toi, indices.toIndices.length - toi);
		}
	}

	@Override
	public int convertJointIndex(int oldJointIndex, int addedJointIndex, @Nullable AtomicInteger removedJointIndex)
	{
		return oldJointIndex;
	}
	
	@Override
	public int convertJointIndex(int oldJointIndex, int addedJointIndex)
	{
		return oldJointIndex;
	}
	
	@Override
	public int[][] convertSparseIndices(
		int[][] oldSparseIndices, int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		return oldSparseIndices.clone();
	}

	@Override
	public int[] convertSparseToJointIndex(int[] oldSparseToJointIndex)
	{
		return oldSparseToJointIndex.clone();
	}

	@Override
	public double[] convertSparseWeights(double[] oldSparseWeights,
		int[] oldSparseIndexToJointIndex, int[] sparseIndexToJointIndex)
	{
		return oldSparseWeights.clone();
	}

	@Override
	public boolean hasFastJointIndexConversion()
	{
		return true;
	}

	@Override
	protected boolean maintainsJointIndexOrder()
	{
		return true;
	}
	
	
}
