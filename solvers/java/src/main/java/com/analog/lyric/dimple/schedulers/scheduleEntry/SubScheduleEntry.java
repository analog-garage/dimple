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
import java.util.Map;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import org.eclipse.jdt.annotation.Nullable;


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
	
	@Override
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
	
	@Override
	public IScheduleEntry copy(Map<Node,Node> old2newObjs)
	{
		return copy(old2newObjs, false);
	}
	@Override
	public IScheduleEntry copyToRoot(Map<Node,Node> old2newObjs)
	{
		return copy(old2newObjs, true);
	}

	public IScheduleEntry copy(Map<Node,Node> old2newObjs, boolean copyToRoot)
	{
		
		FactorGraph newGraph = (FactorGraph)old2newObjs.get(this.getSchedule().getFactorGraph());
		return new SubScheduleEntry(newGraph.getSchedule());
	}

	@Override
	public @Nullable Iterable<Port> getPorts()
	{
		//This is a subgraph.
		ArrayList<Port> ports = new ArrayList<Port>();

		FactorGraph subGraph = this.getSchedule().getFactorGraph();
		
		if (subGraph == null)	// If no sub-graph associated with this graph, don't return any ports
			return null;
		
		//Get all the non boundary variables associated with this sub graph
		//and add their ports.  subGraph getVariables does not return boundary variables.
		for (Variable v : subGraph.getVariables())
		{
			for (int index = 0, end = v.getSiblingCount(); index < end; index++)
			
				//whatsLeft.remove(p);
				ports.add(new Port(v,index));
		}
		
		//Get all the factors associated with this subgraph and add the ports
		for (Factor f : subGraph.getNonGraphFactors())
		{
			for (int index = 0, end = f.getSiblingCount(); index < end; index++)
				//whatsLeft.remove(p);
				ports.add(new Port(f,index));
		}
		return ports;
		
	}
}
