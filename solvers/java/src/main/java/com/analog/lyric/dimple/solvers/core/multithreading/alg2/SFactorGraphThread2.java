package com.analog.lyric.dimple.solvers.core.multithreading.alg2;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraphNode;

public class SFactorGraphThread2 implements Callable<Object>
{
	private LinkedBlockingQueue<StaticDependencyGraphNode> _workQueue;
	private AtomicInteger _nodesDone;
	
	public SFactorGraphThread2(LinkedBlockingQueue<StaticDependencyGraphNode> 
		workQueue,
		int numNodes,
		AtomicInteger nodesDone)
	{
		_workQueue = workQueue;
		_nodesDone = nodesDone;
	}
	
	@Override
	public Object call() throws Exception 
	{
		while(true)
		{
			StaticDependencyGraphNode entry = _workQueue.take();	// Pull the next entry from the work queue (blocking if there are none)
			
			if (entry instanceof Poison)
			{
				_workQueue.add(entry);
				break;
			}
			
			entry.getScheduleEntry().update();
			
			int nodesLeft = _nodesDone.decrementAndGet();
			
			if (nodesLeft == 0)
			{
				_workQueue.add(new Poison());
				break;
			}
						
			for (int i = 0; i < entry.getNumDependents(); i++)
			{
				StaticDependencyGraphNode dependent = entry.getDependent(i);
				
				synchronized (dependent)
				{
					
					int numDepsLeft = dependent.getNumDependenciesLeft();
					numDepsLeft--;
					dependent.setNumDependenciesLeft(numDepsLeft);
					
					//TODO: can move outside synchronized I think
					if (numDepsLeft == 0)
					{
						_workQueue.add(dependent);
						dependent.setNumDependenciesLeft(dependent.getNumDependencies());
					}
				}
				
			}
	
			
		}
		return null;

	}
}
