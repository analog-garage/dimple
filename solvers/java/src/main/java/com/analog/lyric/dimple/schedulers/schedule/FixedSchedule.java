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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import org.eclipse.jdt.annotation.Nullable;


/**
 * @author jeffb
 * 
 *         A FixedSchedule is a schedule that has a fixed update order, and does
 *         not change dynamically as the solver runs.
 */
public class FixedSchedule extends ScheduleBase implements IGibbsSchedule
{
	protected ArrayList<IScheduleEntry> _schedule = new ArrayList<IScheduleEntry>();
	
	public FixedSchedule(){}
	public FixedSchedule(IScheduleEntry[] entries)
	{
		add(entries);
	}
	public FixedSchedule(Iterable<IScheduleEntry> entries)
	{
		add(entries);
	}
	
	public ArrayList<IScheduleEntry> getSchedule()
	{
		return _schedule;
	}
	

	
	/*
	 * (non-Javadoc)
	 * @see com.lyricsemi.dimple.schedulers.schedule.ISchedule#verify(com.lyricsemi.dimple.model.MFactorGraph)
	 * 
	 * This method is called when setSchedule is called on MFactorGraph.  By default, FixedSchedule makes sure
	 * all edges of the FactorGraph are updated at least once.  It also makes sure all nodes are valid
	 * members of the FactorGraph whos schedule is being set.
	 */
	@Override
	public void attach(FactorGraph factorGraph)
	{
		super.attach(factorGraph);
		
		ISolverFactorGraph sFactorGraph = factorGraph.getSolver();
		if (sFactorGraph != null && sFactorGraph.checkAllEdgesAreIncludedInSchedule())
		{
			//A graph consists of owned variables, owned factors and sub graphs
			//Users should be able to update any of the edges or nodes in the graph or its subgraphs
			//Users should be able to specify an update of the sub graph.  If they do this, the schedule
			//of that sub graph should be used.

			//Both of these will be filled in with all the ports of the Factor Graph of interest.
			HashSet<Port> setOfAllPorts = new HashSet<Port>();
			HashSet<Port> whatsLeft = new HashSet<Port>();


			//Add all factor's ports to the list of things that must be updated.
			for (Factor f : factorGraph.getNonGraphFactors().values())
			{
				for (int index = 0, end = f.getSiblingCount(); index < end; index++)
				{
					setOfAllPorts.add(new Port(f,index));
					whatsLeft.add(new Port(f,index));
				}
			}

			//If this is a nested graph, we shouldn't be updating the boundary variables.  otherwise
			//include them.  getVariables only includes boundary variables if this is the parent graph.
			VariableList vl = null;
			vl = factorGraph.getVariables();

			//Add all variable's ports to things that can/must be updated.
			for (Variable v : vl.values())
			{
				for (int index = 0, end = v.getSiblingCount(); index < end; index++)
				{
					whatsLeft.add(new Port(v,index));
					setOfAllPorts.add(new Port(v,index));
				}
			}


			//Create our set of all sub graphs.
			HashSet<FactorGraph> subGraphs = new HashSet<FactorGraph>();
			for (FactorGraph graph : factorGraph.getNestedGraphs())
			{
				subGraphs.add(graph);
			}

			//Next we're going to go through the schedule and make sure each item is a member of the
			//set of all ports for this Factor Graph.  We'll also remove each port in the schedule from
			//whatsLeft to ensure everything is updated at least once.

			//Go through schedule
			for (IScheduleEntry entry : _schedule)
			{
				Iterable<Port> ports = entry.getPorts();

				if (ports != null)
				{
					for (Port p : ports)
					{
						//Make sure the element is contained in the Factor Graph's ports.
						if (!setOfAllPorts.contains(p))
							throw new DimpleException("Schedule contains illegal port: " + p);

						//Also remove it from whatsLeft to indicate the edge has been updated.
						whatsLeft.remove(p);
					}
				}
				else
				{
					// This is a hack needed for schedulers that use sub-schedules without there being sub-graphs.
					// As a result, they can't return ports, because they would get ports from the variables and factors in the sub-graph.
					// The GibbsSequentialScanScheudler is an example of a scheduler that uses sub-schedules without there being sub-graphs.
					// So, for this case, we simply skip the check that all ports are covered, and trust the scheduler.
					// Perhaps one day we can restructure this test so that it doesn't rely on sub-schedules being associated with sub-graphs.
					if (whatsLeft.size() != 0)
						whatsLeft.clear();
					break;
				}
			}

			//Now, complain if we didn't update a port we should have updated.
			if (whatsLeft.size() != 0)
			{
				Port p = whatsLeft.iterator().next();

				throw new DimpleException("Schedule didn't update all ports.  First missing port: " + p);
			}
		}
	}
	
	public FixedSchedule(ISchedule s)
	{
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
		if (fg != null)
			add(new SubScheduleEntry(fg.getSchedule()));
		else
			add(new NodeScheduleEntry(node));
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
	
	public void add(INode from, INode to)
	{
		add(new EdgeScheduleEntry(from,to));
	}
	// Add one schedule entry
	public void add(IScheduleEntry entry)
	{
		_schedule.add(entry);
	}
	
	// Add a series of schedule entries
	public void add(@Nullable IScheduleEntry[] entries)
	{
		if (entries != null) for (IScheduleEntry entry : entries) add(entry);
	}
	public void add(@Nullable Iterable<IScheduleEntry> entries)
	{
		if (entries != null) for (IScheduleEntry entry : entries) add(entry);
	}
	
	// Add a sub-schedule
	public void add(@Nullable ISchedule s)
	{
		if (s != null) _schedule.add(new SubScheduleEntry(s));
	}
	
	
	// Remove node or edge schedule entries containing a specified node
	public final void remove(INode n)
	{
		for (final Iterator<IScheduleEntry> iterator = _schedule.iterator(); iterator.hasNext(); )	// Use iterator to avoid concurrent modification
		{
			IScheduleEntry s = iterator.next();
			if (s instanceof NodeScheduleEntry)
			{
				if (((NodeScheduleEntry)s).getNode().getId() == n.getId())
					iterator.remove();
			}
			else if (s instanceof EdgeScheduleEntry)
			{
				if (((EdgeScheduleEntry)s).getNode().getId() == n.getId())
					iterator.remove();
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
		for (INode n : blockScheduleEntry.getNodeList())
			remove(n);
		
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
	public ISchedule copy(Map<Node,Node> old2newObjs)
	{
		return copy(old2newObjs, false);
	}
	@Override
	public ISchedule copyToRoot(Map<Node,Node> old2newObjs)
	{
		return copy(old2newObjs, true);
	}
	
	public ISchedule copy(Map<Node,Node> old2newObjs, boolean copyToRoot)
	{
		FactorGraph templateGraph = requireNonNull(getFactorGraph());
		
		FixedSchedule fs = (FixedSchedule)templateGraph.getSchedule();

		ArrayList<IScheduleEntry> schedule = fs.getSchedule();
		final int size = schedule.size();
		ArrayList<IScheduleEntry> newSchedule = new ArrayList<IScheduleEntry>(size);

		for (int i = 0; i < size; i++)
		{
			IScheduleEntry entry = schedule.get(i);

			IScheduleEntry newEntry = copyToRoot ?
				entry.copyToRoot(old2newObjs) :
					entry.copy(old2newObjs);
				if (newEntry != null)
					newSchedule.add(newEntry);

		}
		return new FixedSchedule(newSchedule);
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
