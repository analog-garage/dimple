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

package com.analog.lyric.dimple.test.solvers.sumproduct;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static com.analog.lyric.math.Utilities.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import cern.colt.list.IntArrayList;

import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.SampleDataLayer;
import com.analog.lyric.dimple.events.DimpleEventLogger;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterables;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.FactorToVariableMessageEvent;
import com.analog.lyric.dimple.solvers.core.VariableToFactorMessageEvent;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductTableFactor;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.model.RandomGraphGenerator;
import com.analog.lyric.util.misc.IMapList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDiscreteLikelihood extends DimpleTestBase
{
	@Test
	public void testSingleFactor()
	{
		FactorGraph fg = new FactorGraph();
		try (CurrentModel cur = using(fg))
		{
			Bit a = bit("a"), b = bit("b");
		
			IFactorTable table = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
			table.setWeightForIndices(.2, 0, 0);
			table.setWeightForIndices(.8, 0, 1);
			table.setWeightForIndices(.7, 1, 0);
			table.setWeightForIndices(.3, 1, 1);
			
			Factor factor = name("F", fg.addFactor(table, a, b));
			factor.setDirectedTo(new int[] { 1 });
			
			testGraph(fg, false);
		}
	}
	
	@Test
	public void testTwoFactors()
	{
		FactorGraph fg = new FactorGraph();
		try (CurrentModel cur = using(fg))
		{
			Bit a = bit("a"), b = bit("b"), c = bit("c");
		
			IFactorTable table = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
			table.setWeightForIndices(.2, 0, 0);
			table.setWeightForIndices(.8, 0, 1);
			table.setWeightForIndices(.7, 1, 0);
			table.setWeightForIndices(.3, 1, 1);
			
			Factor fab = name("F(a,b)", fg.addFactor(table, a, b));
			fab.setDirectedTo(new int[] { 1 });
			
			Factor fbc = name("F(b,c)", fg.addFactor(table, b, c));
			fbc.setDirectedTo(new int[] { 1 });

			testGraph(fg, false);
		}
	}
	
	@Test
	public void testRandomTrees()
	{
		testRand.setSeed(0x8474da1a9a0f86ddL);
		System.out.format("%x\n", testRand.getSeed());
		FactorGraph fg = new RandomGraphGenerator(testRand).maxBranches(2).buildRandomTree(4);
		testGraph(fg, false);

		fg = new RandomGraphGenerator(testRand).maxBranches(5).buildRandomTree(20);
		testGraph(fg, false);
	}
	
	private void testGraph(FactorGraph fg, boolean debug)
	{
		SumProductSolverGraph sfg = requireNonNull(fg.setSolverFactory(new SumProductSolver()));
		if (debug)
		{
			@SuppressWarnings("resource")
			DimpleEventLogger logger = new DimpleEventLogger();
			logger.log(FactorToVariableMessageEvent.class, sfg);
			logger.log(VariableToFactorMessageEvent.class, sfg);
		}
		

		final double logZ = sfg.computeLogPartitionFunction();
		if (debug)
		{
			System.out.format("log Z=%g, Z=%g\n", logZ, energyToWeight(-logZ));
		}
		
		// Do solve again on a copy of the graph with all factors merged into single giant factor.
		final BiMap<Object,Object> old2new = HashBiMap.create();
		final BiMap<Object,Object> new2old = old2new.inverse();
		FactorGraph model2 = fg.copyRoot(old2new);
		IMapList<Factor> factors2 = model2.getFactors();
		Factor factor2 = model2.join(factors2.toArray(new Factor[factors2.size()]));
		SumProductSolverGraph sfg2 = requireNonNull(model2.setSolverFactory(new SumProductSolver()));
		SumProductTableFactor sfactor2 = (SumProductTableFactor)sfg2.getSolverFactor(factor2);
		model2.solve();

		IFactorTable beliefs = sfactor2.getBeliefTable();
		
		int N = 100;
		
		SampleDataLayer dataLayer = DataLayer.createSample(fg);
		
		for (int i = 0; i < N; ++i)
		{
			for (Variable var : FactorGraphIterables.variables(fg))
			{
				Discrete discrete = var.asDiscreteVariable();
				DiscreteValue value = Value.create(discrete.getDomain());
				final int index = testRand.nextInt(discrete.getDomain().size());
				value.setIndex(index);
				dataLayer.put(var, value);
				discrete.setGuessIndex(index);
				if (debug)
				{
					System.out.format("%s=%d\n", discrete, index);
				}
			}
			
			final double ll = sfg.getScore() - logZ;
			final double likelihood = energyToWeight(ll);
			
			IntArrayList indices = new IntArrayList(factor2.getSiblingCount());
			for (Variable var : factor2.getSiblings())
			{
				indices.add(requireNonNull(dataLayer.get(new2old.get(var))).getIndex());
			}
			indices.trimToSize();
			
			final double expectedll = beliefs.getEnergyForIndices(indices.elements());
			final double expectedLikelihood = energyToWeight(expectedll);
			
			assertEquals(expectedLikelihood, likelihood, 1e-10);
			assertEquals(expectedll, ll, 1e-10);
		}
	}
}
