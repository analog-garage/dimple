package com.analog.lyric.dimple.model.domains;

import java.util.concurrent.atomic.AtomicInteger;

public final class JointDomainIndexJoiner extends JointDomainReindexer
{
	/*-------
	 * State
	 */
	
	private final int _hashCode;
	private final JointDomainIndexJoiner _inverse;
	
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
		JointDomainIndexJoiner inverse)
	{
		super(fromDomains, null, toDomains, null);
		_hashCode = computeHashCode();
		_inverse = inverse;
	}
	
	private JointDomainIndexJoiner(JointDomainIndexer fromDomains, int offset)
	{
		super(fromDomains, null, makeToDomains(fromDomains, offset), null);
		_hashCode = computeHashCode();
		_inverse = new JointDomainIndexJoiner(_toDomains, fromDomains, this);
	}
		
	private JointDomainIndexJoiner(JointDomainIndexer fromDomains, int offset, int length)
	{
		super(fromDomains, null, makeToDomains(fromDomains, offset, length), null);
		_hashCode = computeHashCode();
		_inverse = new JointDomainIndexJoiner(_toDomains, fromDomains, this);
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
	public boolean equals(Object other)
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
