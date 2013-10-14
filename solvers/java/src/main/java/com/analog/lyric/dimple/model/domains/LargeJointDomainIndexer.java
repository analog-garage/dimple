package com.analog.lyric.dimple.model.domains;

import java.util.Arrays;
import java.util.BitSet;

import com.analog.lyric.dimple.exceptions.DimpleException;

public final class LargeJointDomainIndexer extends JointDomainIndexer
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final BitSet _outputSet;
	private final int[] _inputIndices;
	private final int[] _outputIndices;
	
	/*--------------
	 * Construction
	 */
	
	LargeJointDomainIndexer(DiscreteDomain[] domains)
	{
		super(domains);
		_outputSet = null;
		_inputIndices = null;
		_outputIndices = null;
	}
	
	LargeJointDomainIndexer(BitSet outputs, DiscreteDomain[] domains)
	{
		super(computeHashCode(outputs, domains), domains);
		_outputSet = outputs;
		
		final int nDomains = domains.length;
		final int nOutputs = outputs.cardinality();
		final int nInputs = nDomains - nOutputs;
		
		if (outputs.length() > nDomains)
		{
			throw new DimpleException("Illegal output set for domain list");
		}
		
		final int[] inputIndices = new int[nInputs];
		final int[] outputIndices = new int[nOutputs];

		int curInput = 0, curOutput = 0;

		for (int i = 0; i < nDomains; ++i)
		{
			if (outputs.get(i))
			{
				outputIndices[curOutput] = i;
				++curOutput;
			}
			else
			{
				inputIndices[curInput] = i;
				++curInput;
			}
		}
		
		_inputIndices = inputIndices;
		_outputIndices = outputIndices;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public boolean equals(Object that)
	{
		if (this == that)
		{
			return true;
		}
		
		if (that instanceof LargeJointDomainIndexer)
		{
			LargeJointDomainIndexer thatDiscrete = (LargeJointDomainIndexer)that;
			return Arrays.equals(_domains, thatDiscrete._domains)
				&& _outputSet.equals(thatDiscrete._outputSet);
		}
		
		return false;
	}
	
	/*------------------
	 * Directed methods
	 */
	
	@Override
	public int[] getInputDomainIndices()
	{
		return _inputIndices == null ? null : _inputIndices.clone();
	}
	
	@Override
	public int getInputDomainIndex(int i)
	{
		return _inputIndices == null ? super.getInputDomainIndex(i) : _inputIndices[i];
	}
	
	@Override
	public BitSet getInputSet()
	{
		BitSet set = _outputSet;

		if (set != null)
		{
			set.flip(0, size());
		}
		
		return set;
	}
	
	@Override
	public int getInputSize()
	{
		return _inputIndices == null ? 0 : _inputIndices.length;
	}
	
	@Override
	public int getOutputDomainIndex(int i)
	{
		return _outputIndices == null ? super.getOutputDomainIndex(i) : _outputIndices[i];
	}
	
	@Override
	public int[] getOutputDomainIndices()
	{
		return _outputIndices == null ? null : _outputIndices.clone();
	}
	
	@Override
	public BitSet getOutputSet()
	{
		return _outputSet == null ? null : (BitSet) _outputSet.clone();
	}

	@Override
	public boolean isDirected()
	{
		return _outputSet != null;
	}
	
	/*---------------------------------------
	 * Unsupported joint index based methods
	 */
	
	@Override
	public int getCardinality()
	{
		throw noJointIndexing("getCardinality");
	}

	@Override
	public int getOutputCardinality()
	{
		throw noJointIndexing("getOutputCardinality");
	}

	@Override
	public int getStride(int i)
	{
		throw noJointIndexing("getStride");
	}

	@Override
	public int getUndirectedStride(int i)
	{
		throw noJointIndexing("getUndirectedStride");
	}

	@Override
	public int undirectedJointIndexFromElements(Object... elements)
	{
		throw noJointIndexing("undirectedJointIndexFromElements");
	}

	@Override
	public int undirectedJointIndexFromIndices(int... indices)
	{
		throw noJointIndexing("undirectedJointIndexFromIndices");
	}

	@Override
	public <T> T[] undirectedJointIndexToElements(int jointIndex, T[] elements)
	{
		throw noJointIndexing("undirectedJointIndexToElements");
	}

	@Override
	public int undirectedJointIndexToElementIndex(int jointIndex, int domainIndex)
	{
		throw noJointIndexing("undirectedJointIndexToElementIndex");
	}

	@Override
	public int[] undirectedJointIndexToIndices(int jointIndex, int[] indices)
	{
		throw noJointIndexing("undirectedJointIndexToIndices");
	}

	private static DimpleException noJointIndexing(String method)
	{
		return new DimpleException("Method '%s' not supported for very large joint domain sizes");
	}
}
