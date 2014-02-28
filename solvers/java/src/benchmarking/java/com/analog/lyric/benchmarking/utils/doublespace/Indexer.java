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
 * Represents an indexer. An indexer produces values within the cardinality of a
 * dimension, in some order.
 * <p>
 * When iterated, an indexer yields its indexes in order.
 */
public interface Indexer extends Iterable<Integer>
{

	/**
	 * Gets the quantity of indexes produced by the indexer.
	 */
	public int getCardinality();

	/**
	 * Gets the nth index produced by the indexer.
	 * 
	 * @param n
	 *            The zero-based position of the desired index.
	 * @return An index value.
	 */
	public int getNth(int n);

}
