/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.data;

import com.analog.lyric.dimple.model.core.FactorGraph;

/**
 * {@link DataLayer} implementation for {@link com.analog.lyric.dimple.model.variables.Variable#getPrior()
 * variable priors}.
 * <p>
 * Priors are stored directly in Variable instances, so this class simply maps to those
 * and has no variable-specific local state.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class PriorDataLayer extends GenericDataLayer
{
	/**
	 * Construct prior data layer for given graph.
	 * <p>
	 * @param graph
	 * @since 0.08
	 */
	public PriorDataLayer(FactorGraph graph)
	{
		super(graph, PriorFactorGraphData.constructor());
	}
}
