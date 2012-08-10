package com.analog.lyric.dimple.schedulers.schedule;

import java.util.HashMap;

import com.analog.lyric.dimple.model.FactorGraph;

public abstract class ScheduleBase implements ISchedule 
{
	protected FactorGraph _factorGraph;
	
	
	/*
	 * This method is called when setSchedule is called on the FactorGraph.
	 */
	public void attach(FactorGraph factorGraph) 
	{
		_factorGraph = factorGraph;
	}
	
	public FactorGraph getFactorGraph()
	{
		return _factorGraph;
	}

	public ISchedule copy(HashMap<Object,Object> old2newObjs) 
	{
		return null;
	}
	public ISchedule copyToRoot(HashMap<Object,Object> old2newObjs) 
	{
		return null;
	}
}
