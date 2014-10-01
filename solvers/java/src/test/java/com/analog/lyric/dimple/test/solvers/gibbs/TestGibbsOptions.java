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

package com.analog.lyric.dimple.test.solvers.gibbs;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.RealJoint;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsRealJoint;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Tests setting of {@link GibbsOptions}
 * @since 0.07
 * @author Christopher Barber
 */
@SuppressWarnings({"null", "deprecation"})
public class TestGibbsOptions extends DimpleTestBase
{
	@Test
	public void test()
	{
		// Test default values
		assertEquals(1, GibbsOptions.numSamples.defaultIntValue());
		assertEquals(0, GibbsOptions.numRandomRestarts.defaultIntValue());
		assertEquals(1, GibbsOptions.scansPerSample.defaultIntValue());
		assertEquals(0, GibbsOptions.burnInScans.defaultIntValue());
		assertFalse(GibbsOptions.saveAllSamples.defaultBooleanValue());
		assertFalse(GibbsOptions.saveAllScores.defaultBooleanValue());
		assertFalse(GibbsOptions.enableAnnealing.defaultValue());
		assertEquals(1.0, GibbsOptions.initialTemperature.defaultDoubleValue(), 1.0);
		assertEquals(1.0, GibbsOptions.annealingHalfLife.defaultDoubleValue(), 1.0);
		
		// Build test graph
		FactorGraph fg = new FactorGraph();
		Bit b1 = new Bit();
		Bit b2 = new Bit();
		fg.addVariables(b1, b2);
		Real r1 = new Real();
		Real r2 = new Real();
		fg.addVariables(r1, r2);
		RealJoint j1 = new RealJoint(2);
		RealJoint j2 = new RealJoint(2);
		fg.addVariables(j1, j2);
		int nVars = fg.getVariableCount();
		
		// Test default initialization
		GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		GibbsDiscrete sb1 = (GibbsDiscrete)sfg.getSolverVariable(b1);
		GibbsDiscrete sb2 = (GibbsDiscrete)sfg.getSolverVariable(b2);
		GibbsReal sr1 = (GibbsReal)sfg.getSolverVariable(r1);
		GibbsReal sr2 = (GibbsReal)sfg.getSolverVariable(r2);
		GibbsRealJoint sj1 = (GibbsRealJoint)sfg.getSolverVariable(j1);
		GibbsRealJoint sj2 = (GibbsRealJoint)sfg.getSolverVariable(j2);
		
		assertEquals(GibbsOptions.numSamples.defaultIntValue(), sfg.getNumSamples());
		assertEquals(GibbsOptions.numRandomRestarts.defaultIntValue(), sfg.getNumRestarts());
		assertEquals(nVars * GibbsOptions.burnInScans.defaultIntValue(), sfg.getBurnInUpdates());
		
		sfg.initialize();
		assertEquals(GibbsOptions.numSamples.defaultIntValue(), sfg.getNumSamples());
		assertEquals(GibbsOptions.numRandomRestarts.defaultIntValue(), sfg.getNumRestarts());
		assertEquals(nVars * GibbsOptions.scansPerSample.defaultIntValue(), sfg.getUpdatesPerSample());
		assertEquals(nVars * GibbsOptions.burnInScans.defaultIntValue(), sfg.getBurnInUpdates());
		assertFalse(sfg.isTemperingEnabled());
		assertEquals(GibbsOptions.initialTemperature.defaultDoubleValue(), sfg.getInitialTemperature(), 0.0);
		assertEquals(GibbsOptions.annealingHalfLife.defaultDoubleValue(), sfg.getTemperingHalfLifeInSamples(), 1e-9);
		
		// Test initialization from options
		fg.setSolverFactory(null);
		sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		sb1 = requireNonNull((GibbsDiscrete)sfg.getSolverVariable(b1));
		sb2 = requireNonNull((GibbsDiscrete)sfg.getSolverVariable(b2));
		sr1 = requireNonNull((GibbsReal)sfg.getSolverVariable(r1));
		sr2 = requireNonNull((GibbsReal)sfg.getSolverVariable(r2));
		sj1 = requireNonNull((GibbsRealJoint)sfg.getSolverVariable(j1));
		sj2 = requireNonNull((GibbsRealJoint)sfg.getSolverVariable(j2));
		fg.setOption(GibbsOptions.numSamples, 3);
		fg.setOption(GibbsOptions.numRandomRestarts, 2);
		fg.setOption(GibbsOptions.scansPerSample, 2);
		fg.setOption(GibbsOptions.burnInScans, 4);
		fg.setOption(GibbsOptions.saveAllSamples, true);
		b2.setOption(GibbsOptions.saveAllSamples, false);
		r2.setOption(GibbsOptions.saveAllSamples, false);
		j2.setOption(GibbsOptions.saveAllSamples, false);
		fg.setOption(GibbsOptions.enableAnnealing, true);
		fg.setOption(GibbsOptions.initialTemperature, Math.PI);
		fg.setOption(GibbsOptions.annealingHalfLife, 3.1);
		
		// These do not take effect until after initialization
		assertEquals(GibbsOptions.numSamples.defaultIntValue(), sfg.getNumSamples());
		assertEquals(GibbsOptions.numRandomRestarts.defaultIntValue(), sfg.getNumRestarts());
		assertFalse(sfg.isTemperingEnabled());
		assertEquals(0.0, sfg.getInitialTemperature(), 0.0);
		assertEquals(Math.log(2), sfg.getTemperingHalfLifeInSamples(), 1e-9);
		
		sfg.initialize();
		assertEquals(3, sfg.getNumSamples());
		assertEquals(2, sfg.getNumRestarts());
		assertEquals(nVars * 2 /*scansPerSample */, sfg.getUpdatesPerSample());
		assertEquals(nVars * 4 /* burnInScans */, sfg.getBurnInUpdates());
		assertEquals(true, sb1.getOptionOrDefault(GibbsOptions.saveAllSamples));
		assertEquals(false, sb2.getOptionOrDefault(GibbsOptions.saveAllSamples));
		assertEquals(true, sr1.getOptionOrDefault(GibbsOptions.saveAllSamples));
		assertEquals(false, sr2.getOptionOrDefault(GibbsOptions.saveAllSamples));
		assertEquals(true, sj1.getOptionOrDefault(GibbsOptions.saveAllSamples));
		assertEquals(false, sj2.getOptionOrDefault(GibbsOptions.saveAllSamples));
		assertTrue(sfg.isTemperingEnabled());
		assertEquals(Math.PI, sfg.getInitialTemperature(), 0.0);
		assertEquals(3.1, sfg.getTemperingHalfLifeInSamples(), 1e-9);
	
		// Test set methods
		sfg.setNumSamples(4);
		assertEquals(4, sfg.getNumSamples());
		assertEquals((Integer)4, sfg.getLocalOption(GibbsOptions.numSamples));
		sfg.setNumRestarts(5);
		assertEquals(5, sfg.getNumRestarts());
		assertEquals((Integer)5, sfg.getLocalOption(GibbsOptions.numRandomRestarts));
		sfg.setUpdatesPerSample(6);
		assertEquals(new Integer(-1), sfg.getLocalOption(GibbsOptions.scansPerSample));
		sfg.setScansPerSample(3);
		assertEquals(3 * nVars, sfg.getUpdatesPerSample());
		assertEquals((Integer)3, sfg.getLocalOption(GibbsOptions.scansPerSample));
		sfg.setBurnInScans(5);
		assertEquals((Integer)5, sfg.getLocalOption(GibbsOptions.burnInScans));
		
		sfg.initialize();
		assertEquals(5 * nVars, sfg.getBurnInUpdates());

		sfg.setUpdatesPerSample(23);
		assertEquals(23, sfg.getUpdatesPerSample());
		assertEquals(new Integer(-1), sfg.getLocalOption(GibbsOptions.scansPerSample));
		sfg.setBurnInUpdates(12);
		assertEquals(12, sfg.getBurnInUpdates());
		assertEquals(new Integer(-1), sfg.getLocalOption(GibbsOptions.burnInScans));
		
		sfg.unsetOption(GibbsOptions.saveAllSamples);
		b2.unsetOption(GibbsOptions.saveAllSamples);
		sfg.saveAllSamples();
		assertEquals(true, sfg.getLocalOption(GibbsOptions.saveAllSamples));
		sfg.disableSavingAllSamples();
		assertEquals(false, sfg.getLocalOption(GibbsOptions.saveAllSamples));
		sb1.saveAllSamples();
		assertEquals(true, sb1.getLocalOption(GibbsOptions.saveAllSamples));
		sb1.disableSavingAllSamples();
		assertEquals(false, sb1.getLocalOption(GibbsOptions.saveAllSamples));
		sr1.saveAllSamples();
		assertEquals(true, sr1.getLocalOption(GibbsOptions.saveAllSamples));
		sr1.disableSavingAllSamples();
		assertEquals(false, sr1.getLocalOption(GibbsOptions.saveAllSamples));
		sj1.saveAllSamples();
		assertEquals(true, sj1.getLocalOption(GibbsOptions.saveAllSamples));
		sj1.disableSavingAllSamples();
		assertEquals(false, sj1.getLocalOption(GibbsOptions.saveAllSamples));
		
		sfg.unsetOption(GibbsOptions.saveAllScores);
		sfg.saveAllScores();
		assertEquals(true, sfg.getLocalOption(GibbsOptions.saveAllScores));
		sfg.disableSavingAllScores();
		assertEquals(false, sfg.getLocalOption(GibbsOptions.saveAllScores));
		
		sfg.disableTempering();
		assertFalse(sfg.isTemperingEnabled());
		assertEquals(false, sfg.getLocalOption(GibbsOptions.enableAnnealing));
		sfg.enableTempering();
		assertTrue(sfg.isTemperingEnabled());
		assertEquals(true, sfg.getLocalOption(GibbsOptions.enableAnnealing));
		
		sfg.disableTempering();
		sfg.unsetOption(GibbsOptions.enableAnnealing);
		sfg.setInitialTemperature(2.345);
		assertEquals((Double)2.345, sfg.getLocalOption(GibbsOptions.initialTemperature));
		assertTrue(sfg.isTemperingEnabled()); // tempering implicitly enabled when setting initial temperature
		
		sfg.disableTempering();
		sfg.unsetOption(GibbsOptions.enableAnnealing);
		sfg.setTemperingHalfLifeInSamples(4);
		assertEquals(4, sfg.getTemperingHalfLifeInSamples(), 1e-9);
		assertEquals(4.0, sfg.getLocalOption(GibbsOptions.annealingHalfLife), 0.0);
		assertTrue(sfg.isTemperingEnabled()); // tempering implicitly enabled when setting tempering half life

	}
}
