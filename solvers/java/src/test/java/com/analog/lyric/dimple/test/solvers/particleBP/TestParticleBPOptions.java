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

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.solvers.particleBP.ParticleBPOptions;

/**
 * Tests for {@link ParticleBPOptions}
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestParticleBPOptions
{
	@Test
	public void test()
	{
		// Test default values
		assertEquals(1.0, ParticleBPOptions.beta.defaultValue(), 0.0);
		assertFalse(ParticleBPOptions.enableTempering.defaultValue());
		assertEquals(1.0, ParticleBPOptions.initialTemperature.defaultValue(), 0.0);
		assertEquals((Integer)1, ParticleBPOptions.iterationsBetweenResamping.defaultValue());
		assertEquals((Integer)1, ParticleBPOptions.numParticles.defaultValue());
		assertEquals((Integer)1, ParticleBPOptions.resamplingUpdatesPerParticle.defaultValue());
		assertEquals(1.0, ParticleBPOptions.temperingHalfLife.defaultValue(), 0.0);
	}
}
