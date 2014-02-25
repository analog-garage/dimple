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

package com.analog.lyric.benchmarking.utils.doublespace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

class ListIndexer implements Indexer
{
	private final List<Integer> _indexes;

	public ListIndexer(int... indexes)
	{
		_indexes = Ints.asList(indexes);
	}

	public ListIndexer(Iterable<Integer> indexes)
	{
		_indexes = new ArrayList<Integer>();
		Iterables.addAll(_indexes, indexes);
	}

	@Override
	public int getCardinality()
	{
		return _indexes.size();
	}

	@Override
	public int getNth(int n)
	{
		return _indexes.get(n);
	}

	@Override
	public Iterator<Integer> iterator()
	{
		return _indexes.iterator();
	}

}
