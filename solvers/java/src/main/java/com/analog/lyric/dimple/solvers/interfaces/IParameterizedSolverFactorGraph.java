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

package com.analog.lyric.dimple.solvers.interfaces;

import java.util.Collection;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphEdgeState;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * Solver graph interface parameterized by types of solver factors and variables.
 * @param <SFactor> is the base type for solver factors in this graph.
 * @param <SVariable> is the base type for solver variables in this graph.
 * @since 0.08
 * @author Christopher Barber
 */
public interface IParameterizedSolverFactorGraph
	<SFactor extends ISolverFactor, SVariable extends ISolverVariable, SEdge extends ISolverEdge>
	extends ISolverFactorGraph
{
	public SEdge createEdgeState(FactorGraphEdgeState edge);
	
	public SFactor createFactor(Factor factor);
	
	public SVariable createVariable(Variable variable);

	public ISolverFactorGraph createSubgraph(FactorGraph subgraph);
	
	@Override
	public @Nullable SEdge getSolverEdge(FactorGraphEdgeState edge);
	
	@Override
	public @Nullable SFactor getSolverFactor(Factor factor, boolean create);
	
	@Override
	public @Nullable SVariable getSolverVariable(Variable variable, boolean create);
	
	/**
	 * Unmodifiable collection of solver factors directly owned by this solver graph.
	 * @since 0.08
	 */
	@Override
	public Collection<SFactor> getSolverFactors();
	
	/**
	 * Unmodifiable collection of solver variables directly owned by this solver graph.
	 * @since 0.08
	 */
	@Override
	public Collection<SVariable> getSolverVariables();
	
	/**
	 * True if solver uses edge-specific state.
	 * @since 0.08
	 */
	public boolean hasEdgeState();
}
