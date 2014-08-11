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

package com.analog.lyric.dimple.test.solvers.particleBP;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPOptions;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPSolver;
import com.analog.lyric.dimple.solvers.particleBP.SFactorGraph;
import com.analog.lyric.dimple.solvers.particleBP.SRealVariable;

/**
 * Tests for {@link ParticleBPOptions}
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestParticleBPOptions
{
	@SuppressWarnings("null")
	@Test
	public void test()
	{
		// Test default values
		assertFalse(ParticleBPOptions.enableTempering.defaultValue());
		assertEquals(1.0, ParticleBPOptions.initialTemperature.defaultValue(), 0.0);
		assertEquals((Integer)1, ParticleBPOptions.iterationsBetweenResampling.defaultValue());
		assertEquals((Integer)1, ParticleBPOptions.numParticles.defaultValue());
		assertEquals((Integer)1, ParticleBPOptions.resamplingUpdatesPerParticle.defaultValue());
		assertEquals(1.0, ParticleBPOptions.temperingHalfLife.defaultValue(), 0.0);
		
		// Set up test graph
		FactorGraph fg = new FactorGraph();
		Real r1 = new Real();
		Real r2 = new Real();
		fg.addVariables(r1, r2);
		
		// Test default initialization on graph
		SFactorGraph sfg = requireNonNull(fg.setSolverFactory(new ParticleBPSolver()));
		SRealVariable sr1 = (SRealVariable)sfg.getSolverVariable(r1);
		SRealVariable sr2 = (SRealVariable)sfg.getSolverVariable(r2);
		sfg.initialize();
		assertFalse(sfg.isTemperingEnabled());
		assertEquals(1.0, sfg.getInitialTemperature(), 0.0);
		assertEquals(1.0, sfg.getTemperingHalfLifeInIterations(), 1e-9);
		assertEquals(1, sfg.getNumIterationsBetweenResampling());
		assertEquals(1, sr1.getResamplingUpdatesPerParticle());
		assertEquals(1, sr2.getResamplingUpdatesPerParticle());
		assertEquals(1, sr1.getNumParticles());
		assertEquals(1, sr2.getNumParticles());
		
		// Test initialization from option on factor graph
		fg.setSolverFactory(null);
		fg.setOption(ParticleBPOptions.enableTempering, true);
		fg.setOption(ParticleBPOptions.initialTemperature, Math.PI);
		fg.setOption(ParticleBPOptions.temperingHalfLife, 3.1);
		fg.setOption(ParticleBPOptions.iterationsBetweenResampling, 2);
		fg.setOption(ParticleBPOptions.resamplingUpdatesPerParticle, 2);
		r2.setOption(ParticleBPOptions.resamplingUpdatesPerParticle, 3);
		fg.setOption(ParticleBPOptions.numParticles, 2);
		r2.setOption(ParticleBPOptions.numParticles, 3);
		sfg = requireNonNull(fg.setSolverFactory(new ParticleBPSolver()));
		sr1 = (SRealVariable)sfg.getSolverVariable(r1);
		sr2 = (SRealVariable)sfg.getSolverVariable(r2);
		
		// These take effect on construction
		assertEquals(2, sr1.getNumParticles());
		assertEquals(3, sr2.getNumParticles());

		// These do not take effect until initialize
		assertFalse(sfg.isTemperingEnabled());
		assertEquals(0.0, sfg.getInitialTemperature(), 0.0);
		assertEquals(Math.log(2), sfg.getTemperingHalfLifeInIterations(), 1e-9);
		assertEquals(1, sfg.getNumIterationsBetweenResampling());
		
		sfg.initialize();
		assertTrue(sfg.isTemperingEnabled());
		assertEquals(Math.PI, sfg.getInitialTemperature(), 0.0);
		assertEquals(3.1, sfg.getTemperingHalfLifeInIterations(), 1e-9);
		assertEquals(2, sfg.getNumIterationsBetweenResampling());
		assertEquals(2, sr1.getResamplingUpdatesPerParticle());
		assertEquals(3, sr2.getResamplingUpdatesPerParticle());
		
		// Test set methods
		sfg.disableTempering();
		assertFalse(sfg.isTemperingEnabled());
		assertEquals(false, sfg.getLocalOption(ParticleBPOptions.enableTempering));
		sfg.enableTempering();
		assertTrue(sfg.isTemperingEnabled());
		assertEquals(true, sfg.getLocalOption(ParticleBPOptions.enableTempering));
		
		sfg.disableTempering();
		sfg.unsetOption(ParticleBPOptions.enableTempering);
		sfg.setInitialTemperature(2.345);
		assertEquals((Double)2.345, sfg.getLocalOption(ParticleBPOptions.initialTemperature));
		assertTrue(sfg.isTemperingEnabled()); // tempering implicitly enabled when setting initial temperature
		
		sfg.disableTempering();
		sfg.unsetOption(ParticleBPOptions.enableTempering);
		sfg.setTemperingHalfLifeInIterations(4);
		assertEquals(4, sfg.getTemperingHalfLifeInIterations(), 1e-9);
		assertEquals(4, sfg.getLocalOption(ParticleBPOptions.temperingHalfLife), 0.0);
		assertTrue(sfg.isTemperingEnabled()); // tempering implicitly enabled when setting tempering half life
		
		sfg.setNumIterationsBetweenResampling(5);
		assertEquals(5, sfg.getNumIterationsBetweenResampling());
		assertEquals((Integer)5, sfg.getOption(ParticleBPOptions.iterationsBetweenResampling));
		
		sfg.setResamplingUpdatesPerParticle(4);
		assertEquals((Integer)4, sfg.getLocalOption(ParticleBPOptions.resamplingUpdatesPerParticle));
		assertEquals(2, sr1.getResamplingUpdatesPerParticle());
		assertEquals(3, sr2.getResamplingUpdatesPerParticle());
		sfg.initialize(); // does not take effect until initialize
		assertEquals(4, sr1.getResamplingUpdatesPerParticle());
		assertEquals(3, sr2.getResamplingUpdatesPerParticle()); // does not override more specific option setting
		
		sfg.setNumParticles(5);
		sfg.initialize(); // does not take effect until initialize
		assertEquals(5, sr1.getNumParticles());
		assertEquals(3, sr2.getNumParticles()); // does not override more specific option setting
		sr2.setNumParticles(6);
		assertEquals(6, sr2.getNumParticles());
		assertEquals((Integer)6, sr2.getLocalOption(ParticleBPOptions.numParticles));
		
	}
}
