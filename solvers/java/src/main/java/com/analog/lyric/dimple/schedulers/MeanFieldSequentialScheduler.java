package com.analog.lyric.dimple.schedulers;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;

/**
 * @author jeffb
 * 
 *         This class generates a sequential schedule for the purpose of the
 *         Mean Field solver. This schedule updates each variable sequentially.
 *         Prior to updating each variable, the edge from each neighboring
 *         factor node is updated.
 * 
 *         WARNING: This scheduler DOES NOT respect any existing sub-graph
 *         scheduler associations. That is, if any sub-graph already has an
 *         associated scheduler, that scheduler is ignored in creating this
 *         schedule.
 * 
 */
public class MeanFieldSequentialScheduler implements IScheduler
{

	public ISchedule createSchedule(FactorGraph g) 
	{
		FixedSchedule schedule = new FixedSchedule();

		for (VariableBase v : g.getVariables())
		{
			for (Port p : v.getPorts())
			{
				INode f = p.getConnectedNode();
				Port fPort = p.getSibling();
				schedule.add(new EdgeScheduleEntry(f,fPort));
			}
			schedule.add(new NodeScheduleEntry(v));
		}

		return schedule;
	}

}
