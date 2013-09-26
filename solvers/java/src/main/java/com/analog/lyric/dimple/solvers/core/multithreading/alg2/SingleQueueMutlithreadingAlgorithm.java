package com.analog.lyric.dimple.solvers.core.multithreading.alg2;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraph;
import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraphNode;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.core.multithreading.MultithreadingAlgorithm;

public class SingleQueueMutlithreadingAlgorithm extends MultithreadingAlgorithm
{

	public SingleQueueMutlithreadingAlgorithm(MultiThreadingManager manager) 
	{
		super(manager);
	}

	@Override
	public void iterate(int numIters) 
	{
 		StaticDependencyGraph dg = getManager().getDependencyGraph();
 		
 		for (int j = 0; j < numIters; j++)
 		{
	 		
	 		LinkedBlockingQueue<StaticDependencyGraphNode> workQueue = new LinkedBlockingQueue<StaticDependencyGraphNode>();
	 		
	 		for (StaticDependencyGraphNode dgn : dg.getInitialEntries())
	 		{
	 			workQueue.add(dgn);
	 		}
	 		
	 		int numThreads = getManager().getNumThreads();
	 		
			ArrayList<Callable<Object>> workers = new ArrayList<Callable<Object>>();
	
			AtomicInteger nodesLeft = new AtomicInteger(dg.getNumNodes());
			
	 		for (int i = 0; i < numThreads; i++)
	 		{
	 			
	 			workers.add(new SFactorGraphThread2(workQueue, dg.getNumNodes(), nodesLeft));
	 		}
	 		
	 		try {
				getManager().getService().invokeAll(workers);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
	}

}
