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

package com.analog.lyric.dimple.solvers.optimizedupdate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.util.misc.Internal;
import com.google.common.primitives.Ints;

/**
 * Produces the sequence of marginalization and output steps necessary to apply the optimized
 * update algorithm.
 * 
 * @param <T> The type that represents a factor table within the optimized update tree.
 * @since 0.06
 * @author jking
 */
@Internal
final class TreeWalker<T>
{
	private final IFactorTable _factorTable;

	private final int[] _mapping;

	/**
	 * Constructs an instance that walks a given factor table.
	 * 
	 * @since 0.06
	 */
	public TreeWalker(IFactorTable factorTable)
	{
		_factorTable = factorTable;
		/*
		 * The remainder of this method sorts the factor table dimensions by domain size,
		 * producing an array that contains a mapping from old dimension index to new dimension
		 * index.
		 */
		final int dimensions = factorTable.getDimensions();
		final int[] domainSizes = new int[dimensions];
		final JointDomainIndexer domainIndexer = factorTable.getDomainIndexer();
		List<Integer> indices = new ArrayList<Integer>(dimensions);
		for (int i = 0; i < dimensions; i++)
		{
			indices.add(i);
			domainSizes[i] = domainIndexer.getDomainSize(i);
		}
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@NonNullByDefault(false)
			@Override
			public int compare(Integer i, Integer j)
			{
				// Decreasing order
				return 0 - Integer.compare(domainSizes[i], domainSizes[j]);
			}
		};
		Collections.sort(indices, comparator);
		_mapping = Ints.toArray(indices);
	}

	/**
	 * Produce the optimized update sequence.
	 * 
	 * @param treeBuilder Receives calls describing the sequence.
	 * @since 0.06
	 */
	public void accept(ITreeBuilder<T> treeBuilder)
	{
		T rootT = treeBuilder.createRootT(_factorTable);
		int order = _factorTable.getDomainIndexer().size();
		loop(0, 1, rootT, order, treeBuilder, _mapping);
	}

	private void loop(final int p,
		int step,
		final T f,
		final int order,
		final ITreeBuilder<T> treeBuilder,
		final int[] entries)
	{
		final int left = p;
		final int right = p + step;
		loop2(left, right, step * 2, f, order, treeBuilder, entries);
		if (right < order)
		{
			loop2(right, left, step * 2, f, order, treeBuilder, entries);
		}
	}

	private void loop2(final int x,
		final int y,
		final int step,
		T f,
		final int order,
		ITreeBuilder<T> treeBuilder,
		int[] entries)
	{
		final int offset = x > y ? 1 : 0;
		for (int i = 0; x + i * step < order; i++)
		{
			int portNum = _mapping[x + i * step];
			int rawLocalDimension = i + offset;
			int localDimension = entries[i + offset];
			f = treeBuilder.buildMarginalize(f, portNum, localDimension);
			// Remove the entry at rawLocalDimension from entries, and decrease all entries
			// greater than that entry by 1:
			int[] entries2 = new int[entries.length - 1];
			int k = 0;
			for (int j = 0; j < entries.length; j++)
			{
				if (j != rawLocalDimension)
				{
					int v = entries[j];
					if (v > entries[rawLocalDimension])
					{
						v -= 1;
					}
					entries2[k] = v;
					k += 1;
				}
			}
			entries = entries2;
		}
		if (y + step < order)
		{
			loop(y, step, f, order, treeBuilder, entries);
		}
		else
		{
			int portNum = _mapping[y];
			treeBuilder.buildOutput(f, portNum);
		}
	}
}