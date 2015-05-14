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

package com.analog.lyric.dimple.schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.IFactorGraphChild;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubgraphScheduleEntry;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.util.misc.Internal;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * A schedule for producing a fixed custom schedule for a given graph.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class CustomScheduler extends SchedulerBase implements IGibbsScheduler
{
	private static final long serialVersionUID = 1L;
	
	/*-------
	 * State
	 */
	
	private final ArrayList<IScheduleEntry> _entries = new ArrayList<>();
	
	private FactorGraph _graph;
	
	private final @Nullable SchedulerOptionKey _schedulerKey;
	
	/*--------------
	 * Construction
	 */

	/**
	 * Construct a new custom scheduler for given graph and scheduler type.
	 * <p>
	 * @param graph is the graph for which the custom schedule will be generated.
	 * @param schedulerType identifies what type of schedules this will produce
	 * and will be returned by {@link #applicableSchedulerOptions()}.
	 * The schedule type may constrain what types of entries may be added. For
	 * instance, Gibbs schedules may not contain edge or factor update entries.
	 * @since 0.08
	 */
	public CustomScheduler(FactorGraph graph, SchedulerOptionKey schedulerType)
	{
		_graph = graph;
		_schedulerKey = schedulerType;
	}
	
	/**
	 * @category internal
	 */
	@Deprecated
	@Internal
	public CustomScheduler(FactorGraph graph)
	{
		_graph = graph;
		_schedulerKey = null;
	}
	
	CustomScheduler(CustomScheduler other, Map<Object,Object> old2new, boolean copyToRoot)
	{
		_graph = (FactorGraph) old2new.get(other.getGraph());
		_schedulerKey = other._schedulerKey;
		_entries.ensureCapacity(other._entries.size());
		for (IScheduleEntry entry : other._entries)
		{
			IScheduleEntry entryCopy = entry.copy(old2new, copyToRoot);
			if (entryCopy != null)
			{
				_entries.add(entryCopy);
			}
		}
	}

	/*----------------------
	 * IOptionValue methods
	 */
	
	@Override
	public boolean isMutable()
	{
		return true;
	}

	/*--------------------
	 * IScheduler methods
	 */
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * If {@link #declaredSchedulerType()} is not null, that will be returned. Otherwise this
	 * will return a list containing both {@link BPOptions#scheduler} and {@link GibbsOptions#scheduler}.
	 */
	@Override
	public List<SchedulerOptionKey> applicableSchedulerOptions()
	{
		SchedulerOptionKey schedulerKey = _schedulerKey;
		if (schedulerKey != null)
		{
			return Collections.singletonList(_schedulerKey);
		}
		else
		{
			return Arrays.asList(BPOptions.scheduler, GibbsOptions.scheduler);
		}
	}
	
	@Override
	public IScheduler copy(Map<Object, Object> old2NewMap, boolean copyToRoot)
	{
		return new CustomScheduler(this, old2NewMap, copyToRoot);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns fixed schedule specified by this custom scheduler (created through
	 * the various add* methods).
	 * <p>
	 * @throws ScheduleValidationException if {@code graph} is not the same as the
	 * {@linkplain #getGraph graph} for which this scheduler was constructed.
	 */
	@Override
	public FixedSchedule createSchedule(FactorGraph graph)
	{
		validateForGraph(graph);
		return new FixedSchedule(this, graph, _entries);
	}
	
	@Override
	public FixedSchedule createSchedule(ISolverFactorGraph solverGraph)
	{
		return createSchedule(solverGraph.getModelObject());
	}

	@Override
	public boolean isCustomScheduler()
	{
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * CustomerSchedulers are only valid for use on the {@link #getGraph() graph} for which they were created.
	 */
	@Override
	public void validateForGraph(FactorGraph graph)
	{
		if (graph != _graph)
		{
			throw new ScheduleValidationException("This scheduler instance can only be used with graph '%s'", _graph);
		}
	}
	
	/*-------------------------
	 * IGibbsScheduler methods
	 */
	
	@Deprecated
	@Override
	public void addBlockScheduleEntry(BlockScheduleEntry blockScheduleEntry)
	{
		addBlockWithReplacement(blockScheduleEntry.getBlockUpdater(), blockScheduleEntry.getBlock());
	}
	
	@Override
	public void addBlockWithReplacement(IBlockUpdater blockUpdater, final VariableBlock block)
	{
		Iterables.removeIf(_entries, new Predicate<IScheduleEntry>() {
			@NonNullByDefault(false)
			@Override
			public boolean apply(IScheduleEntry entry)
			{
				switch (entry.type())
				{
				case NODE:
					return block.contains(((NodeScheduleEntry)entry).getNode());
					
				case EDGE:
					return block.contains(((EdgeScheduleEntry)entry).getNode());
					
				default:
					return false;
				}
			}
		});
		addBlock(blockUpdater, block);
	}
	
	/*-------------------------
	 * CustomScheduler methods
	 */

	/**
	 * Adds all entries to the custom schedule in order.
	 * <p>
	 * Note that the deprecated {@code SubScheduleEntry} type is not supported by this method.
	 * <p>
	 * @since 0.08
	 */
	public void addAll(Iterable<? extends IScheduleEntry> entries)
	{
		if (entries instanceof Collection)
		{
			_entries.ensureCapacity(((Collection<?>)entries).size() + _entries.size());
		}
		for (IScheduleEntry entry : entries)
		{
			addEntry(entry);
		}
	}
	
	/**
	 * Adds a block schedule entry using given updater and variable block.
	 * @since 0.08
	 */
	public void addBlock(IBlockUpdater blockUpdater, VariableBlock block)
	{
		addEntry(new BlockScheduleEntry(blockUpdater, block));
	}

	/**
	 * Adds a block schedule entry using given updater and variable block defined from specified variables.
	 * <p>
	 * This will define a {@link VariableBlock} for the given {@code variables} on the {@linkplain #getGraph()
	 * graph} associated with this scheduler.
	 * <p>
	 * @since 0.08
	 * @see #addBlock(IBlockUpdater, VariableBlock)
	 */
	public void addBlock(IBlockUpdater blockUpdater, Variable ... variables)
	{
		addBlock(blockUpdater, _graph.addVariableBlock(variables));
	}
	
	/**
	 * Adds an edge schedule entry for the given port.
	 * <p>
	 * @param edge a {@link Port} object describing the originating node and index of outgoing edge.
	 * @since 0.08
	 */
	public void addEdge(Port edge)
	{
		addEdge(edge.getNode(), edge.getSiblingNumber());
	}
	
	/**
	 * Adds edge schedule entries for the given ports.
	 * <p>
	 * @since 0.08
	 * @see #addEdge(Port)
	 */
	public void addEdges(Port ... edges)
	{
		for (Port port : edges)
			addEdge(port);
	}

	/**
	 * Adds edge schedule entry for given node and {@code siblingNumber} of outgoing edge.
	 * @since 0.08
	 */
	public void addEdge(INode node, int siblingNumber)
	{
		addEntry(new EdgeScheduleEntry(node, siblingNumber));
	}
	
	/**
	 * Adds edge update from {@code source} to {@code target} nodes.
	 * <p>
	 * If there is more than one edge between {@code source} and {@code target}, then
	 * this will produce an update for the lowest numbered edge from the perspective of
	 * {@code source}.
	 * <p>
	 * @param source either a variable or factor
	 * @param target a factor or variable that is connected to {@code source}.
	 * @since 0.08
	 * @see #addEdge(INode, int)
	 */
	public void addEdge(INode source, INode target)
	{
		addEdge(source, source.findSibling(target));
	}
	
	/**
	 * Add node update for given {@code factor}.
	 * @since 0.08
	 */
	public void addFactor(Factor factor)
	{
		addEntry(new NodeScheduleEntry(factor));
	}
	
	/**
	 * Add node update for {@code factors} in given order.
	 * @since 0.08
	 */
	public void addFactors(Factor ... factors)
	{
		for (Factor factor : factors)
			addFactor(factor);
	}
	
	/**
	 * Add node update for given {@code node}.
	 * <p>
	 * If {@code node} is a {@link FactorGraph}, a subgraph schedule entry will be added, otherwise
	 * a node update entry will be added.
	 * @since 0.08
	 */
	public void addNode(INode node)
	{
		addEntry(node instanceof FactorGraph ?
			new SubgraphScheduleEntry((FactorGraph)node) :
				new NodeScheduleEntry(node));
	}
	
	/**
	 * Adds node updates for {@code nodes} in given order.
	 * @since 0.08
	 * @see #addNode(INode)
	 */
	public void addNodes(INode ... nodes)
	{
		for (INode node : nodes)
		{
			addNode(node);
		}
	}
	
	/**
	 * Adds edge entries following path through specified nodes.
	 * <p>
	 * Creates edge update entries starting from first node and terminating with last node.
	 * <p>
	 * @param nodes each node must not be separated from the previous node by more than one other node
	 * and there must be only one possible path between nodes.
	 * 
	 * @since 0.08
	 */
	public void addPath(INode ... nodes)
	{
		addPath(Arrays.asList(nodes));
	}
	
	/**
	 * Adds edge entries following path through specified nodes.
	 * <p>
	 * Creates edge update entries starting from first node and terminating with last node.
	 * <p>
	 * @param nodes each node must not be separated from the previous node by more than one other node
	 * and there must be only one possible path between nodes.
	 * 
	 * @since 0.08
	 */
	public void addPath(List<INode> nodes)
	{
		// TODO - extend this method to support nodes that are farther than two edges apart if there is a
		// unique path.
		
		if (nodes.size() < 2)
		{
			throw new ScheduleValidationException("addPath requires at least two nodes");
		}
		
		final Iterator<INode> iter = nodes.iterator();
		for (INode from = iter.next(), to = null; iter.hasNext() && (to = iter.next()) != null; from = to)
		{
			int toi = from.findSibling(to);
			if (toi >= 0)
			{
				if (from.findSibling(to, toi + 1) >= 0)
				{
					throw new ScheduleValidationException("There is not a unique path from %s to %s", from, to);
				}
				addEdge(from, to);
			}
			else
			{
				// Not connected directly. See if there is a common sibling.
				Set<INode> commonSiblings = new HashSet<>(from.getSiblings());
				commonSiblings.retainAll(to.getSiblings());

				switch (commonSiblings.size())
				{
				case 0:
					throw new ScheduleValidationException(
						"Nodes %s and %s are not adjacent and don't share common sibling",
						from, to);
				case 1:
					INode middle = Iterables.getOnlyElement(commonSiblings);
					addEdge(from, middle);
					addEdge(middle, to);
					break;
				default:
					throw new ScheduleValidationException(
						"Nodes %s and %s are connected by more than one path", from, to);
				}
			}
		}
	}
	
	/**
	 * Adds node update for given {@code variable}.
	 * @param variable
	 * @since 0.08
	 */
	public void addVariable(Variable variable)
	{
		addEntry(new NodeScheduleEntry(variable));
	}
	
	/**
	 * Adds node update for {@code variables} in given order.
	 * @since 0.08
	 * @see #addVariable(Variable)
	 */
	public void addVariables(Variable ... variables)
	{
		int size = _entries.size();
		try
		{
			for (Variable var : variables)
			{
				addVariable(var);
			}
			size = _entries.size();
		}
		finally
		{
			// If an exception is thrown, remove any added entries.
			for (int i = _entries.size(); --i>=size;)
			{
				_entries.remove(i);
			}
		}
	}
	
	/**
	 * Adds a subgraph schedule entry for given subgraph.
	 * <p>
	 * @param graph must be a subgraph of {@linkplain #getGraph() graph} associated with scheduler.
	 * @since 0.08
	 */
	public void addSubgraph(FactorGraph graph)
	{
		addEntry(new SubgraphScheduleEntry(graph));
	}
	
	/**
	 * The scheduler type that was declared when this scheduler was constructed.
	 * <p>
	 * This should only be null if custom scheduler was created using deprecated
	 * {@link FactorGraph#setSchedule} interface or through the MATLAB interface.
	 * <p>
	 * @since 0.08
	 * @see #CustomScheduler(FactorGraph, SchedulerOptionKey)
	 * @see #applicableSchedulerOptions()
	 */
	public @Nullable SchedulerOptionKey declaredSchedulerType()
	{
		return _schedulerKey;
	}
	
	/**
	 * The graph for which the scheduler was constructed.
	 * <p>
	 * The scheduler will not be able to be used on graphs other than this one and any entries
	 * added to the custom schedule must be for objects that are in this graph or its subgraphs.
	 * @since 0.08
	 * @see #CustomScheduler(FactorGraph, SchedulerOptionKey)
	 */
	public FactorGraph getGraph()
	{
		return _graph;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	/**
	 * Validates entry, infers scheduler key if necessary, and adds to list.
	 */
	@SuppressWarnings("deprecation") // for SUBSCHEDULE
	private void addEntry(IScheduleEntry entry)
	{
		switch (entry.type())
		{
		case EDGE:
		{
			EdgeScheduleEntry edgeEntry = (EdgeScheduleEntry)entry;
			INode node = edgeEntry.getNode();
			assertInGraph(node);
			if (_schedulerKey == GibbsOptions.scheduler)
			{
				throw new ScheduleValidationException("Cannot use edge entry with Gibbs schedule");
			}
			break;
		}
		case NODE:
		{
			NodeScheduleEntry nodeEntry = (NodeScheduleEntry)entry;
			INode node = nodeEntry.getNode();
			if (!node.isVariable() && _schedulerKey == GibbsOptions.scheduler)
			{
				throw new ScheduleValidationException("Cannot use factor node entry with Gibbs schedule");
			}
			assertInGraph(node);
			break;
		}
		case SUBGRAPH:
		{
			SubgraphScheduleEntry graphEntry = (SubgraphScheduleEntry)entry;
			FactorGraph subgraph  = graphEntry.getSubgraph();
			assertInGraph(subgraph);
			break;
		}
		case SUBSCHEDULE:
			throw new ScheduleValidationException("Cannot add SubScheduleEntry to CustomScheduler");
		case VARIABLE_BLOCK:
		{
			BlockScheduleEntry blockEntry = (BlockScheduleEntry)entry;
			assertInGraph(blockEntry.getBlock());
			break;
		}
		case CUSTOM:
			break;
		}
		
		_entries.add(entry);
	}
	
	/**
	 * Verifies that child is in graph tree rooted at {@link _graph} or is a boundary
	 * variable of the graph.
	 */
	private void assertInGraph(IFactorGraphChild child)
	{
		final FactorGraph parent = child.getParentGraph();
		if (_graph != parent &&
			!_graph.isAncestorOf(child.getContainingGraph()) &&
			!(child instanceof Variable && _graph.isBoundaryVariable((Variable)child)))
		{
			throw new ScheduleValidationException("Cannot add entry containing %s because it is not in root graph %s",
				child, _graph);
		}
	}
}
