/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;

/**
 * A utility class for topologically sorting directed nodes.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class DirectedNodeSorter
{
	/**
	 * Computes a topological ordering for directed nodes in graph.
	 * <p>
	 * This assigns a non-negative integer to each node with at least one
	 * directed edge such that each node has a value that is greater than
	 * the value of the nodes that precede it. This function does not detect
	 * directed cycles and will not assign meaningful orders to nodes in a cycle.
	 * <p>
	 * @param fg is the factor graph whose nodes will be ordered.
	 * @return a mapping from each node with at least one directed edge to an
	 * integer. Nodes without directed edges will not be included in the map.
	 * @since 0.07
	 */
	public static Map<Node, Integer> orderDirectedNodes(FactorGraph fg)
	{
		//
		// Build representation of directed portion of graph.
		//
		
		final Map<Node,NodeInfo> nodes = new HashMap<>();
		
		for (Factor factor : fg.getFactorsFlat())
		{
			final int[] directedTo = factor.getDirectedTo();
			if (directedTo == null)
			{
				continue;
			}
			
			final int[] directedFrom = requireNonNull(factor.getDirectedFrom());
			final int toSize = directedTo.length;
			final int fromSize = directedFrom.length;
			
			VarInfo[] toVars = new VarInfo[toSize];
			for (int i = 0; i < toSize; ++i)
			{
				Variable variable = factor.getSibling(directedTo[i]);
				VarInfo varInfo = (VarInfo)nodes.get(variable);
				if (varInfo == null)
				{
					varInfo = new VarInfo(variable);
					nodes.put(variable,  varInfo);
				}
				varInfo.addPrevFactor();
				toVars[i] = varInfo;
			}

			FactorInfo factorInfo = new FactorInfo(factor, toVars, fromSize);
			nodes.put(factor, factorInfo);

			for (int i = 0; i < fromSize; ++i)
			{
				Variable variable = factor.getSibling(directedFrom[i]);
				VarInfo varInfo = (VarInfo)nodes.get(variable);
				if (varInfo == null)
				{
					varInfo = new VarInfo(variable);
					nodes.put(variable,  varInfo);
				}
				varInfo.addNextFactor(factorInfo);
			}
		}
		
		if (nodes.isEmpty())
		{
			return Collections.emptyMap();
		}
		
		//
		//
		//
		
		final Map<Node,Integer> orderingMap = new HashMap<>(nodes.size());
		
		Queue<NodeInfo> queue = new ArrayDeque<>(nodes.size());
		for (NodeInfo nodeInfo : nodes.values())
		{
			if (nodeInfo._order == 0)
			{
				queue.add(nodeInfo);
			}
		}
		
		int order = 0;
		
		while (!queue.isEmpty())
		{
			NodeInfo nodeInfo = queue.remove();
			orderingMap.put(nodeInfo._node, nodeInfo._order);
			
			for (int i = 0, n = nodeInfo.nNextNodes(); i < n; ++i)
			{
				NodeInfo nextNode = nodeInfo.nextNode(i);
				if (++nextNode._order == 0)
				{
					nextNode._order = ++order;
					queue.add(nextNode);
				}
			}
		}
		
		return orderingMap;
	}
	
	private abstract static class NodeInfo
	{
		private final Node _node;
		protected int _order = 0;
		
		private NodeInfo(Node node)
		{
			_node = node;
		}
		
		protected abstract int nNextNodes();
		
		protected abstract NodeInfo nextNode(int i);
	}
	
	private static class FactorInfo extends NodeInfo
	{
		private final VarInfo[] _nextVars;
		
		private FactorInfo(Factor factor, VarInfo[] nextVars, int nPrevVars)
		{
			super(factor);
			_nextVars = nextVars;
			_order = -nPrevVars;
		}
		
		@Override
		protected int nNextNodes()
		{
			return _nextVars.length;
		}
		
		@Override
		protected VarInfo nextNode(int i)
		{
			return _nextVars[i];
		}
	}
	
	private static class VarInfo extends NodeInfo
	{
		private final ArrayList<FactorInfo> _nextFactors = new ArrayList<>();
		
		private VarInfo(Variable var)
		{
			super(var);
		}
		
		@Override
		protected int nNextNodes()
		{
			return _nextFactors.size();
		}
		
		@Override
		protected FactorInfo nextNode(int i)
		{
			return _nextFactors.get(i);
		}

		private void addNextFactor(FactorInfo factorInfo)
		{
			_nextFactors.add(factorInfo);
		}
		
		private void addPrevFactor()
		{
			--_order;
		}
	}
}
