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

import static java.util.Objects.*;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.Internal;

/**
 * Describes an edge from a factor to a variable.
 * <p>
 * Edge state objects are maintained by each {@link FactorGraph} for the edges that connect
 * to it's directly owned {@link Factor}s.
 * <p>
 * To save memory, these objects do not contain a pointer to the owning graph and some methods
 * will require a reference to the parent graph. You can wrap this with an {@link Edge} object
 * if you want a self-contained edge reference.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class EdgeState
{
	/*-------
	 * State
	 */
	
	int _factorToVariableEdgeNumber = -1;
	int _variableToFactorEdgeNumber = -1;
	
	/*------------------------------
	 * FactorGraphEdgeState methods
	 */
	
	/**
	 * The index of this edge within the {@link FactorGraph} that owns it's factor.
	 * <p>
	 * This index can be used to look up this object within the parent graph of the factor
	 * using the {@link FactorGraph#getSiblingEdgeState(int)} method.
	 * <p>
	 * If {@link #isLocal()} then this will be the same as {@link #variableEdgeIndex()}.
	 * <p>
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	public abstract int factorEdgeIndex();
	
	/**
	 * The index of this edge within the {@link FactorGraph} that owns it's variable.
	 * <p>
	 * This index can be used to look up this object within the parent graph of its variable
	 * using the {@link FactorGraph#getSiblingEdgeState(int)} method.
	 * <p>
	 * If {@link #isLocal()} then this will be the same as {@link #factorEdgeIndex()}.
	 * <p>
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	public abstract int variableEdgeIndex();

	/**
	 * Given an end node for this edge, return the corresponding edge index.
	 * 
	 * @param node Must be a variable or factor at one end of the edge.
	 * @return {@link #variableEdgeIndex()} or {@link #factorEdgeIndex()} as appropriate.
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	public abstract int edgeIndex(Node node);

	/**
	 * Given the parent graph containing this edge, return its index in the graph.
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	public abstract int edgeIndexInParent(FactorGraph graph);
	
	/**
	 * Return instance of {@link Factor} end of edge, given parent graph.
	 * <p>
	 * @param graph is the parent graph of either the variable or factor ends of the edge.
	 * @since 0.08
	 */
	abstract public Factor getFactor(FactorGraph graph);
	
	/**
	 * Returns sibling edge number from the perspective of {@link Factor} endpoint of the edge.
	 * <p>
	 * @return sibling edge number or else -1 if edge is not currently connected to its endpoints.
	 * @since 0.08
	 * @see #getVariableToFactorEdgeNumber()
	 */
	public final int getFactorToVariableEdgeNumber()
	{
		return _factorToVariableEdgeNumber;
	}
	
	/**
	 * Return instance of parent of factor end of edge, given parent of either edge.
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	abstract public FactorGraph getFactorParent(FactorGraph graph);
	
	/**
	 * Return instance of {@link Variable} end of edge, given parent graph.
	 * <p>
	 * @param graph is the parent graph of either the variable or factor ends of the edge.
	 * @since 0.08
	 */
	abstract public Variable getVariable(FactorGraph graph);
	
	/**
	 * Returns sibling edge number from the perspective of {@link Variable} endpoint of the edge.
	 * <p>
	 * @return sibling edge number or else -1 if edge is not currently connected to its endpoints.
	 * @since 0.08
	 * @see #getFactorToVariableEdgeNumber()
	 */
	public final int getVariableToFactorEdgeNumber()
	{
		return _variableToFactorEdgeNumber;
	}

	/**
	 * Return instance of parent of variable end of edge, given parent of either edge.
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	abstract public FactorGraph getVariableParent(FactorGraph graph);
	
	/**
	 * Given one endpoint of edge, return the other one.
	 * <p>
	 * That is given the {@link Variable} end of the edge, this returns the {@link Factor}, and
	 * vice-versa.
	 * <p>
	 * @since 0.08
	 */
	public final Node getSibling(INode node)
	{
		final FactorGraph graph = requireNonNull(node.getContainingGraph());
		
		return node.isVariable() ? getFactor(graph) : getVariable(graph);
	}
	
	/**
	 * Given one endpoint of edge, find the sibling index from that endpoint to the other.
	 * <p>
	 * @param node is either the {@link Variable} or {@link Factor} at one end of the edge.
	 * @return index that could be used to lookup this edge using {@link Node#getSiblingEdgeState(int)}
	 * on the given {@code node}.
	 * @since 0.08
	 */
	public final int getSiblingIndex(Node node)
	{
		return node.indexOfSiblingEdgeState(this);
	}
	
	/**
	 * The local id of the {@link Factor} referred to by this edge within its owning {@link FactorGraph}.
	 * @since 0.08
	 */
	public int factorLocalId()
	{
		return NodeId.localIdFromParts(NodeId.FACTOR_TYPE, factorIndex());
	}

	/**
	 * The index of the {@link Factor} referred to by this edge within its owning {@link FactorGraph}.
	 * @since 0.08
	 */
	abstract public int factorIndex();
	
	/**
	 * True if both the factor and variable are owned by the same graph.
	 * @since 0.08
	 */
	abstract public boolean isLocal();
	
	/**
	 * The index of the {@link Variable} referred to by this edge within the graph that owns this edge.
	 * <p>
	 * Note that the graph that owns the edge is the one that owns the edge's {@link Factor}, which may
	 * not be the owner of the variable. In that case, this will be the index of the corresponding
	 * boundary variable.
	 * <p>
	 * @since 0.08
	 */
	abstract public int variableIndex();
	
	/**
	 * The local id of the {@link Variable} referred to by this edge within the graph that owns this edge.
	 * <p>
	 * Note that the graph that owns the edge is the one that owns the edge's {@link Factor}, which may
	 * not be the owner of the variable. In that case, this will return a boundary variable identifier.
	 * <p>
	 * @since 0.08
	 */
	abstract public int variableLocalId();
}