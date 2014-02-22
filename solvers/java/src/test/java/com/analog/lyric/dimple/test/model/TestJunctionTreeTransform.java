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

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.transform.FactorGraphTransformMap;
import com.analog.lyric.dimple.model.transform.FactorGraphTransformMap.AddedDeterministicVariable;
import com.analog.lyric.dimple.model.transform.JunctionTreeTransform;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;

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
	
	@Test
	public void testGrid10()
	{
		testGraph(buildGrid(10, d2));
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
				
				ISolverVariableGibbs sourceSVar = (ISolverVariableGibbs) sourceVar.getSolver();
				ISolverVariableGibbs targetSVar = (ISolverVariableGibbs) targetVar.getSolver();

				targetSVar.setCurrentSample(sourceSVar.getCurrentSampleValue());
			}
			
			// Update values of added variables
			for (AddedDeterministicVariable added : transformMap.addedDeterministicVariables())
			{
				final ISolverVariableGibbs addedSVar = (ISolverVariableGibbs)added.getVariable().getSolver();
				final Value value = addedSVar.getCurrentSampleValue();
				final Value[] inputs = new Value[added.getInputCount()];
				for (int i = inputs.length; --i>=0;)
				{
					final VariableBase inputVar = added.getInput(i);
					final ISolverVariableGibbs inputSVar = (ISolverVariableGibbs) inputVar.getSolver();
					inputs[i] = inputSVar.getCurrentSampleValue();
				}
				
				added.updateValue(value, inputs);
			}
			
			// Compare the joint likelihoods
			final double sourcePotential = sourceGibbs.getTotalPotential();
			final double targetPotential = targetGibbs.getTotalPotential();
			differences[n] = sourcePotential - targetPotential;
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
				var.setName(String.format("v[%d,%d]", i, j));
				vars[i][j] = var;
				
				if (i > 0)
				{
					Discrete prev = vars[i-1][j];
					graph.addFactor(randomTable(prev.getDomain(), var.getDomain()), prev, var);
				}
				if (j > 0)
				{
					Discrete prev = vars[i][j-1];
					graph.addFactor(randomTable(prev.getDomain(), var.getDomain()), prev, var);
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
		assertTrue(transformMap.target().isTree());
		assertModelsEquivalent(transformMap);
	}
	
}
