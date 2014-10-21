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

package com.analog.lyric.dimple.solvers.minsum;

import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.dimple.solvers.core.SNode;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.options.DoubleListOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.EnumOptionKey;
import com.analog.lyric.options.IntegerOptionKey;

/**
 * Options for {@link MinSumSolver}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class MinSumOptions extends SolverOptions
{
	/**
	 * Global damping factor
	 * <p>
	 * This option may be set on entire graph or on individual discrete variables or factors.
	 * <p>
	 * Option is looked up on message creation and when {@linkplain SNode#initialize initialize}
	 * is called.
	 * <p>
	 * Must be a value in the range [0.0, 1.0].
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey damping =
		new DoubleOptionKey(MinSumOptions.class, "damping", 0.0, 0.0, 1.0);
	
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
		new DoubleListOptionKey(MinSumOptions.class, "nodeSpecificDamping");

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
		new IntegerOptionKey(MinSumOptions.class, "maxMessageSize", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);

	/**
	 * Update approach.
	 * <p>
	 * This option may be set on entire graph or on individual discrete factors.
	 * <p>
	 * @since 0.07
	 */
	public static final EnumOptionKey<UpdateApproach> updateApproach = new EnumOptionKey<UpdateApproach>(
		MinSumOptions.class, "updateApproach", UpdateApproach.class, UpdateApproach.AUTOMATIC);

	/**
	 * Execution time scaling factor for weighing execution time and memory allocation costs.
	 * <p>
	 * This option may be set on entire graph or on individual discrete factors.
	 * <p>
	 * Must be a positive number.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey automaticExecutionTimeScalingFactor = new DoubleOptionKey(MinSumOptions.class,
		"automaticExecutionTimeScalingFactor", 1.0, 0.0, Double.POSITIVE_INFINITY);

	/**
	 * Memory allocation scaling factor for weighing execution time and memory allocation costs.
	 * <p>
	 * This option may be set on entire graph or on individual discrete factors.
	 * <p>
	 * Must be a positive number.
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey automaticMemoryAllocationScalingFactor = new DoubleOptionKey(
		MinSumOptions.class, "automaticMemoryAllocationScalingFactor", 10.0, 0.0, Double.POSITIVE_INFINITY);

	/**
	 * Density, below which the optimized update algorithm will use a sparse representation for its
	 * auxiliary factor tables.
	 * <p>
	 * This option may be set on entire graph or on individual discrete factors.
	 * <p>
	 * Must be a value in the range [0.0, 1.0].
	 * <p>
	 * @since 0.07
	 */
	public static final DoubleOptionKey optimizedUpdateSparseThreshold = new DoubleOptionKey(MinSumOptions.class,
		"optimizedUpdateSparseThreshold", 1.0, 0.0, 1.0);
}
