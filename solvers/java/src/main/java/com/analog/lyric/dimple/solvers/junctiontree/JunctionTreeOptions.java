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

package com.analog.lyric.dimple.solvers.junctiontree;

import com.analog.lyric.dimple.model.transform.JunctionTreeTransform;
import com.analog.lyric.dimple.model.transform.VariableEliminator;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.model.transform.VariableEliminatorCostListOptionKey;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.IntegerOptionKey;

/**
 * Options for the junction tree solvers.
 * <p>
 * Options are configured during {@linkplain JunctionTreeSolverGraphBase#initialize initialize}
 * unless otherwise noted.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class JunctionTreeOptions extends BPOptions
{
	/**
	 * If true, then the transformation will condition out any variables that have a fixed value.
	 * This will produce a more efficient graph but will prevent it from being reused if the fixed
	 * value changes.
	 * <p>
	 * False by default.
	 * <p>
	 * @since 0.07
	 */
	public static final BooleanOptionKey useConditioning =
		new BooleanOptionKey(JunctionTreeOptions.class, "useConditioning", false);
	
	/**
	 * Specifies the maximum number of times to attempt to determine an optimal junction tree
	 * transformation.
	 * <p>
	 * Specifies the number of iterations of the {@link VariableEliminator} algorithm when
	 * attempting to determine the variable elimination ordering that determines the junction tree
	 * transformation. Each iteration will pick a cost function from specified
	 * {@link #variableEliminatorCostFunctions} at random and will randomize the order of
	 * variables that have equivalent costs. A higher number of iterations may produce a better
	 * ordering.
	 * <p>
	 * Must be a positive value. Default value is specified by
	 * {@link JunctionTreeTransform#DEFAULT_MAX_TRANSFORMATION_ATTEMPTS}.
	 * <p>
	 * @since 0.07
	 */
	public static final IntegerOptionKey maxTransformationAttempts =
		new IntegerOptionKey(JunctionTreeOptions.class, "maxTransformationAttempts",
			JunctionTreeTransform.DEFAULT_MAX_TRANSFORMATION_ATTEMPTS, 1, Integer.MAX_VALUE);
	
	/**
	 * Specifies which cost functions the variable elimination algorithm should use when
	 * attempting to optimize the junction tree transformation. An empty list indicates that
	 * all should be tried.
	 * <p>
	 * The default is the empty list.
	 * <p>
	 * @see VariableCost
	 * @since 0.07
	 */
	public static final VariableEliminatorCostListOptionKey variableEliminatorCostFunctions =
		new VariableEliminatorCostListOptionKey(JunctionTreeOptions.class, "variableEliminatorCostFunctions");
}
