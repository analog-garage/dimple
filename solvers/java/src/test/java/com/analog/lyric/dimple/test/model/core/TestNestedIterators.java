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

package com.analog.lyric.dimple.test.model.core;

import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterators;
import com.analog.lyric.dimple.model.core.IFactorGraphChildIterator;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestNestedIterators extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
		Variable v0a = bit("v0a");
		Variable v0b = bit("v0b");
		Variable v0c = bit("v0c");
		fg.addVariables(v0a, v0b, v0c);
		Factor f0a = fg.addFactor(new Xor(), v0a, v0b);
		Factor f0b = fg.addFactor(new Xor(), v0b, v0c);
		
		assertOrder(FactorGraphIterators.subgraphs(fg), fg);
		assertOrder(FactorGraphIterators.variables(fg), v0a, v0b, v0c);
		assertOrder(FactorGraphIterators.factors(fg), f0a, f0b);
		
		Variable v0d = bit("v0d");
		FactorGraph fg1a = fg.addGraph(new FactorGraph(new Bit(), new Bit()), v0d, v0b);
		Variable v1a1 = bit("v1a1");
		Variable v1a2 = bit("v1a2");
		fg1a.addVariables(v1a1, v1a2);
		Factor f1a1 = fg1a.addFactor(new Xor(), v0b, v1a1);
		Factor f1a2 = fg1a.addFactor(new Xor(), v1a1, v1a2);
		
		FactorGraph fg2a = fg1a.addGraph(new FactorGraph());
		
		FactorGraph fg2b = fg1a.addGraph(new FactorGraph(new Bit()), v0d);
		Variable v2b1 = bit("v2b1");
		Variable v2b2 = bit("v2b2");
		Factor f2b1 = fg2b.addFactor(new Xor(), v0d, v2b1);
		Factor f2b2 = fg2b.addFactor(new Xor(), v2b1, v2b2);
		
		FactorGraph fg3b = fg1a.addGraph(new FactorGraph());
		FactorGraph fg1b = fg.addGraph(new FactorGraph());
		
		assertOrder(FactorGraphIterators.subgraphs(fg), fg, fg1a, fg2a, fg2b, fg3b, fg1b);
		assertOrder(FactorGraphIterators.subgraphsDownto(fg, 0), fg);
		assertOrder(FactorGraphIterators.subgraphsDownto(fg, 1), fg, fg1a, fg1b);
		assertOrder(FactorGraphIterators.subgraphs(fg1a), fg1a, fg2a, fg2b, fg3b);
		
		assertOrder(FactorGraphIterators.factors(fg), f0a, f0b, f1a1, f1a2, f2b1, f2b2);
		assertOrder(FactorGraphIterators.factorsDownto(fg, 0), f0a, f0b);
		assertOrder(FactorGraphIterators.factorsDownto(fg, 1), f0a, f0b, f1a1, f1a2);
		assertOrder(FactorGraphIterators.factors(fg1a), f1a1, f1a2, f2b1, f2b2);
		
		assertOrder(FactorGraphIterators.variables(fg), v0a, v0b, v0c, v0d, v1a1, v1a2, v2b1, v2b2);
		assertOrder(FactorGraphIterators.variablesDownto(fg, 0), v0a, v0b, v0c, v0d);
		assertOrder(FactorGraphIterators.variablesDownto(fg, 1), v0a, v0b, v0c, v0d, v1a1, v1a2);
		assertOrder(FactorGraphIterators.variables(fg1a), v1a1, v1a2, v2b1, v2b2);
		assertOrder(FactorGraphIterators.variablesAndBoundary(fg1a), v0d, v0b, v1a1, v1a2, v2b1, v2b2);
	}
	
	private Bit bit(String name)
	{
		Bit bit = new Bit();
		bit.setName(name);
		return bit;
	}
	
	private void assertOrder(IFactorGraphChildIterator<FactorGraph> iterator, FactorGraph ... graphs)
	{
		assertEquals(-1, iterator.lastDepth());
		assertSame(graphs[0], iterator.root());
		
		int i = 0;
		while (iterator.hasNext())
		{
			FactorGraph fg = iterator.next();
			assertNotNull(fg);
			assertSame(graphs[i++], fg);
			
			if (fg != iterator.root())
			{
				int depth = fg.getDepthBelowAncestor(iterator.root());
				assertEquals(depth + 1, iterator.lastDepth());
				assertTrue(depth >= 0);
				assertTrue(depth < iterator.maxNestingDepth());
			}
			else
			{
				assertEquals(0, iterator.lastDepth());
			}
		}
		
		assertNull(iterator.next());
		assertEquals(-1, iterator.lastDepth());
		assertEquals(graphs.length, i);
		
		iterator.reset();
		i = 0;
		while (true)
		{
			FactorGraph fg = iterator.next();
			if (fg != null)
			{
				assertSame(graphs[i++], fg);
			}
			else
			{
				assert !iterator.hasNext();
				break;
			}
		}
		assertEquals(-1, iterator.lastDepth());
	}
	
	private <T extends Node> void assertOrder(IFactorGraphChildIterator<T> iterator,
		@SuppressWarnings("unchecked") T ... nodes)
	{
		int i = 0;
		while (iterator.hasNext())
		{
			T node = iterator.next();
			assertNotNull(node);
			assertSame(nodes[i++], node);
			
			if (node != iterator.root())
			{
				int depth = node.getDepthBelowAncestor(iterator.root());
				// FIXME turn on this assertion if we know this is not a boundary variable...
//				assertTrue(depth >= 0);
				assertTrue(depth - 1 < iterator.maxNestingDepth());
			}
		}
		
		assertNull(iterator.next());
		assertEquals(nodes.length, i);
		
		iterator.reset();
		i = 0;
		while (true)
		{
			T node = iterator.next();
			if (node != null)
			{
				assertSame(nodes[i++], node);
			}
			else
			{
				assert !iterator.hasNext();
				break;
			}
		}
		
	}
}
