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

/**
 * Non-conjugate single-variable samplers for Gibbs solver.
 * <p>
 * This package contains interfaces and implementations of single-variable samplers
 * for variables that cannot take advantage of conjugate factor relationships and
 * have to work generally for arbitrary factors. All of the samplers in this package indirectly implement the interface
 * {@link com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IGenericSampler IGenericSampler}, but more specifically
 * implement one of:
 * <ul>
 * <li> {@linkplain com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IDiscreteDirectSampler IDiscreteDirectSampler}
 * <li> {@linkplain com.analog.lyric.dimple.solvers.gibbs.samplers.generic.IMCMCSampler IMCMCSampler}
 * </ul>
 * Some sampler implementations can be configured with options. Currently this includes the classes:
 * <ul>
 * <li>{@linkplain com.analog.lyric.dimple.solvers.gibbs.samplers.generic.MHSampler MHSampler}
 * <li>{@linkplain com.analog.lyric.dimple.solvers.gibbs.samplers.generic.SliceSampler SliceSampler}
 * </ul>
 * When that is the case, the relevant option keys are defined in static public fields within the implementation
 * classes and the sampler can be configured by setting the option on the variable for that
 * sampler instance (or on the graph itself if all variables in the graph will share the same parameters).
 */
@NonNullByDefault
package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;
import org.eclipse.jdt.annotation.NonNullByDefault;

