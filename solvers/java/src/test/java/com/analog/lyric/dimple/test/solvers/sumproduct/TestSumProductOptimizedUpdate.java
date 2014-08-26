/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.test.solvers.sumproduct;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.FactorTableRepresentation;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.JointDomainIndexer;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Bit;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateOptions;
import com.analog.lyric.dimple.solvers.sumproduct.SFactorGraph;
import com.analog.lyric.dimple.solvers.sumproduct.STableFactor;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.google.common.primitives.Ints;

/**
 * @since 0.06
 * @author jking
 */
public class TestSumProductOptimizedUpdate extends DimpleTestBase
{
	static final UpdateApproach defaultApproach = UpdateApproach.UPDATE_APPROACH_NORMAL;
	static final double defaultAllocationScale = 1.0;
	static final double defaultExecutionTimeScale = 100.0;
	static final double defaultSparseThreshold = 1.0;

	/**
	 * Verify that the solver graph has the correct default property values.
	 * 
	 * @since 0.07
	 */
	@Test
	public void testGraphPropertiesDefaults()
	{
		FactorGraph fg = new FactorGraph();
		SFactorGraph sfg = getSFactorGraph(fg);
		assertEquals(defaultApproach, sfg.getOptionOrDefault(UpdateOptions.updateApproach));
		assertEquals(defaultAllocationScale, sfg.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(defaultExecutionTimeScale, sfg.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(defaultSparseThreshold, sfg.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold), 1.0e-9);
	}

	private static SFactorGraph getSFactorGraph(FactorGraph fg)
	{
		SFactorGraph sfg = (SFactorGraph) fg.getSolver();
		assertNotNull(sfg);
		return sfg;
	}

	/**
	 * Verify that the solver factor has the correct default property values.
	 * 
	 * @since 0.07
	 */
	@Test
	public void testFactorPropertiesDefaults()
	{
		Random rand = new Random();
		rand.setSeed(0); // Don't be random
		FactorGraph fg = new FactorGraph();
		Factor f = define2bitFactor(rand, fg);
		STableFactor sft = getSFactorTable(f);
		assertEquals(defaultApproach, sft.getOptionOrDefault(UpdateOptions.updateApproach));
		assertEquals(defaultAllocationScale, sft.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(defaultExecutionTimeScale, sft.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(defaultSparseThreshold, sft.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold), 1.0e-9);
	}

	private static STableFactor getSFactorTable(Factor f)
	{
		STableFactor sft = (STableFactor) f.getSolver();
		assertNotNull(sft);
		return sft;
	}

	private static Factor define2bitFactor(Random rand, FactorGraph fg)
	{
		Bit[] vars = define2bits();
		IFactorTable table = FactorTable.create(vars[0].getDomain(), vars[1].getDomain());
		table.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		final JointDomainIndexer domainIndexer = table.getDomainIndexer();
		final int d = (domainIndexer.getCardinality());
		for (int i = 0; i < d; i++)
		{
			table.setWeightForJointIndex(rand.nextDouble(), i);
		}
		Factor f = fg.addFactor(table, vars);
		return f;
	}

	private static Bit[] define2bits()
	{
		Bit[] vars = new Bit[2];
		for (int i = 0; i < 2; i++)
		{
			vars[i] = new Bit();
		}
		return vars;
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
		SFactorGraph sfg = getSFactorGraph(fg);
		Factor f = define2bitFactor(rand, fg);
		STableFactor sft = getSFactorTable(f);
		// Make sure the factor has the default values initially
		assertEquals(defaultApproach, sft.getOptionOrDefault(UpdateOptions.updateApproach));
		assertEquals(defaultAllocationScale, sft.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(defaultExecutionTimeScale, sft.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(defaultSparseThreshold, sft.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// Set the properties at the graph to non-default values
		final UpdateApproach graphApproach = UpdateApproach.UPDATE_APPROACH_OPTIMIZED;
		final double graphAllocationScale = 2.0;
		final double graphExecutionTimeScale = 50.0;
		final double graphSparseThreshold = 0.6;
		sfg.setOption(UpdateOptions.updateApproach, graphApproach);
		sfg.setOption(UpdateOptions.automaticMemoryAllocationScalingFactor, graphAllocationScale);
		sfg.setOption(UpdateOptions.automaticExecutionTimeScalingFactor, graphExecutionTimeScale);
		sfg.setOption(UpdateOptions.optimizedUpdateSparseThreshold, graphSparseThreshold);
		// Check that the factor returns the values programmed on the graph
		assertEquals(graphApproach, sft.getOptionOrDefault(UpdateOptions.updateApproach));
		assertEquals(graphAllocationScale, sft.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(graphExecutionTimeScale, sft.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(graphSparseThreshold, sft.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// Set the properties at the factor to yet different values
		final UpdateApproach factorApproach = UpdateApproach.UPDATE_APPROACH_AUTOMATIC;
		final double factorAllocationScale = 3.0;
		final double factorExecutionTimeScale = 60.0;
		final double factorSparseThreshold = 0.7;
		sft.setOption(UpdateOptions.updateApproach, factorApproach);
		sft.setOption(UpdateOptions.automaticMemoryAllocationScalingFactor, factorAllocationScale);
		sft.setOption(UpdateOptions.automaticExecutionTimeScalingFactor, factorExecutionTimeScale);
		sft.setOption(UpdateOptions.optimizedUpdateSparseThreshold, factorSparseThreshold);
		// Check that the factor returns the values programmed on the factor
		assertEquals(factorApproach, sft.getOptionOrDefault(UpdateOptions.updateApproach));
		assertEquals(factorAllocationScale, sft.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(factorExecutionTimeScale, sft.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(factorSparseThreshold, sft.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// And that the graph returns its own values still
		assertEquals(graphApproach, sfg.getOptionOrDefault(UpdateOptions.updateApproach));
		assertEquals(graphAllocationScale, sfg.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(graphExecutionTimeScale, sfg.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(graphSparseThreshold, sfg.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold), 1.0e-9);
		// "Unset" the factor properties
		sft.unsetOption(UpdateOptions.updateApproach);
		sft.unsetOption(UpdateOptions.automaticMemoryAllocationScalingFactor);
		sft.unsetOption(UpdateOptions.automaticExecutionTimeScalingFactor);
		sft.unsetOption(UpdateOptions.optimizedUpdateSparseThreshold);
		// And check that the factor again returns the graph values
		assertEquals(graphApproach, sft.getOptionOrDefault(UpdateOptions.updateApproach));
		assertEquals(graphAllocationScale, sft.getOptionOrDefault(UpdateOptions.automaticMemoryAllocationScalingFactor), 1.0e-9);
		assertEquals(graphExecutionTimeScale, sft.getOptionOrDefault(UpdateOptions.automaticExecutionTimeScalingFactor), 1.0e-9);
		assertEquals(graphSparseThreshold, sft.getOptionOrDefault(UpdateOptions.optimizedUpdateSparseThreshold), 1.0e-9);
	}

	private void doTest(final int zeroControl,
		final double sparsityControl,
		final double damping,
		final boolean useMultithreading,
		final int EXPECTED_HASH)
	{
		FactorGraph fg = new FactorGraph();
		Graph g = new Graph(fg, zeroControl);
		ISolverFactorGraph solver = fg.getSolver();
		if (solver != null)
		{
			solver.useMultithreading(useMultithreading);
			SFactorGraph ssolver = (SFactorGraph) solver;
			ssolver.setOption(UpdateOptions.optimizedUpdateSparseThreshold, sparsityControl);
			ssolver.setDamping(damping);
			ssolver.setOption(UpdateOptions.updateApproach, UpdateApproach.UPDATE_APPROACH_OPTIMIZED);
			fg.solve();
			int[][] values = g.getValues();
			int[] flat = Ints.concat(values);
			int hash = Arrays.hashCode(flat);
			assertEquals(EXPECTED_HASH, hash);
		}
		else
		{
			fail("solver was null");
		}
	}

	@Test
	public void testSparse()
	{
		final int zeroControl = 2000;
		final double sparsityControl = 0.9;
		final double damping = 0.9;
		final boolean useMultithreading = false;
		final int EXPECTED_HASH = -1986825801;
		doTest(zeroControl, sparsityControl, damping, useMultithreading, EXPECTED_HASH);
	}

	@Test
	public void testSparseMultithreaded()
	{
		final int zeroControl = 2000;
		final double sparsityControl = 0.9;
		final double damping = 0.0;
		final boolean useMultithreading = true;
		final int EXPECTED_HASH = -546108325;
		doTest(zeroControl, sparsityControl, damping, useMultithreading, EXPECTED_HASH);
	}

	@Test
	public void testVerySparse()
	{
		final int zeroControl = -10;
		final double sparsityControl = 1.0;
		final double damping = 0.9;
		final boolean useMultithreading = false;
		final int EXPECTED_HASH = 1898074408;
		doTest(zeroControl, sparsityControl, damping, useMultithreading, EXPECTED_HASH);
	}

	@Test
	public void testDense()
	{
		final int zeroControl = 0;
		final double sparsityControl = 1.0;
		final double damping = 0.0;
		final boolean useMultithreading = false;
		final int EXPECTED_HASH = -1682890428;
		doTest(zeroControl, sparsityControl, damping, useMultithreading, EXPECTED_HASH);
	}

	@Test
	public void testDenseMultithreaded()
	{
		final int zeroControl = 0;
		final double sparsityControl = 1.0;
		final double damping = 0.0;
		final boolean useMultithreading = true;
		final int EXPECTED_HASH = -1682890428;
		doTest(zeroControl, sparsityControl, damping, useMultithreading, EXPECTED_HASH);
	}

	/**
	 * This test creates a factor connected to several discrete variables whose domain sizes vary.
	 * 
	 * @since 0.06
	 */
	@Test
	public void testMixedDomainSizes()
	{
		Random rand = new Random();
		rand.setSeed(0); // Don't be random
		final FactorGraph fg = new FactorGraph();
		DiscreteDomain[] domains = new DiscreteDomain[5];
		Discrete[] vars = new Discrete[5];
		for (int i = 0; i < domains.length; i++)
		{
			domains[i] = DiscreteDomain.range(1, Math.abs(2 - i) + 2);
			vars[i] = new Discrete(domains[i]);
		}
		IFactorTable table = FactorTable.create(domains);
		table.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
		table.randomizeWeights(rand);
		Factor f = fg.addFactor(table, vars);
		STableFactor stf = (STableFactor) f.getSolver();
		if (stf == null)
		{
			fail("STableFactor is null");
			return;
		}
		// stf.setOptimizedUpdateEnabled(true);
		fg.solve();
		int[] values = new int[vars.length];
		for (int i = 0; i < values.length; i++)
		{
			values[i] = (Integer) vars[i].getValue();
		}
		int[] flat = Ints.concat(values);
		int hash = Arrays.hashCode(flat);
		final int EXPECTED_HASH = 31430530;
		assertEquals(EXPECTED_HASH, hash);
	}

	/**
	 * @since 0.07
	 */
	private void optimizeHelper(final double density, final boolean multithreaded)
	{
		Random rand = new Random();
		rand.setSeed(0); // Don't be random
		final FactorGraph fg = new FactorGraph();
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
		SFactorGraph sfg = (SFactorGraph) fg.getSolver();
		if (sfg == null)
		{
			fail("SFactorGraph is null");
			return;
		}
		sfg.setOption(UpdateOptions.updateApproach, UpdateApproach.UPDATE_APPROACH_AUTOMATIC);
		sfg.useMultithreading(multithreaded);
		// Before optimize, all factors should not have their optimize enable explicitly set
		for (Factor factor : fg.getFactors())
		{
			STableFactor sft = (STableFactor) factor.getSolver();
			if (sft != null)
			{
				boolean isAutomaticOptimizationDecisionMade = sft.isAutomaticOptimizationDecisionMade();
				assertEquals(false, isAutomaticOptimizationDecisionMade);
			}
		}
		sfg.initialize();
		// Afterward, they should
		for (Factor factor : fg.getFactors())
		{
			STableFactor sft = (STableFactor) factor.getSolver();
			if (sft != null)
			{
				boolean isAutomaticOptimizationDecisionMade = sft.isAutomaticOptimizationDecisionMade();
				assertEquals(true, isAutomaticOptimizationDecisionMade);
			}
		}
	}

	/**
	 * @since 0.07
	 */
	@Test
	public void testOptimizeSparse()
	{
		final boolean multithreaded = false;
		final double density = 0.05;
		optimizeHelper(density, multithreaded);
	}

	/**
	 * @since 0.07
	 */
	@Test
	public void testOptimizeDense()
	{
		final boolean multithreaded = false;
		final double density = 1.0;
		optimizeHelper(density, multithreaded);
	}

	/**
	 * @since 0.07
	 */
	@Test
	public void testOptimizeMultithreaded()
	{
		final double density = 1.0;
		final boolean multithreaded = true;
		optimizeHelper(density, multithreaded);
	}

	static private class Graph
	{
		private static Random _rnd = new Random();
		private final int _rows;
		private final int _cols;
		private final Bit[][] _vs;

		public Graph(final FactorGraph fg, int zeroControl)
		{
			_rnd.setSeed(0); // Don't be random
			_rows = 20;
			_cols = 20;
			_vs = createVariables(_rows, _cols);
			final int xBlockSize = 4;
			final int yBlockSize = 4;
			final int blockSize = xBlockSize * yBlockSize;
			IFactorTable factorTable = createFactorTable(blockSize, zeroControl);
			final int blockRows = _rows - yBlockSize + 1;
			final int blockCols = _cols - xBlockSize + 1;
			addFactors(fg, xBlockSize, yBlockSize, blockSize, blockRows, blockCols, factorTable);
			setInput(_rows, _cols);
		}

		private void addFactors(FactorGraph fg,
			final int xBlockSize,
			final int yBlockSize,
			final int blockSize,
			final int blockRows,
			final int blockCols,
			IFactorTable factorTable)
		{
			Bit[] varPatch = new Bit[blockSize];
			for (int yList = 0; yList < blockRows; yList++)
			{
				for (int xList = 0; xList < blockCols; xList++)
				{
					int blockOffset = 0;
					for (int yb = 0; yb < yBlockSize; yb++)
					{
						for (int xb = 0; xb < xBlockSize; xb++)
						{
							varPatch[blockOffset] = _vs[yb + yList][xb + xList];
							blockOffset = blockOffset + 1;
						}
					}
					fg.addFactor(factorTable, varPatch);
				}
			}
		}

		private Bit[][] createVariables(final int rows, final int cols)
		{
			final Bit[][] vs = new Bit[rows][cols];
			for (int row = 0; row < rows; row++)
			{
				for (int col = 0; col < cols; col++)
				{
					vs[row][col] = new Bit();
				}
			}
			return vs;
		}

		private void setInput(final int rows, final int cols)
		{
			for (int row = 0; row < rows; row++)
			{
				for (int col = 0; col < cols; col++)
				{
					_vs[row][col].setInput(_rnd.nextDouble());
				}
			}
		}

		private static IFactorTable createFactorTable(final int blockSize, int zeroControl)
		{
			double[] factorTableValues = new double[65536];
			if (zeroControl >= 0)
			{
				for (int f_index = 0; f_index < factorTableValues.length; ++f_index)
				{
					do
					{
						factorTableValues[f_index] = _rnd.nextDouble();
					} while (factorTableValues[f_index] == 0.0);
				}
				// Make the table sparse by setting some of the values to zero.
				for (int n = 0; n < zeroControl; n++)
				{
					int index;
					do
					{
						index = _rnd.nextInt(factorTableValues.length);
					} while (factorTableValues[index] == 0.0);
					factorTableValues[index] = 0.0;
				}
			}
			else
			{
				for (int n = 0; n < -zeroControl; n++)
				{
					do
					{
						factorTableValues[n] = _rnd.nextDouble();
					} while (factorTableValues[n] == 0.0);
				}
			}
			DiscreteDomain[] domains = new DiscreteDomain[blockSize];
			for (int i = 0; i < domains.length; i++)
			{
				domains[i] = DiscreteDomain.bit();
			}
			IFactorTable factorTable = FactorTable.create(domains);
			factorTable.setWeightsDense(factorTableValues);
			return factorTable;
		}

		public int getValue(int row, int col)
		{
			return (Integer) (_vs[row][col].getValue());
		}

		public int[][] getValues()
		{
			int[][] result = new int[_rows][_cols];
			for (int row = 0; row < _rows; row++)
			{
				for (int col = 0; col < _cols; col++)
				{
					result[row][col] = getValue(row, col);
				}
			}
			return result;
		}
	}
}
