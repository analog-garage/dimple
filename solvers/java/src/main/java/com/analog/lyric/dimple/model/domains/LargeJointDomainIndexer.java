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

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.Comparators;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.values.Value;

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
	public int undirectedJointIndexFromValues(Value ... values)
	{
		throw noJointIndexing("undirectedJointIndexFromValues");
	}

	@Override
	public <T> T[] undirectedJointIndexToElements(int jointIndex, @Nullable T[] elements)
	{
		throw noJointIndexing("undirectedJointIndexToElements");
	}

	@Override
	public Value[] undirectedJointIndexToValues(int jointIndex, Value[] values)
	{
		throw noJointIndexing("undirectedJointIndexToElements");
	}

	@Override
	public int undirectedJointIndexToElementIndex(int jointIndex, int domainIndex)
	{
		throw noJointIndexing("undirectedJointIndexToElementIndex");
	}

	@Override
	public int[] undirectedJointIndexToIndices(int jointIndex, @Nullable int[] indices)
	{
		throw noJointIndexing("undirectedJointIndexToIndices");
	}

	static DimpleException noJointIndexing(String method)
	{
		return new DimpleException("%s' not supported for very large joint domain cardinality", method);
	}

}
