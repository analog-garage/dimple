package com.analog.lyric.dimple.schedulers;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;

/**
 * @author jeffb
 * 
 *         Any scheduler must implement this interface.
 * 
 *         The createSchedule method takes a factor-graph as input, and returns
 *         a schedule of any type.
 */
public interface IScheduler
{
	public ISchedule createSchedule(FactorGraph g) ;
}
