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

import static java.util.Objects.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.analog.lyric.dimple.model.variables.Real;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An iterator that walks through connected nodes in a {@link FactorGraph}
 * using specified {@link #searchOrder} starting from an optionally provided
 * factor or variable node. This will only visit nodes that are reachable from
 * the initial node and that are contained within the specified graph or one
 * of its direct or indirect subgraphs.
 */
public class FactorGraphWalker implements Iterator<INode>
{

	/**
	 * Specifies a search order for visiting nodes in the {@link FactorGraph}. Both
	 * use preorder traversal: i.e. a node is always visited before going on to
	 * its adjacent nodes. Both will also visit a node no more than once, skipping
	 * nodes it has seen before and will increment the cycle count ({@link #getCycleCount})
	 * every time it does so. Both methods will honor the specified {@link #maxSearchDepth}
	 * and will not visit nodes that take more than that many steps to reach.
	 * <dl>
	 * <dt>{@literal DEPTH_FIRST}</dt>
	 * <dd>After visiting a given node each adjacent node
	 * (excluding the one that lead to the node) and nodes connected to it are explored
	 * up the specified {@link #maxSearchDepth} before visiting the next adjacent
	 * node.
	 * </dd>
	 * <dt>{@literal BREADH_FIRST}</dt>
	 * <dd>Visits all nodes at depth n from the starting node before visiting those at
	 * distance n + 1.
	 * </dd>
	 * </dl>
	 */
	public enum Order { DEPTH_FIRST, BREADTH_FIRST }
	
	/*
	 * State
	 */
	
	private @Nullable FactorGraph _rootGraph;
	private @Nullable INode _firstNode;
	private Order _searchOrder = Order.BREADTH_FIRST;
	private int _maxSearchDepth = Integer.MAX_VALUE;
	private int _maxRelativeNestingDepth;

	private Set<INode> _visitedNodes = new LinkedHashSet<INode>();
	private boolean _visitedNodesWasExposed = true;
	private Deque<Port> _portDeque = new ArrayDeque<Port>();
	private int _currentDepth;
	private int _maxDepthSeen;
	private @Nullable INode _nextNode;
	private int _cycleCount;
	private final Port _depthChangeSentinel = new Port(new Real(),-42);
	

	/*
	 * Construction/initialization
	 */
	
	/**
	 * Creates a new walker initialized using {@link #init(FactorGraph, INode)}.
	 */
	public FactorGraphWalker(FactorGraph graph, INode firstNode)
	{
		this.init(graph, firstNode);
	}
	
	/**
	 * Creates a new walker initialized using {@link #init(FactorGraph)}.
	 */
	public FactorGraphWalker(FactorGraph graph)
	{
		this.init(graph);
	}
	
	/**
	 * Creates a new walker initialized using {@link #init()}.
	 */
	public FactorGraphWalker()
	{
		this.init();
	}
	
	/**
	 * Starts search over again using previously specified values
	 * for root graph ({@link #getRootGraph}), starting node {@link #getFirstNode},
	 * {@link #searchOrder}, {@link #maxSearchDepth} and
	 * {@link #maxRelativeNestingDepth}.
	 */
	public FactorGraphWalker init()
	{
		if (this._visitedNodesWasExposed)
		{
			// Someone might still be using the old copy, so we have to make a new one.
			this._visitedNodes = new LinkedHashSet<INode>();
		}
		else
		{
			this._visitedNodes.clear();
		}
		this._portDeque.clear();
		this._currentDepth = 0;
		this._maxDepthSeen = 0;
		this._nextNode = null;
		this._cycleCount= 0;
		
		FactorGraph root = this._rootGraph;
		if (root != null)
		{
			INode node = this._firstNode != null ? this._firstNode : root.getFirstNode();
			if (node != null)
			{
				this._portDeque.add(new Port(node,-1));
			}
		}
		
		return this;
	}
	
	/**
	 * Starts search over using given root graph {@link #getRootGraph}. The
	 * previous value of {@link #getFirstNode} will be cleared. It is ok
	 * for {@code graph} to be null. Otherwise the same as {@link #init()}.
	 */
	public FactorGraphWalker init(FactorGraph graph)
	{
		return this.init(graph, null);
	}
	
	/**
	 * Starts search over using given root graph {@link #getRootGraph} and
	 * starting node ({@link #getFirstNode}). It is ok
	 * for {@code graph} and/or {@code firstNode} to be null, but if non-null
	 * {@code firstNode} must have {@code graph} as an ancestor.
	 * Otherwise the same as {@link #init()}.
	 */
	public FactorGraphWalker init(@Nullable FactorGraph graph, @Nullable INode firstNode)
	{
		this._rootGraph = graph;
		this._firstNode = firstNode;
		if (graph != null && firstNode != null)
		{
			assert(graph.isAncestorOf(firstNode));
		}
		return this.init();
	}

	/**
	 * Returns the maximum {@link FactorGraph} nesting depth that will
	 * be explored by the search.
	 * @see #maxRelativeNestingDepth(int)
	 */
	public final int maxRelativeNestingDepth()
	{
		return this._maxRelativeNestingDepth;
	}
	
	/**
	 * Sets the maximum {@link FactorGraph} nesting depth that will
	 * be explored by the search. When set to the maximum (the default)
	 * every connected node in any direct or indirect subgraph will be
	 * included. Setting it to 0 will restrict the search to nodes that
	 * are contained directly in the root graph.
	 * <p>
	 * Changing this value after visiting some nodes may produce
	 * different results than setting it to that value initially.
	 */
	public final FactorGraphWalker maxRelativeNestingDepth(int depth)
	{
		this._maxRelativeNestingDepth = depth;
		return this;
	}
	
	/**
	 * Returns the maximum depth in the graph that will be searched with
	 * respect to the starting node. The default is the maximum integer value.
	 */
	public final int maxSearchDepth()
	{
		return this._maxSearchDepth;
	}
	
	/**
	 * Sets the maximum depth in the graph that will be searched with
	 * respect to the starting node. Changing this value after the
	 * search has started may result in a different set of nodes visited
	 * than if that depth had been specified initially.
	 */
	public final FactorGraphWalker maxSearchDepth(int depth)
	{
		this._maxSearchDepth = depth;
		return this;
	}
	
	/**
	 * Cause nodes to be visited using {@literal BREADH_FIRST} search {@link Order}.
	 * Changing the search order after calling {@link #hasNext} or {@link #next}
	 * will result in unpredictable behavior.
	 * @see #useBreadthFirst
	 * @see #searchOrder
	 */
	public final FactorGraphWalker useBreadthFirst()
	{
		assert(getVisitedNodesSize() == 0);
		this._searchOrder = Order.BREADTH_FIRST;
		return this;
	}
	
	/**
	 * Cause nodes to be visited using {@literal DEPTH_FIRST} search {@link Order}.
	 * Changing the search order after calling {@link #hasNext} or {@link #next}
	 * will result in unpredictable behavior.
	 * @see #useBreadthFirst
	 * @see #searchOrder
	 */
	public final FactorGraphWalker useDepthFirst()
	{
		assert(getVisitedNodesSize() == 0);
		this._searchOrder = Order.DEPTH_FIRST;
		return this;
	}
	
	/**
	 * Returns the search {@link Order} in which nodes will be visited.
	 * The default value is {@literal BREADTH_FIRST}.
	 * @see #useBreadthFirst
	 * @see #useDepthFirst
	 */
	public final Order searchOrder()
	{
		return this._searchOrder;
	}
	
	/*
	 * Iterator methods
	 */
	
	/** True if call to {@link #next} will return a non-null value. */
	@Override
	public boolean hasNext()
	{
		if (this._nextNode != null)
		{
			return true;
		}
		
		if (this._portDeque.isEmpty())
		{
			return false;
		}
		
		this._nextNode = this.next();
		
		return this._nextNode != null;
	}
	
	/**
	 * Returns the next node in the graph in the given search order.
	 * Nodes that do not meet the {@link #maxSearchDepth} and
	 * {@link #maxRelativeNestingDepth} constraints will not be included.
	 * Returns null when there are no more nodes to visit. It is not
	 * necessary to call {@link FactorGraphWalker#hasNext} before calling
	 * this method.
	 */
	@Override
	public @Nullable INode next()
	{
		INode node = this._nextNode;
		
		if (node != null)
		{
			this._nextNode = null;
			return node;
		}
		
		switch (this._searchOrder)
		{
		case DEPTH_FIRST:
			node = this.nextDepthFirst();
			break;
		case BREADTH_FIRST:
			node = this.nextBreadthFirst();
			break;
		}
		
		return node;
	}
	
	/** Default implementation throws {@link UnsupportedOperationException} */
	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
	
	/*
	 * FactorGraphWalker methods
	 */
	
	/**
	 * True if the walker detected any cycles in the graph when walking the
	 * graph up to the current point.
	 */
	public final int getCycleCount()
	{
		return this._cycleCount;
	}
	
	/**
	 * Returns the first node to be visited in the graph traversal. If
	 * null, an arbitrary node will be selected from the graph.
	 */
	public final @Nullable INode getFirstNode()
	{
		return this._firstNode;
	}
	
	/**
	 * Returns the root graph that is being traversed. Set by
	 * constructor or {@link #init(FactorGraph)} method.
	 */
	public final @Nullable FactorGraph getRootGraph()
	{
		return this._rootGraph;
	}
	
	/**
	 * Returns collection of nodes that were visited by the walker so far in the order in which
	 * they were visited.
	 * @see #getVisitedNodesSize
	 */
	public final Set<INode> getVisitedNodes()
	{
		this._visitedNodesWasExposed = true;
		return this._visitedNodes;
	}
	
	/**
	 * Returns the number of unique nodes that have been visited by the walker so far.
	 * @see #getVisitedNodes
	 */
	public final int getVisitedNodesSize()
	{
		return this._visitedNodes.size();
	}
	
	/*
	 * Private methods
	 */
	
	private @Nullable INode nextBreadthFirst()
	{
		INode node = null;
	
		Deque<Port> queue = this._portDeque;
		
		while (!queue.isEmpty())
		{
			Port portIn = queue.removeFirst();
			if (portIn == _depthChangeSentinel)
			{
				++this._currentDepth;
				continue;
			}
			
			node = portIn.node;
			int relativeDepth = node.getDepthBelowAncestor(Objects.requireNonNull(this._rootGraph));
			
			if (relativeDepth < 0)
			{
				// Skip nodes not descended from root.
				continue;
			}
			
			if (relativeDepth > this._maxRelativeNestingDepth)
			{
				// If node is too deep, replace with ancestor at appropriate depth.
				node = node.getAncestorAtHeight(relativeDepth - this._maxRelativeNestingDepth);
			}
			
			if (!this._visitedNodes.add(node))
			{
				// Skip nodes that have already been visited but add to cycle count.
				++ this._cycleCount;
				continue;
			}
			
			List<? extends INode> siblingNodes = requireNonNull(node).getSiblings();
			int nSiblings = siblingNodes.size();
			if (nSiblings > 0)
			{
				int newDepth = this._currentDepth + 1;
				if (newDepth <= this._maxSearchDepth)
				{
					if (newDepth > this._maxDepthSeen)
					{
						this._maxDepthSeen = newDepth;
						queue.add(_depthChangeSentinel);
					}
					for (int i = 0, size = nSiblings; i < size; ++i)
					{
						INode siblingNode = siblingNodes.get(i);
						if (i != portIn.index)
						{
							queue.add(new Port(siblingNode,siblingNode.getPortNum(node)));
						}
					}
				}
			}
			
			return node;
		}
		
		return null;
	}

	private @Nullable INode nextDepthFirst()
	{
		INode node = null;
		
		Deque<Port> stack = this._portDeque;
		
		while (!stack.isEmpty())
		{
			Port portIn = stack.pop();
			if (portIn == _depthChangeSentinel)
			{
				--this._currentDepth;
				continue;
			}
			
			node = portIn.node;
			int relativeDepth = node.getDepthBelowAncestor(Objects.requireNonNull(this._rootGraph));
			
			if (relativeDepth < 0)
			{
				// Skip nodes not descended from given root.
				continue;
			}
			
			if (relativeDepth > this._maxRelativeNestingDepth)
			{
				// If node is too deep, replace with ancestor at appropriate depth.
				node = node.getAncestorAtHeight(relativeDepth - this._maxRelativeNestingDepth);
			}
			
			if (!this._visitedNodes.add(node))
			{
				// Skip nodes that have already been visited but add to cycle count.
				++this._cycleCount;
				continue;
			}
			
			List<? extends INode> portsOut = requireNonNull(node).getSiblings();
			int nPortsOut = portsOut.size();
			if (nPortsOut > 0)
			{
				int newDepth = this._currentDepth + 1;
				if (newDepth <= this._maxSearchDepth)
				{
					stack.push(_depthChangeSentinel);
					this._currentDepth = newDepth;
					this._maxDepthSeen = Math.max(this._maxDepthSeen, this._currentDepth);
					for (int i = 0, size = nPortsOut; i < size; ++i)
					{
						INode portOut = portsOut.get(i);
						if (i != portIn.index)
						{
							stack.push(new Port(portOut,portOut.getPortNum(node)));
						}
					}
				}
			}
			
			return node;
		}
		
		return null;
	}
	
}
