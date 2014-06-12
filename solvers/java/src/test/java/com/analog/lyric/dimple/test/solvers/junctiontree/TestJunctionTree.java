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
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.domains.JointDomainReindexer;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolverGraphBase;
import com.analog.lyric.dimple.solvers.junctiontreemap.JunctionTreeMAPSolver;
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
public class TestJunctionTree
{
	private final long _seed = new Random().nextLong();
	private final Random _rand = new Random(_seed);
	private final RandomGraphGenerator _graphGenerator = new RandomGraphGenerator(_rand);
	
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
		Factor factor2 = null;
		if (factors2.size() > 0)
		{
			factor2 = model2.join(factors2.toArray(new Factor[factors2.size()]));
		}
		model2.setSolverFactory(jtgraph.getDelegateSolverFactory());
		model2.solve();
		
		// Compare marginal variable beliefs and scores
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
		if (!useMap && factor2 instanceof DiscreteFactor)
		{
			DiscreteFactor discreteFactor2 = (DiscreteFactor) factor2;
			JointDomainIndexer fullDomains = discreteFactor2.getDomainList();
			final double[] fullBeliefs = discreteFactor2.getBelief();
			final int[][] fullBeliefIndices = discreteFactor2.getPossibleBeliefIndices();
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
				final List<? extends VariableBase> factorVars = discreteFactor.getSiblings();
				
				for (int from = 0, remove = nFactorDomains; from < nFullDomains; ++from)
				{
					final VariableBase fromVar = discreteFactor2.getSibling(from);
					final int to = factorVars.indexOf(new2old.get(fromVar));
					fullToMarginal[from] = to >= 0 ? to : remove++;
				}
				
				final JointDomainReindexer marginalizer =
					JointDomainReindexer.createPermuter(fullDomains, factorDomains, fullToMarginal);
		
				final IFactorTable beliefTable2 = fullTable.convert(marginalizer);
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
		
		double score = model.getScore();
		double score2 = model2.getScore();
		assertEquals(score, score2, 1e-10);

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
