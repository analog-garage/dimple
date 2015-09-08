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

package com.analog.lyric.dimple.options;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.solvers.core.proposalKernels.CircularNormalProposalKernel;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.JointMHSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.MHSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.SliceSampler;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeOptions;
import com.analog.lyric.dimple.solvers.lp.LPOptions;
import com.analog.lyric.dimple.solvers.minsum.MinSumOptions;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPOptions;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductOptions;
import com.analog.lyric.options.OptionRegistry;

/**
 * Registry of option keys for known dimple options.
 * <p>
 * This is used primarily for looking up option keys by name from dynamic language
 * front-ends (e.g. MATLAB). Java users should instead directly use the key objects.
 * <p>
 * Includes options from the following classes:
 * <dl>
 * <dt>General options</dt>
 * <dd>
 * <ul>
 * <li>{@link DimpleOptions}
 * <li>{@link SolverOptions}
 * </ul>
 * </dd>
 * <dt>Solver-specific options</dt>
 * <dd>
 * <ul>
 * <li>{@link BPOptions}
 * <li>{@link GibbsOptions}
 * <li>{@link JunctionTreeOptions}
 * <li>{@link LPOptions}
 * <li>{@link MinSumOptions}
 * <li>{@link ParticleBPOptions}
 * <li>{@link SumProductOptions}
 * </ul>
 * </dd>
 * <dt>Proposal kernels options</dt>
 * <dd>
 * <ul>
 * <li>{@link NormalProposalKernel}
 * <li>{@link CircularNormalProposalKernel}
 * </ul>
 * </dd>
 * <dt>Sampler options</dt>
 * <dd>
 * <ul>
 * <li>{@link MHSampler}
 * <li>{@link SliceSampler}
 * </ul>
 * </dd>
 * </dl>
 * 
 * Additional options can be added to the registry using {@linkplain OptionRegistry#addFromClasses addFromClasses}.
 * <p>
 * Instances of this class should be obtained from {@link DimpleEnvironment#optionRegistry()}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public final class DimpleOptionRegistry extends OptionRegistry
{
	// This is deprecated to prevent external users from calling this, but will not be removed.
	/**
	 * @deprecated Instead use {@link DimpleEnvironment#optionRegistry()}.
	 */
	@Deprecated
	public DimpleOptionRegistry()
	{
		super(true);
		
		// NOTE: if you add to this list, make sure to add the same classes to the documentation above.
		
		addFromClasses(
			DimpleOptions.class,
			SolverOptions.class,
		
			// Solver option classes
			BPOptions.class,
			GibbsOptions.class,
			JunctionTreeOptions.class,
			LPOptions.class,
			MinSumOptions.class,
			ParticleBPOptions.class,
			SumProductOptions.class,
		
			// Proposal kernels
			NormalProposalKernel.class,
			CircularNormalProposalKernel.class,
			
			// Samplers
			MHSampler.class,
			JointMHSampler.class,
			SliceSampler.class
			);
	}
}
