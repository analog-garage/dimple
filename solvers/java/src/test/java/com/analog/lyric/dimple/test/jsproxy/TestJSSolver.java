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

package com.analog.lyric.dimple.test.jsproxy;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.jsproxy.JSFactorGraph;
import com.analog.lyric.dimple.jsproxy.JSSolver;
import com.analog.lyric.dimple.jsproxy.JSSolverFactory;
import com.analog.lyric.dimple.solvers.core.DimpleSolverRegistry;

/**
 * Tests for {@link JSSolver} and {@link JSSolverFactory}
 * @since 0.07
 * @author Christopher Barber
 */
public class TestJSSolver extends JSTestBase
{
	
	@Test
	public void test()
	{
		JSSolverFactory solvers = state.solvers;
		DimpleSolverRegistry solverRegistry = state.getEnvironment().getDelegate().solvers();
	
		JSFactorGraph fg = state.createGraph();
		
		for (String solverName : solverRegistry.keySet())
		{
			JSSolver solver = solvers.get(solverName);
			assertNotNull(solver);
			
			fg.setSolver(solver);
			
			assertEquals(solver, fg.getSolver());
			assertEquals(fg.getSolver(), fg.getSolver());
			assertEquals(solver.getDelegate(), fg.getDelegate().getFactorGraphFactory());
		}
	}
	
}
