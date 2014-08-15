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

package com.analog.lyric.dimple.benchmarks.hmm;

import java.util.Random;

import com.analog.lyric.benchmarking.Benchmark;
import com.analog.lyric.dimple.factorfunctions.core.FactorTable;
import com.analog.lyric.dimple.factorfunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Discrete;

@SuppressWarnings({"null", "deprecation"})
public class hmmBenchmark
{
	private static final Random rng = new Random(0);

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmGibbs100x4x4()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph solver = (com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph) fg
				.getSolver();
		solver.setNumSamples(600000); // Aiming for ~1s execution time

		int stages = 100;
		int stateDomainOrder = 4;
		int observationDomainOrder = 4;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmGibbs100000x4x4()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph solver = (com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph) fg
				.getSolver();
		solver.setNumSamples(300); // Aiming for ~1s execution time

		int stages = 100000;
		int stateDomainOrder = 4;
		int observationDomainOrder = 4;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmGibbs1000x1000x1000()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph solver = (com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph) fg
				.getSolver();
		solver.setNumSamples(1500); // Aiming for ~1s execution time

		int stages = 1000;
		int stateDomainOrder = 1000;
		int observationDomainOrder = 1000;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmSumProduct100x4x4()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		fg.getSolver().setNumIterations(1200); // Aiming for ~1s execution time

		int stages = 100;
		int stateDomainOrder = 4;
		int observationDomainOrder = 4;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmSumProduct100000x4x4()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		fg.getSolver().setNumIterations(240); // Aiming for ~1s execution time

		int stages = 100000;
		int stateDomainOrder = 4;
		int observationDomainOrder = 4;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmSumProduct1000x1000x1000()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.sumproduct.Solver());
		fg.getSolver().setNumIterations(3); // Aiming for ~1s execution time

		int stages = 1000;
		int stateDomainOrder = 1000;
		int observationDomainOrder = 1000;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmMinSum100x4x4()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.minsum.Solver());
		fg.getSolver().setNumIterations(6000); // Aiming for ~1s execution time

		int stages = 100;
		int stateDomainOrder = 4;
		int observationDomainOrder = 4;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmMinSum100000x4x4()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.minsum.Solver());
		fg.getSolver().setNumIterations(360); // Aiming for ~1s execution time

		int stages = 100000;
		int stateDomainOrder = 4;
		int observationDomainOrder = 4;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@Benchmark(warmupIterations = 0, iterations = 1)
	public boolean hmmMinSum1000x1000x1000()
	{
		FactorGraph fg = new FactorGraph();
		fg.setSolverFactory(new com.analog.lyric.dimple.solvers.minsum.Solver());
		fg.getSolver().setNumIterations(3); // Aiming for ~1s execution time

		int stages = 1000;
		int stateDomainOrder = 1000;
		int observationDomainOrder = 1000;
		hmmInference(fg, stages, stateDomainOrder, observationDomainOrder);
		return false;
	}

	@SuppressWarnings("unused")
	private void hmmInference(FactorGraph fg, int stages, int stateDomainOrder,
			int observationDomainOrder)
	{
		HmmGraph hmm = new HmmGraph(fg, stages, stateDomainOrder,
				observationDomainOrder);
		fg.solve();
		Integer v0 = (Integer) hmm.getStates()[1].getValue();
		double score = fg.getScore();
	}

	private static class HmmGraph
	{
		private final Discrete[] _states;
		private final Discrete[] _observations;

		public HmmGraph(FactorGraph fg, int stages, int stateDomainOrder,
				int observationDomainOrder)
		{
			DiscreteDomain stateDomain = DiscreteDomain.range(0,
					stateDomainOrder - 1);
			DiscreteDomain observationDomain = DiscreteDomain.range(0,
					observationDomainOrder - 1);
			_states = new Discrete[stages];
			_observations = new Discrete[stages];
			IFactorTable stateToStateTransitionFactorTable = randomFactorTable(
					stateDomain, stateDomain);
			IFactorTable stateToObservationTransitionFactorTable = randomFactorTable(
					stateDomain, observationDomain);
			for (int i = 0; i < _states.length; i++)
			{
				_states[i] = new Discrete(stateDomain);
				_observations[i] = new Discrete(observationDomain);
				fg.addFactor(stateToObservationTransitionFactorTable,
						_states[i], _observations[i]);
				if (i > 0)
				{
					fg.addFactor(stateToStateTransitionFactorTable,
							_states[i - 1], _states[i]);
				}
			}
		}

		private IFactorTable randomFactorTable(DiscreteDomain... domains)
		{
			IFactorTable result = FactorTable.create(domains);
			double[] weights = new double[result.getDomainIndexer()
					.getCardinality()];
			for (int i = 0; i < weights.length; i++)
			{
				weights[i] = rng.nextDouble();
			}
			result.setWeightsDense(weights);
			return result;
		}

		@SuppressWarnings("unused")
		public Discrete[] getObservations()
		{
			return _observations;
		}

		public Discrete[] getStates()
		{
			return _states;
		}
	}
}
