/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.CustomScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.schedule.FixedSchedule;
import com.analog.lyric.dimple.schedulers.schedule.ISchedule;
import com.analog.lyric.dimple.schedulers.schedule.ScheduleValidationException;
import com.analog.lyric.dimple.schedulers.scheduleEntry.BlockScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.EdgeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.IScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.NodeScheduleEntry;
import com.analog.lyric.dimple.schedulers.scheduleEntry.SubScheduleEntry;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.samplers.block.BlockMHSampler;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test for {@link CustomScheduler} class.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@SuppressWarnings("deprecation")
public class TestCustomScheduler extends DimpleTestBase
{
	@SuppressWarnings("deprecation")
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph("fg");
		try (CurrentModel cur = using(fg))
		{
			Bit a = bit("a");
			Bit b = bit("b");
			Bit c = bit("c");
			VariableBlock abc = fg.addVariableBlock(a,b,c);
			VariableBlock ab = fg.addVariableBlock(a,b);
			VariableBlock bc = fg.addVariableBlock(b,c);
			Factor axorb = name("axorb", addFactor(new Xor(), a, b));
			Factor bxorc = name("bxorc", addFactor(new Xor(), b, c));
			
			@SuppressWarnings("deprecation")
			CustomScheduler scheduler = new CustomScheduler(fg);
			assertSame(fg, scheduler.getGraph());
			assertNull(scheduler.declaredSchedulerType());
			assertArrayEquals(new Object[] { BPOptions.scheduler, GibbsOptions.scheduler },
				scheduler.applicableSchedulerOptions().toArray());
			assertEquals(0, scheduler.createSchedule(fg).size());
			assertSchedule(scheduler);
			
			// Basic manual tree schedule
			CustomScheduler bpScheduler = new CustomScheduler(fg, BPOptions.scheduler);
			assertTrue(bpScheduler.isMutable());
			assertTrue(bpScheduler.isCustomScheduler());
			assertSame(fg, bpScheduler.getGraph());
			assertSame(BPOptions.scheduler, bpScheduler.declaredSchedulerType());
			assertArrayEquals(new Object[] { BPOptions.scheduler }, bpScheduler.applicableSchedulerOptions().toArray());
			bpScheduler.validateForGraph(fg);
			bpScheduler.addEdge(a.getPort(0));
			bpScheduler.addEdge(axorb, b);
			bpScheduler.addEdge(c.getPort(0));
			bpScheduler.addEdge(bxorc, b);
			bpScheduler.addNode(b);
			bpScheduler.addEdges(port(axorb, a), port(bxorc, c));
			assertSchedule(bpScheduler,
				port(a, axorb),	port(axorb, b),	port(c, bxorc),	port(bxorc, b),	b, port(axorb, a), port(bxorc, c));
		
			// Try again using paths
			bpScheduler = new CustomScheduler(fg, BPOptions.scheduler);
			bpScheduler.addPath(a, axorb, b, bxorc, c);
			bpScheduler.addPath(c, b, a);
			bpScheduler.addFactor(axorb);
			bpScheduler.addFactors(bxorc, axorb);
			assertSchedule(bpScheduler,
				port(a, axorb),	port(axorb, b),	port(b, bxorc),	port(bxorc, c),
				port(c, bxorc), port(bxorc, b), port(b, axorb), port(axorb, a),
				axorb, bxorc, axorb);
			bpScheduler.addBlockWithReplacement(new BlockMHSampler(), ab);
			bpScheduler.addBlockWithReplacement(new BlockMHSampler(), ab);
			assertSchedule(bpScheduler,
				port(axorb, b),	port(bxorc, c),
				port(c, bxorc), port(bxorc, b), port(axorb, a),
				axorb, bxorc, axorb, ab, ab);
			
			CustomScheduler bpScheduler2 = new CustomScheduler(fg, BPOptions.scheduler);
			bpScheduler2.addAll(bpScheduler.createSchedule(fg));
			assertSchedule(bpScheduler2,
				port(axorb, b),	port(bxorc, c),
				port(c, bxorc), port(bxorc, b), port(axorb, a),
				axorb, bxorc, axorb, ab, ab);
			
			
			// Gibbs schedule
			CustomScheduler gibbsScheduler = new CustomScheduler(fg, GibbsOptions.scheduler);
			gibbsScheduler.addNode(a);
			gibbsScheduler.addNodes(b,c);
			gibbsScheduler.addVariable(a);
			gibbsScheduler.addVariables(b,c);
			// When this fails, no entries should be added:
			expectThrow(ScheduleValidationException.class, gibbsScheduler, "addVariables", c,c,c,new Bit());
			assertSchedule(gibbsScheduler, a,b,c,a,b,c);
			gibbsScheduler.addBlock(new BlockMHSampler(), b, c);
			assertSchedule(gibbsScheduler, a,b,c,a,b,c,bc);
			gibbsScheduler.addBlockWithReplacement(new BlockMHSampler(), abc);
			assertSchedule(gibbsScheduler, bc, abc);
			
			// Errors
			expectThrow(ScheduleValidationException.class, bpScheduler, "addNode", new Bit());
			expectThrow(ScheduleValidationException.class, "Cannot use edge entry with Gibbs.*",
				gibbsScheduler, "addEdge", a, axorb);
			expectThrow(ScheduleValidationException.class, "Cannot use factor node entry with Gibbs.*",
				gibbsScheduler, "addNode", axorb);
			expectThrow(ScheduleValidationException.class, ".*can only be used with graph.*",
				bpScheduler, "validateForGraph", new FactorGraph());
			expectThrow(ScheduleValidationException.class, ".*requires at least two.*", bpScheduler, "addPath", a);
			expectThrow(ScheduleValidationException.class, ".*not adjacent.*", bpScheduler, "addPath", a, c);
			expectThrow(ScheduleValidationException.class, "Cannot add SubScheduleEntry.*",
				bpScheduler, "addAll", Arrays.asList(new SubScheduleEntry(new FixedSchedule())));
		}
		
		FactorGraph fg2 = new FactorGraph("fg2");
		try (CurrentModel cur = using(fg2))
		{
			Bit a = bit("a");
			Bit b = bit("b");
			name("axorb", addFactor(new Xor(), a, b));
			Factor aaxorb = name("aaxorb", addFactor(new Xor(), a, a, b));
			
			CustomScheduler scheduler = new CustomScheduler(fg2, BPOptions.scheduler);
			expectThrow(ScheduleValidationException.class, ".*not a unique path.*",
				scheduler, "addPath", a, aaxorb);
			expectThrow(ScheduleValidationException.class, ".*connected by more than one path.*",
				scheduler, "addPath", a, b);
		}
	}
	
	private static Port port(INode from, INode to)
	{
		return from.getPort(from.findSibling(to));
	}
	
	public static void assertSchedule(CustomScheduler scheduler, Object ... entries)
	{
		assertSchedule(scheduler.createSchedule(scheduler.getGraph()), entries);
	}
	
	public static void assertSchedule(IScheduler scheduler, FactorGraph graph, Object ...entries)
	{
		assertSchedule(scheduler.createSchedule(graph), entries);
	}
	
	public static void assertSchedule(ISchedule schedule, Object ... entries)
	{
		int i = 0;
		for (IScheduleEntry entry : schedule)
		{
			assertTrue(i < entries.length);
			Object expected = entries[i];
			
			if (expected instanceof INode)
			{
				assertTrue(entry instanceof NodeScheduleEntry);
				assertSame(expected, ((NodeScheduleEntry)entry).getNode());
			}
			else if (expected instanceof Port)
			{
				Port port = (Port)expected;
				assertTrue(entry instanceof EdgeScheduleEntry);
				EdgeScheduleEntry edgeEntry = (EdgeScheduleEntry)entry;
				assertSame(port.getNode(), edgeEntry.getNode());
				assertEquals(port.getSiblingNumber(), edgeEntry.getPortNum());
			}
			else if (expected instanceof VariableBlock)
			{
				VariableBlock block = (VariableBlock)expected;
				assertTrue(entry instanceof BlockScheduleEntry);
				BlockScheduleEntry blockEntry = (BlockScheduleEntry)entry;
				assertSame(block, blockEntry.getBlock());
			}
			
			++i;
		}
		assertEquals(entries.length, i);
	}
}
