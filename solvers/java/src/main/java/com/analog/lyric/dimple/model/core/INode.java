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

package com.analog.lyric.dimple.model.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;

/**
 * Base interface for model components.
 * 
 * @since 0.06 - extends {@link IModelEventSource}.
 * @author Christopher Barber
 */
public interface INode extends IFactorGraphChild, INameable, IModelEventSource
{
	/*---------------------------
	 * IFactorGraphChild methods
	 */
	
	@Override
	public @Nullable FactorGraph getParentGraph();

	@Override
	public @Nullable FactorGraph getRootGraph();

	/*---------------
	 * INode methods
	 */
	
	/**
	 * If node is a {@link Factor} returns it, otherwise null.
	 * @see #isFactor()
	 */
	public @Nullable Factor asFactor();
	
	/**
	 * If node is a {@link FactorGraph} returns it, otherwise null.
	 * @see #isFactorGraph()
	 */
	public @Nullable FactorGraph asFactorGraph();
	
	/**
	 * If node is a {@link Variable} returns it, otherwise null.
	 * @see #isVariable()
	 */
	public @Nullable Variable asVariable();
	
	/**
	 * Find lowest sibling number associated with given node.
	 * @param node is another node.
	 * @return sibling number of given node if it is a sibling of this node or else -1.
	 * @since 0.08
	 * @see #findSibling(INode, int)
	 */
	public int findSibling(INode node);

	/**
	 * Find lowest sibling number associated with given node.
	 * @param node is another node.
	 * @param start is the sibling number to start at
	 * @return sibling number >= {@code start} of given node if it is a sibling of this node or else -1.
	 * @since 0.08
	 * @see #findSibling(INode)
	 */
	public int findSibling(INode node, int start);

	/**
	 * Returns the ancestor of this node at the specified height, where height zero
	 * refers to the immediate parent of the node returned by {@link #hasParentGraph}.
	 * Returns null if {@code height} is greater than the distance between this node
	 * and the root graph.
	 * <p>
	 * When this returns a non-null value, then the following should be true:
	 * <pre>
	 *    n.getDepthBelowAncestor(n.getAncestorAtHeight(h)) == h
	 * </pre>
	 * @see #getDepthBelowAncestor
	 */
	@Nullable FactorGraph getAncestorAtHeight(int height);

	public double getBetheEntropy();

	public ArrayList<INode> getConnectedNodeAndParents(int siblingNumber);

	public IMapList<INode> getConnectedNodes();
	public INode getConnectedNodeFlat(int siblingNumber);
    public INode getConnectedNode(int relativeNestingDepth, int siblingNumber);
    public IMapList<INode> getConnectedNodes(int relativeNestingDepth);
    public IMapList<INode> getConnectedNodesFlat();
    public IMapList<INode> getConnectedNodesTop();

	/**
	 * Returns the node's depth below the root {@link FactorGraph}, the number
	 * of graphs visited when walking through the chain of {@link #getParentGraph()}s.
	 */
	public int getDepth();

	/**
	 * Returns the depth of the node relative to the given {@code ancestor}. It is the
	 * number of graphs between the node and the {@code ancestor} graph when walking through
	 * the chain of {@link #getParentGraph()}. Returns 0 if {@code ancestor} is parent
	 * of this node. Returns the negative depth minus one if {@code ancestor} is not an ancestor
	 * of this node.
	 * <p>
	 * When this returns a non-negative value, then the following should be true:
	 * <pre>
	 *   n.getAncestorAtHeight(n.getDepthBelowAncestor(g)) == g
	 * </pre>
	 * @see #getAncestorAtHeight
	 */
	public int getDepthBelowAncestor(FactorGraph ancestor);

	public double getInternalEnergy() ;

	/**
	 * Indicates whether node is a factor, graph, or variable.
	 * <p>
	 * @since 0.07
	 */
	public abstract NodeType getNodeType();
	
	public Port getPort(int siblingNumber);

	public Collection<Port> getPorts();

	/**
	 * Returns reverse sibling edge index from sibling at given index back to this node.
	 * @param siblingNumber
	 * @since 0.08
	 */
	public int getReverseSiblingNumber(int siblingNumber);

	public double getScore() ;

	/**
	 * Returns the ith element of the {@link #getSiblings()} list but without
	 * allocating any temporary objects.
	 */
	public INode getSibling(int siblingNumber);

	/**
	 * Returns an unmodifiable list of sibling nodes attached to this node.
	 * This may allocate a new object, so the caller should avoid calling
	 * this repeatedly for the same node in a loop.
	 * 
	 * @see #getSiblingCount()
	 * @see #getSibling(int)
	 */
    public List<? extends INode> getSiblings();
    
    /**
     * Returns the size of the {@link #getSiblings()} list but without
     * allocating any temporary objects.
     */
    public int getSiblingCount();
    
    public abstract Edge getSiblingEdge(int siblingNumber);

	public abstract EdgeState getSiblingEdgeState(int siblingNumber);

	/**
	 * Get the solver object currently associated with this model node, if any.
	 */
	public @Nullable ISolverNode getSolver();
	
	public boolean hasParentGraph();
	
    public boolean isConnected(INode node);
    
    public void initialize();

	/**
	 * True if this is a {@link Factor}.
	 * @see #asFactor()
	 */
	public boolean isFactor();

	/**
	 * True if this is a {@link FactorGraph}.
	 * @see #asFactorGraph()
	 */
	public boolean isFactorGraph();

	/**
	 * True if this is a {@link Variable}
	 * @see #asVariable()
	 */
	public boolean isVariable();

	/**
     * Invokes {@linkplain ISolverNode#update update} on the {@linkplain #getSolver()} current solver object}.
     * @throws NullPointerException if {@link #getSolver()} is null.
     */
    public void update() ;
    
    /**
     * Invokes {@linkplain ISolverNode#updateEdge updateEdge} on the {@linkplain #getSolver()} current solver object}.
     * @throws NullPointerException if {@link #getSolver()} is null.
     */
	public void updateEdge(int siblingNumber);

	/*------------------
	 * Internal methods
	 */
	
    @Internal
    public void clearMarked();
	
    /**
     * Sets {@link #wasVisited()} to false.
     * 
     * @since 0.05
     */
    @Internal
    public void clearVisited();
	
    /**
     * Boolean utility value that can be used to mark node has having been processed.
     * <p>
     * False by default and reset by {@link #initialize()}.
     * <p>
     * @see #clearMarked()
     * @see #setMarked()
     * 
     * @since 0.05
     */
    @Internal
    public  boolean isMarked();
    
    /**
     * Boolean utility value that can be used to indicate node has been visited.
     * <p>
     * False by default and reset by {@link #initialize()}.
     * <p>
     * @see #clearVisited()
     * @see #setVisited()
     * 
     * @since 0.05
     */
    @Internal
    public  boolean wasVisited();
    
    /**
     * Sets {@link #isMarked()} to true.
     * 
     * @since 0.05
     */
    @Internal
    public  void setMarked();
    
    /**
     * Sets {@link #wasVisited()} to true.
     * 
     * @since 0.05
     */
    @Internal
    public  void setVisited();

	

	/*--------------------
	 * Deprecated methods
	 */

	/**
	 * @deprecated use {@link #findSibling(INode)} instead
	 */
	@Deprecated
	public int getPortNum(INode node) ;

	/**
	 * @deprecated use {@link #getReverseSiblingNumber(int)} instead
	 */
	@Deprecated
	public int getSiblingPortIndex(int siblingNumber);

    @Deprecated
	public void initialize(int siblingNumber);

	/**
	 * @deprecated use {@link #updateEdge(int)} instead.
	 */
	@Deprecated
	public void updateEdge(INode other);
}
