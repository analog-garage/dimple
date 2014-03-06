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
import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.factorfunctions.core.CustomFactorFunctionWrapper;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
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
		FactorFunction factorFunction = factor.getFactorFunction().getContainedFactorFunction();	// In case it's wrapped
		String factorName = factorFunction.getName();
		boolean noFF = factorFunction instanceof CustomFactorFunctionWrapper;

		
		// First see if any custom factor should be created
		if (factorFunction instanceof Xor)
			return new CustomXor(factor);
		else if (noFF && (factorName.equals("CustomXor") || factorName.equals("customXor")))		// For backward compatibility
			return new CustomXor(factor);
		else			// No custom factor exists, so create a generic one
		{
			STableFactor tf = new STableFactor(factor);
			if (_damping != 0)
				setDampingForTableFactor(tf);
			return tf;
		}
	}
	
	// For backward compatibility only; preferable to use "Xor" factor function, which can
	// be evaluated for scoring or other purposes, but still uses the custom factor.  This may be removed at some point.
	// This should return true only for custom factors that do not have a corresponding FactorFunction of the same name
	@Override
	public boolean customFactorExists(String funcName)
	{
		if (funcName.equals("CustomXor") || funcName.equals("customXor"))
			return true;
		else
			return false;
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
			setDampingForTableFactor(tf);
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
	protected void setDampingForTableFactor(STableFactor tf)
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
