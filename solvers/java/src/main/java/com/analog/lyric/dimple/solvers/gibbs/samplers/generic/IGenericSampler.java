/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;

import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.options.IOptionConfigurable;

/**
 * Base interface for non-conjugate single-variable sampler for Gibbs solver.
 */
public interface IGenericSampler extends ISampler, IOptionConfigurable
{
	/**
	 * Initializes sampler state
	 * <p>
	 * @param variableDomain is the domain of the variable to be sampled.
	 */
	public void initialize(Domain variableDomain);
	
	/**
	 * Initializes sampler state
	 * <p>
	 * Implementations should invoke {@link #initialize} with the domain
	 * of {@code var} and {@link #configureFromOptions} using {@code var}
	 * as the option holder.
	 * <p>
	 * @param var is the variable for which samples will be generated.
	 * @since 0.07
	 */
	public void initializeFromVariable(ISolverVariable var);
}
