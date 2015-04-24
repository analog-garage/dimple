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
import java.util.List;
import java.util.Map;

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
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubgraphScheduleEntry;
import com.analog.lyric.dimple.solvers.core.proposalKernels.IBlockProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.util.misc.Internal;

/**
 * A schedule for producing a fixed custom schedule for a given graph.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class CustomScheduler extends SchedulerBase
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
	public ISchedule createSchedule(FactorGraph graph)
	{
		validateForGraph(graph);
		return new FixedSchedule(this, graph, _entries);
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
	 * CustomScheduler methods
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
	
	public void addBlock(IBlockUpdater blockUpdater, VariableBlock block)
	{
		addEntry(new BlockScheduleEntry(blockUpdater, block));
	}
	
	public void addBlock(IBlockProposalKernel proposalKernel, VariableBlock block)
	{
		addEntry(new BlockScheduleEntry(new BlockMHSampler(proposalKernel), block));
	}
	
	public void addBlock(IBlockUpdater blockUpdater, Variable ... variables)
	{
		addBlock(blockUpdater, _graph.addVariableBlock(variables));
	}
	
	public void addBlock(IBlockProposalKernel proposalKernel, Variable ... variables)
	{
		addBlock(proposalKernel, _graph.addVariableBlock(variables));
	}
	
	public void addEdge(Port edge)
	{
		addEdge(edge.getNode(), edge.getSiblingNumber());
	}

	public void addEdge(INode node, int siblingNumber)
	{
		addEntry(new EdgeScheduleEntry(node, siblingNumber));
	}
	
	public void addFactor(Factor factor)
	{
		addEntry(new NodeScheduleEntry(factor));
	}
	
	public void addNode(INode node)
	{
		addEntry(node instanceof FactorGraph ?
			new SubgraphScheduleEntry((FactorGraph)node) :
				new NodeScheduleEntry(node));
	}
	
	public void addVariable(Variable var)
	{
		addEntry(new NodeScheduleEntry(var));
	}
	
	public void addVariables(Variable ... vars)
	{
		int size = _entries.size();
		try
		{
			for (Variable var : vars)
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
			!(child instanceof Variable && !_graph.isBoundaryVariable((Variable)child)))
		{
			throw new ScheduleValidationException("Cannot add entry containing %s because it is not in root graph %s",
				child, _graph);
		}
	}
}
