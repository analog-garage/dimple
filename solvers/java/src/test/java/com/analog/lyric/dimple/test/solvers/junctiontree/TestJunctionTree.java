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

package com.analog.lyric.dimple.test.solvers.junctiontree;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverGraph;
import com.analog.lyric.dimple.test.model.RandomGraphGenerator;
import com.analog.lyric.util.misc.MapList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Unit tests for {@link JunctionTreeSolver}
 * 
 * @since 0.05
 * @author Christopher Barber
 */
public class TestJunctionTree
{
	private final long _seed = -4867686177798148289L;//new Random().nextLong();
	private final Random _rand = new Random(_seed);
	private final RandomGraphGenerator _graphGenerator = new RandomGraphGenerator(_rand);
	
	private static DiscreteDomain d2 = DiscreteDomain.range(0, 1);
	
	@Test
	public void testGrid2()
	{
		testGraph(_graphGenerator.buildGrid(2, d2));
	}
	
	@Test
	public void testGrid3()
	{
		testGraph(_graphGenerator.buildGrid(3, d2));
	}

	@Test
	public void testStudentNetwork()
	{
		testGraph(_graphGenerator.buildStudentNetwork());
	}
	
	private void testGraph(FactorGraph model)
	{
		JunctionTreeSolverGraph jtgraph = model.setSolverFactory(new JunctionTreeSolver());
		jtgraph.getTransformer().random(_rand); // set random generator so we can reproduce failures
		model.solve();
		
		FactorGraph transformedModel = jtgraph.getDelegate().getModelObject();
		RandomGraphGenerator.labelFactors(transformedModel);
		assertTrue(transformedModel.isTree());
		
		// Do solve again on a copy of the graph with all factors merged into single giant factor.
		BiMap<Node,Node> old2new = HashBiMap.create();
		FactorGraph model2 = model.copyRoot(old2new);
		MapList<Factor> factors2 = model2.getFactors();
		model2.join(factors2.toArray(new Factor[factors2.size()]));
		model2.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		model2.solve();
		
		// Compare marginal beliefs
		for (VariableBase variable : model.getVariables())
		{
			final VariableBase variable2 = (VariableBase)old2new.get(variable);
			final Object belief1 = variable.getBeliefObject();
			final Object belief2 = variable2.getBeliefObject();
			
			if (belief1 instanceof double[])
			{
				assertArrayEquals((double[])belief2, (double[])belief1, 1e-10);
			}
			else
			{
				assertEquals(belief1, belief2);
			}
		}
	}
}
