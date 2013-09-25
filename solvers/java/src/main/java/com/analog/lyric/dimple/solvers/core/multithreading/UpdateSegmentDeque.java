package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;


//TODO: Should I use runnable instead of Callable?
public class UpdateSegmentDeque implements Callable
{
	private ConcurrentLinkedQueue<IScheduleEntry> [] _deques;
	private int _which;
	private ArrayList<IScheduleEntry> _nodes;
	private boolean _stealing;
	
	public UpdateSegmentDeque(ArrayList<IScheduleEntry> nodes, 
			int which, ConcurrentLinkedQueue<IScheduleEntry> [] deques, 
			boolean stealing)
	{
		_which = which;
		_deques = deques;
		_nodes= nodes;
		_stealing = stealing;
	}
	
	
	@Override
	public Object call() throws Exception 
	{			
		int which = _which;
		int nodesPerThread = _nodes.size() / _deques.length;
		int first = _which*nodesPerThread;
		int last = first + nodesPerThread - 1;
		
		if (which == _deques.length - 1)
			last = _nodes.size()-1;
		
		for (int i = first; i <= last; i++)
		{
			IScheduleEntry tmp = _nodes.get(i);
			_deques[which].add(tmp);
		}
		
		IScheduleEntry n = _deques[which].poll();
		
	
		while (n != null)
		{	
			//long start = System.currentTimeMillis();
			n.update();
			//long end = System.currentTimeMillis();
						
			n = _deques[which].poll();
			
			if (n == null && _stealing)
			{
				//TODO: Why can't I iterate over one less? 
				for (int i = 0; i < _deques.length; i++)
				{
					//n = _deques[i].poll();
					n = _deques[(_which + i) % _deques.length].poll();
					
					if (n != null)
						break;
				}
			}
			
		}	
		return null;
	}
}
