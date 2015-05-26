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

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterables;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductTableFactor;
import com.analog.lyric.dimple.test.DimpleTestBase;
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
			
			SumProductSolverGraph sfg = requireNonNull(fg.setSolverFactory(new SumProductSolver()));
//			sfg.setOption(BPOptions.normalizeMessages, false);
			sfg.solve();
			
			final double Z = sfg.computeUnnormalizedLogLikelihood();
			assertEquals(2.0, energyToWeight(Z), 1e-10);
			System.out.format("Z=%g (-e^Z=%g)\n", Z, energyToWeight(Z));
			System.out.flush();
			System.err.flush();
			
			a.setFixedValue(0);
			b.setFixedValue(1);
			sfg.solve();
			double ull= sfg.computeUnnormalizedLogLikelihood();
			
			double ll = ull - Z;
			assertEquals(.4, energyToWeight(ll), 1e-10);
			
			a.setFixedValue(1);
			b.setFixedValue(0);
			sfg.solve();
			ll = sfg.computeUnnormalizedLogLikelihood() - Z;
			assertEquals(.7, energyToWeight(ll), 13-10);
			
			testGraph(fg);
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

			testGraph(fg);
		}
	}
	
	private void testGraph(FactorGraph fg)
	{
		for (Variable var : FactorGraphIterables.variables(fg))
		{
			var.setFixedValueObject(null);
		}

		SumProductSolverGraph sfg = requireNonNull(fg.setSolverFactory(new SumProductSolver()));
//		sfg.setOption(BPOptions.normalizeMessages, false);
		sfg.solve();
		
		final double Z = sfg.computeUnnormalizedLogLikelihood();
		
		// Do solve again on a copy of the graph with all factors merged into single giant factor.
		final BiMap<Object,Object> old2new = HashBiMap.create();
		final BiMap<Object,Object> new2old = old2new.inverse();
		FactorGraph model2 = fg.copyRoot(old2new);
		IMapList<Factor> factors2 = model2.getFactors();
		Factor factor2 = null;
		if (factors2.size() > 0)
		{
			factor2 = model2.join(factors2.toArray(new Factor[factors2.size()]));
		}
		SumProductSolverGraph sfg2 = model2.setSolverFactory(new SumProductSolver());
		SumProductTableFactor sfactor2 = (SumProductTableFactor)sfg2.getSolverFactor(factor2);
		model2.solve();

		IFactorTable beliefs = sfactor2.getUnnormalizedBeliefTable();
		beliefs.normalize();
		
		int N = 100;
		
		for (int i = 0; i < N; ++i)
		{
			for (Variable var : FactorGraphIterables.variables(fg))
			{
				Discrete discrete = var.asDiscreteVariable();
				discrete.setFixedValueIndex(testRand.nextInt(discrete.getDomain().size()));
			}
			
			sfg.solve();
			final double ll = sfg.computeUnnormalizedLogLikelihood() - Z;
			final double likelihood = energyToWeight(ll);
			
			IntArrayList indices = new IntArrayList(factor2.getSiblingCount());
			for (Variable var : factor2.getSiblings())
			{
				indices.add(((Discrete)new2old.get(var)).getFixedValueIndex());
			}
			indices.trimToSize();
			
			final double expectedll = beliefs.getEnergyForIndices(indices.elements());
			final double expectedLikelihood = energyToWeight(expectedll);
			
			assertEquals(expectedLikelihood, likelihood, 1e-10);
			assertEquals(expectedll, ll, 1e-10);
		}
	}
}
