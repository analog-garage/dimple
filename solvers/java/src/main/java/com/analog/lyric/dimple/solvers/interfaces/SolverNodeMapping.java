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

import static java.util.Objects.*;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.util.misc.Internal;

/**
 * Maps model objects to their corresponding solvers in the tree of {@link FactorGraph}s under a common
 * {@linkplain com.analog.lyric.dimple.model.core.INode#getRootGraph() root}.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 * @see ISolverFactorGraph#getSolverMapping()
 */
public abstract class SolverNodeMapping
{
	/*-------
	 * State
	 */
	
	private @Nullable DataLayer<? extends IDatum> _conditioningLayer = null;
	
	/*-------------------
	 * Abstract methods
	 */
	
	/**
	 * Add a new subgraph to the mapping..
	 * <p>
	 * This is called by the {@link ISolverFactorGraph} implementation when a subgraph is added, and should
	 * not be invoked in other situations. Solvers that inherit from the standard
	 * {@linkplain com.analog.lyric.dimple.solvers.core.SFactorGraphBase SFactorGraphBase} class do not have
	 * to call this method explicitly.
	 * @param sgraph solver graph that has just been added as a subgraph of a graph already in the tree.
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	public abstract void addSolverGraph(ISolverFactorGraph sgraph);
	
	/*-------------------
	 * Abstract methods
	 */
	
	/**
	 * Returns the root solver graph in the tree.
	 * <p>
	 * This will be the solver that is associated with the root graph in the model graph tree.
	 * <p>
	 * @since 0.08
	 */
	public abstract ISolverFactorGraph getRootSolverGraph();

	/**
	 * Get solver graph for given model graph.
	 * <p>
	 * @param graph must be in the model graph tree associated with this solver graph tree.
	 * @param create indicates whether to create the solver graph on demand. If true, this method should return
	 * a non-null value or throw an exception.
	 * @return solver graph whose {@linkplain ISolverFactorGraph#getModelObject() model} is the specified {@code graph}
	 * or else null if it does not exist and {@code create} is false.
	 * @throws IllegalArgumentException if {@code graph} does not belong to the same model graph tree
	 * @since 0.08
	 * @see #inGraphTree(Node)
	 * @see #getSolverGraphOrNull(FactorGraph)
	 * @see #getSolverGraph(FactorGraph)
	 */
	public abstract @Nullable ISolverFactorGraph getSolverGraph(FactorGraph graph, boolean create);
	
	/**
	 * Remove a subgraph from the mapping.
	 * <p>
	 * This is called by the {@link ISolverFactorGraph} implementation when a subgraph is removed, and should
	 * not be invoked in other situations. Solvers that inherit from the standard
	 * {@linkplain com.analog.lyric.dimple.solvers.core.SFactorGraphBase SFactorGraphBase} class do not have
	 * to call this method explicitly.
	 * @param sgraph is a subgraph of a graph in the tree that has just been removed.
	 * @since 0.08
	 * @category internal
	 */
	@Internal
	public abstract void removeSolverGraph(ISolverFactorGraph sgraph);

	/*------------------
	 * Concrete methods
	 */
	
	/**
	 * Layer containing conditioning information for solver.
	 * @since 0.08
	 * @see #setConditioningLayer
	 */
	public final @Nullable DataLayer<? extends IDatum> getConditioningLayer()
	{
		return _conditioningLayer;
	}
	
	/**
	 * The root of the {@link FactorGraph} tree associated with this state.
	 * <p>
	 * This is simply the {@linkplain ISolverFactorGraph#getModelObject() model graph} of
	 * the {@linkplain #getRootSolverGraph() root solver graph}.
	 * @since 0.08
	 */
	public FactorGraph getRootGraph()
	{
		return getRootSolverGraph().getModelObject();
	}
	
	/**
	 * Return solver graph for given model graph.
	 * <p>
	 * Simply invokes {@link #getSolverGraph(FactorGraph, boolean)} with {@code create} set to {@code true}.
	 * @since 0.08
	 */
	public ISolverFactorGraph getSolverGraph(FactorGraph graph)
	{
		return requireNonNull(getSolverGraph(graph, true));
	}
	
	/**
	 * Return solver graph, if it exists, for given model graph.
	 * <p>
	 * Simply invokes {@link #getSolverGraph(FactorGraph, boolean)} with {@code create} set to {@code false}.
	 * @since 0.08
	 */
	public @Nullable ISolverFactorGraph getSolverGraphOrNull(FactorGraph graph)
	{
		return getSolverGraph(graph, false);
	}
	
	/**
	 * Get solver factor for given model factor.
	 * <p>
	 * @param factor must be in the model graph tree associated with this solver graph tree.
	 * @param create indicates whether to create the solver factor on demand. If true, this method should return
	 * a non-null value or throw an exception.
	 * @return solver factor whose {@linkplain ISolverFactor#getModelObject() model} is the specified {@code factor}
	 * or else null if it does not exist and {@code create} is false.
	 * @throws IllegalArgumentException if {@code factor} does not belong to the same model graph tree
	 * @since 0.08
	 * @see #inGraphTree(Node)
	 * @see #getSolverFactorOrNull(Factor)
	 * @see #getSolverFactor(Factor)
	 */
	public @Nullable ISolverFactor getSolverFactor(Factor factor, boolean create)
	{
		ISolverFactorGraph sgraph = getSolverGraph(factor.requireParentGraph(), create);
		if (sgraph != null)
		{
			return sgraph.getSolverFactor(factor, create);
		}
		
		return null;
	}
	
	/**
	 * Return solver factor, if it exists, for given model factor.
	 * <p>
	 * Simply invokes {@link #getSolverFactor(Factor, boolean)} with {@code create} set to {@code false}.
	 * @since 0.08
	 */
	public @Nullable ISolverFactor getSolverFactorOrNull(Factor factor)
	{
		return getSolverFactor(factor, false);
	}
	
	/**
	 * Return solver factor for given model factor.
	 * <p>
	 * Simply invokes {@link #getSolverFactor(Factor, boolean)} with {@code create} set to {@code true}.
	 * @since 0.08
	 */
	@SuppressWarnings("null")
	public ISolverFactor getSolverFactor(Factor factor)
	{
		return getSolverGraph(factor.getParentGraph()).getSolverFactor(factor, true);
	}
	
	/**
	 * Return solver node for given model node, creating if necessary.
	 * @throws NullPointerException if there is no such
	 * @since 0.08
	 */
	public ISolverNode getSolverNode(INode node)
	{
		ISolverNode snode = null;
		
		switch (node.getNodeType())
		{
		case FACTOR:
			snode = getSolverFactor((Factor)node);
			break;
		case GRAPH:
			snode = getSolverGraph((FactorGraph)node);
			break;
		case VARIABLE:
			snode = getSolverVariable((Variable)node);
			break;
		}

		return requireNonNull(snode);
	}
	
	/**
	 * Get solver variable for given model variable.
	 * <p>
	 * @param variable must be in the model graph tree associated with this solver graph tree.
	 * @param create indicates whether to create the solver variable on demand. If true, this method should return
	 * a non-null value or throw an exception.
	 * @return solver variable whose {@linkplain ISolverVariable#getModelObject() model} is the specified
	 * {@code variable} or else null if it does not exist and {@code create} is false.
	 * @throws IllegalArgumentException if {@code variable} does not belong to the same model graph tree
	 * @since 0.08
	 * @see #inGraphTree(Node)
	 * @see #getSolverVariableOrNull(Variable)
	 * @see #getSolverVariable(Variable)
	 */
	public @Nullable ISolverVariable getSolverVariable(Variable variable, boolean create)
	{
		ISolverFactorGraph sgraph = getSolverGraph(variable.requireParentGraph(), create);
		if (sgraph != null)
		{
			return sgraph.getSolverVariable(variable, create);
		}
		
		return null;
	}
	
	/**
	 * Return solver variable, if it exists, for given model variable.
	 * <p>
	 * Simply invokes {@link #getSolverVariable(Variable, boolean)} with {@code create} set to {@code false}.
	 * @since 0.08
	 */
	public @Nullable ISolverVariable getSolverVariableOrNull(Variable variable)
	{
		return getSolverVariable(variable, false);
	}
	
	/**
	 * Return solver variable for given model variable.
	 * <p>
	 * Simply invokes {@link #getSolverVariable(Variable, boolean)} with {@code create} set to {@code true}.
	 * @since 0.08
	 */
	@SuppressWarnings("null")
	public ISolverVariable getSolverVariable(Variable variable)
	{
		return getSolverGraph(variable.getParentGraph()).getSolverVariable(variable, true);
	}
	
	/**
	 * Returns solver variable block for given model block, if any.
	 * <p>
	 * @since 0.08
	 */
	public @Nullable ISolverVariableBlock getSolverVariableBlock(VariableBlock block)
	{
		return getSolverGraph(block.getParentGraph()).getSolverVariableBlock(block, true);
	}

	/**
	 * Indicate if node is in the model graph tree associated with this solver graph tree.
	 * <p>
	 * This simply tests whether the node's {@link Node#getRootGraph() root} is the same
	 * as this graph tree's {@linkplain #getRootGraph() model root}.
	 * @since 0.08
	 */
	public boolean inGraphTree(Node node)
	{
		return node.getRootGraph() == getRootGraph();
	}
	
	/**
	 * Sets {@linkplain #getConditioningLayer() conditioning layer}.
	 * @since 0.08
	 */
	public final void setConditioningLayer(@Nullable DataLayer<? extends IDatum> layer)
	{
		_conditioningLayer = layer;
	}
	
	/*-------------------
	 * Protected methods
	 */
	
	protected void assertInGraphTree(Node node)
	{
		if (!inGraphTree(node))
		{
			throw new IllegalArgumentException(String.format("'%s' does not belong to model graph tree.", node));
		}
	}
}
