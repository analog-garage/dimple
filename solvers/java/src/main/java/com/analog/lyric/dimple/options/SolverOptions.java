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

import com.analog.lyric.dimple.solvers.core.STableFactorBase;
import com.analog.lyric.options.BooleanOptionKey;
import com.analog.lyric.options.IntegerOptionKey;

/**
 * Defines options that are general to multiple solvers.
 * <p>
 * Solver-specific option classes should be defined as subclasses of this class.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class SolverOptions extends DimpleOptions
{
	/**
	 * Specifies whether to enable use of multiple threads during solves.
	 */
	public final static BooleanOptionKey enableMultithreading =
		new BooleanOptionKey(SolverOptions.class, "enableMultithreading", false);

	/**
	 * Threshold size that determines whether to automatically generate a factor table during initialization.
	 * <p>
	 * This controls whether factor tables will be automatically computed at initialization time for
	 * discrete factors based on the expected number of entries in the table. Factor table creation can
	 * be expensive both in the memory it takes to store large tables and in the time that is needed to
	 * evaluate the factor function for every possible combination of arguments. This option provides a
	 * means to set a threshold for creation.
	 * <p>
	 * The expected size of the table is computed differently based on whether the factor is directed
	 * and the function is deterministic:
	 * 
	 * <ul>
	 * <li>If the factor is not deterministic directed, then the expected size of the factor table
	 * is the sizes of all of the variable domains multiplied together.
	 * <li>If the factor is deterministic and directed the expected size, then the expected size is
	 * the sizes of just the input variable domains multipled together.
	 * </ul>
	 * <p>
	 * This will not prevent factor tables from being generated manually. Only applicable to discrete
	 * table factors (subclasses of {@link STableFactorBase}), and currently only useful when using the Gibbs
	 * solver.
	 * <p>
	 * The default value is the integer max value of 2<sup>31</sup> - 1.
	 * <p>
	 * @since 0.08
	 */
	public static final IntegerOptionKey maxAutomaticFactorTableSize =
		new IntegerOptionKey(SolverOptions.class, "maxAutomaticFactorTableSize", Integer.MAX_VALUE);
}
