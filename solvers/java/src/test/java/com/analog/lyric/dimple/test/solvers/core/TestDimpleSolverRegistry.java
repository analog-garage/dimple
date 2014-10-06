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

package com.analog.lyric.dimple.test.solvers.core;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.solvers.core.DimpleSolverRegistry;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontreemap.JunctionTreeMAPSolver;
import com.analog.lyric.dimple.solvers.lp.LPSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Tests for {@link DimpleSolverRegistry}
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestDimpleSolverRegistry extends DimpleTestBase
{
	private static final Class<?>[] expectedSolverClasses = {
		GibbsSolver.class,
		JunctionTreeSolver.class,
		JunctionTreeMAPSolver.class,
		LPSolver.class,
		MinSumSolver.class,
		ParticleBPSolver.class,
		SumProductSolver.class
	};
	
	@Test
	public void test()
	{
		DimpleSolverRegistry registry = new DimpleSolverRegistry();
		
		for (Class<?> expectedClass : expectedSolverClasses)
		{
			String name = expectedClass.getSimpleName();
			String shortName = name.substring(0, name.length() - "Solver".length());
			
			assertSame(expectedClass, registry.getClass(name));
			assertTrue(expectedClass.isInstance(registry.instantiate(name)));
			assertTrue(expectedClass.isInstance(registry.instantiate(shortName)));
		}
		
		assertNull(registry.get("FooSolver"));
	}
}
