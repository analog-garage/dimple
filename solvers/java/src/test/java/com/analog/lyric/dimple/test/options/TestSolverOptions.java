/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.options;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.options.SolverOptions;
import com.analog.lyric.dimple.solvers.core.SFactorGraphBase;
import com.analog.lyric.dimple.solvers.core.multithreading.MultiThreadingManager;
import com.analog.lyric.dimple.test.dummySolver.DummyFactorGraph;

/**
 * Tests behavior of {@link SolverOptions} on {@link SFactorGraphBase}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestSolverOptions
{
	@Test
	public void test()
	{
		// Test default values
		assertEquals(1, SolverOptions.iterations.defaultIntValue());
		assertEquals(false, SolverOptions.enableMultithreading.defaultBooleanValue());
		
		DimpleEnvironment env = DimpleEnvironment.active();
		
		env.setOption(SolverOptions.iterations, 42);
		assertEquals((Integer)42, env.getOption(SolverOptions.iterations));
		env.setOption(SolverOptions.enableMultithreading, true);
		assertEquals(true, env.getOption(SolverOptions.enableMultithreading));
		
		FactorGraph fg = new FactorGraph();
		assertEquals((Integer)42, fg.getOption(SolverOptions.iterations));
		assertEquals(true, fg.getOption(SolverOptions.enableMultithreading));

		DummyFactorGraph sfg = new DummyFactorGraph(fg);
		// Set manager so that calling useMultithreading(boolean) won't barf.
		sfg.setMultithreadingManager(new MultiThreadingManager(fg));
		
		// Test pre-initialization state. Most if not all options are not updated until
		// initialize() is called.
		
		assertEquals((Integer)42, sfg.getOption(SolverOptions.iterations));
		assertEquals(1, sfg.getNumIterations());
		assertEquals(true, sfg.getOption(SolverOptions.enableMultithreading));
		assertFalse(sfg.useMultithreading());
		
		// Test post-initialization state.
		
		sfg.initialize();
		assertEquals(42, sfg.getNumIterations());
		assertTrue(sfg.useMultithreading());
		
		// Make sure set* methods update option setting.
		assertNull(sfg.getLocalOption(SolverOptions.iterations));
		sfg.setNumIterations(23);
		assertEquals(23, sfg.getNumIterations());
		assertEquals((Integer)23, sfg.getLocalOption(SolverOptions.iterations));
		assertNull(sfg.getLocalOption(SolverOptions.enableMultithreading));
		sfg.useMultithreading(false);
		assertFalse(sfg.useMultithreading());
		assertEquals(false, sfg.getLocalOption(SolverOptions.enableMultithreading));
	}
}
