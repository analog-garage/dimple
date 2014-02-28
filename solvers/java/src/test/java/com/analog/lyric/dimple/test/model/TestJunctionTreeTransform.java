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

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.junit.Test;

import cern.colt.list.IntArrayList;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.transform.FactorGraphTransformMap;
import com.analog.lyric.dimple.model.transform.FactorGraphTransformMap.AddedDeterministicVariable;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransform;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;
import com.analog.lyric.util.misc.Misc;

/**
 * Tests for {@link JunctionTreeTransform}
 */
public class TestJunctionTreeTransform
{
	private final Random rand = new Random(617);
	
	private static final DiscreteDomain d2 = DiscreteDomain.range(0, 1);
	private static final DiscreteDomain d3 = DiscreteDomain.range(0, 2);
	private static final DiscreteDomain d4 = DiscreteDomain.range(0, 3);
	
	@Test
	public void testTrivialLoop()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newDiscrete(2, "a");
		Discrete b = newDiscrete(2, "b");
		addClique(model, a, b);
		addClique(model, a, b);
		testGraph(model);
	}
	
	@Test
	public void testTriangle()
	{
		FactorGraph model = new FactorGraph();
		Discrete a = newDiscrete(2, "a");
		Discrete b = newDiscrete(2, "b");
		Discrete c = newDiscrete(2, "c");
		addClique(model, a, b);
		addClique(model, b, c);
		addClique(model, c, a);
		testGraph(model);
		
	}
	
	@Test
	public void testGrid2()
	{
		testGraph(buildGrid(2, d2));
	}
	
	@Test
	public void testGrid3()
	{
		testGraph(buildGrid(3, d2, d3, d4));
	}
	
	@Test
	public void testGrid4()
	{
		testGraph(buildGrid(4, d2));
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
	 * Numbers in brackets indicate the variable cardinality.
	 */
	@Test
	public void testStudentNetwork()
	{
		FactorGraph model = new FactorGraph();
		Discrete c = newDiscrete(3, "c");
		Discrete d = newDiscrete(3, "d");
		addClique(model, d, c);
		Discrete i = newDiscrete(3, "i");
		Discrete g = newDiscrete(5, "g");
		Discrete s = newDiscrete(10, "s");
		addClique(model, g, d, i);
		addClique(model, s, i);
		Discrete l = newDiscrete(2, "l");
		addClique(model, l, g);
		Discrete j = newDiscrete(2, "j");
		addClique(model, j, l, s);
		Discrete h = newDiscrete(2, "h");
		addClique(model, h, g, j);
		
		testGraph(model);
	}
	
	/*-----------------
	 * Helper methods
	 */
	
	/**
	 * Assert that source and target graphs in {@code transformMap} represent the same
	 * joint distribution down to some level of precision.
	 * 
	 * @param transformMap
	 */
	private void assertModelsEquivalent(FactorGraphTransformMap transformMap)
	{
		final FactorGraph source = transformMap.source();
		final FactorGraph target = transformMap.target();
		
		GibbsSolver gibbs = new GibbsSolver();
		SFactorGraph sourceGibbs = source.setSolverFactory(gibbs);
		SFactorGraph targetGibbs = target.setSolverFactory(gibbs);
		
		final int nSamples = 100;
		
		final double[] differences = new double[nSamples];
		for (int n = 0; n < nSamples; ++n)
		{
			// Generate a sample on the source graph
			source.solve();
			
			// Copy sample values to new graph
			for (VariableBase sourceVar : source.getVariables())
			{
				VariableBase targetVar = (VariableBase) transformMap.sourceToTarget().get(sourceVar);
				
				ISolverVariableGibbs sourceSVar = sourceGibbs.getSolverVariable(sourceVar);
				ISolverVariableGibbs targetSVar = targetGibbs.getSolverVariable(targetVar);

				targetSVar.setCurrentSample(sourceSVar.getCurrentSampleValue());
			}
			
			// Update values of added variables
			for (AddedDeterministicVariable added : transformMap.addedDeterministicVariables())
			{
				final ISolverVariableGibbs addedSVar = targetGibbs.getSolverVariable(added.getVariable());
				final Value value = addedSVar.getCurrentSampleValue();
				final Value[] inputs = new Value[added.getInputCount()];
				for (int i = inputs.length; --i>=0;)
				{
					final VariableBase inputVar = added.getInput(i);
					final ISolverVariableGibbs inputSVar = targetGibbs.getSolverVariable(inputVar);
					inputs[i] = inputSVar.getCurrentSampleValue();
				}
				
				added.updateValue(value, inputs);
			}
			
			// Compare the joint likelihoods
			final double sourcePotential = sourceGibbs.getTotalPotential();
			final double targetPotential = targetGibbs.getTotalPotential();
			final double difference = sourcePotential - targetPotential;
			if (Math.abs(difference) > 1e-10)
			{
				Misc.breakpoint();
			}
			differences[n] = difference;
		}
		
		double variance = new Variance().evaluate(differences);
		assertEquals(0.0, variance, 1e-10);
	}
	private FactorGraph buildGrid(int n, DiscreteDomain ... domains)
	{
		final FactorGraph graph = new FactorGraph();
		
		final Discrete[][] vars = new Discrete[n][n];
		for (int i = 0; i < n; ++i)
		{
			for (int j = 0; j < n; ++j)
			{
				Discrete var = new Discrete(chooseDomain(domains));
				var.setName(String.format("%s%d", intToBase26(i), j));
				vars[i][j] = var;
				
				if (i > 0)
				{
					Discrete prev = vars[i-1][j];
					addClique(graph, prev, var);
				}
				if (j > 0)
				{
					Discrete prev = vars[i][j-1];
					addClique(graph, prev, var);
				}
			}
		}
		
		return graph;
	}
	
	private DiscreteDomain chooseDomain(DiscreteDomain ... domains)
	{
		if (domains.length == 1)
		{
			return domains[0];
		}
		
		return domains[rand.nextInt(domains.length)];
	}
	
	private void addClique(FactorGraph model, Discrete ... variables)
	{
		final Factor factor = model.addFactor(randomTable(variables), variables);
		labelFactor(factor);
	}
	
	private static String intToBase26(int i)
	{
		IntArrayList digits = new IntArrayList();
		for (long l = i & 0xFFFFFFFFL; true; l /= 26)
		{
			digits.add((int)(l % 26));
			if (l < 26)
				break;
		}
		
		StringBuilder sb = new StringBuilder();
		for (int j = digits.size(); --j>=0;)
		{
			sb.append((char)('a' + digits.get(j)));
			// FIXME
		}
		return sb.toString();
	}
	
	/**
	 * Give factor a label of the form f(<i>variables</i>) if it doesn't already have a name.
	 */
	private static void labelFactor(Factor factor)
	{
		if (factor.getExplicitName() == null)
		{
			StringBuffer name = new StringBuffer("f(");
			for (int i = 0, end = factor.getSiblingCount(); i<end; ++i)
			{
				if (i > 0)
					name.append(",");
				name.append(factor.getSibling(i).getLabel());
			}
			name.append(")");
			factor.setLabel(name.toString());
		}
	}

	private Discrete newDiscrete(int cardinality, String name)
	{
		Discrete var = new Discrete(DiscreteDomain.range(1, cardinality));
		var.setName(name);
		return var;
	}

	private IFactorTable randomTable(Discrete ... variables)
	{
		DiscreteDomain[] domains = new DiscreteDomain[variables.length];
		for (int i = variables.length; --i>=0;)
		{
			domains[i] = variables[i].getDomain();
		}
		return randomTable(domains);
	}
	
	private IFactorTable randomTable(DiscreteDomain ... domains)
	{
		IFactorTable table = FactorTable.create(domains);
		table.setRepresentation(FactorTableRepresentation.DENSE_ENERGY);
		table.randomizeWeights(rand);
		return table;
	}
	
	private void testGraph(FactorGraph model)
	{
		JunctionTreeTransform jt = new JunctionTreeTransform().random(rand);
		FactorGraphTransformMap transformMap = jt.transform(model);
		for (Factor factor : transformMap.target().getFactors())
		{
			// Name target factors as a debugging aid
			labelFactor(factor);
		}
		assertTrue(transformMap.target().isTree());
		assertModelsEquivalent(transformMap);
	}
	
}
