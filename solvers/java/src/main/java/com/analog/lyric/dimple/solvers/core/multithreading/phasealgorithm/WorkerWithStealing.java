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

import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import org.eclipse.jdt.annotation.Nullable;

/*
 * Responsible for picking of a chunk of schedule entries,
 * updating the entries, and then stealing work until there's nothing left.
 * 
 * Package protected
 */
class WorkerWithStealing implements Callable<Object>
{
	private ConcurrentLinkedQueue<IScheduleEntry> [] _deques;
	private int _which;
	private ArrayList<IScheduleEntry> _nodes;
	private boolean _stealing;
	
	public WorkerWithStealing(ArrayList<IScheduleEntry> nodes,
			int which, ConcurrentLinkedQueue<IScheduleEntry> [] deques,
			boolean stealing)
	{
		_which = which;
		_deques = deques;
		_nodes= nodes;
		_stealing = stealing;
	}
	
	
	@Override
	public @Nullable Object call() throws Exception
	{
		//Which thread am I?
		int which = _which;
		
		//Figure out how many nodes I shold pick off.
		int nodesPerThread = _nodes.size() / _deques.length;
		int first = _which*nodesPerThread;
		int last = first + nodesPerThread - 1;
		
		//If I'm the last thread, I'm responsible for the last guy.
		if (which == _deques.length - 1)
			last = _nodes.size()-1;
		
		//Add the schedule entries to my queue.
		for (int i = first; i <= last; i++)
		{
			IScheduleEntry tmp = _nodes.get(i);
			_deques[which].add(tmp);
		}
		
		//Pick off the first guy
		IScheduleEntry n = _deques[which].poll();
	
		//Until there's nothing left.
		while (n != null)
		{
			//update the schedule entry.
			n.update();
						
			//get the next guy.
			n = _deques[which].poll();
			
			if (n == null && _stealing)
			{
				//If I'm out of work, see if I can steal work from any of the other guys.
				for (int i = 0; i < _deques.length; i++)
				{
					//Start after my entry so not everyone looks from the beginning.
					n = _deques[(_which + i) % _deques.length].poll();
					
					//If I still find no work, we're done.
					if (n != null)
						break;
				}
			}
			
		}
		return null;
	}
}
