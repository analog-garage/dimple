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

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.WeakIntHashMap;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.util.misc.Internal;

/**
 * Registry of known {@link FactorGraph}s associated with a {@link DimpleEnvironment}.
 * <p>
 * Obtain instance from {@linkplain DimpleEnvironment#factorGraphs factorGraphs()} accessor
 * on {@link DimpleEnvironment} instance.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@ThreadSafe
public class FactorGraphRegistry
{
	/*-------
	 * State
	 */
	
	@GuardedBy("this")
	private long _nextId = 0;
	
	@GuardedBy("this")
	private final WeakIntHashMap<FactorGraph> _graphById = new WeakIntHashMap<>();

	/*--------------
	 * Construction
	 */
	
	@Internal
	public FactorGraphRegistry()
	{
	}
	
	/*---------
	 * Methods
	 */
	
	/**
	 * Returns factor graph with given graph id
	 * @param graphId is the {@linkplain FactorGraph#getGraphId graph id} of the graph to return.
	 * @return the graph with given id or null if it doesn't exist.
	 * @since 0.08
	 */
	public @Nullable FactorGraph getGraphWithId(int graphId)
	{
		synchronized(this)
		{
			return _graphById.get(graphId);
		}
	}
	
	int registerIdForFactorGraph(FactorGraph fg)
	{
		synchronized(_graphById)
		{
			long longid = ++_nextId;
			int id = (int)longid;
			
			if (longid >= 0x100000000L)
			{
				// Id wrapped around. Need to find next available slot.
				// We don't need to support more than 32 bits for the graph ids, but
				// it is theoretically possible to wrap around, so in that unlikely case,
				// we need to check to make sure one is not currently in use.
				while (id != 0 && _graphById.containsKey(id))
				{
					id = (int)++_nextId;
				}
			}
			
			_graphById.put(id, fg);
			
			return id;
		}
	}
	
}
