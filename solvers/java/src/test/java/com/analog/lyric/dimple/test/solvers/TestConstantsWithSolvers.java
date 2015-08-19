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

package com.analog.lyric.dimple.test.solvers;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestConstantsWithSolvers extends DimpleTestBase
{
	@SuppressWarnings("unused")
	@Test
	public void trivialConstantTest()
	{
		try (CurrentModel model = using(new FactorGraph()))
		{
			DiscreteDomain d3 = DiscreteDomain.range(1, 3);
			Discrete a = discrete("a",d3);
			a.setPrior(Value.constant(3));
			Discrete b = discrete("b",d3);
			Discrete c = discrete("c",d3);
			FactorFunction func = new FactorFunction () {
				@Override
				public double evalEnergy(Value[] values)
				{
					double sum = 0.0;
					for (Value value : values)
					{
						sum += value.getDouble();
					}
					return sum;
				}
			};
			Factor fab = name("f(a,b)", addFactor(func, a, b));
			Factor fc = name("f(3,c)", addFactor(func, 3, c));
			
			requireNonNull(fc.getConstantValueByIndex(0)).valueEquals(requireNonNull(a.getPriorValue()));
			
			SumProductSolverGraph sumproduct = requireNonNull(model.graph.setSolverFactory(new SumProductSolver()));
			sumproduct.solve();
			assertArrayEquals(b.getBelief(), c.getBelief(), 1e-15);

			MinSumSolverGraph minsum = requireNonNull(model.graph.setSolverFactory(new MinSumSolver()));
			minsum.solve();
			assertArrayEquals(b.getBelief(), c.getBelief(), 1e-15);
			
			GibbsSolverGraph gibbs = requireNonNull(model.graph.setSolverFactory(new GibbsSolver()));
			gibbs.setOption(GibbsOptions.numSamples, 20000);
			gibbs.setOption(GibbsOptions.saveAllSamples, true);
			gibbs.solve();
			assertArrayEquals(b.getBelief(), c.getBelief(), 1e-2);
		}
	}
}
