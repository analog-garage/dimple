package com.analog.lyric.dimple.solvers.core.multithreading;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;


public class StaticDependencyGraph 
{
	private int _numScheduleEntries;
    private ArrayList<StaticDependencyGraphNode> _initialEntries;
	private ArrayList<ArrayList<IScheduleEntry>> _phases = new ArrayList<ArrayList<IScheduleEntry>>();
	private int _nextNodeId = 0;
	
	public StaticDependencyGraph(FactorGraph fg)
	{
		LastUpdateGraph lug = new LastUpdateGraph();
		
		_initialEntries = new ArrayList<StaticDependencyGraphNode>();
		
		ISchedule schedule = fg.getSchedule();
		
		buildFromSchedule(schedule,lug);

		
	}
	
	private void buildFromSchedule(ISchedule schedule,LastUpdateGraph lug)
	{
		for (IScheduleEntry se : schedule)
		{
			if (se instanceof SubScheduleEntry)
			{
				buildFromSchedule(((SubScheduleEntry)se).getSchedule(), lug);
			}
			else
			{
				StaticDependencyGraphNode dgn = new StaticDependencyGraphNode(se,lug,_nextNodeId);
				_nextNodeId++;
				_numScheduleEntries++;
				
				int phase = dgn.getPhase();
				
				while (_phases.size() <= phase)
				{
					_phases.add(new ArrayList<IScheduleEntry>());
				}
				
				_phases.get(phase).add(dgn.getScheduleEntry());
				
				if (phase == 0)
					_initialEntries.add(dgn);
			}

		}
	}
	
	public ArrayList<ArrayList<IScheduleEntry>> getPhases()
	{
		return _phases;
	}
	
	public void createDotFile(String fileName)
	{
		String str = createDotString();
		PrintWriter writer;
		try {
			writer = new PrintWriter(fileName, "UTF-8");
			writer.write(str);
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new DimpleException(e);
		}
		
	}
	
	public String createDotString()
	{
		String [] colors = {"red","blue","green","pink","purple","gold","black","cyan"};
		StringBuilder sb = new StringBuilder();
		sb.append("digraph graphname {\n");
		
		HashSet<Integer> foundIds = new HashSet<Integer>();
		LinkedList<StaticDependencyGraphNode> nodes = new LinkedList<StaticDependencyGraphNode>();
		
		for (int i = 0; i < _initialEntries.size(); i++)
		{
			nodes.push(_initialEntries.get(i));
			//System.out.println(_initialEntries.get(i).node);
			foundIds.add(_initialEntries.get(i).getId());
		}
		
		while (! nodes.isEmpty())
		{
			StaticDependencyGraphNode node = nodes.pop();
			
			int colorIndex = node.getPhase() % colors.length;
			String color = colors[colorIndex];
			
			sb.append(node.getId() + "[label=\"" + node + "\",color=\"" + color + "\"];\n");
			//System.out.println("got node: " + node.getNode());
			
			for (int i = 0; i  < node.getNumDependents(); i++)
			{
				StaticDependencyGraphNode nextNode = node.getDependent(i);
				//System.out.println("points to: " + nextNode.node);
				sb.append(node.getId() + " -> " + nextNode.getId() + ";\n");
				
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
//	
//	public ArrayList<MapList> getPhases()
//	{
//		if (_phases == null)
//		{
//			ArrayList<ArrayList<NewDependencyGraphNode>> tmpPhases = new ArrayList<ArrayList<NewDependencyGraphNode>>();
//			
//			tmpPhases.add(new ArrayList<SFactorGraph.NewDependencyGraphNode>());
//			
//			for (int i = 0; i < _initialEntries.size(); i++)
//			{
//				tmpPhases.get(0).add(_initialEntries.get(i));
//			}
//			
//			int numNodesDone = _initialEntries.size();
//			
//			ArrayList<NewDependencyGraphNode> justFinished = tmpPhases.get(0);
//			
//			while (numNodesDone != _numNodes)
//			{
//				ArrayList<NewDependencyGraphNode> newStuff = new ArrayList<SFactorGraph.NewDependencyGraphNode>();
//				
//				for (NewDependencyGraphNode n : justFinished)
//				{
//					ArrayList<NewDependencyGraphNode> tmp = n.pretendUpdateAndReturnAvailableDependencies();
//					newStuff.addAll(tmp);
//				}
//				
//				tmpPhases.add(newStuff);
//				justFinished = newStuff;
//				numNodesDone += newStuff.size();
//			}
//				
//			ArrayList<MapList> realRetval = new ArrayList<MapList>();
//			
//			for (int i = 0; i < tmpPhases.size(); i++)
//			{
//				MapList ml = new MapList();
//				for (NewDependencyGraphNode dgn : tmpPhases.get(i))
//				{
//					ml.add(dgn.node);
//				}
//				realRetval.add(ml);
//			}
//			_phases = realRetval;
//		}
//		
//		return _phases;
//	}
//	
//	public int getNumNodes()
//	{
//		return _numNodes;
//	}
//	
//	public ArrayList<NewDependencyGraphNode> getInitialEntries()
//	{
//		return _initialEntries;
//	}
//	
//	public void print()
//	{
//		System.out.println("initial entries: ");
//		for (int i = 0; i < _initialEntries.size(); i++)
//			_initialEntries.get(i).print();
//		
//	}
}
