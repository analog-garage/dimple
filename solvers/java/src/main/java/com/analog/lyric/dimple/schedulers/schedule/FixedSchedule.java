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

package com.analog.lyric.dimple.schedulers.schedule;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;

import cern.colt.map.OpenLongObjectHashMap;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubgraphScheduleEntry;


/**
 * A schedule with a fixed list of schedule entries.
 * <p>
 * Schedule has a fixed update order, and does not change dynamically as the solver runs.
 * <p>
 * @author jeffb
 */
public class FixedSchedule extends ScheduleBase implements IGibbsSchedule
{
	// TODO - schedule will not actually be serializable until schedule entries and nodes are serializable!
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	protected ArrayList<IScheduleEntry> _schedule = new ArrayList<IScheduleEntry>();
	
	/*--------------
	 * Construction
	 */
	
	@Deprecated
	public FixedSchedule()
	{
		super(null, null);
	}
	
	public FixedSchedule(FactorGraph fg)
	{
		super(null, fg);
	}
	
	public FixedSchedule(@Nullable IScheduler scheduler, FactorGraph fg)
	{
		super(scheduler, fg);
	}
	
	@Deprecated
	public FixedSchedule(IScheduleEntry[] entries)
	{
		this();
		add(entries);
	}
	
	public FixedSchedule(FactorGraph fg, IScheduleEntry[] entries)
	{
		this(fg);
		add(entries);
	}

	public FixedSchedule(@Nullable IScheduler scheduler, FactorGraph fg, IScheduleEntry[] entries)
	{
		super(scheduler, fg);
		add(entries);
	}

	public FixedSchedule(@Nullable IScheduler scheduler, FactorGraph fg, Iterable<IScheduleEntry> entries)
	{
		super(scheduler, fg);
		add(entries);
	}

	public FixedSchedule(Iterable<IScheduleEntry> entries)
	{
		this();
		add(entries);
	}
	
	public ArrayList<IScheduleEntry> getSchedule()
	{
		return _schedule;
	}
	
	public FixedSchedule(ISchedule s)
	{
		this();
		add(s);
	}
	
	@Override
	public Iterator<IScheduleEntry> iterator()
	{
		return _schedule.iterator();
	}
	
	public void add(INode node)
	{
		final FactorGraph fg = node.asFactorGraph();
		add(fg != null ? new SubgraphScheduleEntry(fg) : new NodeScheduleEntry(node));
	}
	
	public void add(INode node, int index)
	{
		add(new EdgeScheduleEntry(node, index));
	}
	
	public void add(INode ... nodes)
	{
		for (int i = 0; i < nodes.length; i++)
			add(nodes[i]);
	}
	
	/**
	 * @deprecated this method cannot correctly handle multiple connections between the same
	 * factor and variable. Instead use {@link #add(INode, int)} method.
	 */
	@Deprecated
	public void add(INode from, INode to)
	{
		add(new EdgeScheduleEntry(from,to));
	}
	
	// Add one schedule entry
	public void add(IScheduleEntry entry)
	{
		requireNonNull(entry);
		_schedule.add(entry);
		++_version;
	}
	
	// Add a series of schedule entries
	public void add(@Nullable IScheduleEntry[] entries)
	{
		if (entries != null) for (IScheduleEntry entry : entries) add(entry);
	}
	public void add(@Nullable Iterable<IScheduleEntry> entries)
	{
		if (entries != null)
		{
			for (IScheduleEntry entry : entries)
				add(entry);
		}
	}
	
	// Add a sub-schedule
	public void add(@Nullable ISchedule s)
	{
		if (s != null)
		{
			add(new SubScheduleEntry(s));
		}
	}
	
	// Remove node or edge schedule entries containing a specified node
	private final void removeAllEntriesContaining(Iterable<? extends INode> nodes)
	{
		final OpenLongObjectHashMap nodemap = new OpenLongObjectHashMap();
		for (INode node : nodes)
		{
			nodemap.put(node.getGlobalId(), node);
		}
		
		// Use iterator to avoid concurrent modification
		for (final Iterator<IScheduleEntry> iterator = _schedule.iterator(); iterator.hasNext(); )
		{
			IScheduleEntry s = iterator.next();
			switch (s.type())
			{
			case NODE:
				if (nodemap.containsKey(((NodeScheduleEntry)s).getNode().getGlobalId()))
				{
					iterator.remove();
					++_version;
				}
				break;
				
			case EDGE:
				if (nodemap.containsKey(((EdgeScheduleEntry)s).getNode().getGlobalId()))
				{
					iterator.remove();
					++_version;
				}
				break;
			default:
			}
		}
	}
	
	// Get a particular entry by index (this is used, for example, for randomly selecting entries)
	public final IScheduleEntry get(int index)
	{
		return _schedule.get(index);
	}
	
	
	
	
	// For IGibbsSchedule interface...
	// Add a block schedule entry, which will replace individual node updates included in the block
	@Override
	public void addBlockScheduleEntry(BlockScheduleEntry blockScheduleEntry)
	{
		// Remove any node entries associated with the nodes in the block entry
		removeAllEntriesContaining(blockScheduleEntry.getBlock());
		
		// Add the block entry
		add(blockScheduleEntry);
	}
	// Return the number of entries
	@Override
	public final int size()
	{
		return _schedule.size();
	}
	
	@Override
	public boolean isCustom()
	{
		IScheduler scheduler = _scheduler;
		return scheduler == null || scheduler.isCustomScheduler();
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("FixedSchedule ");
		sb.append(Integer.toString(_schedule.size()));
		sb.append("\n");
		for(IScheduleEntry entry : _schedule)
		{
			sb.append("\t" + entry.toString() + "\n");
		}
		return sb.toString();
	}
}
