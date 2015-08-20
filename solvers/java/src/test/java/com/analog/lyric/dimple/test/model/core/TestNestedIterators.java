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

import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Xor;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorGraphIterables;
import com.analog.lyric.dimple.model.core.FactorGraphIterators;
import com.analog.lyric.dimple.model.core.IFactorGraphChild;
import com.analog.lyric.dimple.model.core.IFactorGraphChildIterator;
import com.analog.lyric.dimple.model.core.IFactorGraphChildren;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Constant;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
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
		FactorGraph fg = new FactorGraph("fg");
		Variable v0a = bit("v0a");
		Variable v0b = bit("v0b");
		Variable v0c = bit("v0c");
		fg.addVariables(v0a, v0b, v0c);
		Factor f0a = fg.addFactor(new Xor(), v0a, v0b);
		Factor f0b = fg.addFactor(new Xor(), v0b, v0c);
		Constant c0a = fg.addConstant(42);
		Constant c0b = fg.addConstant(Math.PI);
		assertSame(fg, c0a.getParentGraph());
		VariableBlock b0a = fg.addVariableBlock(v0a,v0b);
		
		assertOrder(FactorGraphIterators.subgraphs(fg), fg);
		assertOrder(FactorGraphIterables.subgraphs(fg), fg);
		assertOrder(FactorGraphIterators.variables(fg), v0a, v0b, v0c);
		assertOrder(FactorGraphIterables.variables(fg), v0a, v0b, v0c);
		assertOrder(FactorGraphIterators.factors(fg), f0a, f0b);
		assertOrder(FactorGraphIterables.factors(fg), f0a, f0b);
		assertOrder(FactorGraphIterators.constants(fg), c0a, c0b);
		assertOrder(FactorGraphIterables.constants(fg), c0a, c0b);
		assertOrder(FactorGraphIterators.variableBlocks(fg), b0a);
		assertOrder(FactorGraphIterables.variableBlocks(fg), b0a);
		
		Variable v0d = bit("v0d");
		FactorGraph fg1a = fg.addGraph(new FactorGraph(new Bit(), new Bit()), v0d, v0b);
		Variable v1a1 = bit("v1a1");
		Variable v1a2 = bit("v1a2");
		fg1a.addVariables(v1a1, v1a2);
		Factor f1a1 = fg1a.addFactor(new Xor(), v0b, v1a1);
		Factor f1a2 = fg1a.addFactor(new Xor(), v1a1, v1a2);
		Constant c1a1 = fg1a.addConstant(12);
		Constant c1a2 = fg1a.addConstant(0.0);
		VariableBlock b1a1 = fg1a.addVariableBlock(v1a1,v1a2);
		
		FactorGraph fg2a = fg1a.addGraph(new FactorGraph("fg2a"));
		
		FactorGraph fg2b = fg1a.addGraph(new FactorGraph(new Bit()), v0d);
		fg2b.setName("fg2b");
		Variable v2b1 = bit("v2b1");
		Variable v2b2 = bit("v2b2");
		Factor f2b1 = fg2b.addFactor(new Xor(), v0d, v2b1);
		Factor f2b2 = fg2b.addFactor(new Xor(), v2b1, v2b2);
		Constant c2b1 = fg2b.addConstant(1);
		Constant c2b2 = fg2b.addConstant(2);
		
		FactorGraph fg3b = fg1a.addGraph(new FactorGraph("fg3b"));
		FactorGraph fg1b = fg.addGraph(new FactorGraph("fb1b"));
		
		assertOrder(FactorGraphIterators.subgraphs(fg), fg, fg1a, fg2a, fg2b, fg3b, fg1b);
		assertOrder(FactorGraphIterables.subgraphs(fg), fg, fg1a, fg2a, fg2b, fg3b, fg1b);
		assertOrder(FactorGraphIterators.subgraphsDownto(fg, 0), fg);
		assertOrder(FactorGraphIterables.subgraphsDownto(fg, 0), fg);
		assertOrder(FactorGraphIterators.subgraphsDownto(fg, 1), fg, fg1a, fg1b);
		assertOrder(FactorGraphIterables.subgraphsDownto(fg, 1), fg, fg1a, fg1b);
		assertOrder(FactorGraphIterators.subgraphs(fg1a), fg1a, fg2a, fg2b, fg3b);
		assertOrder(FactorGraphIterables.subgraphs(fg1a), fg1a, fg2a, fg2b, fg3b);
		
		assertOrder(FactorGraphIterators.constants(fg), c0a, c0b, c1a1, c1a2, c2b1, c2b2);
		assertOrder(FactorGraphIterables.constants(fg), c0a, c0b, c1a1, c1a2, c2b1, c2b2);
		assertOrder(FactorGraphIterators.constantsDownto(fg, 0), c0a, c0b);
		assertOrder(FactorGraphIterables.constantsDownto(fg, 0), c0a, c0b);
		assertOrder(FactorGraphIterators.constantsDownto(fg, 1), c0a, c0b, c1a1, c1a2);
		assertOrder(FactorGraphIterables.constantsDownto(fg, 1), c0a, c0b, c1a1, c1a2);
		assertOrder(FactorGraphIterators.constants(fg1a), c1a1, c1a2, c2b1, c2b2);
		assertOrder(FactorGraphIterables.constants(fg1a), c1a1, c1a2, c2b1, c2b2);
		
		assertOrder(FactorGraphIterators.factors(fg), f0a, f0b, f1a1, f1a2, f2b1, f2b2);
		assertOrder(FactorGraphIterables.factors(fg), f0a, f0b, f1a1, f1a2, f2b1, f2b2);
		assertOrder(FactorGraphIterators.factorsDownto(fg, 0), f0a, f0b);
		assertOrder(FactorGraphIterables.factorsDownto(fg, 0), f0a, f0b);
		assertOrder(FactorGraphIterators.factorsDownto(fg, 1), f0a, f0b, f1a1, f1a2);
		assertOrder(FactorGraphIterables.factorsDownto(fg, 1), f0a, f0b, f1a1, f1a2);
		assertOrder(FactorGraphIterators.factors(fg1a), f1a1, f1a2, f2b1, f2b2);
		assertOrder(FactorGraphIterables.factors(fg1a), f1a1, f1a2, f2b1, f2b2);
		
		assertOrder(FactorGraphIterators.variables(fg), v0a, v0b, v0c, v0d, v1a1, v1a2, v2b1, v2b2);
		assertOrder(FactorGraphIterables.variables(fg), v0a, v0b, v0c, v0d, v1a1, v1a2, v2b1, v2b2);
		assertOrder(FactorGraphIterators.variablesDownto(fg, 0), v0a, v0b, v0c, v0d);
		assertOrder(FactorGraphIterables.variablesDownto(fg, 0), v0a, v0b, v0c, v0d);
		assertOrder(FactorGraphIterators.variablesDownto(fg, 1), v0a, v0b, v0c, v0d, v1a1, v1a2);
		assertOrder(FactorGraphIterables.variablesDownto(fg, 1), v0a, v0b, v0c, v0d, v1a1, v1a2);
		assertOrder(FactorGraphIterators.variables(fg1a), v1a1, v1a2, v2b1, v2b2);
		assertOrder(FactorGraphIterables.variables(fg1a), v1a1, v1a2, v2b1, v2b2);
		assertOrder(FactorGraphIterators.variablesAndBoundary(fg1a), v0d, v0b, v1a1, v1a2, v2b1, v2b2);
		assertOrder(FactorGraphIterables.variablesAndBoundary(fg1a), v0d, v0b, v1a1, v1a2, v2b1, v2b2);
		
		assertOrder(FactorGraphIterators.boundary(fg));
		assertOrder(FactorGraphIterators.boundary(fg));
		assertOrder(FactorGraphIterators.boundary(fg1a), v0d, v0b);
		assertOrder(FactorGraphIterables.boundary(fg1a), v0d, v0b);
		assertOrder(FactorGraphIterators.boundary(fg2b), v0d);
		assertOrder(FactorGraphIterables.boundary(fg2b), v0d);
		
		assertOrder(FactorGraphIterators.variableBlocks(fg), b0a, b1a1);
		assertOrder(FactorGraphIterables.variableBlocks(fg), b0a, b1a1);
		assertOrder(FactorGraphIterators.variableBlocksDownto(fg, 0), b0a);
		assertOrder(FactorGraphIterables.variableBlocksDownto(fg, 0), b0a);
		assertOrder(FactorGraphIterators.variableBlocks(fg1a), b1a1);
		assertOrder(FactorGraphIterables.variableBlocks(fg1a), b1a1);
		
		for (FactorGraph subgraph : FactorGraphIterables.subgraphs(fg))
		{
			assertSameOrder(subgraph.getOwnedConstants(), FactorGraphIterators.ownedConstants(subgraph));
			assertSameOrder(subgraph.getOwnedFactors(), FactorGraphIterators.ownedFactors(subgraph));
			assertSameOrder(subgraph.getOwnedGraphs(), FactorGraphIterators.ownedSubgraphs(subgraph));
			assertSameOrder(subgraph.getOwnedVariables(), FactorGraphIterators.ownedVariables(subgraph));
			assertSameOrder(subgraph.getOwnedVariableBlocks(), FactorGraphIterators.ownedVariableBlocks(subgraph));
		}
	}
	
	private Bit bit(String name)
	{
		Bit bit = new Bit();
		bit.setName(name);
		return bit;
	}
	
	private <T> void assertSameOrder(Collection<T> collection, Iterator<T> iterator)
	{
		Iterator<T> collectionIter = collection.iterator();
		
		int size = 0;
		while (iterator.hasNext())
		{
			assertTrue(collectionIter.hasNext());
			assertSame(collectionIter.next(), iterator.next());
			++size;
		}
		
		assertEquals(size, collection.size());
	}
	
	private void assertOrder(IFactorGraphChildren<FactorGraph> children, FactorGraph ...graphs)
	{
		assertEquals(graphs.length, children.size());
		assertSame(graphs[0], children.root());
		IFactorGraphChildIterator<FactorGraph> iterator = children.iterator();
		assertSame(children.root(), iterator.root());
		assertEquals(children.maxNestingDepth(), iterator.maxNestingDepth());
		assertOrder(iterator, graphs);
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
	
	@SuppressWarnings("unchecked")
	private <T extends IFactorGraphChild> void assertOrder(IFactorGraphChildren<T> children, T ... expected)
	{
		assertEquals(expected.length, children.size());
		IFactorGraphChildIterator<T> iterator = children.iterator();
		assertSame(children.root(), iterator.root());
		assertEquals(children.maxNestingDepth(), iterator.maxNestingDepth());
		assertOrder(iterator, expected);
	}
		
	private <T extends IFactorGraphChild> void assertOrder(IFactorGraphChildIterator<T> iterator,
		@SuppressWarnings("unchecked") T ... children)
	{
		final FactorGraph root = iterator.root();

		int i = 0;
		while (iterator.hasNext())
		{
			T child = iterator.next();
			assertNotNull(child);
			assertSame(children[i++], child);
			
			if (child != iterator.root())
			{
				int depth = 0;

				for (FactorGraph parent = child.getParentGraph(); parent != null; parent = parent.getParentGraph())
				{
					if (parent == root)
					{
						assertTrue(depth - 1 < iterator.maxNestingDepth());
						break;
					}
					++depth;
				}
				
			}
		}
		
		assertNull(iterator.next());
		assertEquals(children.length, i);
		
		iterator.reset();
		i = 0;
		while (true)
		{
			T node = iterator.next();
			if (node != null)
			{
				assertSame(children[i++], node);
			}
			else
			{
				assert !iterator.hasNext();
				break;
			}
		}
		
	}
}
