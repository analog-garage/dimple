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

package com.analog.lyric.dimple.schedulers.validator;

import static java.util.Objects.*;

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.analog.lyric.dimple.model.core.EdgeState;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterables;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubgraphScheduleEntry;

/**
 * A schedule validator that makes sure that all edges in the graph are visited.
 * @since 0.08
 * @author Christopher Barber
 */
public class AllEdgeScheduleValidator extends InGraphScheduleValidator
{
	/*-------
	 * State
	 */
	
	/**
	 * Maps graph to BitSet describing which ports have been visited using two bits per
	 * edge with factor->variable before variable->factor
	 */
	private Map<FactorGraph, BitSet> _edgeSets = Collections.emptyMap();
	
	/*---------------------------
	 * ScheduleValidator methods
	 */
	
	@Override
	public void start(ISchedule schedule) throws ScheduleValidationException
	{
		super.start(schedule);
		
		// Set bits for all the edges that must be visited.
		HashMap<FactorGraph, BitSet> edgeSets = new HashMap<>();
		final FactorGraph fg = schedule.getFactorGraph();
		if (fg == null)
		{
			throw new ScheduleValidationException("Schedule is not associated with factor graph.");
		}
		for (FactorGraph subgraph : FactorGraphIterables.subgraphs(fg))
		{
			final BitSet edgesToVisit = new BitSet(subgraph.getGraphEdgeStateMaxIndex() * 2 + 2);
			edgeSets.put(subgraph, edgesToVisit);
			
			for (EdgeState edgeState : subgraph.getGraphEdgeState())
			{
				final int edgeOffset = edgeState.edgeIndexInParent(subgraph) * 2;
				
				switch (edgeState.type(subgraph))
				{
				case LOCAL:
					// Add both directions of edge
					edgesToVisit.set(edgeOffset, edgeOffset + 2);
					break;
					
				case OUTER:
					// Only add factor to variable direction
					edgesToVisit.set(edgeOffset);
					break;
					
				case INNER:
					// Only add variable to factor direction
					edgesToVisit.set(edgeOffset + 1);
				}
			}
		}
		_edgeSets = edgeSets;
	}

	@SuppressWarnings("deprecation") // SUBSCHEDULE
	@Override
	public void validateNext(IScheduleEntry entry) throws ScheduleValidationException
	{
		super.validateNext(entry);
		
		switch (entry.type())
		{
		case EDGE:
		{
			EdgeScheduleEntry edgeEntry = (EdgeScheduleEntry)entry;
			Port port = edgeEntry.getPort();
			FactorGraph graph = port.getParentGraph();
			BitSet edgesToVisit = _edgeSets.get(graph);
			if (edgesToVisit != null)
			{
				// ordinal == 0 for FACTOR and 1 for VARIABLE
				edgesToVisit.clear(port.toEdgeState().edgeIndexInParent(graph) * 2 + port.portType().ordinal());
			}
			break;
		}
		case NODE:
		{
			final NodeScheduleEntry nodeEntry = (NodeScheduleEntry)entry;
			final INode node = nodeEntry.getNode();
			final FactorGraph graph = node.getParentGraph();
			if (graph == null)
			{
				throw new ScheduleValidationException("%s has node with no parent graph", entry);
			}
			final BitSet edgesToVisit = _edgeSets.get(graph);
			if (edgesToVisit != null)
			{
				final int offset = node.isVariable() ? 1 : 0;
				for (int i = 0, n = node.getSiblingCount(); i < n; ++i)
				{
					edgesToVisit.clear(node.getSiblingEdgeState(i).edgeIndexInParent(graph)  * 2 + offset);
				}
			}
			break;
		}
		case VARIABLE_BLOCK:
			for (Variable var : ((BlockScheduleEntry)entry).getBlock())
			{
				for (int i = 0, n = var.getSiblingCount(); i < n; ++i)
				{
					FactorGraph graph = var.getParentGraph();
					if (graph == null)
					{
						throw new ScheduleValidationException("%s contains variable %s with no parent graph",
							entry, var);
					}
					final BitSet edgesToVisit = _edgeSets.get(graph);
					if (edgesToVisit != null)
					{
						edgesToVisit.clear(var.getSiblingEdgeState(i).variableEdgeIndex() + 1);
					}
				}
			}
			break;
		case SUBGRAPH:
			// If we see entry for graph, simply remove edges for graph and all of its subgraphs
			// and assume schedule for subgraphs will do the right thing.
			for (FactorGraph subgraph : FactorGraphIterables.subgraphs(((SubgraphScheduleEntry)entry).getSubgraph()))
			{
				_edgeSets.remove(subgraph);
			}
			break;
		case SUBSCHEDULE:
		{
			for (IScheduleEntry subentry :
				((com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry)entry).getSchedule())
			{
				validateNext(subentry);
			}
			break;
		}
		case CUSTOM:
			throw new ScheduleValidationException("%s does not support custom schedule entry %s",
				getClass().getSimpleName(), entry);
		}
	}
	
	@Override
	public void finish() throws ScheduleValidationException
	{
		for (Map.Entry<FactorGraph, BitSet> entry : _edgeSets.entrySet())
		{
			final int firstMissingEdge = entry.getValue().nextSetBit(0);
			if (firstMissingEdge >= 0)
			{
				// Look up edge by its index for the error message
				FactorGraph graph = entry.getKey();
				EdgeState edgeState = requireNonNull(graph.getGraphEdgeState(firstMissingEdge / 2));
				Factor factor = edgeState.getFactor(graph);
				Variable var = edgeState.getVariable(graph);
				Node from, to;
				if ((firstMissingEdge & 1) == 0)
				{
					from = factor; to = var;
				}
				else
				{
					from = var; to = factor;
				}
				throw new ScheduleValidationException("Missing edge entry from %s to %s in graph %s", from, to, graph);
			}
		}
		
		_edgeSets = Collections.emptyMap();
		super.finish();
	}
}
