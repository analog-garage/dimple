package com.analog.lyric.dimple.schedulers;

import java.util.Random;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.RandomWithoutReplacementSchedule;

/**
 * @author jeffb
 * 
 *         This creates a dynamic schedule, which updates factors in a randomly
 *         chosen sequence without replacement. Prior to each factor update, the
 *         corresponding edges of the connected variables are updated. On each
 *         iteration a new random update sequence is generated.
 * 
 *         WARNING: This schedule DOES NOT respect any existing sub-graph
 *         scheduler associations. That is, if any sub-graph already has an
 *         associated scheduler, that scheduler is ignored in creating this
 *         schedule.
 */
public class RandomWithoutReplacementScheduler implements IScheduler
{
	protected Random _rand = new Random();

	public ISchedule createSchedule(FactorGraph g) 
	{
		return new RandomWithoutReplacementSchedule(g, _rand);
	}
	
	// Optionally set the seed for the random generator
	public void setSeed(long seed)
	{
		_rand.setSeed(seed);
	}
}
