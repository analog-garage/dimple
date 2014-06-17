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

package com.analog.lyric.dimple.solvers.optimizedupdate;

import com.analog.lyric.options.DoubleOptionKey;
import com.analog.lyric.options.EnumOptionKey;
import com.analog.lyric.util.misc.Internal;

/**
 * Keys for options related to choosing the factor update algorithm.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public class UpdateOptions
{
	public static final EnumOptionKey<UpdateApproach> updateApproach =
		new EnumOptionKey<UpdateApproach>(UpdateOptions.class, "updateApproach", UpdateApproach.class, UpdateApproach.UPDATE_APPROACH_NORMAL);

	public static final DoubleOptionKey automaticExecutionTimeScalingFactor =
		new DoubleOptionKey(UpdateOptions.class, "automaticExecutionTimeScalingFactor", 100.0, 0.0, Double.POSITIVE_INFINITY);
	
	public static final DoubleOptionKey automaticMemoryAllocationScalingFactor =
		new DoubleOptionKey(UpdateOptions.class, "automaticMemoryAllocationScalingFactor", 1.0, 0.0, Double.POSITIVE_INFINITY);
	
	public static final DoubleOptionKey optimizedUpdateSparseThreshold =
		new DoubleOptionKey(UpdateOptions.class, "optimizedUpdateSparseThreshold", 1.0, 0.0, 1.0);
}
