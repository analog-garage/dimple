package com.analog.lyric.dimple.schedulers.schedule;

import java.util.HashMap;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;


/**
 * @author jeffb
 * 
 *         Any schedule produced by a scheduler must implement this interface. A
 *         schedule is required to implement the Iterable interface, with each
 *         iteration, returning a sequence of ScheduleEntry objects.
 */
public interface ISchedule extends Iterable<IScheduleEntry>
{
	
	/*
	 * This method is called when setSchedule is called on the FactorGraph.
	 */
	public void attach(FactorGraph factorGraph) ;	
	public FactorGraph getFactorGraph();
	public ISchedule copy(HashMap<Object,Object> old2newObjs) ;
	public ISchedule copyToRoot(HashMap<Object,Object> old2newObjs) ;
}
