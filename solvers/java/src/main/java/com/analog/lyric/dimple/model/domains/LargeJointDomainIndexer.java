package com.analog.lyric.dimple.model.domains;

import java.util.Comparator;

import com.analog.lyric.collect.Comparators;
import com.analog.lyric.dimple.exceptions.DimpleException;

public class LargeJointDomainIndexer extends JointDomainIndexer
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	/*--------------
	 * Construction
	 */
	
	LargeJointDomainIndexer(DiscreteDomain[] domains)
	{
		super(domains);
	}
	
	LargeJointDomainIndexer(int hashCode, DiscreteDomain[] domains)
	{
		super(hashCode, domains);
	}
	
	/*------------------
	 * Directed methods
	 */
	
	@Override
	public Comparator<int[]> getIndicesComparator()
	{
		return Comparators.reverseLexicalIntArray();
	}
	
	@Override
	public boolean supportsJointIndexing()
	{
		return false;
	}
	
	@Override
	public boolean supportsOutputIndexing()
	{
		return false;
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

	static DimpleException noJointIndexing(String method)
	{
		return new DimpleException("%s' not supported for very large joint domain cardinality", method);
	}

}
