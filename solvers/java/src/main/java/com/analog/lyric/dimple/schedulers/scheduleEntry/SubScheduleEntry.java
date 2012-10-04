/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.ArrayList;
import java.util.HashMap;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;


/**
 * @author jeffb
 * 
 *         A schedule entry that contains a sub-schedule. The update method runs
 *         through the entire sub-schedule, executing the update method on each
 *         entry.
 */
public class SubScheduleEntry implements IScheduleEntry
{
	private ISchedule _subschedule;
	
	public SubScheduleEntry(ISchedule subschedule)
	{
		_subschedule = subschedule;
	}
	
	public void update() 
	{
		for (IScheduleEntry entry : _subschedule)
		{
			entry.update();
		}
	}
	

	public ISchedule getSchedule()
	{
		return _subschedule;
	}
	
	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, false);
	}
	public IScheduleEntry copyToRoot(HashMap<Object,Object> old2newObjs) 
	{
		return copy(old2newObjs, true);
	}

	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs, boolean copyToRoot) 
	{
		
		FactorGraph newGraph = (FactorGraph)old2newObjs.get(this.getSchedule().getFactorGraph());
		return new SubScheduleEntry(newGraph.getSchedule());
	}

	public Iterable<Port> getPorts()
	{
		//This is a subraph.
		ArrayList<Port> ports = new ArrayList<Port>();

		FactorGraph subGraph = this.getSchedule().getFactorGraph();
		
		//Get all the non boundary variables associated with this sub graph
		//and add their ports.  subGraph getVariables does not return boundary variables.
		for (VariableBase v : subGraph.getVariables())
		{
			for (Port p : v.getPorts())
				//whatsLeft.remove(p);
				ports.add(p);
		}
		
		//Get all the factors associated with this subgraph and add the ports
		for (Factor f : subGraph.getNonGraphFactors())
		{
			for (Port p : f.getPorts())
				//whatsLeft.remove(p);
				ports.add(p);
		}	
		return ports;
		
	}
}
