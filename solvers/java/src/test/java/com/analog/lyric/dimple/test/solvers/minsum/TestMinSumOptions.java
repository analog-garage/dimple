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

import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.And;
import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.core.SolverBase;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph;
import com.analog.lyric.dimple.solvers.minsum.MinSumTableFactor;
import com.analog.lyric.dimple.solvers.minsum.Solver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test setting of {@link BPOptions} values.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestMinSumOptions extends DimpleTestBase
{
	@SuppressWarnings({ "deprecation", "null" })
	@Test
	public void test()
	{
		// Test default values
		assertEquals(0.0, BPOptions.damping.defaultValue(), 0.0);
		assertEquals(Integer.MAX_VALUE, (int)BPOptions.maxMessageSize.defaultValue());
		assertTrue(BPOptions.nodeSpecificDamping.defaultValue().isEmpty());

		final int nVars = 4;
		FactorGraph fg = new FactorGraph();
		Discrete[] vars = new Discrete[nVars];
		for (int i = 0; i < nVars; ++i)
		{
			vars[i] = new Bit();
		}
		Factor f1 = fg.addFactor(new Xor(), vars); // has custom factor
		Factor f2 = fg.addFactor(new And(), vars);
		
		// Check initial defaults
		MinSumSolverGraph sfg = requireNonNull(fg.setSolverFactory(new MinSumSolver()));
		assertEquals(0.0, sfg.getDamping(), 0.0);
		MinSumTableFactor sf1 = (MinSumTableFactor)requireNonNull(f1.getSolver());
		assertEquals(0, sf1.getK());
		assertEquals(0.0, sf1.getDamping(0), 0.0);
		MinSumTableFactor sf2 = (MinSumTableFactor)requireNonNull(f2.getSolver());
		assertEquals(0.0, sf2.getDamping(0), 0.0);
		assertEquals(0, sf2.getK());
		
		assertNull(fg.setSolverFactory(null));
		
		// Set initial options on model
		fg.setOption(BPOptions.damping, .9);
		fg.setOption(BPOptions.maxMessageSize, 10);
		BPOptions.nodeSpecificDamping.set(f1, .4, .5, .6, .7);
		BPOptions.nodeSpecificDamping.set(f2, .3, .4, .5, .6);
		
		// Test options that are updated on initialize()
		sfg = requireNonNull(fg.setSolverFactory(new MinSumSolver()));
		assertEquals(0.0, sfg.getDamping(), 0.0);
		assertEquals(0.0, sf1.getDamping(0), 0.0);
		sf1 = (MinSumTableFactor)requireNonNull(f1.getSolver());
		assertEquals(0, sf1.getK());
		sf2 = (MinSumTableFactor)requireNonNull(f2.getSolver());
		assertEquals(0, sf2.getK());
		
		sfg.initialize();
		assertEquals(.9, sfg.getDamping(), 0.0);
		assertEquals(.4, sf1.getDamping(0), 0.0);
		assertEquals(.5, sf1.getDamping(1), 0.0);
		assertEquals(.6, sf1.getDamping(2), 0.0);
		assertEquals(.7, sf1.getDamping(3), 0.0);
		assertEquals(10, sf1.getK());

		assertEquals(.3, sf2.getDamping(0), 0.0);
		assertEquals(.4, sf2.getDamping(1), 0.0);
		assertEquals(.5, sf2.getDamping(2), 0.0);
		assertEquals(.6, sf2.getDamping(3), 0.0);
		assertEquals(10, sf2.getK());
		
		// Test using set methods
		sfg.setDamping(.5);
		assertEquals(.5, sfg.getDamping(), 0.0);
		assertEquals(.5, requireNonNull(sfg.getLocalOption(BPOptions.damping)), 0.0);
		
		sf1.setK(3);
		assertEquals(3, sf1.getK());
		assertEquals((Integer)3, sf1.getLocalOption(BPOptions.maxMessageSize));
		
		sf1.setDamping(1, .23);
		assertEquals(.4, sf1.getDamping(0), 0.0);
		assertEquals(.23, sf1.getDamping(1), 0.0);
		assertEquals(.6, sf1.getDamping(2), 0.0);
		assertEquals(.7, sf1.getDamping(3), 0.0);
		assertArrayEquals(new double[] { .4,.23,.6,.7},
			BPOptions.nodeSpecificDamping.get(sf1).toPrimitiveArray(), 0.0);
	}
	
	@Test
	public void testSolverEquality()
	{
		SolverBase<?> solver1 = new MinSumSolver();
		SolverBase<?> solver2 = new Solver();
		SolverBase<?> solver3 = new SumProductSolver();
		
		assertEquals(solver1, solver2);
		assertEquals(solver1.hashCode(), solver2.hashCode());
		assertNotEquals(solver1, solver3);
		assertNotEquals(solver1.hashCode(), solver3.hashCode());
	}
}
