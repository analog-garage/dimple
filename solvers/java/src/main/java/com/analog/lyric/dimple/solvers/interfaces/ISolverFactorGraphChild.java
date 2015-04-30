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

import com.analog.lyric.dimple.model.core.IFactorGraphChild;

/**
 * Interface for solver object associated with {@link IFactorGraphChild}.
 * @since 0.08
 * @author Christopher Barber
 */
public interface ISolverFactorGraphChild
{
	/*---------------------------------
	 * ISolverFactorGraphChild methods
	 */
	
	/**
	 * Returns the solver factor graph to which this node belongs.
	 */
	public @Nullable abstract ISolverFactorGraph getParentGraph();

	/**
	 * Gets the highest level solver graph to which this node belongs (could be the node itself).
	 * @since 0.08
	 */
	public abstract ISolverFactorGraph getRootSolverGraph();

	/**
	 * Returns mapping of nodes to solvers for entire solver graph tree.
	 * @since 0.08
	 */
	public abstract SolverNodeMapping getSolverMapping();

	/**
	 * Return the model object associated with this solver node.
	 */
	public abstract IFactorGraphChild getModelObject();

	/**
	 * Initialize state of object.
	 * <p>
	 * Invoked by {@link ISolverFactorGraph#initialize()} implementations.
	 * <p>
	 * @since 0.08
	 */
	public void initialize();
}