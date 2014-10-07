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
	
	package com.analog.lyric.dimple.jsproxy;

import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontreemap.JunctionTreeMAPSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
	
	
	/**
	 * This provides the solvers that are available for use with {@link JSFactorGraph#setSolver(JSSolver)}.
	 * <p>
	 * @since 0.07
	 * @author Christopher Barber
	 */
	public class JSSolverFactory
	{
		private final DimpleApplet _applet;
		
		JSSolverFactory(DimpleApplet applet)
		{
			_applet = applet;
		}
		
		public JSSolver get(String name)
		{
			return new JSSolver(_applet, name);
		}
		
		private JSSolver get(Class<?> solverClass)
		{
			return get(solverClass.getSimpleName());
		}
		
		public JSSolver gibbs()
		{
			return get(GibbsSolver.class);
		}
		
		public JSSolver junctionTree()
		{
			return get(JunctionTreeSolver.class);
		}
		
		public JSSolver junctionTreeMAP()
		{
			return get(JunctionTreeMAPSolver.class);
		}
		
		public JSSolver minSum()
		{
			return get(MinSumSolver.class);
		}
		
		public JSSolver sumProduct()
		{
			return get(SumProductSolver.class);
		}
	}
