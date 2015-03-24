/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import static java.util.Objects.*;

import java.util.UUID;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

/**
 * Represents half an edge in the factor graph.
 */
@Immutable
public abstract class Port implements IFactorGraphChild
{
	/*-------
	 * State
	 */
	
	final EdgeState _edgeState;
	final FactorGraph _graph;
	
	/*--------------
	 * Construction
	 */
	
	Port(EdgeState edgeState, FactorGraph graph)
	{
		_edgeState = edgeState;
		_graph = graph;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public int hashCode()
	{
		return getNode().hashCode()+getSiblingNumber();
	}
	
	@Override
	public boolean equals(@Nullable Object obj)
	{
		if (obj instanceof Port)
		{
			Port p = (Port)obj;
			return p.getNode() == this.getNode() && p.getSiblingNumber() == this.getSiblingNumber();
		}
		return false;
	}
	
	/*---------------------------
	 * IFactorGraphChild methods
	 */

	@Override
	public long getGlobalId()
	{
		return NodeId.globalIdFromParts(_graph.getGraphId(), getLocalId());
	}
	
	@Override
	public long getGraphTreeId()
	{
		return NodeId.graphTreeIdFromParts(_graph.getGraphTreeIndex(), getLocalId());
	}

	@Deprecated
	@Override
	public long getId()
	{
		return getLocalId();
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
		return NodeId.makeUUID(_graph.getEnvironment().getEnvId(), getGlobalId());
	}
	
	/*--------------
	 * Port methods
	 */
	
	@Override
	public String toString()
	{
		return getNode().toString() + " index: " + getSiblingNumber();
	}
	
	/**
	 * The node belonging to this half edge.
	 * @since 0.08
	 * @see #getSiblingNode()
	 */
	public abstract INode getNode();
	
	/**
	 * Returns node at other end of edge from {@linkplain #getNode() this port's node}
	 * @since 0.08
	 */
	public INode getSiblingNode()
	{
		return getNode().getSibling(getSiblingNumber());
	}
	
	/**
	 * Returns the sibling number of the half-edge with respect to {@linkplain #getNode() this port's node}.
	 * @since 0.08
	 */
	public abstract int getSiblingNumber();

	public abstract Port getSiblingPort();
	
	/**
	 * Return the corresponding edge object for this port.
	 * @since 0.08
	 */
	public EdgeState toEdgeState()
	{
		return _edgeState;
	}

	/*--------------------
	 * Deprecated methods
	 */

	/**
	 * @deprecated use {@link #getSiblingNode()} instead.
	 */

	@Deprecated
	public INode getConnectedNode()
	{
		return getNode().getSibling(getSiblingNumber());
	}

	/**
	 * @deprecated instead use {@link #toEdgeState()} to get {@link EdgeState}, use
	 * that to look up corresponding {@link ISolverEdgeState} in solver graph, which will contain the
	 * messages.
	 */
	@Deprecated
	public void setInputMsgValues(Object obj)
	{
		requireNonNull(getNode().getSolver()).setInputMsgValues(getSiblingNumber(), obj);
	}

	/**
	 * @deprecated instead use {@link #toEdgeState()} to get {@link EdgeState}, use
	 * that to look up corresponding {@link ISolverEdgeState} in solver graph, which will contain the
	 * messages.
	 */
	@Deprecated
	public void setOutputMsgValues(Object obj)
	{
		requireNonNull(getNode().getSolver()).setOutputMsgValues(getSiblingNumber(),obj);
	}
	
	/**
	 * @deprecated instead use {@link #toEdgeState()} to get {@link EdgeState}, use
	 * that to look up corresponding {@link ISolverEdgeState} in solver graph, which will contain the
	 * messages.
	 */
	@Deprecated
	public @Nullable Object getInputMsg()
	{
		final ISolverNode snode = getNode().getSolver();
		return snode != null ? snode.getInputMsg(getSiblingNumber()) : null;
	}

	/**
	 * @deprecated instead use {@link #toEdgeState()} to get {@link EdgeState}, use
	 * that to look up corresponding {@link ISolverEdgeState} in solver graph, which will contain the
	 * messages.
	 */
	@Deprecated
	public @Nullable Object getOutputMsg()
	{
		final ISolverNode snode = getNode().getSolver();
		return snode != null ? snode.getOutputMsg(getSiblingNumber()) : null;
	}
	
}
