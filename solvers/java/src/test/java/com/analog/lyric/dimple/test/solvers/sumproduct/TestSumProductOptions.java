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

import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.And;
import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.sumproduct.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph;
import com.analog.lyric.dimple.solvers.sumproduct.STableFactor;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductOptions;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.sampledfactor.SampledFactor;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestSumProductOptions extends DimpleTestBase
{
	@SuppressWarnings({ "deprecation", "null" })
	@Test
	public void test()
	{
		// Test default values
		assertEquals(0.0, SumProductOptions.damping.defaultValue(), 0.0);
		assertTrue(SumProductOptions.nodeSpecificDamping.defaultValue().isEmpty());
		
		assertEquals(Integer.MAX_VALUE, (int)SumProductOptions.maxMessageSize.defaultValue());

		assertFalse(SumProductOptions.enableOptimizedUpdate.defaultValue());
		
		assertEquals((Integer)SampledFactor.DEFAULT_BURN_IN_SCANS_PER_UPDATE,
			SumProductOptions.sampledFactorBurnInScansPerUpdate.defaultValue());
		assertEquals((Integer)SampledFactor.DEFAULT_SAMPLES_PER_UPDATE,
			SumProductOptions.sampledFactorSamplesPerUpdate.defaultValue());
		assertEquals((Integer)SampledFactor.DEFAULT_SCANS_PER_SAMPLE,
			SumProductOptions.sampledFactorScansPerSample.defaultValue());

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
		SFactorGraph sfg = requireNonNull(fg.setSolverFactory(new SumProductSolver()));
		assertEquals(0.0, sfg.getDamping(), 0.0);
		STableFactor sf1 = (STableFactor)requireNonNull(f1.getSolver());
		assertEquals(0, sf1.getK());
		assertEquals(0.0, sf1.getDamping(0), 0.0);
		STableFactor sf2 = (STableFactor)requireNonNull(f2.getSolver());
		assertEquals(0.0, sf2.getDamping(0), 0.0);
		assertEquals(0, sf2.getK());
		assertFalse(sfg.getDefaultOptimizedUpdateEnabled());
		assertFalse(sf1.isOptimizedUpdateEnabled());
		assertEquals(SampledFactor.DEFAULT_BURN_IN_SCANS_PER_UPDATE, sfg.getSampledFactorBurnInScansPerUpdate());
		assertEquals(SampledFactor.DEFAULT_SAMPLES_PER_UPDATE, sfg.getSampledFactorSamplesPerUpdate());
		assertEquals(SampledFactor.DEFAULT_SCANS_PER_SAMPLE, sfg.getSampledFactorScansPerSample());
		
		assertNull(fg.setSolverFactory(null));
		
		// Set initial options on model
		fg.setOption(SumProductOptions.damping, .9);
		fg.setOption(SumProductOptions.maxMessageSize, 10);
		SumProductOptions.nodeSpecificDamping.set(f1, .4, .5, .6, .7);
		SumProductOptions.nodeSpecificDamping.set(f2, .3, .4, .5, .6);
		fg.setOption(SumProductOptions.enableOptimizedUpdate, true);
		f2.setOption(SumProductOptions.enableOptimizedUpdate, false);
		fg.setOption(SumProductOptions.sampledFactorBurnInScansPerUpdate, 3);
		fg.setOption(SumProductOptions.sampledFactorSamplesPerUpdate, 4);
		fg.setOption(SumProductOptions.sampledFactorScansPerSample, 5);
		
		// Test options that are updated on initialize()
		sfg = requireNonNull(fg.setSolverFactory(new SumProductSolver()));
		assertEquals(0.0, sfg.getDamping(), 0.0);
		assertEquals(0.0, sf1.getDamping(0), 0.0);
		sf1 = (STableFactor)requireNonNull(f1.getSolver());
		assertEquals(0, sf1.getK());
		sf2 = (STableFactor)requireNonNull(f2.getSolver());
		assertEquals(0, sf2.getK());
		assertFalse(sfg.getDefaultOptimizedUpdateEnabled());
		assertEquals(SampledFactor.DEFAULT_BURN_IN_SCANS_PER_UPDATE, sfg.getSampledFactorBurnInScansPerUpdate());
		assertEquals(SampledFactor.DEFAULT_SAMPLES_PER_UPDATE, sfg.getSampledFactorSamplesPerUpdate());
		assertEquals(SampledFactor.DEFAULT_SCANS_PER_SAMPLE, sfg.getSampledFactorScansPerSample());
		SDiscreteVariable sv1 = (SDiscreteVariable)vars[0].getSolver();
		assertEquals(0.0, sv1.getDamping(0), 0.0);
		
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
		
		assertEquals(.9, sv1.getDamping(0), 0.0);

		assertTrue(sfg.getDefaultOptimizedUpdateEnabled());
		assertTrue(sf1.isOptimizedUpdateEnabled());
		assertFalse(sf2.isOptimizedUpdateEnabled());
		
		assertEquals(3, sfg.getSampledFactorBurnInScansPerUpdate());
		assertEquals(4, sfg.getSampledFactorSamplesPerUpdate());
		assertEquals(5, sfg.getSampledFactorScansPerSample());

		// Test using set methods
		sfg.setDamping(.5);
		assertEquals(.5, sfg.getDamping(), 0.0);
		assertEquals(.5, requireNonNull(sfg.getLocalOption(SumProductOptions.damping)), 0.0);
		
		sf1.setK(3);
		assertEquals(3, sf1.getK());
		assertEquals((Integer)3, sf1.getLocalOption(SumProductOptions.maxMessageSize));
		
		sf1.setDamping(1, .23);
		assertEquals(.4, sf1.getDamping(0), 0.0);
		assertEquals(.23, sf1.getDamping(1), 0.0);
		assertEquals(.6, sf1.getDamping(2), 0.0);
		assertEquals(.7, sf1.getDamping(3), 0.0);
		assertArrayEquals(new double[] { .4,.23,.6,.7},
			SumProductOptions.nodeSpecificDamping.get(sf1).toPrimitiveArray(), 0.0);
		
		sfg.setDefaultOptimizedUpdateEnabled(false);
		assertFalse(sfg.getDefaultOptimizedUpdateEnabled());
		assertEquals(false, sfg.getOption(SumProductOptions.enableOptimizedUpdate));
		
		assertNull(sf1.getLocalOption(SumProductOptions.enableOptimizedUpdate));
		sf1.enableOptimizedUpdate();
		assertTrue(sf1.isOptimizedUpdateEnabled());
		assertEquals(true, sf1.getLocalOption(SumProductOptions.enableOptimizedUpdate));
		sf1.disableOptimizedUpdate();
		assertFalse(sf1.isOptimizedUpdateEnabled());
		assertEquals(false, sf1.getLocalOption(SumProductOptions.enableOptimizedUpdate));
	}
}
