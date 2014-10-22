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

package com.analog.lyric.dimple.solvers.junctiontreemap;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverBase;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph;

/**
 * @since 0.05
 * @author Christopher Barber
 */
public class JunctionTreeMAPSolver extends JunctionTreeSolverBase<MinSumSolverGraph, JunctionTreeMAPSolverGraph>
{
	/*----------------
	 * Object methods
	 */
	
	@Override
	@NonNullByDefault(false)
	public boolean equals(Object obj)
	{
		return obj instanceof JunctionTreeMAPSolver;
	}
	
	@Override
	public int hashCode()
	{
		return JunctionTreeMAPSolver.class.hashCode();
	}
	
	/*------------------------------
	 *  IFactorGraphFactory methods
	 */
	
	@Override
	public final JunctionTreeMAPSolverGraph createFactorGraph(FactorGraph graph)
	{
		return new JunctionTreeMAPSolverGraph(graph, new MinSumSolver());
	}
}
