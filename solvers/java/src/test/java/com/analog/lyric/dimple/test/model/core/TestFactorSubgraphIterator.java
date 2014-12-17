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

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.FactorSubgraphIterator;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestFactorSubgraphIterator extends DimpleTestBase
{
	@Test
	public void test()
	{
		FactorGraph fg = new FactorGraph();
		
		assertOrder(new FactorSubgraphIterator(fg), fg);
		
		FactorGraph fg1a = fg.addGraph(new FactorGraph());
		FactorGraph fg2a = fg1a.addGraph(new FactorGraph());
		FactorGraph fg2b = fg1a.addGraph(new FactorGraph());
		FactorGraph fg3b = fg1a.addGraph(new FactorGraph());
		FactorGraph fg1b = fg.addGraph(new FactorGraph());
		
		assertOrder(new FactorSubgraphIterator(fg), fg, fg1a, fg2a, fg2b, fg3b, fg1b);
	}
	
	private void assertOrder(FactorSubgraphIterator iterator, FactorGraph ... graphs)
	{
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
				assertTrue(depth >= 0);
				assertTrue(depth < iterator.maxNestingDepth());
			}
		}
		
		assertNull(iterator.next());
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
	}
}
