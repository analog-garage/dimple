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

/**
 * Methods for creating various types of indexers.
 */
public class IndexerFactory
{

	/**
	 * Creates an indexer that yields a single index.
	 * 
	 * @param index
	 *            The index to yield.
	 */
	public static Indexer just(int index)
	{
		return new StrideIndexer(index);
	}

	/**
	 * Creates an indexer that yields the indexes described by an array.
	 * 
	 * @param indexes
	 *            The indexes to yield.
	 */
	public static Indexer list(int... indexes)
	{
		return new ListIndexer(indexes);
	}

	/**
	 * Creates an indexer that yields the indexes described by an Iterable.
	 * 
	 * @param indexes
	 *            The indexes to yield.
	 */
	public static Indexer list(Iterable<Integer> indexes)
	{
		return new ListIndexer(indexes);
	}

	/**
	 * Creates an indexer that yields the indexes described by a starting index
	 * and finishing index, which are both inclusive. If finish is greater than
	 * start, then yields from start up to finish. If finish is less than start,
	 * then yields from start down to finish. If finish is equal to start, then
	 * yields a single index.
	 * 
	 * @param start
	 *            The first index to yield.
	 * @param finish
	 *            The last index to yield.
	 */
	public static Indexer range(int start, int finish)
	{
		return new StrideIndexer(start, finish);
	}

	/**
	 * Creates an indexer that yields the indexes described by a starting and
	 * finishing index and a stride. The provided stride value must have the
	 * same sign as the difference between finish and start.
	 * 
	 * @param start
	 *            The first index to yield.
	 * @param stride
	 *            The difference between successive indexes to yield.
	 * @param finish
	 *            A value beyond which no further indexes are yielded.
	 */
	public static Indexer range(int start, int stride, int finish)
	{
		return new StrideIndexer(start, stride, finish);
	}

	/**
	 * Creates an indexer that yields the indexes described by other indexers,
	 * in sequence.
	 * 
	 * @param indexers
	 *            The indexers that produce the values to yield.
	 */
	public static Indexer sequence(Indexer... indexers)
	{
		return new SequenceIndexer(indexers);
	}

}
