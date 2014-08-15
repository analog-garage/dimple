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

package com.analog.lyric.dimple.test.solvers.minsum;

import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.minsum.SFactorGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.solvers.core.TestMessageUpdateEventHandler;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestMinSumMessageEvents extends DimpleTestBase
{
	private final Random _rand = new Random(42);
	
	@Test
	public void testDiscrete()
	{
		//
		// Set up model/solver
		//
		
		final int n = 4;
		final DiscreteDomain d6 = DiscreteDomain.range(1, 6);
		final FactorGraph model = new FactorGraph();
		final Discrete[] vars = new Discrete[n];
		for (int i = 0; i < n; ++i)
		{
			Discrete var = new Discrete(d6);
			var.setName("var" + i);
			vars[i] = var;
		}
		model.addVariables(vars);
		
		final Factor[] factors = new Factor[n];
		for (int i = 0; i < n; ++i)
		{
			IFactorTable table = FactorTable.create(d6,d6);
			table.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
			table.randomizeWeights(_rand);
			
			int j = (i + 1) % n;
			Discrete v1 = vars[i], v2 = vars[j];
			Factor factor = factors[i] = model.addFactor(table, v1, v2);
			factor.setName(String.format("factor%d-%d",i,j));
		}
		
		SFactorGraph solver = model.createSolver(new MinSumSolver());
		
		//
		// Test events
		//
		
		TestMessageUpdateEventHandler handler = TestMessageUpdateEventHandler.setUpListener(solver);
		
		handler.testNodeSchedule(solver);
		handler.testEdgeSchedule(solver);
		
	}

}
