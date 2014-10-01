/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.options.IOptionHolder;

/**
 * @deprecated Will be removed in future release. Use {@link GibbsSolverGraph} instead.
 */
@Deprecated
public class SFactorGraph extends GibbsSolverGraph
{
	/**
	 * @deprecated set corresponding {@link GibbsOptions} using {@linkplain IOptionHolder#setOption setOption}
	 * on graph instead of using this class to configure Gibbs solver.
	 */
	@Deprecated
	public static class Arguments
	{
		public int numSamples = -1;
		public int updatesPerSample = -1;
		public int scansPerSample = Integer.MIN_VALUE;
		public int burnInUpdates = -1;
		public @Nullable Boolean temper = null;
		public double initialTemperature = Double.NaN;
		public double temperingHalfLifeInSamples = Double.NaN;
	}

	protected SFactorGraph(FactorGraph factorGraph, @Nullable SFactorGraph.Arguments arguments)
	{
		super(factorGraph);
		if (arguments != null)
		{
			if (arguments.numSamples > 0)
			{
				setNumSamples(arguments.numSamples);
			}
			if (arguments.updatesPerSample > 0)
			{
				setUpdatesPerSample(arguments.updatesPerSample);
			}
			if (arguments.scansPerSample > 0)
			{
				setScansPerSample(arguments.scansPerSample);
			}
			if (arguments.burnInUpdates >=0)
			{
				setBurnInUpdates(arguments.burnInUpdates);
			}
			if (arguments.temper != null)
			{
				setTempering(requireNonNull(arguments.temper));
			}
			if (!Double.isNaN(arguments.initialTemperature))
			{
				setOption(GibbsOptions.initialTemperature, arguments.initialTemperature);
			}
			if (!Double.isNaN(arguments.temperingHalfLifeInSamples))
			{
				setOption(GibbsOptions.annealingHalfLife, arguments.temperingHalfLifeInSamples);
			}
		}
	}
}
