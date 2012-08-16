/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class DummyFactorGraph extends SFactorGraphBase
{
	public DummyFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph) 
	{
		super(factorGraph);
	}

	public ISolverFactor createCustomFactor(com.analog.lyric.dimple.model.Factor factor)  
	{
		String funcName = factor.getModelerFunctionName();
		if (funcName.equals("dummyCustomFactor"))
		{    		
			return new DummyCustomFactor(factor);
		}
		else
			throw new DimpleException("Not implemented");
	}

	public ISolverFactor createFactor(com.analog.lyric.dimple.model.Factor factor)  
	{
		if (customFactorExists(factor.getFactorFunction().getName()))
			return createCustomFactor(factor);
		else
			return new DummyTableFactor(factor);
	}

	
	public ISolverVariable createVariable(VariableBase var)  
	{
		return new DummyVariable(var);
	}

	@Override
	public boolean customFactorExists(String funcName) 
	{
		if (funcName.equals("dummyCustomFactor"))
			return true;
		else
			return false;	
	}

	@Override
	public void initialize() 
	{

	}
}
