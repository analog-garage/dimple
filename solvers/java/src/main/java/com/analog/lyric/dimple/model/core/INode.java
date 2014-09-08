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
import java.util.List;

import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.IMapList;
import com.analog.lyric.util.misc.Internal;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base interface for model components.
 * 
 * @since 0.06 - extends {@link IModelEventSource}.
 * @author Christopher Barber
 */
public interface INode  extends INameable, IModelEventSource
{
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
	 * Indicates whether node is a factor, graph, or variable.
	 * <p>
	 * @since 0.07
	 */
	public abstract NodeType getNodeType();
	
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
    
    /**
     * Returns the ith element of the {@link #getSiblings()} list but without
     * allocating any temporary objects.
     */
    public INode getSibling(int i);

    /**
     * Adds {@code node} as a sibling of this node.
     * <p>
     * Note that this does not perform the converse operation so to connect two nodes to each
     * other you need to invoke this on each of them:
     * <pre>
     *    node1.connect(node2);
     *    node2.connect(node1);
     * </pre>
     */
    public void connect(INode node);
    
    /**
     * Removes sibling node with given index from list of siblings.
     * <p>
     * Note that as with {@link #connect(INode)}, this does not perform the converse operation.
     * 
     * @throws ArrayIndexOutOfBoundsException if index is not in the range [0,{@link #getSiblingCount()}-1].
     * @see #disconnect(INode)
     */
    public void disconnect(int index);
    
    /**
     * Removes sibling node from list of siblings.
     * <p>
     * Note that as with {@link #connect(INode)}, this does not perform the converse operation.
     * 
     * @throws DimpleException if node is not a sibling of this node.
     * @see #disconnect(int)
     */
    public void disconnect(INode node);
    
    public boolean isConnected(INode node);
    public IMapList<INode> getConnectedNodes();
	public INode getConnectedNodeFlat(int portNum);
    public INode getConnectedNode(int relativeNestingDepth, int portNum);
    public IMapList<INode> getConnectedNodes(int relativeNestingDepth);
    public IMapList<INode> getConnectedNodesFlat();
    public IMapList<INode> getConnectedNodesTop();

    //TODO: should these only be on solver?
    public void update() ;
	public void updateEdge(int outPortNum);
	public void updateEdge(INode other);
	
	public @Nullable ISolverNode getSolver();
	
	public void setParentGraph(@Nullable FactorGraph parentGraph) ;
	public @Nullable FactorGraph getParentGraph();
	public @Nullable FactorGraph getRootGraph();
	public boolean hasParentGraph();
	public int getPortNum(INode node) ;
	public ArrayList<INode> getConnectedNodeAndParents(int index);
	public Port getPort(int i);
	public ArrayList<Port> getPorts();
	
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
	
	public double getScore() ;
	public double getInternalEnergy() ;
	public double getBetheEntropy() ;
	
	// REFACTOR: give this a better name to make it clear this is returning the siblings index
	// back to this node, e.g. getSiblingInversePortIndex. Also, wouldn't it be better to use the
	// term "edge index" or "sibling index" instead?
	public int getSiblingPortIndex(int index);
	
	public void initialize();
	public void initialize(int portNum);

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
}
