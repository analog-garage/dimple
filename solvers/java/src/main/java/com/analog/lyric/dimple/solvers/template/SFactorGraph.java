package com.analog.lyric.dimple.solvers.template;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public class SFactorGraph  extends SFactorGraphBase
{

	public SFactorGraph(FactorGraph fg) 
	{
		super(fg);
	}

	@Override
	public ISolverVariable createVariable(VariableBase var) 
	{
		return new SVariable(var);
	}

	@Override
	public ISolverFactor createFactor(Factor factor) 
	{
		return new STableFactor(factor);
	}

}
