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

package com.analog.lyric.dimple.test.data;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.collect.PrimitiveIterator;
import com.analog.lyric.dimple.data.DenseFactorGraphData;
import com.analog.lyric.dimple.data.IDatum;
import com.analog.lyric.dimple.data.PriorDataLayer;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteEnergyMessage;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DiscreteWeightMessage;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Tests of {@link PriorDataLayer}
 * @since 0.08
 * @author Christopher Barber
 */
public class TestPriorDataLayer extends DimpleTestBase
{
	@Test
	public void test()
	{
		try (CurrentModel root = using(new FactorGraph()))
		{
			PriorDataLayer priorLayer = new PriorDataLayer(root.graph);
			assertInvariants(priorLayer);
			assertFalse(priorLayer.objectEquals(new PriorDataLayer(new FactorGraph()))); // because roots are different
			
			Variable a = bit("a"), b = bit("b");

			// Create an extra subgraph that will be deleted to leave a hole in the graph tree indices
			FactorGraph tmpSubgraph = root.graph.addGraph(new FactorGraph());
			
			FactorGraph subgraph = root.graph.addGraph(new FactorGraph());
			Variable c;
			try (CurrentModel sub = using(subgraph))
			{
				c = bit("c");
			}
			
			root.graph.remove(tmpSubgraph);
			
			assertInvariants(priorLayer);
			
			a.setPrior(1);
			assertTrue(Value.create(DiscreteDomain.bit(), 1).valueEquals((Value)requireNonNull(priorLayer.get(a))));
			assertEquals(1, priorLayer.size());
			b.setPrior(0);
			assertTrue(Value.create(DiscreteDomain.bit(), 0).valueEquals((Value)requireNonNull(priorLayer.get(b))));
			assertEquals(2, priorLayer.size());
			assertInvariants(priorLayer);
			
			assertSame(a.getPrior(), priorLayer.remove(a)); // valid because Java guarantees execution order (unlike C)
			assertNull(a.getPrior());
			assertNull(priorLayer.remove(a));
			assertEquals(1, priorLayer.size());
			
			priorLayer.clear();
			assertNull(b.getPrior());
			assertEquals(0, priorLayer.size());
			
			priorLayer.set(a, 1);
			assertTrue(Value.create(DiscreteDomain.bit(), 1).valueEquals(requireNonNull(a.getPriorValue())));
			IDatum value = new DiscreteWeightMessage(2);
			assertNull(priorLayer.put(b, value));
			assertSame(value, priorLayer.get(b));
			
			value = new DiscreteEnergyMessage(2);
			priorLayer.put(c, value);
			assertSame(value, c.getPrior());
			assertInvariants(priorLayer);
			
			requireNonNull(priorLayer.getDataForGraph(root.graph)).clear();
			assertNull(a.getPrior());
			assertNull(b.getPrior());
			assertSame(value, c.getPrior());
			
			PrimitiveIterator.OfInt iter =
				requireNonNull(priorLayer.getDataForGraph(subgraph)).getLocalIndices().iterator();
			while (iter.hasNext())
			{
				iter.nextInt();
				try
				{
					iter.remove();
					fail("expected UnsupportedOperationException");
				}
				catch (UnsupportedOperationException ex)
				{
				}
			}
			
			// Don't allow setting graph data to anything other than PriorFactorGraphData
			expectThrow(IllegalArgumentException.class, priorLayer, "setDataForGraph",
				new DenseFactorGraphData<Variable,IDatum>(priorLayer, root.graph, Variable.class, IDatum.class));
		}
	}
	
	
	public static void assertInvariants(PriorDataLayer layer)
	{
		TestDataLayer.assertInvariants(layer);
		
		FactorGraph root = layer.rootGraph();
		
		int count = 0;
		for (Variable var : root.getVariables())
		{
			IDatum prior = var.getPrior();
			assertSame(prior, layer.get(var));
			assertEquals(prior != null, layer.containsKey(var));
			assertEquals(prior != null, layer.containsDataFor(var));
			if (prior != null)
			{
				++count;
			}
		}
		
		assertEquals(count, layer.size());
		
		PriorDataLayer clone = layer.clone();
		assertEquals(layer.getDataForGraph(root), clone.getDataForGraph(root));
		assertTrue(clone.objectEquals(layer));
		
		assertFalse(layer.equals(new PriorDataLayer(new FactorGraph())));
	}
}
