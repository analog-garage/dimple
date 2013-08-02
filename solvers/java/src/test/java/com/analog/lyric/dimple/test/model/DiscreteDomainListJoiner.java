package com.analog.lyric.dimple.test.model;

import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.analog.lyric.dimple.model.JointDiscreteDomain;

public class DiscreteDomainListJoiner extends DiscreteDomainListConverter
{
	/*-------
	 * State
	 */
	
	private final DiscreteDomainListJoiner _inverse;
	
	/*--------------
	 * Construction
	 */
	
	public static DiscreteDomainListJoiner createJoiner(DiscreteDomainList fromDomains, int offset, int length)
	{
		return new DiscreteDomainListJoiner(fromDomains, offset, length, null);
	}
	
	public static DiscreteDomainListJoiner createSplitter(DiscreteDomainList fromDomains, int offset)
	{
		return new DiscreteDomainListJoiner(fromDomains, offset, -1, null);
	}
	
	private DiscreteDomainListJoiner(
		DiscreteDomainList fromDomains,
		int offset, int length,
		DiscreteDomainListJoiner inverse
		)
	{
		super(fromDomains, null, makeToDomains(fromDomains, offset, length), null);
		if (inverse == null)
		{
			inverse = new DiscreteDomainListJoiner(_toDomains, offset, -length, this);
		}
		_inverse = inverse;
	}
	
	private static DiscreteDomainList makeToDomains(DiscreteDomainList fromDomains, int offset, int length)
	{
		final boolean split = length < 0 ;
		DiscreteDomainList joinedDomainList = null;
		
		if (split)
		{
			joinedDomainList = ((JointDiscreteDomain)fromDomains.get(offset)).getDomainList();
			length = joinedDomainList.size();
		}
		
		final int fromSize = fromDomains.size();
		final int toSize = split ? fromSize - 1 + length : fromSize + 1 - length;
		
		final DiscreteDomain[] toDomains = new DiscreteDomain[toSize];
		
		
		int from = 0, to = 0;
		for (; from < offset; ++from, ++to)
		{
			toDomains[to] = fromDomains.get(from);
		}

		if (split)
		{
			for (int j = 0; j < length; ++j, ++to)
			{
				toDomains[to] = joinedDomainList.get(j);
			}
			++from;
		}
		else
		{
			final DiscreteDomain[] joinedDomains = new DiscreteDomain[length];
			for (int j = 0; j < length; ++j, ++from)
			{
				joinedDomains[j] = fromDomains.get(from);
			}
			toDomains[to++] = DiscreteDomain.joint(joinedDomains);
		}

		for (; from < fromSize; ++from, ++to)
		{
			toDomains[to] = fromDomains.get(from);
		}
		
		return DiscreteDomainList.create(toDomains);
	}

	/*-------------------------------------
	 * DiscreteDomainListConverter methods
	 */
	
	@Override
	public DiscreteDomainListJoiner getInverse()
	{
		return _inverse;
	}
	
	@Override
	public double[] convertDenseWeights(double[] oldWeights)
	{
		return oldWeights.clone();
	}
	
	@Override
	public void convertIndices(Indices indices)
	{
		int index = _fromDomains.jointIndexFromIndices(indices.fromIndices);
		_toDomains.jointIndexToIndices(index, indices.toIndices);
	}

	@Override
	public int convertJointIndex(int oldJointIndex, int addedJointIndex, AtomicInteger removedJointIndex)
	{
		return oldJointIndex;
	}
	
	@Override
	public int convertJointIndex(int oldJointIndex, int addedJointIndex)
	{
		return oldJointIndex;
	}
}
