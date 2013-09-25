package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.concurrent.LinkedBlockingQueue;

import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.schedulers.dependencyGraph.DependencyGraphNode;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;

public class SFactorGraphThread implements Runnable
{
	JeffMultithreadingAlgorithm _alg;

	public SFactorGraphThread(JeffMultithreadingAlgorithm alg)
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
