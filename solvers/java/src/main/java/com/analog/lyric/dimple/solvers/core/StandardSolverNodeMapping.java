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

import static java.util.Objects.*;

import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ExtendedArrayList;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.SolverNodeMapping;

/**
 * Standard implementation of {@link SolverNodeMapping}.
 * <p>
 * This is the implementation used by
 * {@linkplain com.analog.lyric.dimple.solvers.core.SFactorGraphBase SFactorGraphBase}.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class StandardSolverNodeMapping extends SolverNodeMapping
{
	/*-------
	 * State
	 */
	
	private final ExtendedArrayList<ISolverFactorGraph> _sgraphs;
	
	/*--------------
	 * Construction
	 */
	
	StandardSolverNodeMapping(ISolverFactorGraph sgraph)
	{
		_sgraphs = new ExtendedArrayList<>(1);
		if (sgraph.getModelObject().getRootIndex() == 0)
		{
			// Only add if root, because otherwise we expect this root state to get replaced very soon.
			_sgraphs.add(sgraph);
		}
	}
	
	/*------------------------------------
	 * SolverFactorGraphHierarchy methods
	 */
	
	@Override
	public void addSolverGraph(ISolverFactorGraph sgraph)
	{
		_sgraphs.set(sgraph.getModelObject().getRootIndex(), sgraph);
	}
	
	@Override
	public ISolverFactorGraph getRootSolverGraph()
	{
		return _sgraphs.get(0);
	}
	
	@Override
	public @Nullable ISolverFactorGraph getSolverGraph(FactorGraph graph, boolean create)
	{
		assertInHierarchy(graph);
		
		ISolverFactorGraph sgraph = _sgraphs.getOrNull(graph.getRootIndex());

		if (sgraph == null && create)
		{
			final Deque<FactorGraph> stack = new ArrayDeque<>();

			do
			{
				stack.push(graph);
				graph = graph.requireParentGraph();
				sgraph = _sgraphs.getOrNull(graph.getRootIndex());
			} while (sgraph == null);

			while (!stack.isEmpty())
			{
				sgraph = requireNonNull(sgraph).getSolverSubgraph(stack.pop(), true);
			}
		}
		
		return sgraph;
	}
	
	@Override
	public @Nullable ISolverFactorGraph getSolverGraphOrNull(FactorGraph graph)
	{
		assertInHierarchy(graph);
		return _sgraphs.getOrNull(graph.getRootIndex());
	}
	
	@SuppressWarnings("null")
	@Override
	public ISolverFactorGraph getSolverGraph(FactorGraph graph)
	{
		assertInHierarchy(graph);
		
		ISolverFactorGraph sgraph = _sgraphs.getOrNull(graph.getRootIndex());

		if (sgraph == null)
		{
			final Deque<FactorGraph> stack = new ArrayDeque<>();

			do
			{
				stack.push(graph);
				graph = graph.requireParentGraph();
				sgraph = _sgraphs.getOrNull(graph.getRootIndex());
			} while (sgraph == null);

			while (!stack.isEmpty())
			{
				sgraph = requireNonNull(sgraph).getSolverSubgraph(stack.pop(), true);
			}
		}
		
		return sgraph;
	}
	
	@Override
	public void removeSolverGraph(ISolverFactorGraph sgraph)
	{
		int index = sgraph.getModelObject().getRootIndex();
		assert(sgraph == _sgraphs.get(index));
		_sgraphs.set(index, null);
	}
}