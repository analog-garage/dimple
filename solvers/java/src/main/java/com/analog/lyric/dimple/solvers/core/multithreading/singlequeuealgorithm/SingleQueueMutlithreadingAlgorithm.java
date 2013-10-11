/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.core.multithreading.singlequeuealgorithm;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraph;
import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraphNode;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.core.multithreading.MultithreadingAlgorithm;

/*
 * This algorithm uses a single work queue.  It uses the dependency graph code that
 * does not unroll loops.
 */
public class SingleQueueMutlithreadingAlgorithm extends MultithreadingAlgorithm
{

	public SingleQueueMutlithreadingAlgorithm(MultiThreadingManager manager) 
	{
		super(manager);
	}

	/*
	 * Sets up the worker threads and kicks them off.
	 */
	@Override
	public void iterate(int numIters) 
	{
		//get the dependency graph.
 		StaticDependencyGraph dg = getManager().getDependencyGraph();
 		 		
 		//do each iteration.
 		for (int j = 0; j < numIters; j++)
 		{
	 		//create the work qeuue
	 		LinkedBlockingQueue<StaticDependencyGraphNode> workQueue = new LinkedBlockingQueue<StaticDependencyGraphNode>();

	 		//Add all entries with no dependencies.
	 		for (StaticDependencyGraphNode dgn : dg.getInitialEntries())
	 		{
	 			workQueue.add(dgn);
	 		}

	 		//Keep track of how many nodes are left to update.
			AtomicInteger nodesLeft = new AtomicInteger(dg.getNumNodes());

	 		
			//Instantiate the Callable objects.
	 		int numThreads = getManager().getNumWorkers();	 		
			ArrayList<Callable<Object>> workers = new ArrayList<Callable<Object>>();			
	 		for (int i = 0; i < numThreads; i++)	 			
	 			workers.add(new SingleQueueWorker(workQueue, dg.getNumNodes(), nodesLeft));
	 		
	 		//Kick off the work
	 		try {
				getManager().getService().invokeAll(workers);
			} catch (InterruptedException e) {
				throw new DimpleException(e);
			}
 		}
	}

}
