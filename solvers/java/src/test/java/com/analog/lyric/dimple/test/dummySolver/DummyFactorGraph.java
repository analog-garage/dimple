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

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.SchedulerOptionKey;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class DummyFactorGraph extends SFactorGraphBase<ISolverFactor,ISolverVariable,ISolverEdgeState>
{
	public DummyFactorGraph(FactorGraph factorGraph)
	{
		this(factorGraph, null);
	}
	
	public DummyFactorGraph(FactorGraph factorGraph, @Nullable ISolverFactorGraph parent)
	{
		super(factorGraph, parent);
	}

	public ISolverFactor createCustomFactor(Factor factor)
	{
		String funcName = factor.getModelerFunctionName();
		if (funcName.equals("dummyCustomFactor"))
		{
			return new DummyCustomFactor(factor, this);
		}
		else
			throw new DimpleException("Not implemented");
	}

	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		if (customFactorExists(factor.getFactorFunction().getName()))
			return createCustomFactor(factor);
		else
			return new DummyTableFactor(factor, this);
	}

	@Override
	public ISolverFactorGraph createSubgraph(FactorGraph subgraph)
	{
		return new DummyFactorGraph(subgraph, this);
	}
	
	@Override
	public ISolverVariable createVariable(Variable var)
	{
		if (var instanceof Discrete)
		{
			return new DummyDiscreteVariable((Discrete)var, this);
		}
		else if (var instanceof Real)
		{
			return new DummyRealVariable((Real)var, this);
		}
		
		throw DimpleException.unsupported("DummyFactorGraph.createVariable with variable not Real or Discrete");
	}

	@Override
	public boolean customFactorExists(String funcName)
	{
		if (funcName.equals("dummyCustomFactor"))
			return true;
		else
			return false;
	}

	/**
	 * {@inheritDoc}
	 * @return {@code null}
	 */
	@Override
	public @Nullable SchedulerOptionKey getSchedulerKey()
	{
		return null;
	}
	
	@Override
	public void setMultithreadingManager(@Nullable MultiThreadingManager manager)
	{
		super.setMultithreadingManager(manager);
	}
	
	/*
	 * 
	 */
	@Override
	protected void doUpdateEdge(int edge)
	{
	}

	@Override
	protected String getSolverName()
	{
		return "Dummy";
	}

}
