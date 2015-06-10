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

package com.analog.lyric.dimple.test.solvers.sumproduct;

import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.FiniteFieldAdd;
import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.FiniteFieldDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.FiniteFieldVariable;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.solvers.core.TestMessageUpdateEventHandler;

/**
 * Tests generation of message events in sumproduct solver.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestSumProductMessageEvents extends DimpleTestBase
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
		
		SumProductSolverGraph solver = model.createSolver(new SumProductSolver());
		
		//
		// Test events
		//
		
		TestMessageUpdateEventHandler handler = TestMessageUpdateEventHandler.setUpListener(solver);
		
		handler.testNodeSchedule(solver);
		handler.testEdgeSchedule(solver);
		
	}
	
	@Test
	public void testFiniteField()
	{
		//
		// Set up model/solver
		//
		
		final int n = 4;
		final FiniteFieldDomain domain = DiscreteDomain.finiteField(0x2f);
		final FactorGraph model = new FactorGraph();
		final Discrete[] vars = new Discrete[n];
		for (int i = 0; i < n; ++i)
		{
			Discrete var = new FiniteFieldVariable(domain);
			var.setName("var" + i);
			vars[i] = var;
		}
		model.addVariables(vars);
		
		final Factor[] factors = new Factor[n];
		for (int i = 0; i < n; ++i)
		{
			IFactorTable table = FactorTable.create(domain,domain);
			table.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
			table.randomizeWeights(_rand);
			
			int j = (i + 1) % n;
			Discrete v1 = vars[i], v2 = vars[j];
			Factor factor = factors[i] = model.addFactor(table, v1, v2);
			factor.setName(String.format("factor%d-%d",i,j));
		}
		
		Factor factor = model.addFactor(new FiniteFieldAdd(), vars[0], vars[1], vars[2]);
		factor.setName("ff-add");
		
		SumProductSolverGraph solver = model.createSolver(new SumProductSolver());
		
		//
		// Test events
		//
		
		TestMessageUpdateEventHandler handler = TestMessageUpdateEventHandler.setUpListener(solver);
		
		handler.testNodeSchedule(solver);
		handler.testEdgeSchedule(solver);
		
	}
	
	@Test
	public void testReal()
	{
		//
		// Set up model/solver
		//
		
		final int n = 4;
		final FactorGraph model = new FactorGraph();
		final Real[] vars = new Real[n];
		for (int i = 0; i < n; ++i)
		{
			Real var = new Real();
			var.setName("var" + i);
			vars[i] = var;
		}
		model.addVariables(vars);
		
		final Factor[] factors = new Factor[n];
		for (int i = 0; i < n; ++i)
		{
			int j = (i + 1) % n;
			Real v1 = vars[i], v2 = vars[j];
			Factor factor = factors[i] = model.addFactor(new Normal(0.0, 1.0), v1, v2);
			factor.setName(String.format("factor%d-%d",i,j));
		}
		
		SumProductSolverGraph solver = model.createSolver(new SumProductSolver());
		
		//
		// Test events
		//
		
		TestMessageUpdateEventHandler handler = TestMessageUpdateEventHandler.setUpListener(solver);
		
		handler.testNodeSchedule(solver);
		handler.testEdgeSchedule(solver);
	}
	
	@Test
	public void testRealJoint()
	{
		//
		// Set up model/solver
		//
		
		final int n = 4;
		final FactorGraph model = new FactorGraph();
		final RealJoint[] vars = new RealJoint[n];
		for (int i = 0; i < n; ++i)
		{
			RealJoint var = new RealJoint(2);
			var.setName("var" + i);
			vars[i] = var;
		}
		model.addVariables(vars);
		
		final Factor[] factors = new Factor[n];
		for (int i = 0; i < n; ++i)
		{
			int j = (i + 1) % n;
			RealJoint v1 = vars[i], v2 = vars[j];
			double[] means = new double[] { 1.0, 2.0 };
			double[][] covariance = new double[][]{new double [] {2.0, 1.0}, new double [] {1.0, 2.0}};
			Factor factor = factors[i] = model.addFactor(new MultivariateNormal(means, covariance), v1, v2);
			factor.setName(String.format("factor%d-%d",i,j));
		}
		
		SumProductSolverGraph solver = model.createSolver(new SumProductSolver());
		
		//
		// Test events
		//
		
		TestMessageUpdateEventHandler handler = TestMessageUpdateEventHandler.setUpListener(solver);
		
		handler.testNodeSchedule(solver);
		handler.testEdgeSchedule(solver);
	}
	
}
