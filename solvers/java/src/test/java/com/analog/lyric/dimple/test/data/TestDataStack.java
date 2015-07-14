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
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.analog.lyric.dimple.data.DataLayer;
import com.analog.lyric.dimple.data.DataStack;
import com.analog.lyric.dimple.data.GenericDataLayer;
import com.analog.lyric.dimple.data.PriorDataLayer;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDataStack extends DimpleTestBase
{
	@Test
	public void test()
	{
		try (CurrentModel root = using(new FactorGraph("root")))
		{
			Real a = real("a");
			Real b = real("b");
			normal(0.0, 1.0, a, b);
			
			PriorDataLayer layer0 = new PriorDataLayer(root.graph);
			DataStack priorStack = new DataStack(layer0);
			assertInvariants(priorStack);
			assertSame(layer0, priorStack.get(0));
			
			GenericDataLayer layer1 = GenericDataLayer.dense(root.graph);
			DataStack stack = new DataStack(layer0, layer1);
			assertInvariants(stack);
			assertSame(layer0, stack.get(0));
			assertSame(layer1, stack.get(1));
			
			// Test computeTotalEnergy
			NormalParameters normal = new NormalParameters(0, 1);
			a.setPrior(0.0);
			expectThrow(IllegalStateException.class, "There is no value for.*", stack, "computeTotalEnergy");
			b.setPrior(1.0);
			layer1.set(b, 42); // superceded by prior
			assertEquals(normal.evalEnergy(0) + normal.evalEnergy(1) - 2 * normal.getNormalizationEnergy(),
				stack.computeTotalEnergy(), 1e-15);
			b.setPrior(null);
			assertEquals(normal.evalEnergy(0) + normal.evalEnergy(42) - 2 * normal.getNormalizationEnergy(),
				stack.computeTotalEnergy(), 1e-15);
			NormalParameters bPrior = new NormalParameters(1,2);
			b.setPrior(bPrior);
			assertEquals(normal.evalEnergy(0) + normal.evalEnergy(42) - 2 * normal.getNormalizationEnergy() +
				bPrior.evalEnergy(42),
				stack.computeTotalEnergy(), 1e-15);
		}

		try {
			@SuppressWarnings("unused")
			DataStack stack = new DataStack(new ArrayList<DataLayer<?>>());
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException ex) {}

		try {
			@SuppressWarnings("unused")
			DataStack stack =
				new DataStack(GenericDataLayer.dense(new FactorGraph()), GenericDataLayer.dense(new FactorGraph()));
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException ex) {}
	}
	
	private void assertInvariants(DataStack stack)
	{
		final FactorGraph root = stack.rootGraph();
		final int size = stack.size();
		
		assertTrue(size > 0);
		assertFalse(stack.isEmpty());
		
		for (int i = 0; i < size; ++i)
		{
			DataLayer<?> layer = stack.get(i);
			assertSame(root, layer.rootGraph());
		}
		expectThrow(IndexOutOfBoundsException.class, stack, "get", -1);
		expectThrow(IndexOutOfBoundsException.class, stack, "get", size);
		expectThrow(UnsupportedOperationException.class, stack, "add", GenericDataLayer.sparse(root));
		assertEquals(size, stack.size());
	}
}
