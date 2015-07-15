/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.data.FactorGraphData;
import com.analog.lyric.dimple.data.ValueDataLayer;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterables;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.data.TestDataLayer;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestGibbsSampleLayer extends DimpleTestBase
{
	@Test
	public void test()
	{
		try (CurrentModel root = using(new FactorGraph("root")))
		{
			GibbsSolverGraph sgraph = requireNonNull(root.graph.setSolverFactory(new GibbsSolver()));
			ValueDataLayer layer = sgraph.getSampleLayer();
			
			assertInvariants(layer);
			assertTrue(layer.isEmpty());
			
			FactorGraphData<Variable,Value> rootData = requireNonNull(layer.getDataForGraph(root.graph));
			assertTrue(rootData.isEmpty());
			
			FactorGraph fg2 = new FactorGraph("fg2");
			GibbsSolverGraph sgraph2 = requireNonNull(fg2.setSolverFactory(new GibbsSolver()));
			ValueDataLayer layer2 = sgraph2.getSampleLayer();
			assertNotEquals(layer, layer2);
			assertNotEquals(rootData, layer2.getDataForGraph(root.graph));

			Real a = real("a");
			@SuppressWarnings("unused")
			Bit b = bit("b");
			sgraph.initialize();
			assertInvariants(layer);
			assertEquals(2, layer.size());
			assertEquals(2, rootData.size());
			
			// Unsupported operations
			try	{
				layer.clear();
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException ex) {}
			try	{
				rootData.clear();
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException ex) {}
			try {
				rootData.remove(a);
				fail("expected UnsupportedOperationException");
			} catch (UnsupportedOperationException ex) {}
			try {
				rootData.setByLocalIndex(200, Value.create(42));
				fail("expected IllegalArgumentException");
			} catch (IllegalArgumentException ex) {}
		}
	}
	
	private void assertInvariants(ValueDataLayer layer)
	{
		TestDataLayer.assertInvariants(layer);
		assertTrue(layer.isView());
		
		for (FactorGraph graph : FactorGraphIterables.subgraphs(layer.rootGraph()))
		{
			FactorGraphData<Variable,Value> data = requireNonNull(layer.getDataForGraph(graph));
			for (Variable var : graph.getOwnedVariables())
			{
				ISolverVariableGibbs svar = (ISolverVariableGibbs)requireNonNull(var.getSolver());
				assertSame(svar.getCurrentSampleValue(), data.get(var));
				assertSame(svar.getCurrentSampleValue(), layer.get(var));
			}
		}
	}
}
