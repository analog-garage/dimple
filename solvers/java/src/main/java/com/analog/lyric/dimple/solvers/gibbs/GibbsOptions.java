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

import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.StringOptionKey;

/**
 * Options for Gibbs solver.
 * <p>
 * Unless otherwise stated options take effect upon initialization of the target object.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class GibbsOptions extends SolverOptions
{
	/**
	 * The number of updates of all of the variables in the graph to perform during burn-in in Gibbs solver.
	 * <p>
	 * The effective updates per burn-in will be the number of variables to be updated in the graph times this number.
	 * <p>
	 * The default value is zero.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey burnInScans =
		new IntegerOptionKey(GibbsOptions.class, "burnInScans", 0, -1, Integer.MAX_VALUE);
	
	/**
	 * The number of samples to generate per restart in Gibbs solver.
	 * <p>
	 * This multiplied by {@link #numRandomRestarts} plus one specifies the number of samples that
	 * will be produced in a call to {@linkplain GibbsSolverGraph#solveOneStep solveOneStep}.
	 * <p>
	 * The default value is one.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey numSamples =
		new IntegerOptionKey(GibbsOptions.class, "numSamples", 1, 1, Integer.MAX_VALUE);
	
	/**
	 * The number of updates of all of the variables in the graph to perform for each sample in Gibbs solver.
	 * <p>
	 * The effective updates per sample will be the number of variables to be updated in the graph times this number.
	 * <p>
	 * The default value is one.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey scansPerSample =
		new IntegerOptionKey(GibbsOptions.class, "scansPerSample", 1, -1, Integer.MAX_VALUE);
	
	/**
	 * The number of times to randomly restart during one round of Gibbs sampling.
	 * <p>
	 * This is the number of additional times to randomly reinitialize the graph, run burn in, and generate
	 * samples during one invocation of {@linkplain GibbsSolverGraph#solveOneStep() solveOneStep}
	 * in the Gibbs solver.
	 * <p>
	 * This number plus one multiplied by the value set for {@link #numSamples} determines the actual number
	 * of samples that will be generated during solve.
	 * <p>
	 * Must be a non-negative integer. The default is zero.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey numRandomRestarts =
		new IntegerOptionKey(GibbsOptions.class, "numRandomRestarts", 0, 0, Integer.MAX_VALUE);
	
	/**
	 * Enables use of a tempering and annealing process in Gibbs solver.
	 * <p>
	 * Defaults to false.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey enableTempering =
		new BooleanOptionKey(GibbsOptions.class, "enableTempering", false);
	
	/**
	 * Specifies the initial temperature for annealing in Gibbs solver.
	 * <p>
	 * Defaults to 1.0.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey initialTemperature =
		new DoubleOptionKey(GibbsOptions.class, "initialTemperature", 1.0, 0.0, Double.MAX_VALUE);
	
	/**
	 * <description>
	 */
	public static final DoubleOptionKey temperingHalfLife =
		new DoubleOptionKey(GibbsOptions.class, "temperingHalfLife", 1, 1.0, Double.MAX_VALUE);
	
	/**
	 * <description>
	 */
	public static final StringOptionKey discreteSampler =
		new StringOptionKey(GibbsOptions.class, "discreteSampler", SDiscreteVariable.DEFAULT_DISCRETE_SAMPLER_NAME);
	
	/**
	 * <description>
	 */
	public static final StringOptionKey realSampler =
		new StringOptionKey(GibbsOptions.class, "realSampler", SRealVariable.DEFAULT_REAL_SAMPLER_NAME);
	
	/**
	 * Specifies whether to save sample values for variables in Gibbs solver.
	 * <p>
	 * Defaults to false.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey saveAllSamples =
		new BooleanOptionKey(GibbsOptions.class, "saveAllSamples", false);
	
	/**
	 * Specifies whether to save sample scores in Gibbs solver.
	 * <p>
	 * If true, then for each sample, the total energy/log-likelihood aka "score" of the graph
	 * will be saved. The saved scores can later be retrieved by {@link GibbsSolverGraph#getAllScores()}.
	 * <p>
	 * Defaults to false.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey saveAllScores =
		new BooleanOptionKey(GibbsOptions.class, "saveAllScores", false);
}
