/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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


package com.analog.lyric.dimple.test;


import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.test.Helpers;

public class SolverMiscSmallTest extends DimpleTestBase
{
	@SuppressWarnings("null")
	@Test
	public void test_getParentAndGetRoot()
	{
		FactorGraph fgRoot = Helpers.MakeSimpleThreeLevelGraph();
		FactorGraph fgMid  = fgRoot.getGraphByName("Mid");
		FactorGraph fgLeaf = fgRoot.getGraphByName("Mid.Leaf");
				
		Variable vRootO1 = fgRoot.getVariableByName("vRootO1");
		Variable vMidO1 = fgRoot.getVariableByName("Mid.vMidO1");
		Variable vLeafO1 = fgRoot.getVariableByName("Mid.Leaf.vLeafO1");
		Factor fRoot = fgRoot.getFactorByName("fRoot");
		Factor fMid = fgRoot.getFactorByName("Mid.fMid");
		Factor fLeaf = fgRoot.getFactorByName("Mid.Leaf.fLeaf");

		ISolverFactorGraph SfgRoot 	= fgRoot.getSolver();
		ISolverFactorGraph SfgMid  	= fgMid.getSolver();
		ISolverFactorGraph SfgLeaf 	= fgLeaf.getSolver();
		ISolverVariable SvRootO1 	= vRootO1.getSolver();
		ISolverVariable SvMidO1 	= vMidO1.getSolver();
		ISolverVariable SvLeafO1 	= vLeafO1.getSolver();
		ISolverFactor SfRoot 		= fRoot.getSolver();
		ISolverFactor SfMid 		= fMid.getSolver();
		ISolverFactor SfLeaf 		= fLeaf.getSolver();
		
		assertNull(SfgRoot.getParentGraph());
		assertEquals(SfgRoot, SfgMid.getParentGraph());
		assertEquals(SfgMid, SfgLeaf.getParentGraph());
		assertEquals(SfgRoot, SvRootO1.getParentGraph());
		assertEquals(SfgMid, SvMidO1.getParentGraph());
		assertEquals(SfgLeaf, SvLeafO1.getParentGraph());
		assertEquals(SfgRoot, SfRoot.getParentGraph());
		assertEquals(SfgMid, SfMid.getParentGraph());
		assertEquals(SfgLeaf, SfLeaf.getParentGraph());
		
		assertEquals(SfgRoot, SfgRoot.getRootSolverGraph());
		assertEquals(SfgRoot, SfgMid.getRootSolverGraph());
		assertEquals(SfgRoot, SfgLeaf.getRootSolverGraph());
		assertEquals(SfgRoot, SvRootO1.getRootSolverGraph());
		assertEquals(SfgRoot, SvMidO1.getRootSolverGraph());
		assertEquals(SfgRoot, SvLeafO1.getRootSolverGraph());
		assertEquals(SfgRoot, SfRoot.getRootSolverGraph());
		assertEquals(SfgRoot, SfMid.getRootSolverGraph());
		assertEquals(SfgRoot, SfLeaf.getRootSolverGraph());
	}
}
