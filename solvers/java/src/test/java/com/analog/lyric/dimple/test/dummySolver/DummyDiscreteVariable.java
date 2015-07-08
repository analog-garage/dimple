/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.dummySolver;

import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.SDiscreteVariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;


public class DummyDiscreteVariable extends SDiscreteVariableBase
{
	protected Discrete _varDiscrete;

	public DummyDiscreteVariable(Discrete var, ISolverFactorGraph parent)
	{
		super(var, parent);
		_varDiscrete = var;
	}

	public Variable getVariable()
	{
		return _model;
	}

	@Override
	protected void doUpdateEdge(int outPortNum)
	{
	}


	@Override
	protected void doUpdate()
	{
	}

	@Override
	public double[] getBelief()
	{
		return new double[getDomain().size()];
	}

}
