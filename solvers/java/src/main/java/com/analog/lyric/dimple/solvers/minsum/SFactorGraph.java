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

package com.analog.lyric.dimple.solvers.minsum;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.dimple.solvers.minsum.customFactors.CustomXor;
import com.analog.lyric.util.misc.IMapList;


public class SFactorGraph extends SFactorGraphBase
{
	protected double _damping = 0;

	public SFactorGraph(com.analog.lyric.dimple.model.core.FactorGraph factorGraph)
	{
		super(factorGraph);
		setMultithreadingManager(new MultiThreadingManager(getModelObject()));
	}

	@Override
	public ISolverVariable createVariable(com.analog.lyric.dimple.model.variables.VariableBase var)
	{
		if (!var.getDomain().isDiscrete())
			throw new DimpleException("only support discrete variables");
		
		return new SVariable(var);
	}


	@Override
	public ISolverFactor createFactor(Factor factor)
	{
		if (customFactorExists(factor.getFactorFunction().getName()))
		{
			return createCustomFactor(factor);
		}
		else
		{
			STableFactor tf = new STableFactor(factor);
			if (_damping != 0)
				setDampingForTableFunction(tf);
			return tf;
		}
	}
	
	
	@Override
	public boolean customFactorExists(String funcName)
	{
		if (funcName.equals("customXor"))
			return true;
		else
			return false;
	}

	public ISolverFactor createCustomFactor(com.analog.lyric.dimple.model.factors.Factor factor)
	{
		String funcName = factor.getFactorFunction().getName();
		if (funcName.equals("customXor"))
		{
			return new CustomXor(factor);
		}
		else
			throw new DimpleException("Not implemented");
	}

	
	/*
	 * Set the global solver damping parameter.  We have to go through all factor graphs
	 * and update the damping parameter on all existing table functions in that graph.
	 */
	public void setDamping(double damping)
	{
		_damping = damping;
		for (Factor f : _factorGraph.getNonGraphFactors())
		{
			STableFactor tf = (STableFactor)f.getSolver();
			setDampingForTableFunction(tf);
		}
	}
	
	public double getDamping()
	{
		return _damping;
	}

	/*
	 * This method applies the global damping parameter to all of the table function's ports
	 * and all of the variable ports connected to it.  This might cause problems in the future
	 * when we support different damping parameters per edge.
	 */
	protected void setDampingForTableFunction(STableFactor tf)
	{
		Factor factor = tf.getFactor();
		IMapList<INode> nodes = factor.getConnectedNodesFlat();
		
		for (int i = 0, endi = factor.getSiblingCount(); i < endi; i++)
		{
			tf.setDamping(i,_damping);
			VariableBase var = (VariableBase)nodes.getByIndex(i);
			for (int j = 0, endj = var.getSiblingCount(); j < endj; j++)
			{
				SVariable svar = (SVariable)var.getSolver();
				svar.setDamping(j,_damping);
			}
		}

	}

}
