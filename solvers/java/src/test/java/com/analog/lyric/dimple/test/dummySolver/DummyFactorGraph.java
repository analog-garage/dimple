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
