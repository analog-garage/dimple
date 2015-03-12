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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.EdgeState;

/**
 * Solver edge state.
 * <p>
 * This interface defines the common interface for solver-specific edge state. Each solver edge should
 * correspond to a {@link EdgeState} in the corresponding model. Unlike {@link ISolverNode},
 * solver edges do not necessarily refer directly to the corresponding model edge object.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see EdgeState
 */
public interface ISolverEdgeState
{
	/**
	 * Returns message from factor to variable ends of the corresponding edge, if one exists.
	 * <p>
	 * The meaning of this message will depend on the solver. For message passing solvers, such
	 * as sum product, it will represent some form of probability distribution of the
	 * edge's variable values conditioned on the graph on the factor's side of the edge.
	 * <p>
	 * @since 0.08
	 */
	public @Nullable Object getFactorToVarMsg();
	
	/**
	 * Returns message from variable to factor ends of the corresponding edge, if one exists.
	 * <p>
	 * The meaning of this message will depend on the solver. For message passing solvers, such
	 * as sum product, it will represent some form of probability distribution of the
	 * edge's variable values conditioned on the graph on the variable's side of the edge..
	 * <p>
	 * @since 0.08
	 */
	public @Nullable Object getVarToFactorMsg();
	
	/**
	 * Resets edge state to an initial state appropriate for the solver.
	 * <p>
	 * @since 0.08
	 */
	public void reset();
	
	/**
	 * Sets edge state from another edge of the same type.
	 * <p>
	 * @param other must have the same type as this object.
	 * @since 0.08
	 */
	public void setFrom(ISolverEdgeState other);
	
	/**
	 * Sets value of message from factor to variable ends of the corresponding edge.
	 * <p>
	 * @param msg the value from which the message will be set. Details will depend on the
	 * specific solver implementation.
	 * @since 0.08
	 * @throws UnsupportedOperationException if edge does not support this operation.
	 */
	public void setFactorToVarMsg(@Nullable Object msg);

	/**
	 * Sets value of message from variable to factor ends of the corresponding edge.
	 * <p>
	 * @param msg the value from which the message will be set. Details will depend on the
	 * specific solver implementation.
	 * @since 0.08
	 * @throws UnsupportedOperationException if edge does not support this operation.
	 */
	public void setVarToFactorMsg(@Nullable Object msg);
}
