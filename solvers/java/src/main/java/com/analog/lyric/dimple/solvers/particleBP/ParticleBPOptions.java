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

package com.analog.lyric.dimple.solvers.particleBP;

import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.ProposalKernelOptionKey;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.DoubleRangeOptionKey;
import com.analog.lyric.options.IntegerOptionKey;

/**
 * Options for the Particle BP solver.
 * <p>
 * Unless otherwise noted, all options take effect upon initialization.
 * <P>
 * @since 0.07
 * @author Christopher Barber
 */
public class ParticleBPOptions extends BPOptions
{
	/**
	 * Affects particle BP {@linkplain ParticleBPReal solver variable} objects.
	 * <p>
	 * Is evaluated both when solver variable is constructed and in {@link ISolverNode#initialize}.
	 * <p>
	 * Defaults to 1.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey numParticles =
		new IntegerOptionKey(ParticleBPOptions.class, "numParticles", 1, 1, Integer.MAX_VALUE);
	
	/**
	 * Specifies number of iterations between resampling in particle BP solver.
	 * <p>
	 * Defaults to 1.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey iterationsBetweenResampling =
		new IntegerOptionKey(ParticleBPOptions.class, "iterationsBetweenResampling", 1, 1, Integer.MAX_VALUE);
	
	/**
	 * Affects particle BP {@linkplain ParticleBPReal solver variable} objects.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey resamplingUpdatesPerParticle =
		new IntegerOptionKey(ParticleBPOptions.class, "resamplingUpdatesPerParticle", 1, 1, Integer.MAX_VALUE);
	
	/**
	 * Specifies proposal kernel for real variables in particle BP solver.
	 * <p>
	 * Affects particle BP {@linkplain ParticleBPReal solver variable} objects.
	 * <p>
	 * The proposal kernel instance can be configured by setting options on specific to that
	 * kernel. See documentation for the specific kernel for details.
	 * <p>
	 * Defaults to {@link NormalProposalKernel}.
	 * <p>
	 * @since 0.07
	 */
	public static final ProposalKernelOptionKey proposalKernel =
		new ProposalKernelOptionKey(ParticleBPOptions.class, "proposalKernel", NormalProposalKernel.class);

	/**
	 * Specifies the domain of initial particle values for real variables in particle BP solver.
	 * <p>
	 * Affects particle BP {@linkplain ParticleBPReal solver variable} objects.
	 * <p>
	 * The range should be a subset of the variable's domain range. If it is not, then this option is ignored
	 * for that variable.
	 * <p>
	 * Defaults to [-infinity, infinity].
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleRangeOptionKey initialParticleRange =
		new DoubleRangeOptionKey(ParticleBPOptions.class, "initialParticleRange");
	
	/**
	 * Enables use of a tempering and annealing process in particle BP solver.
	 * <p>
	 * Defaults to false.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey enableAnnealing =
		new BooleanOptionKey(ParticleBPOptions.class, "enableAnnealing", false);
	
	/**
	 * Specifies the temperature decay rate for annealing in particle BP solver.
	 * <p>
	 * Specifies the rate at which the temperature will be lowered during simulated annealing
	 * in particle BP solver in terms of the number of iterations it will take for the temperature to
	 * be lowered by half.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey annealingHalfLife =
		new DoubleOptionKey(ParticleBPOptions.class, "annealingHalfLife", 1, 1.0, Double.MAX_VALUE);
	
	/**
	 * Specifies the initial temperature for annealing in particle BP solver.
	 * <p>
	 * Defaults to 1.0.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey initialTemperature =
		new DoubleOptionKey(ParticleBPOptions.class, "initialTemperature", 1.0, 0.0, Double.MAX_VALUE);
	
}
