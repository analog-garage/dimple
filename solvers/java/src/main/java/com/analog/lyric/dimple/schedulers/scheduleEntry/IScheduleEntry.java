package com.analog.lyric.dimple.schedulers.scheduleEntry;

import java.util.HashMap;

import com.analog.lyric.dimple.model.Port;

/**
 * @author jeffb
 * 
 *         All types of schedule entries must implement the update() method,
 *         which is to update the portion of the graph associated with the
 *         entry.
 */
public interface IScheduleEntry
{
	public abstract void update() ;
	
	public IScheduleEntry copy(HashMap<Object,Object> old2newObjs) ;
	public IScheduleEntry copyToRoot(HashMap<Object,Object> old2newObjs) ;
	
	public Iterable<Port> getPorts();
}
