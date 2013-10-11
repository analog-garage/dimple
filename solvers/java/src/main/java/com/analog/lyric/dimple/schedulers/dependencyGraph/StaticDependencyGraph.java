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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.schedulers.dependencyGraph.helpers.LastUpdateGraph;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;

/**
 * Creates a static dependency graph for a single iteration.  The crossiteration dependency graphs
 * unroll iterations.
 * 
 * The StaticDependencyGraph consists of StaticDependencyGraphNodes with directed edges
 * indicating what shedule entries depend on other schedule entries.
 */
public class StaticDependencyGraph 
{
	private int _numScheduleEntries;
    private ArrayList<StaticDependencyGraphNode> _initialEntries;
	private ArrayList<ArrayList<IScheduleEntry>> _phases = new ArrayList<ArrayList<IScheduleEntry>>();
	private int _nextNodeId = 0;

	/**
	 * Construct the graph for one iteration
	 */
	public StaticDependencyGraph(FactorGraph fg)
	{
		this(fg,1);
	}
	
	/**
	 * Construct the graph.
	 */
	public StaticDependencyGraph(FactorGraph fg,int iters)
	{
		//Instantiate the data structure that keeps track of the last IScheduleEntry update to touch an edge.
		LastUpdateGraph lug = new LastUpdateGraph();		
		
		//Initialize the initial entries.
		_initialEntries = new ArrayList<StaticDependencyGraphNode>();
		
		//Get the schedule
		ISchedule schedule = fg.getSchedule();
		
		//Do the work of building the dependency graph.
		//Allow building dependency graph for multiple iterations
		for (int i = 0; i < iters; i++)
			buildFromSchedule(schedule,lug);
	}
	
	/**
	 * Multithreading can be done in phases of indepdent schedule entries.  Each phase
	 * contains a list of scheduleEntries that can be updated concurrently.
	 */
	public ArrayList<ArrayList<IScheduleEntry>> getPhases()
	{
		return _phases;
	}
	
	/**
	 * Produces a GraphViz file for viewing the dependency graph.
	 * Naming variables and factors will result in a more readable dependency graph.
	 */
	@SuppressWarnings("resource")
	public void createDotFile(String fileName)
	{
		String str = createDotString();
		PrintWriter writer;
		
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.write(str);
			writer.close();
		} catch (Exception e) {
			throw new DimpleException(e);
		}
		
	}
	
	/*
	 * Produce the GraphViz string to be saved to a dot file.
	 */
	public String createDotString()
	{
		//Provide different colors for each phase.
		String [] colors = {"red","blue","green","pink","purple","gold","black","cyan"};
		
		StringBuilder sb = new StringBuilder();
		sb.append("digraph graphname {\n");
		
		//Keep track of the nodes already added.
		HashSet<Integer> foundIds = new HashSet<Integer>();
		
		
		LinkedList<StaticDependencyGraphNode> nodes = new LinkedList<StaticDependencyGraphNode>();
		
		//Start with the first phase.
		for (int i = 0; i < _initialEntries.size(); i++)
		{
			nodes.push(_initialEntries.get(i));
			foundIds.add(_initialEntries.get(i).getId());
		}
		
		//until we've output all nodes
		while (! nodes.isEmpty())
		{
			//get the next node.
			StaticDependencyGraphNode node = nodes.pop();
			
			//color it based on its phase.
			int colorIndex = node.getPhase() % colors.length;
			String color = colors[colorIndex];
			
			//print out the node.
			sb.append(node.getId() + "[label=\"" + node + "\",color=\"" + color + "\"];\n");
			
			//now print out the edges.
			for (int i = 0; i  < node.getNumDependents(); i++)
			{
				StaticDependencyGraphNode nextNode = node.getDependent(i);
				sb.append(node.getId() + " -> " + nextNode.getId() + ";\n");
				
				//if we haven't already printed this guy, add it to the list.
				if (!foundIds.contains(nextNode.getId()))
				{
					nodes.add(nextNode);
					foundIds.add(nextNode.getId());
				}
			}
		}
		
		sb.append("}\n");
		return sb.toString();
	}

	/*
	 * How many ScheduleEntry nodes are in this graph.
	 */
	public int getNumNodes()
	{
		return _numScheduleEntries;
	}
	
	/*
	 * Returns the StaticDependencyGraphNodes of the first phase.
	 */
	public ArrayList<StaticDependencyGraphNode> getInitialEntries()
	{
		return _initialEntries;
	}
	
	
	/*
	 * Recursive method for building dependency graph from an ISchedule node.
	 */
	private void buildFromSchedule(ISchedule schedule,LastUpdateGraph lug)
	{
		if (! (schedule instanceof FixedSchedule))
			throw new DimpleException("Cannot currently create dependency graph of Dynamic Schedule");
		
		//For each entry in the schedule
		for (IScheduleEntry se : schedule)
		{
			
			//If this is a subscheduleEntry, recurse.
			if (se instanceof SubScheduleEntry)
			{
				buildFromSchedule(((SubScheduleEntry)se).getSchedule(), lug);
			}
			else
			{
				//Instantiate a static dependency graph node (builds dependencies)
				StaticDependencyGraphNode dgn = new StaticDependencyGraphNode(se,lug,_nextNodeId);
				
				//Increment some counters.
				_nextNodeId++;
				_numScheduleEntries++;
				
				//Add this entry to the correct phase.
				int phase = dgn.getPhase();				
				while (_phases.size() <= phase)
					_phases.add(new ArrayList<IScheduleEntry>());
				_phases.get(phase).add(dgn.getScheduleEntry());
				
				//if this is phase 0, add it to the initial entries.
				if (phase == 0)
					_initialEntries.add(dgn);
			}
		}
	}
	
}
