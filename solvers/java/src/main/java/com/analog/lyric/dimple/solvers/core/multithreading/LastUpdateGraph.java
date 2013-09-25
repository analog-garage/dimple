package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Node;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.util.misc.MapList;


public class LastUpdateGraph 
{
//	private HashMap<INode,NodeLastUpdates> _node2updates = 
//			new HashMap<INode, SFactorGraph.NodeLastUpdates>();
//	
	private HashMap<Edge,StaticDependencyGraphNode> _edge2lastNode = new HashMap<Edge, StaticDependencyGraphNode>();
	
	public LastUpdateGraph()
	{
//		MapList<INode> nodes = fg.getNodes();
//		for (INode n : nodes)
//		{
//			for (INode sibling : n.getSiblings())
//			{
//				_edge2lastNode.put(arg0, arg1)
//			}
//		}

	}
	
	public ArrayList<Edge> getEdges(NodeScheduleEntry nse)
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

	public ArrayList<Edge> getEdges(EdgeScheduleEntry ese)
	{
		ArrayList<Edge> retval = new ArrayList<Edge>();

		INode n = ese.getNode();
		int portNum = ese.getPortNum();
		
		for (int i = 0; i < n.getSiblings().size(); i++)
		{
			if (i == portNum)
				retval.add(new Edge(n,n.getSiblings().get(i)));
			else
				retval.add(new Edge(n.getSiblings().get(i),n));
		}
		
		return retval;
	}
	
	public ArrayList<Edge> getEdges(IScheduleEntry entry)
	{
		
		if (entry instanceof NodeScheduleEntry)
			return getEdges((NodeScheduleEntry)entry);
		else if (entry instanceof EdgeScheduleEntry)
			return getEdges((EdgeScheduleEntry)entry);
		else
			throw new DimpleException("Not supported");
	}
	
	public StaticDependencyGraphNode getLastNode(Edge e)
	{
		return _edge2lastNode.get(e);
	}
	
	public void setLastNode(Edge e, StaticDependencyGraphNode node)
	{
		_edge2lastNode.put(e, node);
	}
	
//	
//	public NodeLastUpdates getNodeLastUpdates(INode n)
//	{
//		return _node2updates.get(n);
//	}
//	
//	public void update(NewDependencyGraphNode dgn)
//	{
//		NodeLastUpdates nlu = _node2updates.get(dgn.node);
//		nlu.lastUpdate = dgn;
//		
//		for (int i = 0; i < dgn.inports.size(); i++)
//		{
//			int portnum = dgn.inports.get(i);
//			INode sibling = dgn.node.getSiblings().get(portnum);
//			int siblingportnum = dgn.node.getSiblingPortIndex(portnum);
//			getNodeLastUpdates(sibling).lastOutputUpdates[siblingportnum] = dgn;
//		}
//		
//		for (int i = 0; i < dgn.outports.size(); i++)
//		{
//			int portnum = dgn.outports.get(i);
//			INode sibling = dgn.node.getSiblings().get(portnum);
//			int siblingportnum = dgn.node.getSiblingPortIndex(portnum);
//			getNodeLastUpdates(sibling).lastInputUpdates[siblingportnum] = dgn;
//		}
//		
//	}
}
