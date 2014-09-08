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

import java.util.EnumMap;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.util.misc.Internal;

/**
 * Stores a collection of cost estimates keyed by CostType. Used to compare the normal factor update
 * algorithm with the optimized update factor algorithm.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public final class Costs extends EnumMap<CostType, Double>
{
	private static final long serialVersionUID = 1;

	/**
	 * Creates a new, empty, collection of costs.
	 * 
	 * @since 0.07
	 */
	public Costs()
	{
		super(CostType.class);
	}

	/**
	 * Adds a collection of costs to this one.
	 * 
	 * @param otherCosts
	 * @since 0.07
	 */
	public void add(Costs otherCosts)
	{
		for (CostType costType : otherCosts.keySet())
		{
			Double thisCost = get(costType);
			Double otherCost = otherCosts.get(costType);
			put(costType, thisCost + otherCost);
		}
	}

	@Override
	public Double get(@Nullable Object key)
	{
		Double result = super.get(key);
		return result == null ? 0.0 : result;
	}
}
