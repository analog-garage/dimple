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

import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.factors.Factor;

/**
 * Base interface for solver factors.
 */
public interface ISolverFactor extends ISolverNode
{
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public Factor getModelObject();
	
	@Override
	public ISolverFactorGraph getParentGraph();
	
	@Override
	public ISolverVariable getSibling(int edge);

	/*-----------------------
	 * ISolverFactor methods
	 */
	
	public Object getBelief() ;

	/**
	 * Create solver edge state object appropriate to the specified factor edge.
	 * <p>
	 * This method allows the factor to customize the edge state and is typically
	 * used by custom factor implementations.
	 * <p>
	 * @param edge is the corresponding model edge.
	 * @return a newly created edge state object or null if the edge creation is to
	 * be delegated back to the caller.
	 * @since 0.08
	 */
	public @Nullable ISolverEdgeState createEdge(EdgeState edge);
	
	public int[][] getPossibleBeliefIndices() ;
	
	public void setDirectedTo(int [] indices);
}
