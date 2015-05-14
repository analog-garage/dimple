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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.schedulers.CustomScheduler;
import com.analog.lyric.dimple.schedulers.DefaultScheduler;
import com.analog.lyric.dimple.schedulers.FloodingScheduler;
import com.analog.lyric.dimple.schedulers.GibbsDefaultScheduler;
import com.analog.lyric.dimple.schedulers.IGibbsScheduler;
import com.analog.lyric.dimple.schedulers.IScheduler;
import com.analog.lyric.dimple.schedulers.SchedulerOptionKey;
import com.analog.lyric.dimple.schedulers.SequentialScheduler;
import com.analog.lyric.dimple.schedulers.TreeOrSequentialScheduler;
import com.analog.lyric.dimple.schedulers.validator.ScheduleValidatorOptionKey;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.options.LocalOptionHolder;
import com.analog.lyric.options.OptionValidationException;

/**
 * Tests for {@link SchedulerOptionKey} and {@link ScheduleValidatorOptionKey}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestSchedulerOptionKey extends DimpleTestBase
{
	
	@SuppressWarnings("null")
	@Test
	public void test()
	{
		SchedulerOptionKey schedulerKey =
			new SchedulerOptionKey(TestSchedulerOptionKey.class, "key11", DefaultScheduler.class);
		assertSame(IScheduler.class, schedulerKey.type());
		assertSame(DefaultScheduler.class, schedulerKey.defaultClass());
		assertSame(DefaultScheduler.class, schedulerKey.defaultValue().getClass());
		assertSame(DimpleEnvironment.active().schedulers(), schedulerKey.getRegistry());
		assertNull(schedulerKey.getValidatorKey());
		
		FactorGraph fg = new FactorGraph();
		ISolverFactorGraph sfg = fg.setSolverFactory(new SumProductSolver());
		
		schedulerKey.set(fg, "SequentialScheduler");
		assertSame(SequentialScheduler.class, schedulerKey.get(fg).getClass());
		schedulerKey.set(fg, FloodingScheduler.class);
		assertSame(FloodingScheduler.class, schedulerKey.get(fg).getClass());
		IScheduler scheduler = new TreeOrSequentialScheduler();
		schedulerKey.set(fg, scheduler);
		assertSame(scheduler, schedulerKey.get(fg));
		assertSame(scheduler, schedulerKey.convertToValue(scheduler));
		expectThrow(OptionValidationException.class, schedulerKey, "convertToValue", Object.class);
		
		scheduler = new CustomScheduler(fg, schedulerKey);
		assertSame(scheduler, schedulerKey.validate(scheduler, fg));
		
		FactorGraph fg2 = new FactorGraph();
		expectThrow(OptionValidationException.class, schedulerKey, "validate", scheduler, fg2);
		
		FactorGraph subgraph = fg.addGraph(new FactorGraph());
		assertTrue(schedulerKey.validForDelegator(new DefaultScheduler(), subgraph));
		assertTrue(schedulerKey.validForDelegator(scheduler, fg));
		assertTrue(schedulerKey.validForDelegator(scheduler, sfg));
		assertFalse(schedulerKey.validForDelegator(scheduler, subgraph));
		// Returns true by default if there is no graph to validate against:
		assertTrue(schedulerKey.validForDelegator(scheduler, new Bit()));
		assertTrue(schedulerKey.validForDelegator(scheduler, new LocalOptionHolder()));
		
		assertSame(scheduler, schedulerKey.validate(scheduler, sfg));
		assertSame(scheduler, schedulerKey.validate(scheduler, new Bit()));
		
		//
		// Test Gibbs key
		//
		
		assertSame(GibbsDefaultScheduler.class, GibbsOptions.scheduler.defaultClass());
		assertSame(IGibbsScheduler.class, GibbsOptions.scheduler.type());
		assertSame(GibbsOptions.scheduleValidator, GibbsOptions.scheduler.getValidatorKey());
		expectThrow(ClassCastException.class, GibbsOptions.scheduler, "validate", new SequentialScheduler(), fg);
		assertNull(GibbsOptions.scheduler.get(fg));
		assertSame(GibbsDefaultScheduler.class, GibbsOptions.scheduler.getOrDefault(fg).getClass());
		expectThrow(ClassCastException.class, fg, "setOption", GibbsOptions.scheduler, new FloodingScheduler());
		// FIXME - figure out why this fails with cast exception on build machine. Need Java upgrade perhaps?
//		expectThrow(OptionValidationException.class, GibbsOptions.scheduler, "set", fg, "FloodingScheduler");
		
	}
}
