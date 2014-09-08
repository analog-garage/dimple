/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.template;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * The Solver FactorGraph is responsible for creating variables and factors.
 * 
 * @author shershey
 *
 */
public class SFactorGraph  extends SFactorGraphBase
{

	/**
	 * The constructor attaches to the model object.
	 * 
	 * @param fg
	 */
	public SFactorGraph(FactorGraph fg) 
	{
		super(fg);
	}

	/**
	 * createVariable creates a solver object for a model variable object.
	 */
	@Override
	public ISolverVariable createVariable(Variable var) 
	{
		return new SVariable(var);
	}

	/**
	 * createFactor creates a solver object for a model factor object.
	 */
	@Override
	public ISolverFactor createFactor(Factor factor) 
	{
		return new STableFactor(factor);
	}

	/*
	 * 
	 */
	protected void doUpdateEdge(int edge)
	{
	}

}
