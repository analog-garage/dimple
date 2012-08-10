package com.analog.lyric.dimple.solvers.particleBP;

import com.analog.lyric.dimple.solvers.core.SolverBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public class Solver extends SolverBase
{

	public ISolverFactorGraph createFactorGraph(com.analog.lyric.dimple.model.FactorGraph factorGraph) 
	{
		return new SFactorGraph(factorGraph, null);
	}
	
}
