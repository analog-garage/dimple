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

import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.util.misc.Internal;

/**
 * CostOptimizer requires an implementation of this interface, typically supplied by a
 * SFactorGraph, to access solver-specific functions.
 * 
 * @since 0.07
 * @author jking
 */
@Internal
public interface ISFactorGraphToCostOptimizerAdapter
{
	Costs estimateCostOfNormalUpdate(IFactorTable factorTable);

	int getWorkers(FactorGraph factorGraph);

	Costs estimateCostOfOptimizedUpdate(IFactorTable factorTable);

	UpdateSettings getFactorTableUpdateSettings(IFactorTable factorTable);
}
