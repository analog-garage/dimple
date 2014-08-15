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

package com.analog.lyric.dimple.test.solvers.lp;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.solvers.lp.LPOptions;
import com.analog.lyric.dimple.solvers.lp.LPSolver;
import com.analog.lyric.dimple.solvers.lp.SFactorGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Test initalization of {@link LPOptions}.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class TestLPOptions extends DimpleTestBase
{
	@Test
	public void test()
	{
		// Test default values
		assertEquals("", LPOptions.LPSolver.defaultValue());
		assertEquals("", LPOptions.MatlabLPSolver.defaultValue());
		
		FactorGraph fg = new FactorGraph();
		LPOptions.LPSolver.set(fg, "GLPK");
		LPOptions.MatlabLPSolver.set(fg, "glpkIP");
		
		SFactorGraph sfg = requireNonNull(fg.setSolverFactory(new LPSolver()));
		
		assertEquals("GLPK", LPOptions.LPSolver.get(sfg));
		assertEquals("glpkIP", LPOptions.MatlabLPSolver.get(sfg));
	
		// Test pre-initialization values. Most if not all options don't take effect until initialize().
		assertEquals("", sfg.getLPSolverName());
		assertEquals("", sfg.getMatlabLPSolver());
		
		// Test post-initialization values.
		sfg.initialize();
		assertEquals("GLPK", sfg.getLPSolverName());
		assertEquals("glpkIP", sfg.getMatlabLPSolver());
		
		// Make sure that set* methods update options locally.
		assertNull(sfg.getLocalOption(LPOptions.LPSolver));
		assertNull(sfg.getLocalOption(LPOptions.MatlabLPSolver));
		
		sfg.setLPSolverName("LpSolve");
		assertEquals("LpSolve", sfg.getLPSolverName());
		assertEquals("LpSolve", sfg.getLocalOption(LPOptions.LPSolver));
		
		sfg.setMatlabLPSolver("gurobi");
		assertEquals("gurobi", sfg.getMatlabLPSolver());
		assertEquals("gurobi", sfg.getLocalOption(LPOptions.MatlabLPSolver));
	}
}
