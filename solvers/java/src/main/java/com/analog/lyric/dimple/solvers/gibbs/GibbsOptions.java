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
import com.analog.lyric.options.LongOptionKey;
import com.analog.lyric.options.StringOptionKey;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class GibbsOptions extends SolverOptions
{
	/**
	 * <description>
	 */
	public static final IntegerOptionKey numSamples =
		new IntegerOptionKey(GibbsOptions.class, "numSamples", 1);
	
	/**
	 * <description>
	 */
	public static final IntegerOptionKey updatePerSample =
		new IntegerOptionKey(GibbsOptions.class, "updatePerSample", -1);
	
	/**
	 * <description>
	 */
	public static final IntegerOptionKey burnInUpdates =
		new IntegerOptionKey(GibbsOptions.class, "burnInUpdates", 0);
	
	/**
	 * <description>
	 */
	public static final IntegerOptionKey scansPerSample =
		new IntegerOptionKey(GibbsOptions.class, "scansPerSample", 1);
	
	/**
	 * <description>
	 */
	public static final IntegerOptionKey burnInScans =
		new IntegerOptionKey(GibbsOptions.class, "burnInScans", -1);
	
	/**
	 * <description>
	 */
	public static final IntegerOptionKey numRandomRestarts =
		new IntegerOptionKey(GibbsOptions.class, "numRandomRestarts", 0);
	
	/**
	 * <description>
	 */
	public static final BooleanOptionKey enableTempering =
		new BooleanOptionKey(GibbsOptions.class, "enableTempering", false);
	
	/**
	 * <description>
	 */
	public static final DoubleOptionKey initialTemperature =
		new DoubleOptionKey(GibbsOptions.class, "initialTemperature", 1);
	
	/**
	 * <description>
	 */
	public static final DoubleOptionKey temperingHalfLife =
		new DoubleOptionKey(GibbsOptions.class, "temperingHalfLife", 1);
	
	/**
	 * <description>
	 */
	public static final LongOptionKey randomSeed =
		new LongOptionKey(GibbsOptions.class, "randomSeed", -1);
	
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
	 * <description>
	 */
	public static final BooleanOptionKey saveAllSamples =
		new BooleanOptionKey(GibbsOptions.class, "saveAllSamples", false);
}
