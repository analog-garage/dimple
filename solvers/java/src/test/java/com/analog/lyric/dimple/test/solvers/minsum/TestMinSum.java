/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.dimple.test.solvers.minsum;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.model.variables.VariableBlock;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.optimizedupdate.UpdateApproach;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * Tests for Min-sum solver
 * @since 0.08
 * @author Christopher Barber
 */
public class TestMinSum extends DimpleTestBase
{
	/**
	 * Test using min-sum so solve "small parsimony" problem in evolutionary bioinformatics.
	 * <p>
	 * The test generates a binary tree of random mutations to a DNA strand.
	 * It then uses the shape of the tree and the leaves of the tree to attempt
	 * to reconstruct the values of the strands on the internal nodes. Min-sum should
	 * be able to produce an answer that has the same or better score than the original
	 * tree, where the score is the sum of the hamming distances along the edges.
	 * <p>
	 * The idea for this test was taken from an assignment in the Coursera Bioinformatics Algorithms courses from UCSD.
	 */
	@Test
	public void smallParsimonyTest()
	{
		smallParsimonyCase(1, 10, .1);
		smallParsimonyCase(5, 20, .1);
		smallParsimonyCase(10, 20, .1);
		smallParsimonyCase(20, 20, .2);
	}
	
	private void smallParsimonyCase(int strandLength, int treeSize, double mutationRate)
	{
		// Each nucleotide in the strand is considered to be independent of the others,
		// so the resulting variables in the graph should not be connected to each other
		// through a factor. Instead, we will use variable blocks to tie them together.
		//
		// We use the guess values to store the generated nucleotide values for each variable.
		// TODO - use data abstraction layer for this TBD
		
		final DiscreteDomain nucleotides = DiscreteDomain.create('A','C','G','T');

		final FactorFunction delta = new FactorFunction() {
			@Override
			public double evalEnergy(Value[] values)
			{
				assertEquals(2, values.length);
				return values[0].valueEquals(values[1]) ? 0.0 : 1.0;
			}
		};
		
		final FactorGraph fg = new FactorGraph();
		
		// Need to attach solver so that we can set guesses.
		fg.setOption(BPOptions.updateApproach, UpdateApproach.NORMAL);
		fg.setSolverFactory(new MinSumSolver());

		try (CurrentModel curModel = using(fg))
		{
			// Generate root block
			final VariableBlock root = block(discretes("n0_", nucleotides, strandLength));
			for (Variable var : root)
			{
				int index = testRand.nextInt(4);
				var.setGuess(nucleotides.getElement(index));
			}
			
			ArrayList<VariableBlock> leaves = new ArrayList<>(treeSize);
			ArrayList<VariableBlock> internal = new ArrayList<>();
			
			int mutations = 0;
			
			leaves.add(root);
			
			for (int i = 1; i < treeSize; i += 2)
			{
				// Pick a random leaf
				VariableBlock leaf = leaves.remove(testRand.nextInt(leaves.size()));
				internal.add(leaf);
				
				// Mutate it randomly twice to create child blocks. This simulates multiple steps of
				// evolution. A single mutation event would be expected to result in one mutated child
				// and a clone.
				
				VariableBlock left = block(discretes(String.format("n%d_", i), nucleotides, strandLength));
				leaves.add(left);
				for (int j = 0; j < strandLength; ++j)
				{
					Discrete cur = (Discrete)left.get(j), prev = (Discrete)leaf.get(j);
					int prevIndex = prev.getGuessIndex();
					int nextIndex = testRand.nextBoolean(mutationRate) ? testRand.nextInt(4) : prevIndex;
					if (prevIndex != nextIndex)
						++mutations;
					cur.setGuessIndex(nextIndex);
					addFactor(delta, prev, cur);
				}

				VariableBlock right = block(discretes(String.format("n%d_", i+1), nucleotides, strandLength));
				leaves.add(right);
				for (int j = 0; j < strandLength; ++j)
				{
					Discrete cur = (Discrete)right.get(j), prev = (Discrete)leaf.get(j);
					int prevIndex = prev.getGuessIndex();
					int nextIndex = testRand.nextBoolean(mutationRate) ? testRand.nextInt(4) : prevIndex;
					if (prevIndex != nextIndex)
						++mutations;
					cur.setGuessIndex(nextIndex);
					addFactor(delta, prev, cur);
				}
			}
			
			// Now solve using Min-Sum
			
			// The initial score should simply be the sum of all of the energy functions for the guesses,
			// which should be the same as the total hamming distance.
			double expectedScore = 0.0;
			for (Factor factor : fg.getFactors())
			{
				expectedScore += factor.getScore();
			}
			assertEquals(mutations, expectedScore, 0.0);
			
			// clear the guesses on the non-leaves
			for (VariableBlock block : internal)
			{
				for (Variable var : block)
				{
					var.setGuess(null);
					// Add a random input to avoid ambiguous result - see BUG 408
					var.setPrior(randomPrior());
				}
			}
			
			// set fixed values on the leaves
			for (VariableBlock block : leaves)
			{
				for (Variable var : block)
				{
					Discrete d = (Discrete)var;
					d.setPriorIndex(d.getGuessIndex());
				}
			}
			
			fg.solve();
			
			// Set guesses from inferred best value
			for (VariableBlock block : internal)
			{
				for (Variable var : block)
				{
					Discrete d = (Discrete)var;
					double maxBelief = Double.NEGATIVE_INFINITY;
					for (double belief : d.getBelief())
					{
						// Make sure BUG 408 will not affect the result
						assertNotEquals(belief, maxBelief, 0.0);
						if (belief > maxBelief)
							maxBelief = belief;
					}
					
					d.setGuessIndex(d.getValueIndex());
				}
			}
			
			double score = 0.0;
			for (Factor factor : fg.getFactors())
			{
				score += factor.getScore();
			}
			
			if (expectedScore < score)
			{
				fail(String.format("Expected %f or better but got %f", expectedScore, score));
			}
		}
	}
	
	private double[] randomPrior()
	{
		double[] input = new double[4];
		for (int i = 0; i < 4; ++i)
		{
			input[i] = 1000 + testRand.nextDouble();
		}
		return input;
	}
	
}
