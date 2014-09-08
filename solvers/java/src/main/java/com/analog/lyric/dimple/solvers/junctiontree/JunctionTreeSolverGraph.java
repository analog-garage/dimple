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

package com.analog.lyric.dimple.solvers.junctiontree;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.IFactorGraphFactory;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @since 0.05
 * @author Christopher Barber
 */
public class JunctionTreeSolverGraph extends JunctionTreeSolverGraphBase<SumProductSolverGraph>
{
	private final @Nullable JunctionTreeSolverGraph _parent;
	private final JunctionTreeSolverGraph _root;

	/*--------------
	 * Construction
	 */

	JunctionTreeSolverGraph(FactorGraph sourceModel, @Nullable IFactorGraphFactory<?> solverFactory,
		@Nullable JunctionTreeSolverGraph parent)
	{
		super(sourceModel, solverFactory);
		_parent = parent;
		_root = parent != null ? parent.getRootGraph() : this;
	}

	JunctionTreeSolverGraph(FactorGraph sourceModel, IFactorGraphFactory<?> solverFactory)
	{
		this(sourceModel, solverFactory,  null);
	}
	
	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public @Nullable JunctionTreeSolverGraph getParentGraph()
	{
		return _parent;
	}
	
	@Override
	public JunctionTreeSolverGraph getRootGraph()
	{
		return _root;
	}

	/*----------------------------
	 * ISolverFactorGraph methods
	 */
	
	@Override
	public JunctionTreeSolverGraph createSubGraph(FactorGraph subgraph, @Nullable IFactorGraphFactory<?> factory)
	{
		return new JunctionTreeSolverGraph(subgraph, factory, this);
	}
}
