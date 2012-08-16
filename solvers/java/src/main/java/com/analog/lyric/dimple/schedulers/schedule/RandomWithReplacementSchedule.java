/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

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
 *         This is a dynamic schedule, which updates factors in a randomly
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
public class RandomWithReplacementSchedule extends ScheduleBase
{
	protected FactorList _factors;
	protected int _numFactors;
	protected Random _rand;


	public RandomWithReplacementSchedule(FactorGraph factorGraph, Random rand)
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
	}

	@Override
	public Iterator<IScheduleEntry> iterator()
	{
		ArrayList<IScheduleEntry> updateList = new ArrayList<IScheduleEntry>();

		// One iteration consists of the number of factor updates equaling the total number of factors, even though not all factors will necessarily be updated
		for (int iFactor = 0; iFactor < _numFactors; iFactor++)
		{
			int factorIndex = _rand.nextInt(_numFactors);
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
