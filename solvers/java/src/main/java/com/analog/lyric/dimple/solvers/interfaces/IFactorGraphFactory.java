package com.analog.lyric.dimple.solvers.interfaces;

import com.analog.lyric.dimple.model.FactorGraph;

public interface IFactorGraphFactory 
{
	public ISolverFactorGraph createFactorGraph(FactorGraph graph) ;
}
