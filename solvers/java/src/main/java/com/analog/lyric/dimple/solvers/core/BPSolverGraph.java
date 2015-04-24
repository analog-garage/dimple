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

package com.analog.lyric.dimple.solvers.core;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.SchedulerOptionKey;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * Base implementation class for belief-propagation-style solvers.
 * <p>
 * Solver graphs derived from this class are expected to implement some form of belief propagation or
 * similar message-passing based inference and make use of general options specified in {@link BPOptions}.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class BPSolverGraph
	<SFactor extends ISolverFactor, SVariable extends ISolverVariable, SEdge extends ISolverEdgeState>
	extends SFactorGraphBase<SFactor, SVariable, SEdge>
{
	/*--------------
	 * Construction
	 */
	
	protected BPSolverGraph(FactorGraph graph, @Nullable ISolverFactorGraph parent)
	{
		super(graph, parent);
	}
	
	/*----------------------------
	 * ISolverFactorGraph methods
	 */
	
	/**
	 * {@inheritDoc}
	 * @return {@link BPOptions#scheduler};
	 */
	@Override
	public @Nullable SchedulerOptionKey getSchedulerKey()
	{
		return BPOptions.scheduler;
	}
}
