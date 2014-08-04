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
import com.analog.lyric.options.DoubleListOptionKey;
import com.analog.lyric.options.DoubleOptionKey;
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
	 */
	public static final DoubleOptionKey damping =
		new DoubleOptionKey(MinSumOptions.class, "damping", 0.0);
	
	/**
	 * Maximum size of discrete belief messages.
	 * <p>
	 * If less than the full domain size of the message, then messages will be truncated
	 * to this number of dimensions with the lowest weight dimensions omitted.
	 */
	public static final IntegerOptionKey maxMessageSize =
		new IntegerOptionKey(MinSumOptions.class, "maxMessageSize", Integer.MAX_VALUE);

	/**
	 * Node specific damping values.
	 * <p>
	 * This option may be set on individual discrete variables or factors. It must either be
	 * an empty list to indicate damping is turned off or to a list of damping values of the
	 * same length as the number of siblings.
	 * <p>
	 * Option is looked up on message creation and when {@linkplain SNode#initialize initialize}
	 * is called.
	 */
	public static final DoubleListOptionKey nodeSpecificDamping =
		new DoubleListOptionKey(MinSumOptions.class, "nodeSpecificDamping");
}
