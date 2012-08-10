package com.analog.lyric.dimple.schedulers;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.GibbsRandomScanSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;

/**
 * @author jeffb
 * 
 *         This creates a dynamic schedule, which updates only one variable that is
 *         randomly chosen. Prior to doing so, it updates the corresponding
 *         edges of the connected factors. This allows one iteration to
 *         correspond to exactly one variable update.
 * 
 *         WARNING: This schedule DOES NOT respect any existing sub-graph
 *         scheduler associations. That is, if any sub-graph already has an
 *         associated scheduler, that scheduler is ignored in creating this
 *         schedule. I believe this is a necessary limitation for Gibbs sampling
 *         to operate properly.
 *         
 */
public class GibbsRandomScanScheduler implements IScheduler
{
	public ISchedule createSchedule(FactorGraph g) 
	{
		return new GibbsRandomScanSchedule(g);
	}
}
