/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.events.SolverEventSource;
import com.analog.lyric.dimple.model.core.FactorGraphChild;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraphChild;

/**
 * Abstract base implementation of {@link ISolverFactorGraphChild}.
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class SChild<MChild extends FactorGraphChild> extends SolverEventSource implements
	ISolverFactorGraphChild
{
	/*-------
	 * State
	 */

	protected final MChild _model;

	/*--------------
	 * Construction
	 */
	
	protected SChild(MChild child)
	{
		_model = child;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.format("[%s %s]", getClass().getSimpleName(), _model);
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public MChild getModelObject()
    {
    	return _model;
    }
	
	@Override
	public MChild getModelEventSource()
	{
		return _model;
	}
	
	@Override
	public ISolverFactorGraph getRootSolverGraph()
	{
		return getContainingSolverGraph().getRootSolverGraph();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Clears internal state flags and resets messages for edges.
	 */
	@Override
	public void initialize()
	{
		clearFlags();
	}
	
}
