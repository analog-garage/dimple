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

package com.analog.lyric.dimple.solvers.core.multithreading.alg1;

import java.util.concurrent.LinkedBlockingQueue;

import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.crossiteration.DependencyGraphNode;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;

/*
 * Thread that does the schedule entry updates.
 */
public class SingleQueueWorker implements Runnable
{
	SingleQueueCrossIterationMultithreadingAlgorithm _alg;

	public SingleQueueWorker(SingleQueueCrossIterationMultithreadingAlgorithm alg)
	{
		_alg = alg;
	}

	@Override
	public void run()
	{
		LinkedBlockingQueue<DependencyGraphNode<IScheduleEntry>> workQueue = _alg.getWorkQueue();
		
		try
		{
			while(true)
			{
				DependencyGraphNode<IScheduleEntry> entry = workQueue.take();	// Pull the next entry from the work queue (blocking if there are none)
				IScheduleEntry scheduleEntry = entry.getObject();
				INode node = (scheduleEntry instanceof NodeScheduleEntry) ? ((NodeScheduleEntry)scheduleEntry).getNode() : ((EdgeScheduleEntry)scheduleEntry).getNode();
				synchronized (node)												// Synchronize on the node: don't allow updating the same node in more than one thread at the same time
				{
					scheduleEntry.update();										// Run it
				}
				
			
				_alg.scheduleEntryCompleted(entry);						// Tell the main thread that it's done
			}
		}
		catch (InterruptedException e) {return;}
		catch (Exception e)
		{
			_alg.setSubThreadException(e);
			_alg.getWaitNotify().doNotify();		// Notify the parent thread that we're done (not really done, but it's an exception)
			Thread.currentThread().interrupt();
			return;
		}
	}
}
