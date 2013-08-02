package com.analog.lyric.dimple.test.model;

import java.util.Arrays;

import com.analog.lyric.dimple.model.DiscreteDomainList;

/**
 * {@link DiscreteDomainListConverter} implementation that permutes the domain order
 * including added and removed domains in the permutation.
 */
public class DiscreteDomainListPermuter extends DiscreteDomainListConverter
{

	/*-------
	 * State
	 */
	
	final DiscreteDomainListPermuter _inverse;
	final int[] _oldToNewIndex;
	
	/*---------------
	 * Construction
	 */
	
	private DiscreteDomainListPermuter(
		DiscreteDomainList fromDomains,
		DiscreteDomainList addedDomains,
		DiscreteDomainList toDomains,
		DiscreteDomainList removedDomains,
		int[] oldToNewIndex,
		DiscreteDomainListPermuter inverse)
	{
		super(fromDomains, addedDomains, toDomains, removedDomains);
		
		if (inverse == null)
		{
			final int fromSize = fromDomains.size();
			final int addedSize = addedDomains == null ? 0 : addedDomains.size();
			final int toSize = toDomains.size();
			final int removedSize = removedDomains == null ? 0 : removedDomains.size();
			
			final int size = fromSize + addedSize;
			
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
					from < fromSize ? fromDomains.getDomainSize(from) : addedDomains.getDomainSize(from - fromSize);
				int toDomainSize =
					to < toSize ? toDomains.getDomainSize(to) : removedDomains.getDomainSize(to - toSize);
				if (fromDomainSize != toDomainSize)
				{
					throw new IllegalArgumentException(
						String.format("'oldToNewIndex' domain size mismatch at index %d", from));
				}
				newToOldIndex[to] = from;
			}
			
			inverse = new DiscreteDomainListPermuter(toDomains, removedDomains,
				fromDomains, addedDomains, newToOldIndex, this);
		}
		
		_inverse = inverse;
		_oldToNewIndex = oldToNewIndex;
	}

	DiscreteDomainListPermuter(
		DiscreteDomainList fromDomains,
		DiscreteDomainList addedDomains,
		DiscreteDomainList toDomains,
		DiscreteDomainList removedDomains,
		int[] oldToNewIndex)
	{
		this(fromDomains, addedDomains, toDomains, removedDomains, oldToNewIndex, null);
	}
	
	/*-------------------------------------
	 * DiscreteDomainListConverter methods
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

	@Override
	public DiscreteDomainListPermuter getInverse()
	{
		return _inverse;
	}

}
