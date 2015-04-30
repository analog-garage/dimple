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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.SumOfInputs;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.transform.OptionVariableEliminatorCostList;
import com.analog.lyric.dimple.model.transform.VariableEliminator;
import com.analog.lyric.dimple.model.transform.VariableEliminator.OrderIterator;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Ordering;
import com.analog.lyric.dimple.model.transform.VariableEliminator.Stats;
import com.analog.lyric.dimple.model.transform.VariableEliminator.VariableCost;
import com.analog.lyric.dimple.model.transform.VariableEliminatorCostListOptionKey;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.options.LocalOptionHolder;

/**
 * Test cases for {@link VariableEliminator}
 */
public class TestVariableEliminator extends DimpleTestBase
{
	// It doesn't matter what factor function we use for these tests, since we
	// will never evaluate it.
	private static final FactorFunction factorFunction = new SumOfInputs();
	
	@Test
	public void testTreeModels()
	{
		// Tree models should produce the same result regardless of the cost
		// function since they should all value variables with no more than one
		// remaining edge as zero cost.
		
		final Stats treeStats = expectedStats()
			.addedEdges(0)
			.addedEdgeWeight(0)
			.conditionedVariables(0);
		
		//
		// A simple linear chain graph
		//
		
		{
			FactorGraph model = new FactorGraph();
			Discrete a = newVar(5, "a");
			Discrete b = newVar(2, "b");
			Discrete c = newVar(10, "c");
			addClique(model, a, b);
			addClique(model, b, c);
			
			testEliminator(model, VariableCost.MIN_NEIGHBORS, treeStats, a, b, c);
			testEliminator(model, VariableCost.WEIGHTED_MIN_NEIGHBORS, treeStats, a, b, c);
			testEliminator(model, VariableCost.MIN_FILL, treeStats, a, b, c);
			testEliminator(model, VariableCost.WEIGHTED_MIN_FILL, treeStats, a, b, c);
			testEliminator(model, VariableCost.MIN_NEIGHBORS, treeStats);
		}
		
		//
		// Simple tree
		//
		
		{
			FactorGraph model = new FactorGraph();
			Discrete a = newVar(3, "a");
			Discrete b = newVar(4, "b");
			Discrete c = newVar(5, "c");
			Discrete d = newVar(6, "d");
			
			model.addVariables(a,b,c,d);
			addClique(model, a, d);
			addClique(model, b, d);
			addClique(model, c, d);
			
			testEliminator(model, VariableCost.MIN_NEIGHBORS, treeStats, a, b, c, d);
			testEliminator(model, VariableCost.WEIGHTED_MIN_NEIGHBORS, treeStats, a, b, c, d);
			testEliminator(model, VariableCost.MIN_FILL, treeStats, a, b, c, d);
			testEliminator(model, VariableCost.WEIGHTED_MIN_FILL, treeStats, a, b, c, d);
		}
		
		//
		// Simple binary tree
		//
		
		{
			FactorGraph model = new FactorGraph();
			Discrete a = newVar(2, "a");
			Discrete b = newVar(2, "b");
			Discrete c = newVar(4, "c");
			addClique(model, a, b, c);
			Discrete d = newVar(2, "d");
			Discrete e = newVar(2, "e");
			Discrete f = newVar(4, "f");
			addClique(model, d, e, f);
			Discrete g = newVar(8, "g");
			addClique(model, c, f, g);
			
			testEliminator(model, VariableCost.MIN_NEIGHBORS, treeStats, a, b, c, g, d, e, f);
			testEliminator(model, VariableCost.WEIGHTED_MIN_NEIGHBORS, treeStats, a, b, d, e, g, c, f);
			testEliminator(model, VariableCost.MIN_FILL, treeStats, a, b, c, d, e, f, g);
			testEliminator(model, VariableCost.WEIGHTED_MIN_FILL, treeStats, a, b, c, d, e, f, g);
		}
	}
	
	@Test
	public void testMinimalLoop()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newVar(2, "a");
		Discrete b = newVar(2, "b");
		addClique(model, a, b);
		addClique(model, a, b);
		
		testEliminator(model, VariableCost.MIN_NEIGHBORS,
			expectedStats().addedEdges(0).addedEdgeWeight(0)
			.maxCliqueSize(2).maxCliqueCardinality(4)
			.variablesWithDuplicateEdges(2)
			, a, b);
	}
	
	/**
	 * Graph of form:
	 * <pre>
	 *  a[2]------b[3]
	 *   |          |
	 *   |          |
	 *   |          |
	 *  d[5]------c[4]
	 * </pre>
	 */
	@Test
	public void testSquare()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newVar(2, "a");
		Discrete b = newVar(3, "b");
		Discrete c = newVar(4, "c");
		Discrete d = newVar(5, "d");
		addClique(model, a, b);
		addClique(model, b, c);
		addClique(model, c, d);
		addClique(model, a, d);
		
		testEliminator(model, VariableCost.MIN_NEIGHBORS,
			expectedStats().addedEdges(1).addedEdgeWeight(15)
			.maxCliqueSize(3).maxCliqueCardinality(60),
			a, b, c, d);

		testEliminator(model, VariableCost.WEIGHTED_MIN_NEIGHBORS,
			expectedStats().addedEdges(1).addedEdgeWeight(8)
			.maxCliqueSize(3).maxCliqueCardinality(40),
			b, d, a, c);

		testEliminator(model, VariableCost.MIN_FILL,
			expectedStats().addedEdges(1).addedEdgeWeight(15)
			.maxCliqueSize(3).maxCliqueCardinality(60),
			a, b, c, d);

		testEliminator(model, VariableCost.WEIGHTED_MIN_FILL,
			expectedStats().addedEdges(1).addedEdgeWeight(8)
			.maxCliqueSize(3).maxCliqueCardinality(40),
			b, a, c, d);
		
		testGenerate(model, true,
			thresholdStats().maxCliqueCardinality(5),
			expectedStats().addedEdges(1).addedEdgeWeight(8)
			.maxCliqueSize(3).maxCliqueCardinality(40),
			b, d, a, c);
		
		testGenerate(model, true,
			thresholdStats().maxCliqueCardinality(60),
			expectedStats().addedEdges(1).addedEdgeWeight(15)
			.maxCliqueSize(3).maxCliqueCardinality(60),
			a, b, c, d);

		//
		// Test conditioning
		//
		
		b.setFixedValue(2);
		
		testEliminator(model, VariableCost.MIN_NEIGHBORS,
			expectedStats().addedEdges(1).addedEdgeWeight(15)
			.maxCliqueSize(3).maxCliqueCardinality(60)
			.conditionedVariables(0),
			a, b, c, d);

		testEliminator(model, true, VariableCost.MIN_NEIGHBORS,
			expectedStats().addedEdges(0).addedEdgeWeight(0)
			.maxCliqueSize(2).maxCliqueCardinality(20)
			.conditionedVariables(1),
			b, a, c, d);
		
	}
	
	/**
	 * Graph of form:
	 * <pre>
	 *  a[2]===---c[4]------e[6]------g[3]
	 *   |          |         |         |
	 *   |          |         |         |
	 *   |          |         |         |
	 *  b[3]------d[5]---===f[7]------h[4]
	 * </pre>
	 */
	@Test
	public void testLadder()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newVar(2, "a");
		Discrete b = newVar(3, "b");
		addClique(model, a, b);
		Discrete c = newVar(4, "c");
		Discrete d = newVar(5, "d");
		addClique(model, c, d);
		addClique(model, a, a, c); // add double link from a to a-c factor.
		addClique(model, b, d);
		Discrete e = newVar(6, "e");
		Discrete f = newVar(7, "f");
		addClique(model, e, f);
		addClique(model, c, e);
		addClique(model, d, f, f); // add double link from f to d-f factor
		Discrete g = newVar(3, "g");
		Discrete h = newVar(4, "h");
		addClique(model, g, h);
		addClique(model, e, g);
		addClique(model, f, h);
		
		testEliminator(model, VariableCost.MIN_NEIGHBORS,
			expectedStats().addedEdges(3).addedEdgeWeight(63)
			.maxCliqueSize(3).maxCliqueCardinality(210)
			.factorsWithDuplicateVariables(2),
			a, b, c, d, e, f, g, h);
		
		testEliminator(model, VariableCost.WEIGHTED_MIN_NEIGHBORS,
			expectedStats().addedEdges(3).addedEdgeWeight(59)
			.maxCliqueSize(3).maxCliqueCardinality(168)
			, b, a, h, d, c, f, e, g);
		
		testEliminator(model, VariableCost.MIN_FILL,
			expectedStats().addedEdges(3).addedEdgeWeight(63)
			.maxCliqueSize(3).maxCliqueCardinality(210)
			, a, b, c, d, e, f, g, h);
		
		testEliminator(model, VariableCost.WEIGHTED_MIN_FILL,
			expectedStats().addedEdges(3).addedEdgeWeight(59)
			.maxCliqueSize(3).maxCliqueCardinality(168)
			, b, a, h, g, d, c, e, f);
		
		//
		// Test conditioning
		//
		
		c.setFixedValue(2);
		
		testEliminator(model, false, VariableCost.WEIGHTED_MIN_NEIGHBORS,
			expectedStats().addedEdges(3).addedEdgeWeight(59)
			.maxCliqueSize(3).maxCliqueCardinality(168)
			.conditionedVariables(0)
			, b, a, h, d, c, f, e, g);
		
		testEliminator(model, true, VariableCost.WEIGHTED_MIN_NEIGHBORS,
			expectedStats().addedEdges(1).addedEdgeWeight(21)
			.maxCliqueSize(3).maxCliqueCardinality(126)
			.conditionedVariables(1)
			, c, a, b, d, e, f, g, h);
	}

	/**
	 * Graph of form
	 * <pre>
	 * a[10]      b[100]      c[9]
	 * d[3]       e[2]        f[3]
	 * g[2]       h[5]        i[2]
	 * j[7]       k[2]        l[5]
	 * </pre>
	 */
	@Test
	public void testGrid()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newVar(10, "a");
		Discrete b = newVar(100, "b");
		addClique(model, a, b);
		Discrete c = newVar(9, "c");
		addClique(model, b, c);
		Discrete d = newVar(3, "d");
		addClique(model, a, d);
		Discrete e = newVar(2, "e");
		addClique(model, d, e);
		addClique(model, b, e);
		Discrete f = newVar(3, "f");
		addClique(model, e, f);
		addClique(model, c, f);
		Discrete g = newVar(2, "g");
		addClique(model, d, g);
		Discrete h = newVar(5, "h");
		addClique(model, g, h);
		addClique(model, e, h);
		Discrete i = newVar(2, "i");
		addClique(model ,h, i);
		addClique(model, f, i);
		Discrete j = newVar(7, "j");
		addClique(model, g, j);
		Discrete k = newVar(2, "k");
		addClique(model, j, k);
		addClique(model, h, k);
		Discrete l = newVar(5, "l");
		addClique(model, k, l);
		addClique(model, i, l);
		
		testEliminator(model, VariableCost.MIN_NEIGHBORS,
			expectedStats().addedEdges(9).addedEdgeWeight(646)
			.maxCliqueSize(4).maxCliqueCardinality(3000)
			, a, c, j, l, b, d, e, f, g, h, i, k);
		
		testEliminator(model, VariableCost.WEIGHTED_MIN_NEIGHBORS,
			expectedStats().addedEdges(12).addedEdgeWeight(194)
			.maxCliqueSize(5).maxCliqueCardinality(18000)
			, j, l, h, k, g, i, f, b, a, c, d, e);
		
		testEliminator(model, VariableCost.MIN_FILL,
			expectedStats().addedEdges(9).addedEdgeWeight(646)
			.maxCliqueSize(4).maxCliqueCardinality(3000)
			, a, c, b, j, l, k, d, e, f, g, h, i);
		
		testEliminator(model, VariableCost.WEIGHTED_MIN_FILL,
			expectedStats().addedEdges(11).addedEdgeWeight(190)
			.maxCliqueSize(4).maxCliqueCardinality(18000)
			, j, l, k, h, g, i, f, b, a, c, d, e);
			
	}
	
	/**
	 * Extended student Bayesian network from Koller's Probabilistic Graphical Models (Figure 9.8)
	 * <pre>
	 *   c[3]
	 *     |
	 *     v
	 *   d[3]   i[3]
	 *      \   /  \
	 *       v v    v
	 *       g[5]  s[10]
	 *       / |     |
	 *      /  v     |
	 *      | l[2]   |
	 *      |     \  |
	 *      |      v v
	 *      |      j[2]
	 *      |     /
	 *       \   /
	 *        v v
	 *        h[2]
	 * </pre>
	 */
	@Test
	public void testStudentNetwork()
	{
		FactorGraph model = new FactorGraph();
		Discrete c = newVar(3, "c");
		Discrete d = newVar(3, "d");
		addClique(model, d, c);
		Discrete i = newVar(3, "i");
		Discrete g = newVar(5, "g");
		Discrete s = newVar(10, "s");
		addClique(model, g, d, i);
		addClique(model, s, i);
		Discrete l = newVar(2, "l");
		addClique(model, l, g);
		Discrete j = newVar(2, "j");
		addClique(model, j, l, s);
		Discrete h = newVar(2, "h");
		addClique(model, h, g, j);
		
		testEliminator(model, VariableCost.MIN_NEIGHBORS,
			expectedStats().addedEdges(1).addedEdgeWeight(50)
			.maxCliqueSize(4).maxCliqueCardinality(200)
			, c, d, i, h, g, s, l, j);
		
		testEliminator(model, VariableCost.WEIGHTED_MIN_NEIGHBORS,
			expectedStats().addedEdges(2).addedEdgeWeight(12)
			.maxCliqueSize(4).maxCliqueCardinality(120)
			, c, h, s, d, g, i, l, j);
		
		testEliminator(model, VariableCost.MIN_FILL,
			expectedStats().addedEdges(1).addedEdgeWeight(50)
			.maxCliqueSize(4).maxCliqueCardinality(200)
			, c, d, h, i, g, s, l, j);
		
		testEliminator(model, VariableCost.WEIGHTED_MIN_FILL,
			expectedStats().addedEdges(2).addedEdgeWeight(12)
			.maxCliqueSize(4).maxCliqueCardinality(120)
			, c, d, h, g, i, s, l, j);
	}
	
	@Test
	public void testNonDiscrete()
	{
		// For now we do not support non-Discrete variables at all.
		FactorGraph model = new FactorGraph();
		model.setSolverFactory(new GibbsSolver());
		Discrete a = newVar(4, "a");
		Discrete b = newVar(5, "b");
		Real r = new Real();
		r.setName("r");
		addClique(model, a, b, r);
		
		VariableEliminator eliminator = new VariableEliminator(model, false);
		expectThrow(DimpleException.class, ".*cannot handle non-discrete variable.*",
			eliminator, "orderIterator", VariableCost.MIN_FILL);
		eliminator = new VariableEliminator(model, true);
		expectThrow(DimpleException.class, ".*cannot handle non-discrete variable.*",
			eliminator, "orderIterator", VariableCost.MIN_FILL);
		
		r.setFixedValue(1.0);
		eliminator = new VariableEliminator(model, false);
		expectThrow(DimpleException.class, ".*cannot handle non-discrete variable.*",
			eliminator, "orderIterator", VariableCost.MIN_FILL);
		
		testEliminator(model, true, VariableCost.MIN_FILL,
			expectedStats().addedEdges(0).addedEdgeWeight(0)
			.maxCliqueSize(2).maxCliqueCardinality(20),
			r, a, b);
	}
	
	@Test
	public void testStats()
	{
		Stats stats = new Stats();
		assertEquals(-1, stats.addedEdges());
		assertEquals(-1, stats.addedEdgeWeight());
		assertEquals(-1, stats.conditionedVariables());
		assertEquals(-1, stats.maxCliqueSize());
		assertEquals(-1, stats.maxCliqueCardinality());
		assertCompareTo(0, stats, stats, stats);
		assertTrue(stats.meetsThreshold(stats));
		
		assertSame(stats, stats.addedEdges(2));
		assertSame(stats, stats.addedEdgeWeight(10));
		assertSame(stats, stats.conditionedVariables(1));
		assertSame(stats, stats.maxCliqueSize(3));
		assertSame(stats, stats.maxCliqueCardinality(20));
		assertEquals(2, stats.addedEdges());
		assertEquals(10, stats.addedEdgeWeight());
		assertEquals(1, stats.conditionedVariables());
		assertEquals(3, stats.maxCliqueSize());
		assertEquals(20, stats.maxCliqueCardinality());
		assertCompareTo(0, stats, stats, stats);
		
		assertCompareTo(-1, stats, new Stats().addedEdges(3), new Stats().addedEdges(100));
		assertCompareTo(0, stats, new Stats().addedEdges(3), new Stats());
		assertCompareTo(1, stats, new Stats().addedEdgeWeight(8), new Stats().addedEdgeWeight(100));
		assertCompareTo(-1, stats, new Stats().maxCliqueSize(4), new Stats().maxCliqueSize(10));
		assertCompareTo(1, stats, new Stats().maxCliqueCardinality(10), new Stats().maxCliqueCardinality(200));
		assertCompareTo(-1, stats,
			new Stats().maxCliqueCardinality(30).addedEdgeWeight(5),
			new Stats().maxCliqueCardinality(1000).addedEdgeWeight(1000));
		assertCompareTo(1, stats,
			new Stats().addedEdgeWeight(5).maxCliqueSize(4),
			new Stats().addedEdgeWeight(1000).maxCliqueSize(1000));
		assertCompareTo(-1, stats,
			new Stats().maxCliqueSize(3).addedEdges(3),
			new Stats().maxCliqueSize(0).addedEdges(0));
		assertCompareTo(1, stats,
			new Stats().maxCliqueSize(2).addedEdges(3),
			new Stats().maxCliqueSize(0).addedEdges(0));
		
		assertTrue(stats.meetsThreshold(new Stats().addedEdges(2)));
		assertFalse(stats.meetsThreshold(new Stats().addedEdges(1)));
		assertTrue(stats.meetsThreshold(new Stats().addedEdgeWeight(11).maxCliqueSize(3).maxCliqueCardinality(20)));
		assertFalse(stats.meetsThreshold(new Stats().addedEdgeWeight(11).maxCliqueSize(2).maxCliqueCardinality(20)));
		assertFalse(stats.meetsThreshold(new Stats().addedEdgeWeight(9).maxCliqueSize(4).maxCliqueCardinality(20)));
		assertFalse(stats.meetsThreshold(new Stats().addedEdgeWeight(20).maxCliqueSize(4).maxCliqueCardinality(19)));
		
		// conditionedVariables() does not participate in compareTo or meetsThreshold
		assertCompareTo(0, new Stats().conditionedVariables(1), new Stats().conditionedVariables(2),
			new Stats().conditionedVariables(2));
		assertTrue(new Stats().conditionedVariables(2).meetsThreshold(new Stats().conditionedVariables(0)));
		
		// Test alreadyGoodForExactInference
		assertTrue(new Stats().addedEdges(0).conditionedVariables(0).factorsWithDuplicateVariables(0)
			.variablesWithDuplicateEdges(0).mergedFactors(0)
			.alreadyGoodForFastExactInference());
		assertFalse(new Stats().addedEdges(0).conditionedVariables(0).factorsWithDuplicateVariables(0)
			.variablesWithDuplicateEdges(0)
			.alreadyGoodForFastExactInference());
		assertFalse(new Stats().addedEdges(0).conditionedVariables(0).factorsWithDuplicateVariables(0)
			.alreadyGoodForFastExactInference());
		assertFalse(new Stats().addedEdges(1).conditionedVariables(0).factorsWithDuplicateVariables(0)
			.alreadyGoodForFastExactInference());
		assertFalse(new Stats().addedEdges(0).conditionedVariables(1).factorsWithDuplicateVariables(0)
			.alreadyGoodForFastExactInference());
		assertFalse(new Stats().addedEdges(0).conditionedVariables(0).factorsWithDuplicateVariables(1)
			.alreadyGoodForFastExactInference());
	}
	
	/**
	 * Test {@link OptionVariableEliminatorCostList} and {@link VariableEliminatorCostListOptionKey}.
	 * 
	 * @since 0.07
	 */
	@Test
	public void testCostListOptionKey()
	{
		assertTrue(OptionVariableEliminatorCostList.EMPTY.isEmpty());
		
		// Test list type
		OptionVariableEliminatorCostList list = new OptionVariableEliminatorCostList();
		assertTrue(list.isEmpty());
		list = new OptionVariableEliminatorCostList(VariableCost.WEIGHTED_MIN_FILL.function());
		assertArrayEquals(new Object[] { VariableCost.WEIGHTED_MIN_FILL.function() }, list.toArray());
		list = new OptionVariableEliminatorCostList(VariableCost.WEIGHTED_MIN_NEIGHBORS);
		assertArrayEquals(new Object[] { VariableCost.WEIGHTED_MIN_NEIGHBORS.function() }, list.toArray());
	
		// Test key construction
		VariableEliminatorCostListOptionKey key = new VariableEliminatorCostListOptionKey(getClass(), "key");
		assertSame(OptionVariableEliminatorCostList.class, key.type());
		assertTrue(key.defaultValue().isEmpty());
		assertSame(getClass(), key.getDeclaringClass());
		assertEquals("key", key.name());
		
		key = new VariableEliminatorCostListOptionKey(getClass(), "x", VariableCost.MIN_FILL);
		assertEquals("x", key.name());
		assertArrayEquals(new Object[] { VariableCost.MIN_FILL.function() }, key.defaultValue().toArray());
		
		key = new VariableEliminatorCostListOptionKey(getClass(), "x",
			VariableCost.MIN_NEIGHBORS.function(), VariableCost.MIN_FILL.function());
		assertArrayEquals(new Object[] { VariableCost.MIN_NEIGHBORS.function(), VariableCost.MIN_FILL.function() },
			key.defaultValue().toArray());
		
		// Test convertValue
		assertSame(list, key.convertToValue(list));
		assertArrayEquals(
			new Object[] { VariableCost.MIN_FILL.function() },
			key.convertToValue(VariableCost.MIN_FILL).toArray());
		assertArrayEquals(
			new Object[] { VariableCost.MIN_FILL.function() },
			key.convertToValue(VariableCost.MIN_FILL.function()).toArray());
		assertArrayEquals(
			new Object[] { VariableCost.MIN_FILL.function() },
			key.convertToValue("MIN_FILL").toArray());
		assertArrayEquals(
			new Object[] { VariableCost.MIN_FILL.function() },
			key.convertToValue(new VariableCost[] { VariableCost.MIN_FILL }).toArray());
		assertArrayEquals(
			new Object[] { VariableCost.MIN_FILL.function() },
			key.convertToValue(new VariableEliminator.CostFunction[] { VariableCost.MIN_FILL.function() }).toArray());
		assertArrayEquals(
			new Object[] { VariableCost.MIN_FILL.function(), VariableCost.MIN_NEIGHBORS.function() },
			key.convertToValue(new Object[] { "MIN_FILL", VariableCost.MIN_NEIGHBORS } ).toArray());
		
		expectThrow(ClassCastException.class, key, "convertToValue", new double[] { 2.3 });
		
		// Test set methods
		LocalOptionHolder holder = new LocalOptionHolder();
		key.set(holder, VariableCost.MIN_FILL);
		assertArrayEquals(
			new Object[] { VariableCost.MIN_FILL.function() },
			requireNonNull(holder.getOption(key)).toArray());
		key.set(holder, VariableCost.MIN_NEIGHBORS.function());
		assertArrayEquals(
			new Object[] { VariableCost.MIN_NEIGHBORS.function() },
			requireNonNull(holder.getOption(key)).toArray());
	}
	
	/*----------------
	 * Helper methods
	 */
	
	private void addClique(FactorGraph model, Variable ... variables)
	{
		model.addFactor(factorFunction, variables);
	}
	
	private void assertCompareTo(int expected, Stats stats1, Stats stats2, Stats threshold)
	{
		assertEquals(expected, stats1.compareTo(stats2, threshold));
		assertEquals(-expected, stats2.compareTo(stats1, threshold));
	}
	
	private Discrete newVar(int cardinality, String name)
	{
		Discrete var = new Discrete(DiscreteDomain.range(1, cardinality));
		var.setName(name);
		return var;
	}

	private void testEliminator(FactorGraph model, VariableCost cost,
		Stats expectedStats, Variable ... expectedOrder)
	{
		testEliminator(model, false, cost, expectedStats, expectedOrder);
	}
	
	private void testEliminator(FactorGraph model, boolean useConditioning, VariableCost cost,
		Stats expectedStats, Variable ... expectedOrder)
	{
		final boolean deterministic = expectedOrder.length != 0;
		final VariableEliminator eliminator =
			deterministic?
				new VariableEliminator(model, useConditioning, null) :
				new VariableEliminator(model, useConditioning);
		assertSame(model, eliminator.getModel());
		assertEquals(useConditioning, eliminator.usesConditioning());
		if (deterministic)
		{
			assertNull(eliminator.getRandomizer());
		}
		else
		{
			assertNotNull(eliminator.getRandomizer());
		}
		
		OrderIterator iterator = eliminator.orderIterator(cost);
		assertSame(eliminator, iterator.getEliminator());
		assertSame(cost, requireNonNull(iterator.getCostEvaluator()).type());
		assertSame(cost, requireNonNull(iterator.getStats().cost()).type());
		
		int nVariables = model.getVariableCount();
		assertEquals(nVariables, iterator.size());
		
		if (deterministic)
		{
			for (Variable expectedVariable : expectedOrder)
			{
				assertTrue(iterator.hasNext());
				Variable actualVariable = iterator.next();
				assertSame(expectedVariable, actualVariable);
				assertEquals(--nVariables, iterator.size());
			}
		}
		else
		{
			while (iterator.hasNext())
			{
				iterator.next();
				assertEquals(--nVariables, iterator.size());
			}
		}
		assertFalse(iterator.hasNext());
		assertNull(iterator.next());
		assertEquals(0, iterator.size());
		assertEquals(0, nVariables);

		expectThrow(UnsupportedOperationException.class, iterator, "remove");
		
		assertStats(expectedStats, iterator.getStats());
		
		// Test generate() method
		int nAttempts = deterministic ? -1 : 1;
		Ordering ordering = VariableEliminator.generate(model, useConditioning, nAttempts, expectedStats, cost);
		assertStats(expectedStats, ordering.stats);
		if (deterministic)
		{
			assertEquals(expectedOrder.length, ordering.variables.size());
			for (int i = 0, end = expectedOrder.length; i < end; ++i)
			{
				Variable expectedVariable = expectedOrder[i];
				Variable actualVariable = ordering.variables.get(i);
				assertSame(expectedVariable, actualVariable);
			}
		}
	}
	
	private void testGenerate(
		FactorGraph model,
		boolean useConditioning,
		Stats thresholdStats,
		Stats expectedStats,
		Variable ... expectedOrder)
	{
		Ordering ordering = VariableEliminator.generate(model, useConditioning, -1, thresholdStats);
		assertStats(expectedStats, ordering.stats);
	}
		
	private void assertStats(Stats expected, Stats actual)
	{
		if (expected.addedEdges() >= 0)
		{
			assertEquals(expected.addedEdges(), actual.addedEdges());
		}
		if (expected.addedEdgeWeight() >= 0)
		{
			assertEquals(expected.addedEdgeWeight(), actual.addedEdgeWeight());
		}
		if (expected.conditionedVariables() >= 0)
		{
			assertEquals(expected.conditionedVariables(), actual.conditionedVariables());
		}
		if (expected.factorsWithDuplicateVariables() >= 0)
		{
			assertEquals(expected.factorsWithDuplicateVariables(), actual.factorsWithDuplicateVariables());
		}
		if (expected.maxCliqueSize() >= 0)
		{
			assertEquals(expected.maxCliqueSize(), actual.maxCliqueSize());
		}
		if (expected.maxCliqueCardinality() >= 0)
		{
			assertEquals(expected.maxCliqueCardinality(), actual.maxCliqueCardinality());
		}
	}
	
	private Stats expectedStats()
	{
		return new Stats();
	}
	
	private Stats thresholdStats()
	{
		return new Stats();
	}
}
