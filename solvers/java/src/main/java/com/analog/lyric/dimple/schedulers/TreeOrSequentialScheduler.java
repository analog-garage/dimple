package com.analog.lyric.dimple.schedulers;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;

/**
 * @author jeffb
 * 
 *         If this graph is a tree, or any of it's sub-graphs are trees, this
 *         class generates a tree-schedule. Otherwise it generates a sequential
 *         schedule on the portion of the graph that is not a tree.
 * 
 *         This scheduler respects any schedulers already assigned to
 *         sub-graphs. That is, if a sub-graph already has a scheduler
 *         associated with it, that scheduler will be used for that sub-graph
 *         instead of this one.
 */
public class TreeOrSequentialScheduler extends TreeSchedulerAbstract
{
	@Override
	protected ISchedule createNonTreeSchedule(FactorGraph g) 
	{
		SequentialScheduler sequentialScheduler = new SequentialScheduler();
		sequentialScheduler.setSubGraphScheduler(TreeOrSequentialScheduler.class);
		
		return sequentialScheduler.createSchedule(g);
	}


}
