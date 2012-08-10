package com.analog.lyric.dimple.test.dummySolver;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.solvers.core.SolverBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public class DummySolver extends SolverBase
{

	public ISolverFactorGraph createFactorGraph(FactorGraph factorGraph) 
	{
		return new DummyFactorGraph(factorGraph);
	}
	
}
