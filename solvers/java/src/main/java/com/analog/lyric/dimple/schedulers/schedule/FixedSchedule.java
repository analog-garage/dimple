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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.VariableList;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;


/**
 * @author jeffb
 * 
 *         A FixedSchedule is a schedule that has a fixed update order, and does
 *         not change dynamically as the solver runs.
 */
public class FixedSchedule extends ScheduleBase
{
	protected ArrayList<IScheduleEntry> _schedule = new ArrayList<IScheduleEntry>();
	
	public FixedSchedule(){}
	public FixedSchedule(IScheduleEntry[] entries)
	{
		add(entries);
	}
	public FixedSchedule(Collection<IScheduleEntry> entries)
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
	 * This method is called when setSchedule is called on MFactorGraph.  The FixedSchedule makes sure
	 * all edges of the FactorGraph are updated at least once.  It also makes sure all nodes are valid
	 * members of the FactorGraph whos schedule is being set.
	 */
	public void attach(FactorGraph factorGraph) 
	{		
		super.attach(factorGraph);
		
		
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
			for (int index = 0; index < f.getSiblings().size(); index++)
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
		for (VariableBase v : vl.values())
		{
			for (int index = 0; index < v.getSiblings().size(); index++)
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
	
	public FixedSchedule(ISchedule s)
	{
		add(s);
	}
	
	public Iterator<IScheduleEntry> iterator()
	{
		return _schedule.iterator();
	}
	
	// Add one schedule entry
	public void add(IScheduleEntry entry)
	{
		_schedule.add(entry);
	}
	
	// Add a series of schedule entries
	public void add(IScheduleEntry[] entries)
	{
		if (entries != null) for (IScheduleEntry entry : entries) add(entry);
	}
	public void add(Collection<IScheduleEntry> entries)
	{
		if (entries != null) for (IScheduleEntry entry : entries) add(entry);
	}
	
	// Add a sub-schedule
	public void add(ISchedule s)
	{
		if (s != null) _schedule.add(new SubScheduleEntry(s));
	}
	
	public ISchedule copy(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, false);
	}
	public ISchedule copyToRoot(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, true);
	}
	
	public ISchedule copy(HashMap<Object,Object> old2newObjs, boolean copyToRoot) 
	{
		FactorGraph templateGraph = getFactorGraph();
		
		
		if (templateGraph.getSchedule() != null)
		{
			FixedSchedule fs = (FixedSchedule)templateGraph.getSchedule();
			
			ArrayList<IScheduleEntry> schedule = fs.getSchedule();
			ArrayList<IScheduleEntry> newSchedule = new ArrayList<IScheduleEntry>();
			
			for (int i = 0; i < schedule.size(); i++)
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
		else
		{
			return null;
			
		}
	}
	
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
