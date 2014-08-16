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

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.analog.lyric.dimple.schedulers.dependencyGraph.StaticDependencyGraphNode;
import org.eclipse.jdt.annotation.Nullable;

/*
 * Object that retrieves data from the work queue until there is nothing left.
 * 
 * Package protected
 */
class SingleQueueWorker implements Callable<Object>
{
	private LinkedBlockingQueue<StaticDependencyGraphNode> _workQueue;
	private AtomicInteger _nodesDone;
	
	public SingleQueueWorker(LinkedBlockingQueue<StaticDependencyGraphNode>
		workQueue,
		int numNodes,
		AtomicInteger nodesDone)
	{
		_workQueue = workQueue;
		_nodesDone = nodesDone;
	}
	
	@Override
	public @Nullable Object call() throws Exception
	{
		//Keep going until there is nothing left.
		while(true)
		{
			StaticDependencyGraphNode entry = _workQueue.take();	// Pull the next entry from the work queue (blocking if there are none)
			
			//If we find sentinel, there's nothing left to do.  Put the sentinel back for the next thread.
			if (entry.isSentinel())
			{
				_workQueue.add(entry);
				break;
			}
			
			//run the update.
			entry.getScheduleEntry().update();
			
			//Decrement the number of ndoes that are done.
			int nodesLeft = _nodesDone.decrementAndGet();
			
			//If we're done, add sentinel and quit.
			if (nodesLeft == 0)
			{
				_workQueue.add(StaticDependencyGraphNode.createSentinel());
				break;
			}
						
			//For every single dependent, we have to decrement dependencies and,
			//if those reach 0, add the dependency to the queue.
			for (int i = 0; i < entry.getNumDependents(); i++)
			{
				StaticDependencyGraphNode dependent = entry.getDependent(i);
				
				//We have to synchronize the decrement of number of dependencies.
				int numDepsLeft = 0;
				
				synchronized (dependent)
				{
					//Decrement
					numDepsLeft = dependent.getNumDependenciesLeft();
					numDepsLeft--;
					dependent.setNumDependenciesLeft(numDepsLeft);
				}
				
				//If there are 0 dependencies left, this was the last guy to touch it so
				//we can add a guy to the queue and reset the number of dependencies left.
				if (numDepsLeft == 0)
				{
					_workQueue.add(dependent);
					dependent.setNumDependenciesLeft(dependent.getNumDependencies());
				}

			}
	
			
		}
		return null;

	}
}
