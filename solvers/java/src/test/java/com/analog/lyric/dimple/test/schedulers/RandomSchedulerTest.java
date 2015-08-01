/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.schedulers;

import static com.analog.lyric.dimple.environment.DimpleEnvironment.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.XorDelta;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.RandomWithReplacementScheduler;
import com.analog.lyric.dimple.schedulers.RandomWithoutReplacementScheduler;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.google.common.collect.Iterables;


public class RandomSchedulerTest extends DimpleTestBase
{
	protected static boolean debugPrint = false;

	@Test
	public void test1()
	{
		if (debugPrint) System.out.println("== test1 ==");

		FactorGraph g = new FactorGraph();
		g.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		RandomWithoutReplacementScheduler scheduler = new RandomWithoutReplacementScheduler();
		g.setOption(BPOptions.scheduler, scheduler);
		requireNonNull(g.getSolver()).setNumIterations(20);

		DiscreteDomain bit = DiscreteDomain.bit();
		Discrete a = new Discrete(bit);
		Discrete b = new Discrete(bit);
		Discrete c = new Discrete(bit);
		Discrete d = new Discrete(bit);
		a.setName("a");
		b.setName("b");
		c.setName("c");
		d.setName("d");

		Factor f1 = g.addFactor(new XorDelta(), a, b, c);
		Factor f2 = g.addFactor(new XorDelta(), a, c, d);
		Factor f3 = g.addFactor(new XorDelta(), a, b, c);
		Factor f4 = g.addFactor(new XorDelta(), a, c, d);
		f1.setName("F1");
		f2.setName("F2");
		f3.setName("F3");
		f4.setName("F4");

		a.setPrior(0.9, 0.1);
		b.setPrior(0.9, 0.1);
		c.setPrior(0.9, 0.1);
		d.setPrior(0.5, 0.5);

		ISchedule schedule = scheduler.createSchedule(g);
		IScheduleEntry[] entries = Iterables.toArray(schedule, IScheduleEntry.class);
		assertEquals(16, entries.length);
		
		// Make sure there is a node entry for each factor in any order.
		Set<INode> factorsSeen = new HashSet<INode>();
		for (int i = 3; i < 16; i += 4)
		{
			assertEquals(IScheduleEntry.Type.NODE, entries[i].type());
			NodeScheduleEntry nodeEntry = (NodeScheduleEntry)entries[i];
			assert(nodeEntry.getNode() instanceof Factor);
			factorsSeen.add(nodeEntry.getNode());
		}
		assertEquals(4, factorsSeen.size());
	}

	@Test
	public void test2()
	{
		if (debugPrint) System.out.println("== test2 ==");

		activeRandom().setSeed(42);
		
		FactorGraph g = new FactorGraph();
		g.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		RandomWithReplacementScheduler scheduler = new RandomWithReplacementScheduler();
		g.setOption(BPOptions.scheduler, scheduler);
		requireNonNull(g.getSolver()).setNumIterations(20);

		DiscreteDomain bit = DiscreteDomain.bit();
		Discrete a = new Discrete(bit);
		Discrete b = new Discrete(bit);
		Discrete c = new Discrete(bit);
		Discrete d = new Discrete(bit);
		a.setName("a");
		b.setName("b");
		c.setName("c");
		d.setName("d");

		Factor f1 = g.addFactor(new XorDelta(), a, b, c);
		Factor f2 = g.addFactor(new XorDelta(), a, c, d);
		Factor f3 = g.addFactor(new XorDelta(), a, b, c);
		Factor f4 = g.addFactor(new XorDelta(), a, c, d);
		f1.setName("F1");
		f2.setName("F2");
		f3.setName("F3");
		f4.setName("F4");

		a.setPrior(0.9, 0.1);
		b.setPrior(0.9, 0.1);
		c.setPrior(0.9, 0.1);
		d.setPrior(0.5, 0.5);

		// Make sure there is approximately the same number of entries for each factor in any order.
		Map<INode,Integer> factorCounts = new HashMap<INode,Integer>();
		factorCounts.put(f1, 0);
		factorCounts.put(f2, 0);
		factorCounts.put(f3, 0);
		factorCounts.put(f4, 0);

		for (int j = 0; j < 10000; ++j)
		{
			ISchedule schedule = scheduler.createSchedule(g);
			IScheduleEntry[] entries = Iterables.toArray(schedule, IScheduleEntry.class);
			assertEquals(16, entries.length);

			for (int i = 3; i < 16; i += 4)
			{
				assertEquals(IScheduleEntry.Type.NODE, entries[i].type());
				NodeScheduleEntry nodeEntry = (NodeScheduleEntry)entries[i];
				assert(nodeEntry.getNode() instanceof Factor);
				factorCounts.put(nodeEntry.getNode(), factorCounts.get(nodeEntry.getNode()) + 1);
			}
		}

		for (Integer count : factorCounts.values())
		{
			assertEquals(10000.0, count, 100);
		}
	}

}
