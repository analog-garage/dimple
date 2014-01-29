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

package com.analog.lyric.dimple.solvers.core.multithreading.phasealgorithm;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.solvers.core.multithreading.MultithreadingAlgorithm;

/*
 * Divides the dependency graph into multiple phases where each phase contains
 * completely independent schedule entries.  This method allows the multiple threads
 * to maintain their own queues without any interaction while in a single phase.
 * There is some interaction in that, if a thread runs out of work, it will steal
 * work from another thread.
 */
public class PhaseMultithreadingAlgorithm extends MultithreadingAlgorithm 
{

	public PhaseMultithreadingAlgorithm(MultiThreadingManager manager) 
	{
		super(manager);

	}

	/*
	 * Iteration simply runs through the phases and updates all the schedule
	 * entries in that phase concurrently.
	 */
	@Override
	public void iterate(int numIters) 
	{
		ArrayList<ArrayList<IScheduleEntry>> phases = getManager().getDependencyGraph().getPhases();
		ExecutorService service = getManager().getService();
		int numThreads = getManager().getNumWorkers();
		
		for (int i = 0; i < numIters; i++)
		{
			for (int j = 0; j < phases.size(); j++)
			{				
				updateScheduleEntries(service, phases.get(j), numThreads, true);
			}
		}

	}

	/*
	 * Update all schedule entries assuming there are no dependencies between them.
	 */
	@SuppressWarnings("unchecked")
	public void updateScheduleEntries(ExecutorService service, 
			ArrayList<IScheduleEntry> scheduleEntries, 
			int numThreads, boolean stealing)
	{
		
		//Provide an array of concurrent linked queues so that each thread can use work
		//stealing if they run out of work.
		ConcurrentLinkedQueue<IScheduleEntry> [] deques = new ConcurrentLinkedQueue[numThreads];
		
		for (int i = 0; i < deques.length; i++)
			deques[i] = new ConcurrentLinkedQueue<IScheduleEntry>();
		
		ArrayList<Callable<Object>> ll = new ArrayList<Callable<Object>>(numThreads);

		//Instantiate the Callable object that will do the updates. Each object is responsible
		//for filling its queue so that building the queues is also multithreaded.
		for (int i = 0; i < numThreads; i++)
			ll.add(new WorkerWithStealing(scheduleEntries, i, deques, stealing));
				
		//Kick off the threads and wait for them to complete.
		try {
			service.invokeAll(ll);
		} catch (InterruptedException e) {
			throw new DimpleException(e);			
		}
	}

}
