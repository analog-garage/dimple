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

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.util.misc.MapList;


public interface INode  extends INameable
{
	/**
	 * If node is a {@link Factor} returns it, otherwise null.
	 * @see #isFactor()
	 */
	public Factor asFactor();
	
	/**
	 * If node is a {@link FactorGraph} returns it, otherwise null.
	 * @see #isFactorGraph()
	 */
	public FactorGraph asFactorGraph();
	
	/**
	 * If node is a {@link VariableBase} returns it, otherwise null.
	 * @see #isVariable()
	 */
	public VariableBase asVariable();
	
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
	 * True if this is a {@link VariableBase}
	 * @see #asVariable()
	 */
	public boolean isVariable();

    public ArrayList<INode> getSiblings();
    public void connect(INode node);
    public boolean isConnected(INode node);
    public boolean isConnected(INode node, int portIndex);
    public MapList<INode> getConnectedNodes();
	public INode getConnectedNodeFlat(int portNum);
    public INode getConnectedNode(int relativeNestingDepth, int portNum);
    public MapList<INode> getConnectedNodes(int relativeNestingDepth);
    public MapList<INode> getConnectedNodesFlat();
    public MapList<INode> getConnectedNodesTop();

    //TODO: should these only be on solver?
    public void update() ;
	public void updateEdge(int outPortNum);
	public void updateEdge(INode other);
	
	public ISolverNode getSolver();
	
	public void setParentGraph(FactorGraph parentGraph) ;
	public FactorGraph getParentGraph();
	public FactorGraph getRootGraph();
	public boolean hasParentGraph();
	public int getPortNum(INode node) ;
	public ArrayList<INode> getConnectedNodeAndParents(int index);
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
	FactorGraph getAncestorAtHeight(int height);
	
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
	public int getSiblingPortIndex(int index);
	public void initialize();
	public void initialize(int portNum);

}
