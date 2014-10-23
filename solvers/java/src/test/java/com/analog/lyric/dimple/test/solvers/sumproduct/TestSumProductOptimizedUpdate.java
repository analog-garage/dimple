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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.CustomTypeSafeMatcher;
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
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableList;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.schedulers.FloodingScheduler;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductTableFactor;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.options.IOptionHolder;

/**
 * @since 0.06
 * @author jking
 */
public class TestSumProductOptimizedUpdate extends DimpleTestBase
{
	static final UpdateApproach defaultApproach = UpdateApproach.AUTOMATIC;
	static final double defaultAllocationScale = 10.0;
	static final double defaultExecutionTimeScale = 1.0;
	static final double defaultSparseThreshold = 1.0;
	
	private static SumProductSolverGraph getSumProductSolverGraph(FactorGraph fg)
	{
		SumProductSolverGraph sfg = (SumProductSolverGraph) fg.getSolver();
		assertNotNull(sfg);
		return sfg;
	}
	
	private static SumProductTableFactor getSumProductFactorTable(Factor f)
	{
		SumProductTableFactor sft = (SumProductTableFactor) f.getSolver();
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
		SumProductSolverGraph sfg = getSumProductSolverGraph(fg);
		checkDefaults(sfg);
	}
	
	public static Factor add2BitFactor(Random rand, FactorGraph fg)
	{
		Bit[] vars = new Bit[2];
		for (int i = 0; i < 2; i++)
		{
			vars[i] = new Bit();
		}
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
	
	/**
	 * Verify that the solver factor has the correct default property values.
	 * 
	 * @since 0.07
	 */
	@Test
	public void testFactorPropertiesDefaults()
	{
		FactorGraph fg = new FactorGraph();
		Factor f = add2BitFactor(new Random(), fg);
		SumProductTableFactor sft = getSumProductFactorTable(f);
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
		SumProductSolverGraph sfg = getSumProductSolverGraph(fg);
		Factor f = add2BitFactor(rand, fg);
		SumProductTableFactor sft = getSumProductFactorTable(f);
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
			SumProductSolverGraph ssolver = (SumProductSolverGraph) solver;

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

	/**
	 * Asserts that two lists of arrays of doubles are equal.
	 */
	public static void assertEqualListDoubleArray(List<Object> expected, List<Object> actual)
	{
		int l = actual.size();
		assertEquals(l, expected.size());
		for (int i = 0; i < l; i++)
		{
			final double[] expectedArray = (double[]) expected.get(i);
			CustomTypeSafeMatcher<double[]> closeEnough = new CustomTypeSafeMatcher<double[]>(Arrays.toString(expectedArray)) {
				static final double EPSILON = 1.0e-6;
				@Override
				public boolean matchesSafely(@Nullable double[] actualArray) {
					if (actualArray == null || actualArray.length != expectedArray.length)
					{
						return false;
					}
					for (int i1 = 0; i1 < actualArray.length; i1++)
					{
						if (Math.abs(actualArray[i1] - expectedArray[i1]) >= EPSILON)
						{
							return false;
						}
					}
					return true;
				}
			};
			assertThat((double[]) actual.get(i), closeEnough);
		}
	}

	private void doTest(final int zeroControl,
		final double sparsityControl,
		final double damping,
		final boolean useMultithreading)
	{
		FactorGraph fg = new FactorGraph();
		@SuppressWarnings("unused")
		Graph g = new Graph(fg, zeroControl);
		runSolver(fg, sparsityControl, damping, useMultithreading);
	}

	public static List<Object> getBeliefs(FactorGraph fg)
	{
		VariableList variables = fg.getVariables();
		List<Object> beliefs = new ArrayList<>(variables.size());
		for (Variable v : variables)
		{
			if (v instanceof Discrete)
			{
				Discrete discrete = (Discrete) v;
				beliefs.add(discrete.getBelief());
			}
		}
		return beliefs;
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
		SumProductSolverGraph sfg = (SumProductSolverGraph) fg.getSolver();
		if (sfg == null)
		{
			fail("SumProductSolverGraph is null");
			return;
		}
		sfg.setOption(BPOptions.updateApproach, UpdateApproach.AUTOMATIC);
		sfg.useMultithreading(multithreaded);
		// Before optimize, all factors should not have their optimize enable explicitly set
		for (Factor factor : fg.getFactors())
		{
			SumProductTableFactor sft = (SumProductTableFactor) factor.getSolver();
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
			SumProductTableFactor sft = (SumProductTableFactor) factor.getSolver();
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

	static public class Graph
	{
		private static Random _rnd = new Random();
		private final int _rows;
		private final int _cols;
		private final Bit[][] _vs;

		public Graph(final FactorGraph fg, int zeroControl)
		{
			_rnd.setSeed(0); // Don't be random
			_rows = 7;
			_cols = 7;
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
	}

	private void doTest2(final double sparsityControl,
		final double damping,
		final boolean useMultithreading,
		final Random rnd)
	{
		FactorGraph fg = new FactorGraph();
		fg.setScheduler(new FloodingScheduler());
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

	static double[] randomlySplit(Sampler sampler, double total, int pieces)
	{
		double[] xs = new double[pieces];
		double sum = 0.0;
		for (int i = 0; i < pieces; i++)
		{
			double x = sampler.next();
			xs[i] = x;
			sum += x;
		}
		double[] result = new double[pieces];
		int sumi = 0;
		for (int i = 0; i < pieces - 1; i++)
		{
			double piece = total * xs[i] / sum;
			result[i] = piece;
			sumi += piece;
		}
		if (pieces > 0)
		{
			result[pieces - 1] = total - sumi;
		}
		return result;
	}
	
	static double[] randomlySplit(final Random rnd, double total, int pieces)
	{
		Sampler sampler = new Sampler()
		{
			@Override
			public double next()
			{
				return rnd.nextDouble();
			}
		};
		return randomlySplit(sampler, total, pieces);
	}

	static int[] randomlySplit(Sampler sampler, int total, int pieces)
	{
		double[] ds = randomlySplit(sampler, (double) total, pieces);
		int[] result = new int[pieces];
		for (int i = 0; i < pieces; i++)
		{
			result[i] = (int) ds[i];
		}
		return result;
	}
	
	interface Sampler
	{
		double next();
	}
	
	static class ParetoDistributed implements Sampler
	{
		private final Random _rnd;
		private final double _xm;
		private final double _a;

		public ParetoDistributed(Random rnd, double xm, double a)
		{
			_rnd = rnd;
			_xm = xm;
			_a = a;
		}
		
		@Override
		public double next()
		{
			double u = 1 - _rnd.nextDouble();
			double result = _xm / (Math.pow(u, 1.0 / _a));
			return result;
		}
	}
	
	static public class Graph2
	{
		private final Random _rnd;
		
		private final Map<Integer, List<Variable>> _vs;

		public Graph2(final FactorGraph fg, final Random rnd)
		{
			_rnd = rnd;
			_vs = new TreeMap<>();
			
			// How many factor tables.
			final int factorTableCount = _rnd.nextInt(10) + 2;
			
			// How many entries to split across all of the factor tables.
			final int totalEntries = Math.max(500, _rnd.nextInt((int) (0.05 * 1024 * 1024)));
			
			// Split the entries up across the factor tables, using a Pareto distribution so that there is likely to be
			// fewer large tables and more smaller tables. Then ensure that no dimension has fewer than 1 entry, which may
			// result in the actual total entry count differing from totalEntries.
			final int[] factorTableSizes = randomlySplit(new ParetoDistributed(_rnd, 1.0, 1.0), totalEntries, factorTableCount);
			for (int i = 0; i < factorTableCount; i++)
			{
				factorTableSizes[i] = Math.max(factorTableSizes[i], 1);
			}

			for (int i_factorTable = 0; i_factorTable < factorTableCount; i_factorTable++)
			{
				final IFactorTable factorTable = createFactorTable(factorTableSizes[i_factorTable]);
				final JointDomainIndexer domainIndexer = factorTable.getDomainIndexer();
				final int dimensions = domainIndexer.size();
				Variable[] variables = new Variable[dimensions];
				do
				{
					for (int dimension = 0; dimension < dimensions; dimension++)
					{
						final int domainSize = domainIndexer.getDomainSize(dimension);
						List<Variable> candidateVariables = _vs.get(domainSize);
						if (candidateVariables == null)
						{
							candidateVariables = new ArrayList<>();
							_vs.put(domainSize, candidateVariables);
						}
						Variable candidateVariable;
						do
						{
							int p = _rnd.nextInt(candidateVariables.size() + 1);
							if (p == candidateVariables.size())
							{
								DiscreteDomain variableDomain = DiscreteDomain.range(0, domainSize - 1);
								Discrete discreteVariable = new Discrete(variableDomain);
								candidateVariable = discreteVariable;
								candidateVariables.add(candidateVariable);
								if (_rnd.nextDouble() < 0.2)
								{
									double[] inputValues = new double[domainSize];
									for (int i = 0; i < domainSize; i++)
									{
										inputValues[i] = _rnd.nextDouble();
									}
									discreteVariable.setInput(inputValues);
								}
							}
							else
							{
								candidateVariable = candidateVariables.get(p);
								// Check to see if this variable is already selected for this factor
								for (int i = 0; i < dimension; i++)
								{
									if (variables[i] == candidateVariable)
									{
										candidateVariable = null;
										break;
									}
								}
							}
						} while (candidateVariable == null);
						variables[dimension] = candidateVariable;
					}
					fg.addFactor(factorTable, variables);
				} while (_rnd.nextDouble() < 0.9);
			}
		}

		private IFactorTable createFactorTable(int factorTableSize)
		{
			// Choose a density, favoring low-density tables. This value will not be the actual density, as the table's
			// yet-to-be-decided dimensions and the factorTableSize ultimately determine the actual density.
			final double density = _rnd.nextDouble() * _rnd.nextDouble();

			final int cardinality = (int) (factorTableSize / density);
			
			// Split the cardinality up into a product.
			double logSize = Math.max(Math.log(cardinality), Math.log(2.0));
			
			// Choose the quantity of dimensions.
			// Limit the quantity to the case where all domains are size 2.
			final int maxDimensions = (int) (logSize / Math.log(2.0));
			// Use a Pareto distribution to favor smaller dimension counts.
			int dimensions = Math.min(maxDimensions, (int)(new ParetoDistributed(_rnd, 1.0, 1.0).next() * 3.0));
			
			// Create the domains
			DiscreteDomain[] domains = new DiscreteDomain[dimensions];
			// Assuming each dimension has at least domain size 2, split the remaining cardinality into a product
			// with as many multiplicands as the quantity of dimensions. Use a Pareto distribution to favor smaller
			// domain sizes.
			double[] logSizeSplit = randomlySplit(new ParetoDistributed(_rnd, 1.0, 1.0), logSize - dimensions * Math.log(2.0), dimensions);
			for (int dimension = 0; dimension < dimensions; dimension++)
			{
				logSizeSplit[dimension] += Math.log(2.0);
				int domainSize = (int) Math.exp(logSizeSplit[dimension]);
				domains[dimension] = DiscreteDomain.range(0, domainSize - 1);
			}
			IFactorTable factorTable = FactorTable.create(domains);
			JointDomainIndexer indexer = factorTable.getDomainIndexer();
			int[] indices = null;
			if (indexer.supportsJointIndexing() && indexer.getCardinality() <= 1.0e6)
			{
				factorTable.setRepresentation(FactorTableRepresentation.DENSE_WEIGHT);
			}
			if (indexer.supportsJointIndexing() && indexer.getCardinality() <= 1.0e6 && density > 0.5)
			{
				factorTable.randomizeWeights(_rnd);
				final int count = indexer.getCardinality() - factorTableSize;
				for (int i = 0; i < count; i++)
				{
					do {
						indices = indexer.randomIndices(_rnd, indices);
					} while (factorTable.getWeightForIndices(indices) == 0.0);
					factorTable.setWeightForIndices(0.0, indices);
				}
			}
			else {
				for (int i = 0; i < factorTableSize; i++)
				{
					do {
						indices = indexer.randomIndices(_rnd, indices);
					} while (factorTable.getWeightForIndices(indices) != 0.0);
					factorTable.setWeightForIndices(_rnd.nextDouble(), indices);
				}
			}
			if (indexer.supportsJointIndexing() && indexer.getCardinality() <= 1.0e6)
			{
				factorTable.setRepresentation(FactorTableRepresentation.SPARSE_WEIGHT);
			}
			return factorTable;
		}
	}
}
