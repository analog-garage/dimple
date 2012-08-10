package com.analog.lyric.dimple.schedulers.schedule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.FactorList;
import com.analog.lyric.dimple.model.INode;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;

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
public class RandomWithoutReplacementSchedule extends ScheduleBase
{
	protected FactorList _factors;
	protected int _numFactors;
	protected int[] _factorIndices;
	protected Random _rand;


	public RandomWithoutReplacementSchedule(FactorGraph factorGraph, Random rand)
	{
		_factorGraph = factorGraph;
		_rand = rand;
		initialize();
	}

	@Override
	public void attach(FactorGraph factorGraph) 
	{
		super.attach(factorGraph);
		initialize();
	}

	protected void initialize()
	{
		_factors = _factorGraph.getNonGraphFactors();
		_numFactors = _factors.size();
		_factorIndices = new int[_numFactors];
	}

	@Override
	public Iterator<IScheduleEntry> iterator()
	{
		ArrayList<IScheduleEntry> updateList = new ArrayList<IScheduleEntry>();
		
		// Randomize the sequence
		for (int i = 0; i < _numFactors; i++) _factorIndices[i] = i;
		for (int iFactor = _numFactors - 1; iFactor > 0; iFactor--)
		{
			int randRange = iFactor + 1;
		    int randFactor = _rand.nextInt(randRange);
		    int nextIndex = _factorIndices[randFactor];
		    _factorIndices[randFactor] = _factorIndices[iFactor];
		    _factorIndices[iFactor] = nextIndex;
		}

		// One iteration consists of the number of factor updates equaling the total number of factors, even though not all factors will necessarily be updated
		for (int iFactor = 0; iFactor < _numFactors; iFactor++)
		{
			int factorIndex = _factorIndices[iFactor];
			Factor f = ((ArrayList<Factor>)_factors.values()).get(factorIndex);
			for (Port p : f.getPorts())
			{
				INode v = p.getConnectedNode();
				Port vPort = p.getSibling();
				updateList.add(new EdgeScheduleEntry(v,vPort));
			}
			updateList.add(new NodeScheduleEntry(f));
		}

		return updateList.iterator();
	}

}
