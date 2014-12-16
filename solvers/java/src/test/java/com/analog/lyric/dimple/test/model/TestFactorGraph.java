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

package com.analog.lyric.dimple.test.model;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.test.DimpleTestBase;

public class TestFactorGraph extends DimpleTestBase
{
	@Test
	public void test()
	{
		// Temporarily change active environment to demonstrate that is what
		// is getting used when graph is constructed.
		DimpleEnvironment env = new DimpleEnvironment();
		DimpleEnvironment.setActive(env);
		
		FactorGraph fg = new FactorGraph();
		
		DimpleEnvironment.setActive(DimpleEnvironment.defaultEnvironment());
		assertNotSame(env, DimpleEnvironment.active());
		
		assertFactorGraphInvariants(fg);
		assertEquals("Graph", fg.getClassLabel());
		assertTrue(fg.getNumStepsInfinite());
		assertEquals(1, fg.getNumSteps());
		assertTrue(fg.getFactorGraphStreams().isEmpty());
		assertNull(fg.getExplicitlySetScheduler());
		assertTrue(fg.getSiblings().isEmpty());
		assertTrue(fg.isTree());
		assertEquals(0, fg.getVariableCount());
		assertEquals(0, fg.getFactorCount());
		assertFalse(fg.isSolverRunning());
		assertSame(fg, fg.getRootGraph());
		
		// Test environment and option parents
		assertSame(env, fg.getEnvironment());
		assertSame(env, fg.getEventParent());
		assertSame(env, fg.getOptionParent());
		fg.setEventAndOptionParent(null);
		assertNull(fg.getOptionParent());
		assertNull(fg.getEventParent());
		fg.setEventAndOptionParent(env);
		assertSame(env, fg.getOptionParent());
		
		fg.setSolverFactory(null);
		assertNull(fg.getSolver());
		
		Bit b1 = new Bit();
		assertSame(DimpleEnvironment.active(), b1.getEnvironment()); // defaults to active environment
		Bit b2 = new Bit();
		Factor sum1 = fg.addFactor(new Sum(), b1, b2);
		assertSame(fg, b1.getParentGraph());
		assertSame(b1, fg.getVariableByUUID(b1.getUUID()));
		assertNull(b1.getSolver());
		assertSame(fg, b2.getParentGraph());
		assertSame(b2, fg.getVariableByUUID(b2.getUUID()));
		assertNull(b2.getSolver());
		assertSame(fg, sum1.getParentGraph());
		assertSame(sum1, fg.getFactorByUUID(sum1.getUUID()));
		assertNull(sum1.getSolver());
		assertSame(env, b1.getEnvironment()); // gets environment from parent graph
		
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		assertTrue(fg.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph);
		assertTrue(b1.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscrete);
		assertTrue(b2.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.SumProductDiscrete);
		assertTrue(sum1.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.SumProductTableFactor);
		
		fg.setSolverFactory(null);
		assertNull(fg.getSolver());
		assertNull(b1.getSolver());
		assertNull(b2.getSolver());
		assertNull(sum1.getSolver());
	}
	
	public static void assertFactorGraphInvariants(FactorGraph fg)
	{
		assertSame(fg, fg.asFactorGraph());
		assertTrue(fg.isFactorGraph());
		
		int id = fg.getGraphId();
		assertTrue(id != 0);
		assertSame(fg, fg.getEnvironment().factorGraphs().getGraphWithId(id));
		
		VariableList vars = fg.getVariables();
		assertEquals(vars.size(), fg.getVariableCount());
		for (Variable var : vars)
		{
			assertSame(var, fg.getVariableByUUID(var.getUUID()));
		}
	}
}
