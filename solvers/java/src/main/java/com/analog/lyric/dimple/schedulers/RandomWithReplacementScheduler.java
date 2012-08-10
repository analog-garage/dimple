package com.analog.lyric.dimple.schedulers;

import java.util.Random;

import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.RandomWithReplacementSchedule;

/**
 * @author jeffb
 * 
 *         This creates a dynamic schedule, which updates factors in a randomly
 *         chosen sequence with replacement. Prior to each factor update, the
 *         corresponding edges of the connected variables are updated. The
 *         number of factors updated per iteration is equal to the total number
 *         of factors in the graph. However, since the factors are chosen
 *         randomly with replacement, not all factors are necessarily updated in
 *         a single iteration.
 * 
 *         WARNING: This schedule DOES NOT respect any existing sub-graph
 *         scheduler associations. That is, if any sub-graph already has an
 *         associated scheduler, that scheduler is ignored in creating this
 *         schedule.
 */
public class RandomWithReplacementScheduler implements IScheduler
{
	protected Random _rand = new Random();

	public ISchedule createSchedule(FactorGraph g) 
	{
		return new RandomWithReplacementSchedule(g, _rand);
	}
	
	// Optionally set the seed for the random generator
	public void setSeed(long seed)
	{
		_rand.setSeed(seed);
	}
}
