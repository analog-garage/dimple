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

package com.analog.lyric.dimple.solvers.junctiontree;

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.proxy.ProxyDiscreteSolverVariable;
import com.analog.lyric.dimple.solvers.interfaces.IDiscreteSolverVariable;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class JunctionTreeDiscreteSolverVariable
	extends ProxyDiscreteSolverVariable<IDiscreteSolverVariable>
	implements IJunctionTreeSolverVariable<IDiscreteSolverVariable>
{
	/*-------
	 * State
	 */
	
	private final JunctionTreeSolverGraphBase<?> _root;
	
	/*--------------
	 * Construction
	 */

	/**
	 * @param modelVariable
	 */
	protected JunctionTreeDiscreteSolverVariable(Discrete modelVariable, JunctionTreeSolverGraphBase<?> root)
	{
		super(modelVariable);
		_root = root;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public JunctionTreeSolverGraphBase<?> getRootGraph()
	{
		return _root;
	}

	/*-------------------------
	 * ProxySolverNode methods
	 */
	
	@Override
	public @Nullable IDiscreteSolverVariable getDelegate()
	{
		return (IDiscreteSolverVariable) _root.getDelegateSolverVariable(this);
	}

	@Override
	public int getValueIndex()
	{
		if (getModelObject().hasFixedValue())	// If there's a fixed value set, use that instead of the belief
			return getModelObject().getFixedValueIndex();
					
		double[] belief = (double[])getBelief();
		int numValues = requireNonNull(belief).length;
		double maxBelief = Double.NEGATIVE_INFINITY;
		int maxBeliefIndex = -1;
		for (int i = 0; i < numValues; i++)
		{
			double b = belief[i];
			if (b > maxBelief)
			{
				maxBelief = b;
				maxBeliefIndex = i;
			}
		}
		return maxBeliefIndex;
	}
}
