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

package com.analog.lyric.dimple.matlabproxy;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.options.DimpleOptionRegistry;
import com.analog.lyric.dimple.schedulers.CustomScheduler;
import com.analog.lyric.dimple.schedulers.IGibbsScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.SchedulerOptionKey;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IBlockUpdater;
import com.analog.lyric.options.IOptionKey;
import com.analog.lyric.options.OptionKey;
import com.analog.lyric.util.misc.Matlab;

/**
 * MATLAB proxy for {@link IScheduler}
 * @since 0.08
 * @author Christopher Barber
 */
@Matlab
public class PScheduler extends PObject
{
	/*-------
	 * State
	 */
	
	private final IScheduler _scheduler;
	
	/*--------------
	 * Construction
	 */
	
	PScheduler(IScheduler scheduler)
	{
		_scheduler = scheduler;
	}
	
	/**
	 * Constructs a custom scheduler with specified entries.
	 * <p>
	 * @param pgraph identifies the graph for which the scheduler is being created
	 * @since 0.08
	 */
	PScheduler(PFactorGraphVector pgraph, SchedulerOptionKey schedulerKey, @Nullable Object[] scheduleEntries)
	{
		this(new CustomScheduler(pgraph.getGraph(), schedulerKey));
		addCustomEntries(scheduleEntries);
	}
	
	/**
	 * @deprecated only to support Matlab FactorGraph.Schedule setter
	 */
	@Deprecated
	PScheduler(PFactorGraphVector pgraph, @Nullable Object[] scheduleEntries)
	{
		this(new CustomScheduler(pgraph.getGraph()));
		addCustomEntries(scheduleEntries);
	}
	
	PScheduler(PFactorGraphVector pgraph, String schedulerKey, @Nullable Object[] scheduleEntries)
	{
		this(pgraph, lookupSchedulerKey(pgraph.getGraph().getEnvironment(), schedulerKey), scheduleEntries);
	}
	
	private static SchedulerOptionKey lookupSchedulerKey(DimpleEnvironment env, String schedulerKey)
	{
		final DimpleOptionRegistry options = env.optionRegistry();

		IOptionKey<?> key = null;
		
		if (schedulerKey.contains("."))
		{
			// If str contains a dot require an exact match.
			key = options.get(schedulerKey);
		}
		else
		{
			// Otherwise use a regexp
			ArrayList<IOptionKey<?>> keys = options.getAllMatching(Pattern.quote(schedulerKey) + "\\w+\\.scheduler");
			switch (keys.size())
			{
			case 0:
				break;
			case 1:
				key = keys.get(0);
				break;
			default:
				throw new ScheduleValidationException("'%s' is ambiguous could be any of: %s", schedulerKey, keys);
			}
		}

		if (key == null)
		{
			throw new ScheduleValidationException("'%s' does not refer to a known option key", schedulerKey);
		}
		if (!(key instanceof SchedulerOptionKey))
		{
			throw new ScheduleValidationException("'%s' does not refer to a scheduler option key", schedulerKey);
		}
		
		return (SchedulerOptionKey)key;
	}
	
	/*-----------------
	 * PObject methods
	 */
	
	@Override
	public IScheduler getDelegate()
	{
		return _scheduler;
	}
	
	@Override
	public IScheduler getModelerObject()
	{
		return _scheduler;
	}
	
	@Override
	public boolean isScheduler()
	{
		return true;
	}
	
	/*--------------------
	 * PScheduler methods
	 */

	public void addBlockScheduleEntry(IBlockUpdater blockUpdater, PVariableBlock block)
	{
		if (_scheduler instanceof IGibbsScheduler)
		{
			IGibbsScheduler scheduler = (IGibbsScheduler)_scheduler;
			scheduler.addBlockWithReplacement(blockUpdater, block.getDelegate());
		}
		else
		{
			throw new UnsupportedOperationException("addBlockScheduleEntry cannot be used on non-Gibbs scheduler");
		}
	}
	
	public void addBlockScheduleEntry(IBlockUpdater blockUpdater, Object[] variables)
	{
		addBlockScheduleEntry(blockUpdater, PFactorGraphVector.addVariableBlock(null, variables));
	}
	
	/**
	 * Add entries to a custom schedule.
	 * <p>
	 * @param scheduleEntries an ordered list of the following types of entries:
	 * <ul>
	 * <li>{@link SchedulerOptionKey} instance or its {@linkplain OptionKey#qualifiedName() qualified name}
	 * may be specified as the first entry in the list to identify the scheduler type.
	 * <li>{@link PNodeVector} containing node entries
	 * <li>An {@code Object[]} array containing two elements specifying an edge schedule entry. The first
	 * element is a single-node {@link PNodeVector} specifying the originating node, and the second entry
	 * specifying the destination node for the edge update. If there is more than one edge connecting the
	 * source and destination, this specifies the lowest numbered one with respect to the origin node.
	 * <li>A n {@code Object[]} array containing a block schedule entry specification. The first element
	 * is an instance of {@link IBlockUpdater} and the remaining entries should contain the variables in
	 * the block.
	 * </ul>
	 * @throws UnsupportedOperationException if this does not hold a {@link CustomScheduler}.
	 * @since 0.08
	 */
	public void addCustomEntries(@Nullable Object[] scheduleEntries)
	{
		final CustomScheduler scheduler = assertCustom();
		
		if (scheduleEntries == null)
		{
			return;
		}
		
		//Convert schedule to a list of nodes and edges
		for (Object entry : scheduleEntries)
		{
			if (entry instanceof Object[])
			{
				final Object[] array = (Object[])entry;
				
				if (array.length >= 2 && array[0] instanceof IBlockUpdater)
				{
					// This is a block schedule entry
					final IBlockUpdater blockUpdater = (IBlockUpdater)array[0];
					
					if (array[1] instanceof PVariableBlock)
					{
						scheduler.addBlock(blockUpdater, ((PVariableBlock)array[1]).getDelegate());
					}
					else
					{
						scheduler.addBlock(blockUpdater, PHelpers.convertToVariableArray(array, 1));
					}
				}
				else
				{
					// Entry is a pair of nodes, that represent an edge
					if (array.length != 2)
						throw new DimpleException("Length of array containing edge must be 2");

					INode node1 = PHelpers.convertToNode(array[0]);
					INode node2 = PHelpers.convertToNode(array[1]);
					int portNum = node1.findSibling(node2);

					scheduler.addEdge(node1, portNum);
				}
			}
			else
			{
				for (Node node : PHelpers.convertToNodeArray(entry))
				{
					scheduler.addNode(node);
				}
			}
		}
	}
	
	public void addCustomPath(Object[] nodes)
	{
		ArrayList<INode> list = new ArrayList<>(nodes.length);
		for (Object obj : nodes)
		{
			if (obj instanceof INode)
			{
				list.add((INode)obj);
			}
			else if (obj instanceof PNodeVector)
			{
				PNodeVector vec = (PNodeVector)obj;
				if (vec.size() != 1)
				{
					throw new IllegalArgumentException("Can only use scalar nodes in path.");
				}
				list.add(vec.getModelerNode(0));
			}
		}
		assertCustom().addPath(list);
	}
	
	public String[] applicableSchedulerOptions()
	{
		final List<? extends SchedulerOptionKey> keys = _scheduler.applicableSchedulerOptions();
		final int n = keys.size();
		final String[] names = new String[n];
		for (int i = 0; i < n; ++i)
		{
			names[i] = keys.get(i).qualifiedName();
		}
		return names;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private CustomScheduler assertCustom()
	{
		if (!(_scheduler instanceof CustomScheduler))
		{
			throw new UnsupportedOperationException(String.format("%s is not a CustomScheduler", _scheduler));
		}
		
		return (CustomScheduler)_scheduler;
	}
}
