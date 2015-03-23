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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public class DummyCustomFactor extends SFactorBase
{
	
	public DummyCustomFactor(Factor factor, ISolverFactorGraph parent)
	{
		super(factor, parent);
	}

	@Override
	public void doUpdateEdge(int outPortNum)
	{
		
	}

	@SuppressWarnings("null")
	public @Nullable Object getDefaultMessage(Port port)
	{
		Discrete var = (Discrete)port.getSiblingNode();
		DummyDiscreteVariable v = (DummyDiscreteVariable)var.getSolver();
		return v.getDefaultMessage(port);
	}

	@Override
	public void initialize()
	{
	}
}
