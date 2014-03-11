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
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverGraph;
import com.analog.lyric.dimple.solvers.junctiontree.map.JunctionTreeMapSolver;
import com.analog.lyric.dimple.solvers.junctiontree.map.JunctionTreeMapSolverGraph;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.test.model.RandomGraphGenerator;
import com.analog.lyric.dimple.test.model.TestJunctionTreeTransform;
import com.analog.lyric.util.misc.MapList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Unit tests for {@link JunctionTreeSolver}
 * <p>
 * @since 0.05
 * @author Christopher Barber
 * @see TestJunctionTreeTransform
 */
public class TestJunctionTree
{
	private final long _seed = new Random().nextLong();
	private final Random _rand = new Random(_seed);
	private final RandomGraphGenerator _graphGenerator = new RandomGraphGenerator(_rand);
	
	@Test
	public void testGrid2()
	{
		testGraph(_graphGenerator.buildGrid(2));
	}
	
	@Test
	public void testGrid3()
	{
		testGraph(_graphGenerator.buildGrid(3));
	}

	@Test
	public void testStudentNetwork()
	{
		testGraph(_graphGenerator.buildStudentNetwork());
	}
	
	@Test
	public void testRandomGraphs()
	{
		final int nGraphs = 20;
		final int maxSize = 1000;
		RandomGraphGenerator gen = _graphGenerator.maxBranches(2).maxTreeWidth(3);

		for (int i = 0; i < nGraphs; ++i)
		{
			testGraph(gen.buildRandomGraph(_rand.nextInt(maxSize) + 1));
		}
		
	}
	
	private void testGraph(FactorGraph model)
	{
		try
		{
			testGraphImpl(model);
		}
		catch (Throwable ex)
		{
			String msg = String.format("%s. TestJunctionTreeTransform._seed==%dL", ex.toString(), _seed);
			ex.printStackTrace(System.err);
			System.err.format(">>> TestJunctionTreeTransform._seed==%dL;<<<\n", _seed);
			throw new RuntimeException(msg, ex);
		}
	}
	
	private void testGraphImpl(FactorGraph model)
	{
		JunctionTreeSolverGraph jtgraph = model.setSolverFactory(new JunctionTreeSolver());
		jtgraph.getTransformer().random(_rand); // set random generator so we can reproduce failures
		model.solve();
		
		FactorGraph transformedModel = jtgraph.getDelegate().getModelObject();
		RandomGraphGenerator.labelFactors(transformedModel);
		assertTrue(transformedModel.isForest());
		
		// Do solve again on a copy of the graph with all factors merged into single giant factor.
		BiMap<Node,Node> old2new = HashBiMap.create();
		FactorGraph model2 = model.copyRoot(old2new);
		MapList<Factor> factors2 = model2.getFactors();
		if (factors2.size() > 0)
		{
			model2.join(factors2.toArray(new Factor[factors2.size()]));
		}
		model2.setSolverFactory(new SumProductSolver());
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
		
		//
		// Now try MAP (minsum)
		//
		
		JunctionTreeMapSolverGraph jtmapgraph = model.setSolverFactory(new JunctionTreeMapSolver());
		jtgraph.getTransformer().random(_rand); // set random generator so we can reproduce failures
		model.solve();
		
		transformedModel = jtmapgraph.getDelegate().getModelObject();
		RandomGraphGenerator.labelFactors(transformedModel);
		assertTrue(transformedModel.isForest());
		
		model2.setSolverFactory(new MinSumSolver());
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
