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

import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * Describes an edge between a {@link Factor} and {@link Variable}.
 * 
 * @since 0.08
 * @author Christopher Barber
 * @see Node#getSiblingEdge(int)
 */
public final class Edge implements IFactorGraphChild
{
	/**
	 * Classifies type of edge.
	 * 
	 * @since 0.08
	 * @author Christopher Barber
	 */
	public enum Type
	{
		/**
		 * A local edge connecting a variable and factor owned by the same graph.
		 */
		LOCAL,
		/**
		 * A boundary edge connecting a factor owned by graph to a boundary variable from
		 * an outer graph.
		 */
		OUTER,
		/**
		 * A boundary edge connecting a variable owned by this graph to a factor from
		 * a subgraph.
		 */
		INNER;
	}
	
	/*--------
	 * State
	 */
	
	/**
	 * The underlying edge state.
	 */
	private final EdgeState _edge;
	
	/**
	 * One of the graphs containing this edge. This is the parent of the edge's variable or factor
	 * node (or both if the edge is local).
	 */
	private final FactorGraph _graph;
	
	/*--------------
	 * Construction
	 */
	
	Edge(FactorGraph graph, EdgeState edgeState)
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
	
	/*---------------------------
	 * IFactorGraphChild methods
	 */
	
	@Override
	public FactorGraph getContainingGraph()
	{
		return _graph;
	}

	@Override
	public final long getGlobalId()
	{
		return Ids.globalIdFromParts(_graph.getGraphId(), getLocalId());
	}
	
	@Override
	public long getGraphTreeId()
	{
		return Ids.graphTreeIdFromParts(_graph.getGraphTreeIndex(), getLocalId());
	}
	
	@Deprecated
	@Override
	public long getId()
	{
		return getLocalId();
	}
	
	@Override
	public final int getLocalId()
	{
		return Ids.localIdFromParts(Ids.EDGE_TYPE, _edge.edgeIndexInParent(_graph));
	}
	
	@Override
	public @Nullable FactorGraph getParentGraph()
	{
		return _graph;
	}

	@Override
	public @Nullable FactorGraph getRootGraph()
	{
		return _graph.getRootGraph();
	}
	
	@Override
	public UUID getUUID()
	{
		return Ids.makeUUID(_graph.getEnvironment().getEnvId(), getGlobalId());
	}
	
	/*--------------
	 * Edge methods
	 */
	
	public int edgeIndex()
	{
		return _edge.factorEdgeIndex();
	}

	/**
	 * The underlying edge state object.
	 * <p>
	 * The edge state object is what is actually held by the graph's
	 * <p>
	 * @since 0.08
	 */
	public EdgeState edgeState()
	{
		return _edge;
	}
	
	/**
	 * Returns the factor node on this edge.
	 * @since 0.08
	 * @see #variable()
	 */
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
	
	/**
	 * True if edge connects variable and factor locally within the same graph.
	 * @since 0.08
	 */
	public boolean isLocal()
	{
		return _edge.isLocal();
	}
	
	/**
	 * Describe type of edge with respect to {@link #graph}.
	 * @since 0.08
	 */
	public Type type()
	{
		return _edge.type(_graph);
	}
	
	/**
	 * Returns the variable node on this edge.
	 * @since 0.08
	 * @see #factor()
	 */
	public Variable variable()
	{
		return _edge.getVariable(_graph);
	}
}
