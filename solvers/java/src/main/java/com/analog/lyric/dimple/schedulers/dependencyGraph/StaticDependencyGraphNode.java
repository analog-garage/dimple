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
import java.util.Collections;
import java.util.List;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.helpers.Edge;
import com.analog.lyric.dimple.schedulers.dependencyGraph.helpers.LastUpdateGraph;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import org.eclipse.jdt.annotation.NonNullByDefault;

/*
 * A single node in the StaticDependencyGraph.
 * Corresponds to an IScheduleEntry object.
 */
public class StaticDependencyGraphNode
{
	private int _phase;
	private List<StaticDependencyGraphNode> _dependents = new ArrayList<StaticDependencyGraphNode>();
	private  int _numDependencies;
	private int _numDependenciesLeft;
	private IScheduleEntry _scheduleEntry;
	private int _id = -1;
		
	private static class Sentinel extends StaticDependencyGraphNode
	{
		private Sentinel()
		{
			super(bogusEntry());
		}
		
		// HACK to shut up null warnings
		@NonNullByDefault(false)
		private static IScheduleEntry bogusEntry()
		{
			return null;
		}
		
		@Override
		public boolean isSentinel()
		{
			return true;
		}
	}
	
	/*
	 * Provide empty constructor so we can generate a sentinel object for telling a thread
	 * when to stop working.
	 */
	private StaticDependencyGraphNode(IScheduleEntry entry)
	{
		_dependents = Collections.EMPTY_LIST;
		_scheduleEntry = entry;
	}
	
	public static StaticDependencyGraphNode createSentinel()
	{
		return new Sentinel();
	}
	
	/*
	 * The constructor will set up dependencies.
	 */
	public StaticDependencyGraphNode(IScheduleEntry scheduleEntry,
			LastUpdateGraph lastUpdateGraph, int id)
	{
		_phase = 0;
		_scheduleEntry = scheduleEntry;
		_id = id;
		
		//retrievew all directed edges associated with this schedule entry.
		//Will be all in/out edges for a node update.  Will be all input edges
		//except for one and an output edge for an edge update.
		ArrayList<Edge> edges = lastUpdateGraph.getEdges(scheduleEntry);
	
		//For each edge we find the last DependencyGraphNode that used that edge
		//as an input or output.
		for (Edge e : edges)
		{
			StaticDependencyGraphNode lastNode = lastUpdateGraph.getLastNode(e);
			
			if (lastNode != null)
			{
				//Since someone has previously touched this edge, add myself as
				//a dependent.  My phase will be the largest phase before me + 1
				_phase = Math.max(lastNode._phase+1, _phase);
				lastNode.addDependent(this);
				
				//Also increment the number of dependencies I have.
				_numDependencies++;
				_numDependenciesLeft++;
				
			}
			
			//Set self as last node to use this edge.
			lastUpdateGraph.setLastNode(e,this);
		}
	}
	
	/*
	 * Add a dependent.
	 */
	public void addDependent(StaticDependencyGraphNode node)
	{
		_dependents.add(node);
	}
	
	
	/*
	 * Retrieve the node associated with the schedule entry.
	 */
	public INode getNode()
	{
		if (_scheduleEntry instanceof EdgeScheduleEntry)
			return ((EdgeScheduleEntry)_scheduleEntry).getNode();
		else if (_scheduleEntry instanceof NodeScheduleEntry)
			return ((NodeScheduleEntry)_scheduleEntry).getNode();
		else
			throw new DimpleException("Not supported");
	}
	
	/*
	 * Retrieve the dependent at the specified index.
	 */
	public StaticDependencyGraphNode getDependent(int num)
	{
		return _dependents.get(num);
	}
	
	/*
	 * Get the number of dependents.
	 */
	public int getNumDependents()
	{
		return _dependents.size();
	}
	
	/*
	 * The ID is used for plotting.
	 */
	public int getId()
	{
		return _id;
	}
	
	/*
	 * The schedule entry to be updated with this node.
	 */
	public IScheduleEntry getScheduleEntry()
	{
		return _scheduleEntry;
	}
	
	/*
	 * Indicates when this node can be updated.
	 */
	public int getPhase()
	{
		return _phase;
	}
	
	public int getNumDependenciesLeft()
	{
		return _numDependenciesLeft;
	}
	
	public void setNumDependenciesLeft(int num)
	{
		_numDependenciesLeft = num;
	}
	
	public int getNumDependencies()
	{
		return _numDependencies;
	}
	
	public boolean isSentinel()
	{
		return false;
	}
	
	/*
	 * Retrievew the schedule entry stringand then makes it more concise.
	 */
	@Override
	public String toString()
	{
		int first = _scheduleEntry.toString().indexOf('[');
		return _scheduleEntry.toString().substring(first);
	}
}
