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

package com.analog.lyric.dimple.test.model;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestVariableBlock extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph root = new FactorGraph("root");

		try (CurrentModel cur = using(root))
		{
			Variable v1 = real("v1");
			Variable v2 = real("v2");
			
			VariableBlock block1 = root.addVariableBlock(v1, v2);
			assertBlock(block1, v1, v2);
			assertEquals(block1, block1);
			assertNotEquals(block1, "foo");
			assertEquals(root, block1.getParentGraph());
			
			VariableBlock block1a = root.addVariableBlock(v1, v2);
			assertSame(block1, block1a);
			
			// Blocks are only the same if the variable order is the same
			VariableBlock block2 = root.addVariableBlock(v2, v1);
			assertNotEquals(block1, block2);
			assertBlock(block2, v2, v1);
			assertNotEquals(block1.hashCode(), block2.hashCode());
			
			assertArrayEquals(new Object[] { block1, block2 }, root.getOwnedVariableBlocks().toArray());
			
			FactorGraph subgraph = root.addGraph(new FactorGraph());
			subgraph.setName("subgraph");
			assertSame(block2, subgraph.getChildByGraphTreeId(block2.getGraphTreeId()));
			
			try (CurrentModel cur2 = using(subgraph))
			{
				Variable v3 = real("v3");
				
				VariableBlock block3 = subgraph.addVariableBlock(v1,v3);
				assertBlock(block3, v1, v3);
			}
		}
		
	}
	
	private void assertBlock(VariableBlock block, Variable ... expectedVariables)
	{
		assertInvariants(block);
		
		final int expectedLength = expectedVariables.length;
		assertEquals(expectedLength, block.size());
		for (int i = expectedLength; --i>=0;)
		{
			assertSame(expectedVariables[i], block.get(i));
		}
	}
	
	private void assertInvariants(VariableBlock block)
	{
		int id = block.getLocalId();
		assertEquals(Ids.VARIABLE_BLOCK_TYPE, Ids.typeIndexFromLocalId(id));
		
		FactorGraph parent = block.getParentGraph();
		assertSame(block, parent.getVariableBlockByLocalId(id));
		assertSame(block, parent.getChildByGraphTreeId(block.getGraphTreeId()));
		assertTrue(parent.ownsDirectly(block));
		
		FactorGraph grandparent = parent.getParentGraph();
		if (grandparent != null)
		{
			assertNotSame(block, grandparent.getVariableBlockByLocalId(id));
			assertSame(block, grandparent.getChildByGraphTreeId(block.getGraphTreeId()));
		}
		
		/*
		 * Test IDimpleEventSource methods
		 */
		
		assertEquals(block.toString(), block.getEventSourceName());
		assertEquals(block.getParentGraph(), block.getModelEventSource());
		block.notifyListenerChanged(); // does nothing
		
		/*
		 * Test List invariants
		 */
		
		assertFalse(block.isEmpty());
		assertTrue(block.size() > 0);
		
		Object[] objs = block.toArray();
		Variable[] vars = block.toArray(new Variable[0]);
		assertEquals(block.size(), objs.length);
		assertArrayEquals(objs, vars);
		
		int i = -1;
		for (Variable var : block)
		{
			++i;
			assertSame(var, block.get(i));
			assertSame(var, vars[i]);
			assertEquals(var.getGraphTreeId(), block.getVariableGraphTreeId(i));
			
			assertEquals(i, block.indexOf(var));
			assertEquals(i, block.lastIndexOf(var));
			assertTrue(block.contains(var));
		}
		
		assertEquals(i+1, block.size());
		
		assertFalse(block.contains("foo"));
		assertEquals(-1, block.indexOf("bogus"));
		assertEquals(-1, block.indexOf(new Real()));
		assertEquals(-1, block.lastIndexOf("bogus"));
		assertEquals(-1, block.lastIndexOf(new Real()));
		
		assertTrue(block.containsAll(Collections.emptyList()));
		assertTrue(block.containsAll(Arrays.asList(vars)));
		assertFalse(block.containsAll(Arrays.asList("foo")));
		
		assertEquals(block.subList(0, block.size()), block);
		// VariableBlock.equals only true if other object is also a VariableBlock
		assertNotEquals(block, block.subList(0, block.size()));
	}
	
	@Test
	public void testErrors()
	{
		FactorGraph fg = new FactorGraph("fg");
		
		expectThrow(IllegalArgumentException.class, "Cannot create empty VariableBlock", fg, "addVariableBlock");
		
		Variable v1 = new Real();
		v1.setName("v1");
		expectThrow(IllegalArgumentException.class, "Variable 'v1' not in graph tree", fg, "addVariableBlock", v1);
		
		fg.addVariables(v1);
		expectThrow(IllegalArgumentException.class, "Variable 'v1' was specified more than once",
			fg, "addVariableBlock", v1, v1);
		
		FactorGraph fg2 = new FactorGraph("fg2");
		expectThrow(IllegalArgumentException.class, "Variable 'v1' not in graph tree", fg2, "addVariableBlock", v1);
		
		VariableBlock block = fg.addVariableBlock(v1);
		assertUnsupported(block, "add", v1);
		assertUnsupported(block, "add", 1, v1);
		assertUnsupported(block, "addAll", new ArrayList<Variable>());
		assertUnsupported(block, "addAll", 42, new ArrayList<Variable>());
		assertUnsupported(block, "clear");
		assertUnsupported(block, "remove", "foo");
		assertUnsupported(block, "remove", 42);
		assertUnsupported(block, "removeAll", Collections.emptyList());
		assertUnsupported(block, "retainAll", Collections.emptyList());
		assertUnsupported(block, "set", 42, v1);

		assertEquals(v1, block.get(0));
		fg.remove(v1);
		expectThrow(IllegalStateException.class, ".*no longer in graph", block, "get", 0);
	}
	
	private void assertUnsupported(VariableBlock block, String methodName, Object ... args)
	{
		expectThrow(UnsupportedOperationException.class, block, methodName, args);
	}
}
