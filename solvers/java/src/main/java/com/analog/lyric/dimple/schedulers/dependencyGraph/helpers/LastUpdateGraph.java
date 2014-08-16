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

package com.analog.lyric.dimple.schedulers.dependencyGraph.helpers;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraphNode;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import org.eclipse.jdt.annotation.Nullable;

/*
 * The LastUpdateGraph data structure provides the ability to retrieve edges associated
 * with IScheduleEntry entries.  It also provides a mechanism to see the last StaticDependencyGraphNode
 * that touched an edge.  This is used by the StaticDependencyGraph when building the dependency graph.
 */
public class LastUpdateGraph
{
	private HashMap<Edge,StaticDependencyGraphNode> _edge2lastNode = new HashMap<Edge, StaticDependencyGraphNode>();
	
	public LastUpdateGraph()
	{

	}
	
	/*
	 * Retrieve the edges used by a schedule update.
	 */
	public ArrayList<Edge> getEdges(IScheduleEntry entry)
	{
		
		if (entry instanceof NodeScheduleEntry)
			return getEdges((NodeScheduleEntry)entry);
		else if (entry instanceof EdgeScheduleEntry)
			return getEdges((EdgeScheduleEntry)entry);
		else
			throw new DimpleException("Not supported");
	}
	
	/*
	 * Get the last node to either read from or write on this edge.
	 */
	public @Nullable StaticDependencyGraphNode getLastNode(Edge e)
	{
		return _edge2lastNode.get(e);
	}
	
	/*
	 * Set the last node to either read or write from this edge.
	 */
	public void setLastNode(Edge e, StaticDependencyGraphNode node)
	{
		_edge2lastNode.put(e, node);
	}
	

	/*
	 * Private method to get edges associated with a node schedule entry.
	 * (All input and output edges)
	 */
	private ArrayList<Edge> getEdges(NodeScheduleEntry nse)
	{
		ArrayList<Edge> retval = new ArrayList<Edge>();

		INode n = nse.getNode();
		for (INode other : n.getSiblings())
		{
			retval.add(new Edge(n,other));
			retval.add(new Edge(other,n));
		}
		return retval;
	}

	/*
	 * Private method to get edges associated with a node schedule entry.
	 * (All input except one and one output edge)
	 */
	private ArrayList<Edge> getEdges(EdgeScheduleEntry ese)
	{
		INode n = ese.getNode();
		int size = n.getSiblingCount();
		int portNum = ese.getPortNum();
		ArrayList<Edge> retval = new ArrayList<Edge>(size);

		
		for (int i = 0; i < size; i++)
		{
			INode n2 = n.getSibling(i);
			if (i == portNum)
				retval.add(new Edge(n,n2));
			else
				retval.add(new Edge(n2,n));
		}
		
		return retval;
	}
	
}
