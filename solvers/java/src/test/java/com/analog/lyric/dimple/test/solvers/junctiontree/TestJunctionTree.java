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

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.solvers.core.SolverBase;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverGraph;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverGraphBase;
import com.analog.lyric.dimple.solvers.junctiontreemap.JunctionTreeMAPSolver;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.model.RandomGraphGenerator;
import com.analog.lyric.dimple.test.model.TestJunctionTreeTransform;
import com.analog.lyric.util.misc.MapList;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.math.DoubleMath;

/**
 * Unit tests for {@link JunctionTreeSolver}
 * <p>
 * @since 0.05
 * @author Christopher Barber
 * @see TestJunctionTreeTransform
 */
public class TestJunctionTree extends DimpleTestBase
{
	private final long _seed = new Random().nextLong();
	private final Random _rand = new Random(_seed);
	private final RandomGraphGenerator _graphGenerator = new RandomGraphGenerator(_rand);
	
	@Test
	public void testSolverEquality()
	{
		SolverBase<?> solver1 = new JunctionTreeSolver();
		SolverBase<?> solver2 = new JunctionTreeSolver();
		SolverBase<?> solver3 = new com.analog.lyric.dimple.solvers.junctiontree.Solver();
		
		SolverBase<?> solver4 = new JunctionTreeMAPSolver();
		SolverBase<?> solver5 = new JunctionTreeMAPSolver();
		SolverBase<?> solver6 = new com.analog.lyric.dimple.solvers.junctiontreemap.Solver();
		
		assertEquals(solver1, solver2);
		assertEquals(solver1.hashCode(), solver2.hashCode());
		assertEquals(solver2, solver3);
		assertEquals(solver2.hashCode(), solver3.hashCode());
		assertNotEquals(solver3, solver4);
		assertNotEquals(solver3.hashCode(), solver4.hashCode());
		assertEquals(solver4, solver5);
		assertEquals(solver4.hashCode(), solver5.hashCode());
		assertEquals(solver5, solver6);
		assertEquals(solver5.hashCode(), solver6.hashCode());
	}
	
	@Test
	public void testTriangle()
	{
		testGraph(_graphGenerator.buildTriangle());
	}
	
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
			testGraph(gen.buildRandomGraph(_rand.nextInt(maxSize) + 10));
		}
		
	}
	
	@Test
	public void testBug429()
	{
		FactorGraph graph = new FactorGraph();
		Discrete a = new Bit(), b = new Bit(), c = new Bit(), d = new Bit();
		a.setName("a");
		b.setName("b");
		c.setName("c");
		d.setName("d");
		a.setInput(.3,.7);

		IFactorTable ab = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
		ab.setWeightForIndices(.6, 0, 0);
		ab.setWeightForIndices(.4, 0, 1);
		ab.setWeightForIndices(.2, 1, 0);
		ab.setWeightForIndices(.8, 1, 1);
		Factor abf = graph.addFactor(ab, a,b);
		abf.setName("ab");
		abf.setDirectedTo(b);

		IFactorTable ac = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
		ac.setWeightForIndices(.4, 0, 0);
		ac.setWeightForIndices(.6, 0, 1);
		ac.setWeightForIndices(.875, 1, 0);
		ac.setWeightForIndices(.125, 1, 1);
		Factor acf = graph.addFactor(ac, a,c);
		acf.setName("ac");
		acf.setDirectedTo(c);

		IFactorTable bcd = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit(), DiscreteDomain.bit());
		bcd.setWeightForIndices(.2, 0, 0, 0);
		bcd.setWeightForIndices(.8, 0, 0, 1);
		bcd.setWeightForIndices(.15, 0, 1, 0);
		bcd.setWeightForIndices(.85, 0, 1, 1);
		bcd.setWeightForIndices(.6, 1, 0, 0);
		bcd.setWeightForIndices(.4, 1, 0, 1);
		bcd.setWeightForIndices(.7, 1, 1, 0);
		bcd.setWeightForIndices(.3, 1, 1, 1);
		Factor bcdf = graph.addFactor(bcd, b,c,d);
		bcdf.setName("bcd");
		bcdf.setDirectedTo(d);

		for (int i = 10; --i>=0;)
		{
			JunctionTreeSolverGraph sgraph = graph.createSolver(new JunctionTreeSolver());
			sgraph.solve();
			assertArrayEquals(new double[] { .48, .52 }, d.getBelief(), 0.01);
		}
		

		testGraph(graph);
	}
	
	@Test
	public void testBug429A()
	{
		FactorGraph graph = new FactorGraph();
		Discrete a = new Bit(), b = new Bit(), c = new Bit(), d = new Bit(), e = new Bit(), f = new Bit();
		a.setName("a");
		b.setName("b");
		c.setName("c");
		d.setName("d");
		e.setName("e");
		f.setName("f");
		
		e.setInput(.1, .9);
		
		IFactorTable ea = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
		ea.setWeightForIndices(0.3, 0, 0);
		ea.setWeightForIndices(.7, 0, 1);
		ea.setWeightForIndices(.55, 1, 0);
		ea.setWeightForIndices(.45, 1, 1);
		Factor eaf = graph.addFactor(ea, e, a);
		eaf.setName("ea");
		eaf.setDirectedTo(a);
		
		IFactorTable ab = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
		ab.setWeightForIndices(.6, 0, 0);
		ab.setWeightForIndices(.4, 0, 1);
		ab.setWeightForIndices(.2, 1, 0);
		ab.setWeightForIndices(.8, 1, 1);
		Factor abf = graph.addFactor(ab, a, b);
		abf.setName("ab");
		abf.setDirectedTo(b);

		IFactorTable ac = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
		ac.setWeightForIndices(.4, 0, 0);
		ac.setWeightForIndices(.6, 0, 1);
		ac.setWeightForIndices(.875, 1, 0);
		ac.setWeightForIndices(.125, 1, 1);
		Factor acf = graph.addFactor(ab, a, c);
		acf.setName("ac");
		acf.setDirectedTo(c);
		
		IFactorTable bcd = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit(), DiscreteDomain.bit());
		bcd.setWeightForIndices(.2, 0, 0, 0);
		bcd.setWeightForIndices(.8, 0, 0, 1);
		bcd.setWeightForIndices(.15, 0, 1, 0);
		bcd.setWeightForIndices(.85, 0, 1, 1);
		bcd.setWeightForIndices(.6, 1, 0, 0);
		bcd.setWeightForIndices(.4, 1, 0, 1);
		bcd.setWeightForIndices(.7, 1, 1, 0);
		bcd.setWeightForIndices(.3, 1, 1, 1);
		Factor bcdf = graph.addFactor(bcd, b, c, d);
		bcdf.setName("bcd");
		bcdf.setDirectedTo(d);

		IFactorTable df = FactorTable.create(DiscreteDomain.bit(), DiscreteDomain.bit());
		df.setWeightForIndices(.15, 0, 0);
		df.setWeightForIndices(.85, 0, 1);
		df.setWeightForIndices(.1, 1, 0);
		df.setWeightForIndices(.9, 1, 1);
		Factor dff = graph.addFactor(df, d, f);
		dff.setName("df");
		dff.setDirectedTo(f);
		
		testGraph(graph);
	}

	
	/*------------------
	 *  Helper methods
	 */
	
	void testGraph(FactorGraph model)
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
		testGraphImpl(model, false);
		testGraphImpl(model, true);
	}
	
	private void testGraphImpl(FactorGraph model, boolean useMap)
	{
		testGraphImpl(model, useMap, false);
		
		// Choose a variable at random and give it a fixed value.
		final VariableList variables = model.getVariables();
		final int varIndex = _rand.nextInt(variables.size());
		final Discrete variable = variables.getByIndex(varIndex).asDiscreteVariable();
		final int valueIndex = _rand.nextInt(variable.getDomain().size());
		variable.asDiscreteVariable().setFixedValueIndex(valueIndex);
		
		testGraphImpl(model, useMap, false);
		
		testGraphImpl(model, useMap, true);
		
		// Clear fixed value
		variable.setInputObject(null);
	}
	
	private void testGraphImpl(FactorGraph model, boolean useMap, boolean useConditioning)
	{
		JunctionTreeSolverGraphBase<?> jtgraph =
			model.createSolver(useMap ? new JunctionTreeMAPSolver() : new JunctionTreeSolver());
		jtgraph.useConditioning(useConditioning);
		jtgraph.getTransformer().random(_rand); // set random generator so we can reproduce failures
		model.solve();
		
		FactorGraph transformedModel = requireNonNull(jtgraph.getDelegate()).getModelObject();
		RandomGraphGenerator.labelFactors(transformedModel);
		assertTrue(transformedModel.isForest());
		
		// Do solve again on a copy of the graph with all factors merged into single giant factor.
		final BiMap<Node,Node> old2new = HashBiMap.create();
		final BiMap<Node,Node> new2old = old2new.inverse();
		FactorGraph model2 = model.copyRoot(old2new);
		MapList<Factor> factors2 = model2.getFactors();
		Factor megafactor = null;
		if (factors2.size() > 0)
		{
			megafactor = model2.join(factors2.toArray(new Factor[factors2.size()]));
		}
		model2.setSolverFactory(jtgraph.getDelegateSolverFactory());
		model2.solve();
		
		// Compare marginal variable beliefs and scores
		for (Variable variable : model.getVariables())
		{
			final Variable variable2 = (Variable)old2new.get(variable);
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
			
			// Compare scores
			double score = variable.getScore();
			double score2 = variable2.getScore();
			assertEquals(score, score2, 1e-10);
			
			if (!useMap)
			{
				// Compare entropy
				double entropy = variable.getBetheEntropy();
				double entropy1 = variable2.getBetheEntropy();
				assertEquals(entropy, entropy1, 1e-10);

				// Compare internal energy
				double internalEnergy = variable.getInternalEnergy();
				double internalEnergy2 = variable2.getInternalEnergy();
				assertEquals(internalEnergy, internalEnergy2, 1e-10);
			}
		}
		
		// Compare factor beliefs
		if (!useMap && megafactor instanceof DiscreteFactor)
		{
			DiscreteFactor discreteMegafactor = (DiscreteFactor) megafactor;
			JointDomainIndexer fullDomains = discreteMegafactor.getDomainList();
			final double[] fullBeliefs = discreteMegafactor.getBelief();
			final int[][] fullBeliefIndices = discreteMegafactor.getPossibleBeliefIndices();
			final IFactorTable fullTable = FactorTable.create(fullDomains);
			fullTable.setWeightsSparse(fullBeliefIndices, fullBeliefs);

			final int nFullDomains = fullDomains.size();
			final int[] fullToMarginal = new int[nFullDomains];
			
			for (Factor factor : model.getFactors())
			{
				final DiscreteFactor discreteFactor = (DiscreteFactor)factor;
				JointDomainIndexer factorDomains = discreteFactor.getDomainList();
				final int nFactorDomains = factorDomains.size();
				
				final double[] beliefs = discreteFactor.getBelief();
				final int[][] beliefIndices = discreteFactor.getPossibleBeliefIndices();
				
				// Marginalize corresponding beliefs from full table.
				final List<? extends Variable> factorVars = discreteFactor.getSiblings();
				
				for (int from = 0, remove = nFactorDomains; from < nFullDomains; ++from)
				{
					final Variable fromVar = discreteMegafactor.getSibling(from);
					final int to = factorVars.indexOf(new2old.get(fromVar));
					fullToMarginal[from] = to >= 0 ? to : remove++;
				}
				
				final JointDomainReindexer marginalizer =
					JointDomainReindexer.createPermuter(fullDomains, factorDomains, fullToMarginal);
		
				final IFactorTable beliefTable2 = fullTable.convert(marginalizer);
				// Set direction to match original factor table so that sparse indices will be in same order.
				beliefTable2.setDirected(discreteFactor.getFactorTable().getOutputSet());
				double[] beliefs2 = beliefTable2.getWeightsSparseUnsafe();
				int[][] beliefIndices2 = beliefTable2.getIndicesSparseUnsafe();
				
				// BUG 27 can result in beliefs that are close to but not equal to zero so we have
				// to filter out beliefs that are close to zero.
				int i = 0, j = 0;
				while (true)
				{
					while (i < beliefs.length && DoubleMath.fuzzyEquals(beliefs[i], 0.0, 1e-15))
					{
						++i;
					}
					
					while (j < beliefs2.length && DoubleMath.fuzzyEquals(beliefs2[j], 0.0, 1e-15))
					{
						++j;
					}
					
					if (i >= beliefs.length || j >= beliefs2.length)
					{
						break;
					}
					
					assertEquals(beliefs[i], beliefs2[j], 1e-12);
					assertArrayEquals(beliefIndices[i], beliefIndices2[j]);
					++i;
					++j;
				}
				
				assertEquals(beliefs.length, i);
				assertEquals(beliefs2.length, j);
			}
		}
		
		// Compare scores for two versions with same guesses
		for (int i = 0; i < 10; ++i)
		{
			// Randomly set guesses
			for (Map.Entry<Node,Node> entry : old2new.entrySet())
			{
				Node node = entry.getKey();
				if (node instanceof Discrete)
				{
					Discrete var = (Discrete)node;
					Discrete var2 = (Discrete)entry.getValue();
					if (!var.hasFixedValue())
					{
						int guessIndex = _rand.nextInt(var.getDomain().size());
						var.setGuessIndex(guessIndex);
						var2.setGuessIndex(guessIndex);
						assertEquals(var.getScore(), var2.getScore(), 1e-14);
					}
				}
			}
			
			double score = model.getScore();
			double score2 = model2.getScore();
			assertEquals(score, score2, 1e-10);
		}
			
		if (!useMap)
		{
			double internalEnergy = model.getInternalEnergy();
			double internalEnergy2 = model2.getInternalEnergy();
			assertEquals(internalEnergy, internalEnergy2, 1e-10);

			// The entropy and free energy depend on the factorization, and thus cannot be compared vs. the
			// joint factor and cannot be easily compared with original model either because it depends on
			// the beliefs....
		}
	}
}
