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

package com.analog.lyric.dimple.schedulers.dependencyGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BatchScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;

public class ScheduleDependencyGraph extends BasicDependencyGraph<IScheduleEntry, DependencyGraphNode<IScheduleEntry>>
{
	private static final long serialVersionUID = 1L;


	// Create a dependency graph of schedule entries reflecting only the structure of the factor graph and the schedule
	public ScheduleDependencyGraph(FactorGraph factorGraph, int numIterations) 
	{
		HashMap<Port, List<Integer>> portToScheduleMap = new HashMap<Port, List<Integer>>();
		HashMap<IScheduleEntry, DependencyGraphNode<IScheduleEntry>> scheduleEntryMap = new HashMap<IScheduleEntry, DependencyGraphNode<IScheduleEntry>>();
		
		for (int iteration = 0; iteration < numIterations; iteration++)
			createOneIterationOfScheduleDependencyGraph(factorGraph.getSchedule(), portToScheduleMap, scheduleEntryMap);
	}
	
	
	// Add one-iterations worth of schedule entries to the schedule dependency graph
	protected void createOneIterationOfScheduleDependencyGraph(ISchedule schedule, HashMap<Port, List<Integer>> portToScheduleMap, HashMap<IScheduleEntry, DependencyGraphNode<IScheduleEntry>> scheduleEntryMap) 
	{
		for (IScheduleEntry entry : schedule)
		{
			if (entry instanceof NodeScheduleEntry)
			{
				// Node schedule entry
				DependencyGraphNode<IScheduleEntry> node = new DependencyGraphNode<IScheduleEntry>(entry);
				add(node);						// Add this schedule entry to the dependency graph
				int nodeIndex = size()-1;		// Get the index of the node just added (this will be used in the portUpdateList)

				for (Port inputPort : ((NodeScheduleEntry)entry).getNode().getPorts())
				{
					// See what schedule entries have updated this port already, if any
					List<Integer> portUpdateList = (List<Integer>)portToScheduleMap.get(inputPort);
					if ((portUpdateList != null) && !portUpdateList.isEmpty())
					{
						int lastUpdate = portUpdateList.get(portUpdateList.size()-1);
						addDependency(node, get(lastUpdate));	// Assumes entries are not removed from the graph, otherwise this will get the wrong item
					}
					
					// Add to the list of schedule entries that have updated the corresponding output port (all output ports update)
					Port outputPort = inputPort.getSibling();
					if (portToScheduleMap.containsKey(outputPort))
						portToScheduleMap.get(outputPort).add(nodeIndex);
					else
					{
						List<Integer> newList = new ArrayList<Integer>();
						newList.add(nodeIndex);
						portToScheduleMap.put(outputPort, newList);
					}
				}
				
				// Entry also depends on the same entry in the last iteration and its dependents
				// This is needed for damping so that a single-edge node doesn't get updated again before the value from the previous iteration was created and used
				DependencyGraphNode<IScheduleEntry> thisNodeLastIteration = scheduleEntryMap.get(entry);
				if (thisNodeLastIteration != null)
				{
					addDependencies(node, thisNodeLastIteration.getDependents());
					addDependency(node, thisNodeLastIteration);
				}
				scheduleEntryMap.put(entry, node);
			}
			else if (entry instanceof EdgeScheduleEntry)
			{
				// Edge schedule entry
				DependencyGraphNode<IScheduleEntry> node = new DependencyGraphNode<IScheduleEntry>(entry);
				add(node);						// Add this schedule entry to the dependency graph
				int nodeIndex = size()-1;		// Get the index of the node just added (this will be used in the portUpdateList)
				
				int outPortNum = ((EdgeScheduleEntry)entry).getPortNum();
				for (Port inputPort : ((EdgeScheduleEntry)entry).getNode().getPorts())
				{
					if (inputPort.getId() != outPortNum)		// Only look at input ports that are used to create the output
					{
						// See what schedule entries have updated this port already, if any
						List<Integer> portUpdateList = (List<Integer>)portToScheduleMap.get(inputPort);
						if ((portUpdateList != null) && !portUpdateList.isEmpty())
						{
							int lastUpdate = portUpdateList.get(portUpdateList.size()-1);
							addDependency(node, get(lastUpdate));	// Assumes entries are not removed from the graph, otherwise this will get the wrong item
						}
					}
				}
				
				// Add to the list of schedule entries that have updated the corresponding output port (just one output port update)
				Port outPort = ((ArrayList<Port>)((EdgeScheduleEntry)entry).getNode().getPorts()).get(outPortNum).getSibling();
				if (portToScheduleMap.containsKey(outPort))
					portToScheduleMap.get(outPort).add(nodeIndex);
				else
				{
					List<Integer> newList = new ArrayList<Integer>();
					newList.add(nodeIndex);
					portToScheduleMap.put(outPort, newList);
				}
				
				// Entry also depends on the same entry in the last iteration and its dependents
				// This is needed for damping so that a single-edge node doesn't get updated again before the value from the previous iteration was created and used
				DependencyGraphNode<IScheduleEntry> thisNodeLastIteration = scheduleEntryMap.get(entry);
				if (thisNodeLastIteration != null)
				{
					addDependencies(node, thisNodeLastIteration.getDependents());
					addDependency(node, thisNodeLastIteration);
				}
				scheduleEntryMap.put(entry, node);
			}
			else if (entry instanceof BatchScheduleEntry)
			{
				// A poor way to handle batch entries, but at least it runs
				FixedSchedule subSchedule = new FixedSchedule();
				for(IScheduleEntry singleEntry : ((BatchScheduleEntry)entry).getEntries())
					subSchedule.add(singleEntry);
				createOneIterationOfScheduleDependencyGraph(subSchedule, portToScheduleMap, scheduleEntryMap);	// Recurse to the sub-graph
			}
			else if (entry instanceof SubScheduleEntry)
				createOneIterationOfScheduleDependencyGraph(((SubScheduleEntry)entry).getSchedule(), portToScheduleMap, scheduleEntryMap);	// Recurse to the sub-graph
			else
				throw new DimpleException("Unhandled schedule entry class: " + entry.getClass().getName());
			
			interruptCheck();
		}

	}
	
	
	// Allow interruption (if the solver is run as a thread)
	protected void interruptCheck()
	{
		try {Thread.sleep(0);}
		catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			return;
		}
	}

}
