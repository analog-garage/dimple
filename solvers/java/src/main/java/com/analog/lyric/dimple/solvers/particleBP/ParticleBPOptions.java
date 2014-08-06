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

import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.LongOptionKey;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class ParticleBPOptions extends SolverOptions
{
	/**
	 * <description>
	 */
	public static final DoubleOptionKey beta =
		new DoubleOptionKey(ParticleBPOptions.class, "beta", 1.0);
	
	/**
	 * <description>
	 */
	public static final BooleanOptionKey enableTempering =
		new BooleanOptionKey(ParticleBPOptions.class, "enableTempering", false);
	
	/**
	 * <description>
	 */
	public static final DoubleOptionKey initialTemperature =
		new DoubleOptionKey(ParticleBPOptions.class, "initialTemperature", 1.0);
	
	/**
	 * <description>
	 */
	public static final IntegerOptionKey iterationsBetweenResamping =
		new IntegerOptionKey(ParticleBPOptions.class, "iterationsBetweenResamping", 1);
	
	/**
	 * <description>
	 */
	public static final IntegerOptionKey numParticles =
		new IntegerOptionKey(ParticleBPOptions.class, "numParticles", 1);
	
	/**
	 * <description>
	 */
	public static final LongOptionKey randomSeed = new LongOptionKey(ParticleBPOptions.class, "randomSeed");

	/**
	 * <description>
	 */
	public static final IntegerOptionKey resamplingUpdatesPerParticle =
		new IntegerOptionKey(ParticleBPOptions.class, "resamplingUpdatesPerParticle", 1);
	
	/**
	 * <description>
	 */
	public static final DoubleOptionKey temperingHalfLife =
		new DoubleOptionKey(ParticleBPOptions.class, "temperingHalfLife", 1);
	
}
