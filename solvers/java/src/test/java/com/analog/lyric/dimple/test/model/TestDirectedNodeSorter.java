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

package com.analog.lyric.dimple.test.model;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.DirectedNodeSorter;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Tests for {@link DirectedNodeSorter}
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestDirectedNodeSorter extends DimpleTestBase
{
	@Test
	public void test()
	{
		RandomGraphGenerator generator = new RandomGraphGenerator(new Random(42));
		
		// Undirected cases
		testCase(generator.buildRandomGraph(20));
		testCase(generator.buildGrid(3));
		
		// Fully directed cases
		testCase(generator.buildStudentNetwork());
		generator.direction(RandomGraphGenerator.Direction.FORWARD);
		testCase(generator.buildRandomGraph(20));
		testCase(generator.buildGrid(3));
	}
	
	private void testCase(FactorGraph fg)
	{
		testInvariants(fg, DirectedNodeSorter.orderDirectedNodes(fg));
	}
	
	private void testInvariants(FactorGraph fg, Map<Node, Integer> orderMap)
	{
		for (Factor factor : fg.getFactors())
		{
			assertEquals(factor.isDirected(), orderMap.containsKey(factor));

			if (!factor.isDirected())
			{
				continue;
			}
			
			int order = orderMap.get(factor);
			int[] to = requireNonNull(factor.getDirectedTo());
			for (int i : to)
			{
				Variable var = factor.getSibling(i);
				assertTrue(orderMap.containsKey(var));
				assertTrue(order < orderMap.get(var));
			}
			int[] from = requireNonNull(factor.getDirectedFrom());
			for (int i : from)
			{
				Variable var = factor.getSibling(i);
				assertTrue(orderMap.containsKey(var));
				assertTrue(order > orderMap.get(var));
			}
			
			if (from.length == 0)
			{
				assertEquals(0, order);
			}
			else
			{
				assertTrue(order > 0);
			}
		}
		
		for (Node node : orderMap.keySet())
		{
			assertTrue(orderMap.get(node) >= 0);
			if (node instanceof Factor)
			{
				assertTrue(((Factor)node).isDirected());
			}
		}
	}
}
