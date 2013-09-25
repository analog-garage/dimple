package com.analog.lyric.dimple.solvers.core.multithreading;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;

public class BillMultithreadingAlgorithm extends MultithreadingAlgorithm 
{

	public BillMultithreadingAlgorithm(MultiThreadingManager manager) 
	{
		super(manager);

	}

	@Override
	public void iterate(int numIters) 
	{
		ArrayList<ArrayList<IScheduleEntry>> phases = getManager().getPhases();
		ExecutorService service = getManager().getService();
		int numThreads = getManager().getNumThreads();
		
		for (int i = 0; i < numIters; i++)
		{
			for (int j = 0; j < phases.size(); j++)
			{				
				updateScheduleEntries(service, phases.get(j), numThreads, true);
			}
		}

	}

	public void updateScheduleEntries(ExecutorService service, 
			ArrayList<IScheduleEntry> scheduleEntries, 
			int numThreads, boolean stealing)
	{
		ConcurrentLinkedQueue<IScheduleEntry> [] deques = new ConcurrentLinkedQueue[numThreads];
		for (int i = 0; i < deques.length; i++)
			deques[i] = new ConcurrentLinkedQueue<IScheduleEntry>();
		
		//for (int i = 0; i < deques.length; )
		//Add stuff to the deques
		int numNodes = scheduleEntries.size();
		
		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);
		int nodesPerThread = scheduleEntries.size() / numThreads;
		
		for (int i = 0; i < numThreads; i++)
		{
			ll.add(new UpdateSegmentDeque(scheduleEntries, i, deques, stealing));
		}
				
		try {
			service.invokeAll(ll);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
	}

}
