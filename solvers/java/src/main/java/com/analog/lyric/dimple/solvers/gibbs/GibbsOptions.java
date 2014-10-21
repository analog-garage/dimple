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
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.CDFSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.GenericSamplerOptionKey;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.SliceSampler;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.IntegerOptionKey;

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
	
	/**
	 * Specifies which sampler to use for discrete variables in Gibbs solver.
	 * <p>
	 * @since 0.07
	 */
	public static final GenericSamplerOptionKey discreteSampler =
		new GenericSamplerOptionKey(GibbsOptions.class, "discreteSampler", CDFSampler.class);
	
	/**
	 * Specifies which sampler to use for real variables in Gibbs solver.
	 * <p>
	 * @since 0.07
	 */
	public static final GenericSamplerOptionKey realSampler =
		new GenericSamplerOptionKey(GibbsOptions.class, "realSampler", SliceSampler.class);
	
	/**
	 * Determines whether conjugate sampling is automatically used when possible.
	 * <p>
	 * Note that if a specific sampler is set on the solver or model instance of a variable, that
	 * sampler will be used regardless of this setting.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey enableAutomaticConjugateSampling =
		new BooleanOptionKey(GibbsOptions.class, "enableAutomaticConjugateSampling", true);
	
	/**
	 * Specifies whether to compute belief moments for RealJoint variables in Gibbs solver.
	 * <p>
	 * If true, the belief moments are computed for each sample on-the-fly (without saving all samples)
	 * The computed moments can later be retrieved by {@link GibbsRealJoint#getSampleMean()} and
	 * {@link GibbsRealJoint#getSampleCovariance()}.  (Note that this option applies to RealJoint
	 * and Complex variables.  Real variables always compute similar statistics, and do not
	 * have a corresponding option to enable them.)
	 * 
	 * <p>
	 * Defaults to false.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey computeRealJointBeliefMoments =
		new BooleanOptionKey(GibbsOptions.class, "computeRealJointBeliefMoments", false);

	/**
	 * Enables use of a tempering and annealing process in Gibbs solver.
	 * <p>
	 * Defaults to false.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey enableAnnealing =
		new BooleanOptionKey(GibbsOptions.class, "enableAnnealing", false);
	
	/**
	 * Specifies the temperature decay rate for annealing in Gibbs solver.
	 * <p>
	 * Specifies the rate at which the temperature will be lowered during simulated annealing
	 * in Gibbs solver in terms of the number of samples it will take for the temperature to
	 * be lowered by half.
	 * <p>
	 * Defaults to 1.0.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey annealingHalfLife =
		new DoubleOptionKey(GibbsOptions.class, "annealingHalfLife", 1, 1.0, Double.MAX_VALUE);
	
	/**
	 * Specifies the initial temperature for annealing in Gibbs solver.
	 * <p>
	 * Defaults to 1.0.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey initialTemperature =
		new DoubleOptionKey(GibbsOptions.class, "initialTemperature", 1.0, 0.0, Double.MAX_VALUE);
}
