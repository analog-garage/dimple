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

package com.analog.lyric.dimple.solvers.sumproduct;

import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.dimple.solvers.core.SNode;
import com.analog.lyric.dimple.solvers.sumproduct.sampledfactor.SampledFactor;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.DoubleListOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.IntegerOptionKey;
import com.analog.lyric.options.LongOptionKey;

/**
 * Options for sum-product solver.
 * <p>
 * Unless otherwise noted, options are configured during initialization phase.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class SumProductOptions extends SolverOptions
{
	/**
	 * Global damping factor
	 * <p>
	 * This option may be set on entire graph or on individual discrete variables or factors.
	 * <p>
	 * Must be a value in the range [0.0, 1.0].
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey damping =
		new DoubleOptionKey(SumProductOptions.class, "damping", 0.0, 0.0, 1.0);
	
	public static final BooleanOptionKey enableOptimizedUpdate =
		new BooleanOptionKey(SumProductOptions.class, "enableOptimizedUpdate", false);
	
	/**
	 * Maximum size of discrete belief messages.
	 * <p>
	 * If less than the full domain size of the message, then messages will be truncated
	 * to this number of dimensions with the lowest weight dimensions omitted.
	 * <p>
	 * Must be a positive number.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey maxMessageSize =
		new IntegerOptionKey(SumProductOptions.class, "maxMessageSize", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
	
	/**
	 * Node specific damping values.
	 * <p>
	 * This option may be set on individual discrete variables or factors. It must either be
	 * an empty list to indicate damping is turned off or to a list of damping values of the
	 * same length as the number of siblings.
	 * <p>
	 * Option is looked up on message creation and when {@linkplain SNode#initialize initialize}
	 * is called.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleListOptionKey nodeSpecificDamping =
		new DoubleListOptionKey(SumProductOptions.class, "nodeSpecificDamping");
	
	public static final LongOptionKey randomSeed =
		new LongOptionKey(SumProductOptions.class, "randomSeed");

	public static final IntegerOptionKey sampledFactorBurnInScansPerUpdate =
		new IntegerOptionKey(SumProductOptions.class, "sampledFactorBurnInScansPerUpdate",
			SampledFactor.DEFAULT_BURN_IN_SCANS_PER_UPDATE, 1, Integer.MAX_VALUE);
	
	public static final IntegerOptionKey sampledFactorSamplesPerUpdate =
		new IntegerOptionKey(SumProductOptions.class, "sampledFactorSamplesPerUpdate",
			SampledFactor.DEFAULT_SAMPLES_PER_UPDATE, 1, Integer.MAX_VALUE);
	
	public static final IntegerOptionKey sampledFactorScansPerSample =
		new IntegerOptionKey(SumProductOptions.class, "sampledFactorScansPerSample",
			SampledFactor.DEFAULT_SCANS_PER_SAMPLE, 1, Integer.MAX_VALUE);
	
}
