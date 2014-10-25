/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
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

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;

public interface ISolverFactorGibbs extends ISolverFactor, ISolverNodeGibbs
{
	@Override
	public double getPotential();
	
	/**
	 * @since 0.07
	 */
	public int getTopologicalOrder();

	/**
	 * @since 0.07
	 */
	public void setTopologicalOrder(int order);
	
	/**
	 * Updates neighboring dependent variable sample values based on the current
	 * value. Should only be invoked if factor is deterministic and is directed
	 * from variable with given {@code variableIndex}.
	 * 
	 * @param variableIndex
	 * @param oldValue
	 */
	public void updateNeighborVariableValue(int variableIndex, Value oldValue);
	public void updateNeighborVariableValuesNow(@Nullable Collection<IndexedValue> oldValues);
	public void updateEdgeMessage(int portIndex);
}
