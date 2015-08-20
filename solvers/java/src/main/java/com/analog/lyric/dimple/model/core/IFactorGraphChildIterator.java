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

package com.analog.lyric.dimple.model.core;

import java.util.Iterator;

/**
 * An iterator over contents of a {@link FactorGraph} and its subgraphs.
 * @param <T>
 * @since 0.08
 * @author Christopher Barber
 */
public interface IFactorGraphChildIterator<T> extends Iterator<T>
{
	/**
	 * The nesting depth of the last object returned by the iterator.
	 * @since 0.08
	 */
	public abstract int lastDepth();
	
	/**
	 * Returns the maximum depth below the {@link #root} to include in the iteration.
	 * @since 0.08
	 */
	public abstract int maxNestingDepth();

	/**
	 * Resets iterator back to initial state on construction.
	 * 
	 * @since 0.08
	 */
	public abstract void reset();

	/**
	 * Returns the root graph visited by this iterator.
	 * @since 0.08
	 */
	public abstract FactorGraph root();

}
