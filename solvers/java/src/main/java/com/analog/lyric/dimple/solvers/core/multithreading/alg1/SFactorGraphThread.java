package com.analog.lyric.dimple.solvers.core.multithreading.alg1;

import java.util.concurrent.LinkedBlockingQueue;

import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.crossiteration.DependencyGraphNode;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;

public class SFactorGraphThread implements Runnable
{
	SingleQueueCrossIterationMultithreadingAlgorithm _alg;

	public SFactorGraphThread(SingleQueueCrossIterationMultithreadingAlgorithm alg)
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
