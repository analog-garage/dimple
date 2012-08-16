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

package com.analog.lyric.dimple.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.analog.lyric.dimple.FactorFunctions.XorDelta;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.DiscreteDomain;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.schedulers.RandomWithReplacementScheduler;
import com.analog.lyric.dimple.schedulers.RandomWithoutReplacementScheduler;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;


public class RandomSchedulerTest
{
	protected static boolean debugPrint = false;

	@Test
	public void test1() 
	{
		if (debugPrint) System.out.println("== test1 ==");

		FactorGraph g = new FactorGraph();
		g.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		RandomWithoutReplacementScheduler scheduler = new RandomWithoutReplacementScheduler();
		scheduler.setSeed(1);		// Make it repeatable
		g.setScheduler(scheduler);
		g.getSolver().setNumIterations(20);

		DiscreteDomain bit = new DiscreteDomain(0.0,1.0); 
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

		a.setInput(0.9, 0.1);
		b.setInput(0.9, 0.1);
		c.setInput(0.9, 0.1);
		d.setInput(0.5, 0.5);

		ISchedule schedule = scheduler.createSchedule(g);

		int i = 0;
		for (IScheduleEntry entry : schedule)
		{
			if (debugPrint) System.out.println(entry.toString());
			if (i == 3)
				assertTrue(entry.toString().equals("IScheduleEntry [F4]"));
			else if (i == 7)
				assertTrue(entry.toString().equals("IScheduleEntry [F1]"));
			else if (i == 11)
				assertTrue(entry.toString().equals("IScheduleEntry [F2]"));
			else if (i == 15)
				assertTrue(entry.toString().equals("IScheduleEntry [F3]"));
			i++;
		}

	}

	@Test
	public void test2() 
	{
		if (debugPrint) System.out.println("== test2 ==");

		FactorGraph g = new FactorGraph();
		g.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		RandomWithReplacementScheduler scheduler = new RandomWithReplacementScheduler();
		scheduler.setSeed(1);		// Make it repeatable
		g.setScheduler(scheduler);
		g.getSolver().setNumIterations(20);

		DiscreteDomain bit = new DiscreteDomain(0.0,1.0); 
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

		a.setInput(0.9, 0.1);
		b.setInput(0.9, 0.1);
		c.setInput(0.9, 0.1);
		d.setInput(0.5, 0.5);

		ISchedule schedule = scheduler.createSchedule(g);

		int i = 0;
		for (IScheduleEntry entry : schedule)
		{
			if (debugPrint) System.out.println(entry.toString());
			if (i == 3)
				assertTrue(entry.toString().equals("IScheduleEntry [F3]"));
			else if (i == 7)
				assertTrue(entry.toString().equals("IScheduleEntry [F1]"));
			else if (i == 11)
				assertTrue(entry.toString().equals("IScheduleEntry [F2]"));
			else if (i == 15)
				assertTrue(entry.toString().equals("IScheduleEntry [F2]"));
			i++;
		}

	}

}
