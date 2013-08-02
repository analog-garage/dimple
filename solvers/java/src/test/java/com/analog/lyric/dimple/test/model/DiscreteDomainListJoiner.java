package com.analog.lyric.dimple.test.model;

import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.DiscreteDomainList;
import com.analog.lyric.dimple.model.JointDiscreteDomain;

public final class DiscreteDomainListJoiner extends DiscreteDomainListConverter
{
	/*-------
	 * State
	 */
	
	private final int _hashCode;
	private final DiscreteDomainListJoiner _inverse;
	
	/*--------------
	 * Construction
	 */
	
	public static DiscreteDomainListJoiner createJoiner(DiscreteDomainList fromDomains, int offset, int length)
	{
		return new DiscreteDomainListJoiner(fromDomains, offset, length);
	}
	
	public static DiscreteDomainListJoiner createSplitter(DiscreteDomainList fromDomains, int offset)
	{
		return new DiscreteDomainListJoiner(fromDomains, offset);
	}
	
	private DiscreteDomainListJoiner(
		DiscreteDomainList fromDomains,
		DiscreteDomainList toDomains,
		DiscreteDomainListJoiner inverse)
	{
		super(fromDomains, null, toDomains, null);
		_hashCode = computeHashCode();
		_inverse = inverse;
	}
	
	private DiscreteDomainListJoiner(DiscreteDomainList fromDomains, int offset)
	{
		super(fromDomains, null, makeToDomains(fromDomains, offset), null);
		_hashCode = computeHashCode();
		_inverse = new DiscreteDomainListJoiner(_toDomains, fromDomains, this);
	}
		
	private DiscreteDomainListJoiner(DiscreteDomainList fromDomains, int offset, int length)
	{
		super(fromDomains, null, makeToDomains(fromDomains, offset, length), null);
		_hashCode = computeHashCode();
		_inverse = new DiscreteDomainListJoiner(_toDomains, fromDomains, this);
	}
	
	private static DiscreteDomainList makeToDomains(DiscreteDomainList fromDomains, int offset, int length)
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
		
		return DiscreteDomainList.create(toDomains);
	}

	private static DiscreteDomainList makeToDomains(DiscreteDomainList fromDomains, int offset)
	{
		DiscreteDomainList joinedDomainList = ((JointDiscreteDomain)fromDomains.get(offset)).getDomainList();
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
		
		return DiscreteDomainList.create(toDomains);
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
		
		if (other instanceof DiscreteDomainListJoiner)
		{
			DiscreteDomainListJoiner that = (DiscreteDomainListJoiner)other;
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
