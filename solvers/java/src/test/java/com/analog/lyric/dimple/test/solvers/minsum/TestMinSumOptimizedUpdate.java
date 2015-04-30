/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.test.solvers.minsum;

import static com.analog.lyric.dimple.test.solvers.sumproduct.TestSumProductOptimizedUpdate.*;
import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.FloodingScheduler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolverGraph;
import com.analog.lyric.dimple.solvers.minsum.MinSumTableFactor;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.solvers.sumproduct.TestSumProductOptimizedUpdate.Graph;
import com.analog.lyric.dimple.test.solvers.sumproduct.TestSumProductOptimizedUpdate.Graph2;
import com.analog.lyric.options.IOptionHolder;

/**
 * @since 0.07
 * @author jking
 */
public class TestMinSumOptimizedUpdate extends DimpleTestBase
{
	static final UpdateApproach defaultApproach = UpdateApproach.AUTOMATIC;
	static final double defaultAllocationScale = 10.0;
	static final double defaultExecutionTimeScale = 1.0;
	static final double defaultSparseThreshold = 1.0;
	
	private static MinSumSolverGraph getMinSumSolverGraph(FactorGraph fg)
	{
		MinSumSolverGraph sfg = (MinSumSolverGraph) fg.getSolver();
		assertNotNull(sfg);
		return sfg;
	}
	
	private static MinSumTableFactor getMinSumFactorTable(Factor f)
	{
		MinSumTableFactor sft = (MinSumTableFactor) f.getSolver();
		assertNotNull(sft);
		return sft;
	}
	
	private void checkDefaults(IOptionHolder optionHolder)
	{
		assertEquals(defaultApproach, optionHolder.getOptionOrDefault(BPOptions.updateApproach));
		assertEquals(defaultAllocationScale, optionHolder.getOptionOrDefault(BPOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(defaultExecutionTimeScale, optionHolder.getOptionOrDefault(BPOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(defaultSparseThreshold, optionHolder.getOptionOrDefault(BPOptions.optimizedUpdateSparseThreshold), 1.0e-9);
	}

	/**
	 * Verify that the solver graph has the correct default property values.
	 * 
	 * @since 0.07
	 */
	@Test
	public void testGraphPropertiesDefaults()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new MinSumSolver());
		MinSumSolverGraph sfg = getMinSumSolverGraph(fg);
		checkDefaults(sfg);
	}
	
	/**
	 * Verify that the solver factor has the correct default property values.
	 * 
	 * @since 0.07
	 */
	@Test
	public void testFactorPropertiesDefaults()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new MinSumSolver());
		Factor f = add2BitFactor(new Random(), fg);
		MinSumTableFactor sft = getMinSumFactorTable(f);
		checkDefaults(sft);
	}

	/**
	 * Verify that properties are properly inherited by factors from their graph.
	 * 
	 * @since 0.07
	 */
	@Test
	public void testPropertyInheritance()
	{
		Random rand = new Random();
		rand.setSeed(0); // Don't be random
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new MinSumSolver());
		MinSumSolverGraph sfg = getMinSumSolverGraph(fg);
		Factor f = add2BitFactor(rand, fg);
		MinSumTableFactor sft = getMinSumFactorTable(f);
		// Make sure the factor has the default values initially
		assertEquals(defaultApproach, sft.getOptionOrDefault(BPOptions.updateApproach));
		assertEquals(defaultAllocationScale, sft.getOptionOrDefault(BPOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(defaultExecutionTimeScale, sft.getOptionOrDefault(BPOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(defaultSparseThreshold, sft.getOptionOrDefault(BPOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// Set the properties at the graph to non-default values
		final UpdateApproach graphApproach = UpdateApproach.OPTIMIZED;
		final double graphAllocationScale = 2.0;
		final double graphExecutionTimeScale = 50.0;
		final double graphSparseThreshold = 0.6;
		sfg.setOption(BPOptions.updateApproach, graphApproach);
		sfg.setOption(BPOptions.automaticMemoryAllocationScalingFactor, graphAllocationScale);
		sfg.setOption(BPOptions.automaticExecutionTimeScalingFactor, graphExecutionTimeScale);
		sfg.setOption(BPOptions.optimizedUpdateSparseThreshold, graphSparseThreshold);
		// Check that the factor returns the values programmed on the graph
		assertEquals(graphApproach, sft.getOptionOrDefault(BPOptions.updateApproach));
		assertEquals(graphAllocationScale, sft.getOptionOrDefault(BPOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(graphExecutionTimeScale, sft.getOptionOrDefault(BPOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(graphSparseThreshold, sft.getOptionOrDefault(BPOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// Set the properties at the factor to yet different values
		final UpdateApproach factorApproach = UpdateApproach.AUTOMATIC;
		final double factorAllocationScale = 3.0;
		final double factorExecutionTimeScale = 60.0;
		final double factorSparseThreshold = 0.7;
		sft.setOption(BPOptions.updateApproach, factorApproach);
		sft.setOption(BPOptions.automaticMemoryAllocationScalingFactor, factorAllocationScale);
		sft.setOption(BPOptions.automaticExecutionTimeScalingFactor, factorExecutionTimeScale);
		sft.setOption(BPOptions.optimizedUpdateSparseThreshold, factorSparseThreshold);
		// Check that the factor returns the values programmed on the factor
		assertEquals(factorApproach, sft.getOptionOrDefault(BPOptions.updateApproach));
		assertEquals(factorAllocationScale, sft.getOptionOrDefault(BPOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(factorExecutionTimeScale, sft.getOptionOrDefault(BPOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(factorSparseThreshold, sft.getOptionOrDefault(BPOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// And that the graph returns its own values still
		assertEquals(graphApproach, sfg.getOptionOrDefault(BPOptions.updateApproach));
		assertEquals(graphAllocationScale, sfg.getOptionOrDefault(BPOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(graphExecutionTimeScale, sfg.getOptionOrDefault(BPOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(graphSparseThreshold, sfg.getOptionOrDefault(BPOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// "Unset" the factor properties
		sft.unsetOption(BPOptions.updateApproach);
		sft.unsetOption(BPOptions.automaticMemoryAllocationScalingFactor);
		sft.unsetOption(BPOptions.automaticExecutionTimeScalingFactor);
		sft.unsetOption(BPOptions.optimizedUpdateSparseThreshold);
		// And check that the factor again returns the graph values
		assertEquals(graphApproach, sft.getOptionOrDefault(BPOptions.updateApproach));
		assertEquals(graphAllocationScale, sft.getOptionOrDefault(BPOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(graphExecutionTimeScale, sft.getOptionOrDefault(BPOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(graphSparseThreshold, sft.getOptionOrDefault(BPOptions.optimizedUpdateSparseThreshold), 1.0e-9);
	}

	public static void runSolver(FactorGraph fg,
		final double sparsityControl,
		final double damping,
		final boolean useMultithreading)
	{
		ISolverFactorGraph solver = fg.getSolver();
		if (solver != null)
		{
			MinSumSolverGraph ssolver = (MinSumSolverGraph) solver;

			solver.useMultithreading(true);
			ssolver.setOption(BPOptions.updateApproach, UpdateApproach.NORMAL);
			ssolver.setDamping(damping);
			fg.initialize();
			ssolver.iterate(5);
			List<Object> normalBeliefs = getBeliefs(fg);
			
			solver.useMultithreading(useMultithreading);
			ssolver.setOption(BPOptions.optimizedUpdateSparseThreshold, sparsityControl);
			ssolver.setDamping(damping);
			ssolver.setOption(BPOptions.updateApproach, UpdateApproach.OPTIMIZED);
			fg.initialize();
			ssolver.iterate(5);
			List<Object> optimizedBeliefs = getBeliefs(fg);

			assertEqualListDoubleArray(normalBeliefs, optimizedBeliefs);
		}
		else
		{
			fail("solver was null");
		}
	}

	private void doTest(final int zeroControl,
		final double sparsityControl,
		final double damping,
		final boolean useMultithreading)
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new MinSumSolver());
		@SuppressWarnings("unused")
		Graph g = new Graph(fg, zeroControl);
		runSolver(fg, sparsityControl, damping, useMultithreading);
	}

	@Test
	public void testSparse()
	{
		final int zeroControl = 2000;
		final double sparsityControl = 0.9;
		final double damping = 0.9;
		final boolean useMultithreading = false;
		doTest(zeroControl, sparsityControl, damping, useMultithreading);
	}

	@Test
	public void testSparseMultithreaded()
	{
		final int zeroControl = 2000;
		final double sparsityControl = 0.9;
		final double damping = 0.0;
		final boolean useMultithreading = true;
		doTest(zeroControl, sparsityControl, damping, useMultithreading);
	}

	@Test
	public void testVerySparse()
	{
		final int zeroControl = -10;
		final double sparsityControl = 1.0;
		final double damping = 0.9;
		final boolean useMultithreading = false;
		doTest(zeroControl, sparsityControl, damping, useMultithreading);
	}

	@Test
	public void testDense()
	{
		final int zeroControl = 0;
		final double sparsityControl = 1.0;
		final double damping = 0.0;
		final boolean useMultithreading = false;
		doTest(zeroControl, sparsityControl, damping, useMultithreading);
	}

	@Test
	public void testDenseMultithreaded()
	{
		final int zeroControl = 0;
		final double sparsityControl = 1.0;
		final double damping = 0.0;
		final boolean useMultithreading = true;
		doTest(zeroControl, sparsityControl, damping, useMultithreading);
	}

	/**
	 * @since 0.07
	 */
	private void automaticHelper(final double density, final boolean multithreaded)
	{
		Random rand = new Random();
		rand.setSeed(0); // Don't be random
		final FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new MinSumSolver());
		DiscreteDomain[] domains = new DiscreteDomain[5];
		final int N = 5;
		Discrete[] vars = new Discrete[N];
		for (int i = 0; i < domains.length; i++)
		{
			domains[i] = DiscreteDomain.range(1, Math.abs(2 - i) + 2);
			vars[i] = new Discrete(domains[i]);
		}
		IFactorTable table = FactorTable.create(domains);
		table.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		int[] coordinates = null;
		final JointDomainIndexer domainIndexer = table.getDomainIndexer();
		final int d = (int) (domainIndexer.getCardinality() * density);
		for (int i = 0; i < d; i++)
		{
			coordinates = domainIndexer.randomIndices(rand, coordinates);
			table.setWeightForIndices(rand.nextDouble(), coordinates);
		}
		fg.addFactor(table, vars);
		MinSumSolverGraph sfg = (MinSumSolverGraph) fg.getSolver();
		if (sfg == null)
		{
			fail("MinSumSolverGraph is null");
			return;
		}
		sfg.setOption(BPOptions.updateApproach, UpdateApproach.AUTOMATIC);
		sfg.useMultithreading(multithreaded);
		// Before optimize, all factors should not have their optimize enable explicitly set
		for (Factor factor : fg.getFactors())
		{
			MinSumTableFactor sft = (MinSumTableFactor) factor.getSolver();
			if (sft != null)
			{
				UpdateApproach automaticUpdateApproach = sft.getAutomaticUpdateApproach();
				assertNull(automaticUpdateApproach);
			}
		}
		sfg.initialize();
		// Afterward, they should
		for (Factor factor : fg.getFactors())
		{
			MinSumTableFactor sft = (MinSumTableFactor) factor.getSolver();
			if (sft != null)
			{
				UpdateApproach automaticUpdateApproach = sft.getAutomaticUpdateApproach();
				assertNotNull(automaticUpdateApproach);
			}
		}
	}

	/**
	 * @since 0.07
	 */
	@Test
	public void testAutomaticSparse()
	{
		final boolean multithreaded = false;
		final double density = 0.05;
		automaticHelper(density, multithreaded);
	}

	/**
	 * @since 0.07
	 */
	@Test
	public void testAutomaticDense()
	{
		final boolean multithreaded = false;
		final double density = 1.0;
		automaticHelper(density, multithreaded);
	}

	/**
	 * @since 0.07
	 */
	@Test
	public void testAutomaticMultithreaded()
	{
		final double density = 1.0;
		final boolean multithreaded = true;
		automaticHelper(density, multithreaded);
	}

	private void doTest2(final double sparsityControl,
		final double damping,
		final boolean useMultithreading,
		final Random rnd)
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new MinSumSolver());
		fg.setOption(BPOptions.scheduler, new FloodingScheduler());
		@SuppressWarnings("unused")
		Graph2 g = new Graph2(fg, rnd);
		runSolver(fg, sparsityControl, damping, useMultithreading);
	}
	
	@Test
	public void TestMixedFactorTablesDense()
	{
		final double sparsityControl = 0.2;
		final double damping = 0.0;
		final boolean useMultithreading = false;
		Random rnd = new Random();
		rnd.setSeed(0); // Don't be random
		doTest2(sparsityControl, damping, useMultithreading, rnd);
		doTest2(sparsityControl, damping, useMultithreading, rnd);
		doTest2(sparsityControl, damping, useMultithreading, rnd);
	}
}
