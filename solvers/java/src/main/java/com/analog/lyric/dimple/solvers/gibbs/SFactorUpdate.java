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

package com.analog.lyric.dimple.solvers.gibbs;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.IKeyed;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;

import net.jcip.annotations.NotThreadSafe;

/**
 * @since 0.05
 */
@NotThreadSafe
public final class SFactorUpdate implements IKeyed<ISolverFactorGibbs>
{
	/*-------
	 * State
	 */
	
	private final ISolverFactorGibbs _sfactor;
	private @Nullable Set<IndexedValue> _updates;
	private final int _incrementalUpdateThreshold;

	static enum DeterministicOrder implements Comparator<SFactorUpdate>
	{
		INSTANCE;
		
		@Override
		@NonNullByDefault(false)
		public int compare(SFactorUpdate u1, SFactorUpdate u2)
		{
			return Integer.compare(u1._sfactor.getTopologicalOrder(), u2._sfactor.getTopologicalOrder());
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	SFactorUpdate(ISolverFactorGibbs sfactor)
	{
		_sfactor = sfactor;
		Factor factor = sfactor.getModelObject();
		int nEdges = factor.getSiblingCount();
		_incrementalUpdateThreshold = factor.getFactorFunction().updateDeterministicLimit(nEdges);
		_updates = _incrementalUpdateThreshold > 0 ? new HashSet<IndexedValue>() : null;
	}
	
	/*----------------
	 * IKeyed methods
	 */
	
	@Override
	public final ISolverFactorGibbs getKey()
	{
		return _sfactor;
	}

	/*-----------------------
	 * SFactorUpdate methods
	 */
	
	/**
	 * 
	 * @param argumentIndex index into factor's argument list. This may be different than the
	 * sibling number if the factor has constants.
	 * @param oldValue
	 */
	void addVariableUpdate(int argumentIndex, Value oldValue)
	{
		final Set<IndexedValue> updates = _updates;
		if (updates != null)
		{
			updates.add(new IndexedValue(argumentIndex, oldValue));
			if (updates.size() > _incrementalUpdateThreshold)
			{
				// Once we have exceeded the threshold, there is no point in
				// saving entries.
				_updates = null;
			}
		}
	}
	
	void performUpdate()
	{
		_sfactor.updateNeighborVariableValuesNow(_updates);
	}
	
	ISolverFactorGibbs sfactor()
	{
		return _sfactor;
	}
}
