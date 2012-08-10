package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.solvers.core.SolverBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;


public class Solver extends SolverBase 
{
	protected SFactorGraph.Arguments _factorGraphConfig = new SFactorGraph.Arguments();
	
	public Solver() {}												// Default constructor
	public Solver(int burnInUpdates, int updatesPerSample)			// Constructor without tempering
	{
		_factorGraphConfig.burnInUpdates = burnInUpdates;
		_factorGraphConfig.updatesPerSample = updatesPerSample;
	}
	public Solver(int burnInUpdates, int updatesPerSample, double initialTemperature, double temperingHalfLifeInSamples)
	{
		_factorGraphConfig.burnInUpdates = burnInUpdates;
		_factorGraphConfig.updatesPerSample = updatesPerSample;
		_factorGraphConfig.initialTemperature = initialTemperature;
		_factorGraphConfig.temperingHalfLifeInSamples = temperingHalfLifeInSamples;
		_factorGraphConfig.temper = true;
	}

	public ISolverFactorGraph createFactorGraph(com.analog.lyric.dimple.model.FactorGraph graph)  
	{
		return new SFactorGraph(graph, _factorGraphConfig);
	}

}
