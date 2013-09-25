package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.DependencyGraphNode;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;


public class StaticDependencyGraphNode 
{
	private int _phase;
	private ArrayList<StaticDependencyGraphNode> _dependents = new ArrayList<StaticDependencyGraphNode>();
	private  int _numDependencies;
	private int _numDependenciesLeft;
	private IScheduleEntry _scheduleEntry;
//	public ArrayList<Integer> inports = new ArrayList<Integer>();
//	public ArrayList<Integer> outports = new ArrayList<Integer>();
	private int _id = -1;
	
	
//	public void print()
//	{
//		System.out.println("node: " + node.toString());
//	}
	
	public void addDependent(StaticDependencyGraphNode node)
	{
		_dependents.add(node);
	}
	
	public StaticDependencyGraphNode()
	{
		
	}
//	
//	public ArrayList<NewDependencyGraphNode> pretendUpdateAndReturnAvailableDependencies()
//	{
//		ArrayList<NewDependencyGraphNode> retval = new ArrayList<SFactorGraph.NewDependencyGraphNode>();
//
//		for (int i = 0; i < dependents.size(); i++)
//		{
//			dependents.get(i).numDependenciesLeft--;
//			
//			if (dependents.get(i).numDependenciesLeft == 0)
//			{
//				retval.add(dependents.get(i));
//				dependents.get(i).numDependenciesLeft = dependents.get(i).numDependencies;
//			}
//			
//			if (dependents.get(i).numDependenciesLeft < 0)
//			{
//				System.out.println("Ack");
//				throw new DimpleException("Ack");
//			}
//			
//		}
//		
//		return retval;
//	}
	
	
	
	
	public StaticDependencyGraphNode(IScheduleEntry scheduleEntry,
			LastUpdateGraph lastUpdateGraph, int id)
	{
		_phase = 0;
		_scheduleEntry = scheduleEntry;
		_id = id;
		
		
		ArrayList<Edge> edges = lastUpdateGraph.getEdges(scheduleEntry);
	
		for (Edge e : edges)
		{
			StaticDependencyGraphNode lastNode = lastUpdateGraph.getLastNode(e);
			
			if (lastNode != null)
			{
				_phase = Math.max(lastNode._phase+1, _phase);
				lastNode.addDependent(this);
				_numDependencies++;
				_numDependenciesLeft++;
				
			}
			lastUpdateGraph.setLastNode(e,this);
		}
	}
	
	public INode getNode()
	{
		if (_scheduleEntry instanceof EdgeScheduleEntry)
			return ((EdgeScheduleEntry)_scheduleEntry).getNode();
		else if (_scheduleEntry instanceof NodeScheduleEntry)
			return ((NodeScheduleEntry)_scheduleEntry).getNode();
		else
			throw new DimpleException("Not supported");
	}
	
	public StaticDependencyGraphNode getDependent(int num)
	{
		return _dependents.get(num);
	}
	
	public int getNumDependents()
	{
		return _dependents.size();
	}
	
	public int getId()
	{
		return _id;
	}
	
	public IScheduleEntry getScheduleEntry()
	{
		return _scheduleEntry;
	}
	
	public int getPhase()
	{
		return _phase;
	}
	
	@Override
	public String toString()
	{
		int first = _scheduleEntry.toString().indexOf('[');
		return _scheduleEntry.toString().substring(first);
	}
}
