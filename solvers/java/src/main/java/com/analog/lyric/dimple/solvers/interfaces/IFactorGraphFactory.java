/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.interfaces;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;

public interface IFactorGraphFactory<SolverGraph extends ISolverFactorGraph>
{
	/**
	 * Creates new root solver graph for {@code graph}
	 * <p>
	 * This simply calls {@link #createFactorGraph(FactorGraph, ISolverFactorGraph)} with null parent.
	 * <p>
	 * @param graph
	 * @since 0.08
	 */
	public SolverGraph createFactorGraph(FactorGraph graph);
	
	/**
	 * Creates new solver graph for {@code graph}
	 * <p>
	 * @param graph
	 * @param parent is the parent of the new solver graph. This should be a solver for {@code graph}.
	 * @since 0.08
	 */
	public SolverGraph createFactorGraph(FactorGraph graph, @Nullable ISolverFactorGraph parent);
}
