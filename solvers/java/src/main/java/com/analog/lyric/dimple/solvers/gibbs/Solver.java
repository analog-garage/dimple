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

package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.solvers.core.SolverBase;
import com.analog.lyric.util.misc.Nullable;


public class Solver extends SolverBase<SFactorGraph>
{
	@Deprecated
	protected final @Nullable SFactorGraph.Arguments _factorGraphConfig;
	
	public Solver()
	{
		this(null);
	}
	
	@Deprecated
	public Solver(int burnInUpdates, int updatesPerSample)			// Constructor without tempering
	{
		this(new SFactorGraph.Arguments());
		_factorGraphConfig.burnInUpdates = burnInUpdates;
		_factorGraphConfig.updatesPerSample = updatesPerSample;
	}
	
	@Deprecated
	public Solver(int burnInUpdates, int updatesPerSample, double initialTemperature, double temperingHalfLifeInSamples)
	{
		this(new SFactorGraph.Arguments());
		_factorGraphConfig.burnInUpdates = burnInUpdates;
		_factorGraphConfig.updatesPerSample = updatesPerSample;
		_factorGraphConfig.initialTemperature = initialTemperature;
		_factorGraphConfig.temperingHalfLifeInSamples = temperingHalfLifeInSamples;
		_factorGraphConfig.temper = true;
	}

	@Deprecated
	public Solver(@Nullable SFactorGraph.Arguments config)
	{
		_factorGraphConfig = config;
	}
	
	@Override
	public SFactorGraph createFactorGraph(FactorGraph graph)
	{
		return new SFactorGraph(graph, _factorGraphConfig);
	}

}
