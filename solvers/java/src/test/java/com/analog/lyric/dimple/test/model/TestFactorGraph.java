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

import com.analog.lyric.dimple.factorfunctions.Sum;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.model.variables.VariableList;

public class TestFactorGraph
{
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
		assertFactorGraphInvariants(fg);
		assertEquals("Graph", fg.getClassLabel());
		assertTrue(fg.getNumStepsInfinite());
		assertEquals(1, fg.getNumSteps());
		assertTrue(fg.getFactorGraphStreams().isEmpty());
		assertNull(fg.getAssociatedScheduler());
		assertTrue(fg.getSiblings().isEmpty());
		assertTrue(fg.isTree());
		assertEquals(0, fg.getVariableCount());
		assertEquals(0, fg.getFactorCount());
		assertFalse(fg.isSolverRunning());
		assertSame(fg, fg.getRootGraph());
		
		fg.setSolverFactory(null);
		assertNull(fg.getSolver());
		
		Bit b1 = new Bit();
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
		
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		assertTrue(fg.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph);
		assertTrue(b1.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.SDiscreteVariable);
		assertTrue(b2.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.SDiscreteVariable);
		assertTrue(sum1.getSolver() instanceof com.analog.lyric.dimple.solvers.sumproduct.STableFactor);
		
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
		
		VariableList vars = fg.getVariables();
		assertEquals(vars.size(), fg.getVariableCount());
		for (VariableBase var : vars)
		{
			assertSame(var, fg.getVariableByUUID(var.getUUID()));
		}
	}
}
