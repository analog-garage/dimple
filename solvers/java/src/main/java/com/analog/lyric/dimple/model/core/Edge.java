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

package com.analog.lyric.dimple.model.core;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;


public final class Edge
{
	private final FactorGraph _graph;
	private final EdgeState _edge;
	
	/*--------------
	 * Construction
	 */
	
	public Edge(FactorGraph graph, EdgeState edgeState)
	{
		_graph = graph;
		_edge = edgeState;
	}

	/*----------------
	 * Object methods
	 */
	
	@Override
	public int hashCode()
	{
		return _edge.hashCode();
	}

	@Override
	public boolean equals(@Nullable Object obj)
	{
		return obj instanceof Edge && _edge == ((Edge)obj)._edge;
	}

	@Override
	public String toString()
	{
		return String.format("[Edge %s - %s]", _edge.getFactor(_graph), _edge.getVariable(_graph));
	}
	
	/*--------------
	 * Edge methods
	 */
	
	public int edgeIndex()
	{
		return _edge.factorEdgeIndex();
	}
	
	public EdgeState edgeState()
	{
		return _edge;
	}
	
	public Factor factor()
	{
		return _edge.getFactor(_graph);
	}
	
	public Node getSibling(Node node)
	{
		return _edge.getSibling(node);
	}
	
	public FactorGraph graph()
	{
		return _graph;
	}
	
	public boolean isLocal()
	{
		return _edge.isLocal();
	}
	
	public Variable variable()
	{
		return _edge.getVariable(_graph);
	}
}
