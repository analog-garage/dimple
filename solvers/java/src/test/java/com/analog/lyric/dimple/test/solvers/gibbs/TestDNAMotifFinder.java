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

package com.analog.lyric.dimple.test.solvers.gibbs;

import static java.util.Objects.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.dimple.data.DataStack;
import com.analog.lyric.dimple.data.PriorDataLayer;
import com.analog.lyric.dimple.data.ValueDataLayer;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.dimple.test.data.TestDataLayer;

/**
 * Test of Gibbs with large discrete factor based on "Subtle motif" finding problem from
 * Coursera Bioinformatics Algorithm course.
 * <p>
 * The problem is this: given a set of DNA strands, you are to find a length-k (in this case 15)
 * subsequence from each -- known as a "motif" -- that optimizes a similarity score. This code simply
 * uses a simple integer score representing the number of differences to a "consensus" sequence that
 * consists of the most popular nucleotides at each position across the chosen motifs. Other approaches
 * may attempt to minimize the entropy of the collection of the motifs, or maximize the KL-divergence
 * between the motifs and the distribution of nucleotides in the strands if it is not uniform.
 * <p>
 * This test exercises ability to support large discrete factors that are too big for
 * factor tables and Gibbs's use of the FactorFunction.updateEnergy method.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDNAMotifFinder extends DimpleTestBase
{
	@Test
	public void test()
	{
		testCase(true);
		testCase(false);
	}
	
	private void testCase(boolean useUpdateEnergy)
	{
		boolean print = false;
		
		List<String> strands = Arrays.asList(_dna);
		final int t = strands.size();
		
		int[] motifs = findMotifOffsets(15, strands, 38, useUpdateEnergy);
		
		MotifScoreFunction scoreFunction = new MotifScoreFunction(15, strands, useUpdateEnergy);
		Value[] values = new Value[t];
		for (int i = 0; i < t; ++i)
		{
			values[i] = Value.create(DiscreteDomain.range(0, strands.get(i).length()));
			values[i].setInt(motifs[i]);
			
			int expectedOffset = _motifOffsets[i];
			int offset  = motifs[i];
			
			if (print)
			{
				System.out.format("Expected: %3d %s  Actual: %3d %s\n",
					expectedOffset, _dna[i].substring(expectedOffset,  expectedOffset + 15),
					offset, _dna[i].substring(offset,  offset + 15)
					);
			}
		}
		double score = scoreFunction.evalEnergy(values);
		if (print)
		{
			System.out.format("Score: %f\n", score);
		}
		assertTrue(score <= 39);
	}
	
	private static String[] _dna = new String[] {
		"TCCGAACAACGAGTAGGCGTACTCACCGGCATGGCCGGATACACCGACCATCGCGGACGAGAAAGGGAGGGCTGAAATACAGACAGCGTACTGTATTAAGCAGAAACGAGAGGAGACAGATCTCATCCCTGGTGTGGTGGAACTGGAGGACTCGCCTCGGTGTGAGTCGTAAGGTGACCGACGATGAAATGCAAGTTCCAACGGCCAACAGCGCGTCAACAACAATGCGCACGTGTCGTAAACTGACGTGAGGTCCCCTTATAGCCCATGAAGAACTTTGACTCGCCTCCGGTAGCCGCTAGTTTTATGCGTGAATGTGCGTTATGCCAACTCAAATGTCTCGCAAGTCAATGAATCAACCGCGATTCTTTATTAACTTCATATCAGGCTAACAAGGACAGACAGCAACAAAGTTCTGCAAAACTGTTCCGGTCTCATCCCTAACTCTCTAACTGATAACAGTCTAACTTGCACCAAGAGTCGCTCGATCGACCAAAGAAATTACCGCCGCCTTGCAGTTCCGATGCCTGGAGTCCCCCTCCGTGTGAAGGTGATAAACCATTTGTCCAACAATGTTAGACAATAAACCACGTAAAAGGC",
		"GGAACTAGCTTAAAAAATAGCAGGGTGTGCCTGATCCTTCCGGTGTTTAAGTAGAAGGCAGGACGGACAGAGTTCCATCCACAGAAGCATAGTTTGATCGTATTGGCGACAGGCTGATGCGAAGCTCCGCTCCAAACGAGAGAGATAAATGCATGCGGTTTGGCCTAAGGCGGGGGGGCAACCCGGCTTATCAATTAGCTAGCCTTGCTTTGGAACAAGGGCCAAGCGGGAGGTAAACTCTTCAGCCCGGGTGTCCCAGTAGCGCGATTTGGTGCTAGCCAGGTTTCGATCAAAAGGGGCTCTTGCAACGCTCTCTTCTAAAAATAAAATGCAATTAGTTGGCAGGGTTGATGAGTGTCGAATCCTTGCAAGCGAGATTCTTCCATGCAGTTGCAGCGGGGCGAGGCCAAAGAGCTCAGCTAGCTTGGGGACTCGCGCCCTGCTTATTCACCCTCGGTGCAACTAATCCTTACCGTGAATTTGGTAGATGTCCAAGCATTGTTCTTTATTAATGCACGTGTTAATAGGGGTAGACTATTCCCGTCCCGGCCTACGGTGTCAAAATCAAGTAGGCGCCGAGATTATTCTTGCATGCCTGTA",
		"TGCGAACTAGTTTTCGCAACTTAACGTAGCGCGTGGGGCGTCCCTAGTGGCTCTGTCAAAGCAATTTGGTTCGTTTAGCTGTTATAGTTTTGGATCACAGCGAATAGAGTTAGTCTTGCTAGTCCGTAAATCAACGGACCGCGTCCCATTAAACACAGCTTCGTCGAGTCTATGACGCTCATACTCTACCATGACCGCGCCGGGACGACCGCCAACTCATAAATGAACGCCTAATAGAACCGAAAAGGGTCGGCGGCACAAAACTCCGGAACGTGGTCTGGGTTAACAAAGGCGCGATGATATTGTTCGTAGATCCCTGTTGGACTCTCCAACAAGTTTCCCGGAGGACTCGAGGTTCCAGGCCGAGTAAATAAAAGTTTTCTCGGGGTGGTGCCGGAAGGCGGGAAGTGGTGGTTAGGACAGATAATGACGAAAACAATGGATCGTGGAAGAGATCGCCCAGAGGTTCGATAGGATGTTACGCTACTTGTGTTCGAGGGGGAGACGGTTTCTACCTAGGCGGGTACCACAAAGCTGTTCTCTATTCTGGAAATTATGTACTCTGTTACTTGAATAAAATAAACAGCGGGGTACGCGGAT",
		"ATCCTGACTACGGCGGTTTTCGTCTTGGGTAGGCACGGAGCTAGAGTATACACGGCAGCTCGTAGGGGGTCGATGCGTCTCGATTAGCTCGTTCCTATAGCTCAGCGATATCCCCGGGTTAAGAAGATTGCTCTCGTTACGCACTAGCCTCCGACTCGCGGGGCGTAACCAGTACAGTAAAAGACGCTAGAATCGACGCTTTCGCATAGTAGTCATTTAGAACCCGGGCTTAAACGATCGTACTTGATACACCCCGGGAGATGTGGATACCATTAAGTTAACCAGATCTATATGCGACCAGTCCTGCAGTAAAGATTGGCTGTCTTGGACTTGTATGCAAGCATAATCAGGGCAGAGGCAGTGGTCCGTTGCCTGAGGACGTCAAGAGTTCTCAGTCTAAAGTATTCCGGGGAAATAGTTAGTTGGCATAAGTCCGCCAAAGATCGCAGATGGTTAGTAGGTAACACTGGGGCCCTCCAGCTTAAGCCAAGCTAACTACGCTCAAGCAGGCTTTTTTTTTATGTTGAACAGAAAAAAAGGGGTTTTCACGCACACTTAGCCCTTTCTACGTAAGAGTCATTCTCAATACTGATGTCAGGA",
		"AATTATACATAGGTGTGACTCTATGCTCGGCTATGGAAATAAGGTTCGCGCCGACACCTATGAAGAATTGTCACCCATGTTTTTGTGTCTATCAGCTTTGAGTGAGATTTGGTTTTCACGGGAGAAAGAGGATGTTCTCTGCGTGCGGACTCCTGAGACTTTGCTGAATGATGATGTAGCGGATCCACGAGGAACTGAGGTCCCGCAGCTCCGAGACAGGTGCTGATGCTTTGGCAACGATTTGAGGGCACAATTCCCGAGTACCTAGGATGGTATTCTGTATTGATTGGTTTTTGAAATGTGCTTGATTCGAACCAAGCGAGCAATTGACAAACGCTGTGCCTAGGTATACCTAAAATAAAACTGCGACAGTTGATCAAACATAAAGTAGAGGGGGTCCAAGTATCCATGAGTGATGCTTAGCACACCCTGCTCCCTGGACTTTTGGATTACCCCCTTCTAGCTTGCTTCTAGCTCAAGCTAAGACCTACCCCAATAAGAGGTAGCTAAGAACGGGGTCTGGGCAGTCATCAACGCCCGTGATCGTAAATCGGTCGTCCCACCGCACTCGCCGCGAATTACGAATAGCCATAGATGAGC",
		"TCTAAAAATGGGGCGGCCAGTGAATAAAGCCTGCGCGTATTCGTAGCTGTTTACTCGGGAGACCGGCGCCCGAACAGCGCCCTGCCTAACGCCAGCTTACACCGATAGACGAACACGGTTGGGCTGATATACGTCGAACCTGCCTAACCTTAATACTTTCCCTAGTCAGAAGTTGGCCCGAACTTAAGCGTTCGAATGTAGGAGGACTATGAGAGCAAAGCGCGCGCCCGGTCATTTGCACAGAATTCACGTATGTAGTGTAGAGGCGAGACGGGTTTGTCGCGTACACTGCAGACCCAACAGTTTTACGGCAACACAATATCCGTCCAGCCGTAATACGAGCGCAAAGCACGTAGGGTCATCTGGCTAAAGAATTAGGCGCCACTCATTTTGACGGAGAGCGCTTTGCGATCAGATCAGTGGAGTCCAGATTTGATTGTAACTCACTTACCGCACGGCAACAACGCTCATTCCCGCTAATGTATGAGGTACAGGTTGCACTGGTCAGTTTAATGAAGGTCATAGAACACGGGTTTACGTGAATGCGTGTCGCCATCCTCGGCCGAAATGATGAGTTGCCAGGACCGATCTGGCGCCAGC",
		"AGGTAAGGCTCGCCTCTACATCTCCGTACAAACTATCAGACGTAAAGAAAGCTGGAGGATTGCCAGCGAAAAGTACATAACACAAAGAACAAAAAGAGAAGGGGGTACGGGCTATTCGATCTAGATGGAGGCTAGGCAATAGAAGTTCGATCATCCATGGTAACTAGATATATGCTGAGAGCAAACGATCCCTAGTACCGCCTGTGTTATATGCCACCAATCTTTCTTCAGTTAGAAACCTCATTGTCGGGCGACACCAGGTCGATTCAAGAGGCGAGAGCCCATATGCTCGACCTATGGCGTGAACGCTAAGCGGCTGGAGCAAGAGAGGTGTATCCAACGACGGTTTTGAATTTACAATTCAGCCCACTGATATAAGCTGTATGGACTGACTCTGGAGGGACGCGCTGATATCTAAGGGCTTCGCGTACTAGGGTCACTACGGAAGCCATCGGCACTGTGCATCTTACAAAACGGACGTCCTTGACGGCCCTATGACCTTAGCACAAACGAATTGATGACCGAATGTACAGTACTTTGTGCTGGCTGAGCACTCCCTACCACGATCCGGCCAGCCGATCTGCGTCGAGGCTGCCACGC",
		"AAGCTCAGCTAACTAGGCGTGAATAATAACGGAACACCTTAGGTAATGTTGGGGTCCTTACCACCATTTTACGTGGATCTCTAGACGGGCAGCACAAGCAGACGCTCAACGTAGTAATGCCAAGAAGAGATCTACTCCTGTGTTCACTTACATATATTCCCACTCAGAACCGCGTCTTCTGAACTGAGGAAGAAGTTACACTAACTGCACGAGATACCGGATCTGCACCTAGCCTGCTAGGCGTGGCACACGTAGCGCACCCTCACGGCTGCAATGGAATTTGCACAAAAACCAGCGCGTGGCGGATATTCCTCGTTTACAGAGTGGGTTGGAACATCCGGCGGTCCCGAGAGAACCGTCTTTCCGGTCGCCCATTTTATCAAAGATTGCAGTCTACTTGCCCGTATTCCTTGAGATGATTCGAAGGTCGAAATCGTAGCACATGGCTAACAATCCTGTTATTTATGCAGTAGCCGCGCCGCTTAGACGGCTTACCCCCGATATAGGGGAGCCCACCAGCTATGCCCTGGAAGGGACGATAAATAGCGTTGTGATTTATGATACCTTCACCAGCTTCGTACGTGCATAGAAAAGGAAGGG",
		"TTCGTATCTTTCTCGGCGCCCTGATTCCAGTGATGGATTGTGAGGTCACTTCAAGTGAGATGTGTATTCCCAGCCAATCTATCCGTGTTAACTGATCCTAAACAGAGTGTGCCCAGATTAATGGGAACCCCAGTGTCAAGCGGGCCCTTAACACGGCCTGGTTAGATTCGTTTTAAGTGGGTCCTCTAACTCCTAACATTTTGACTTAAGGGTTTAACCGCTGACAGGCAGTAGCAACGGCTGTAGGGGAACACGAGGTTTTTTAATAAGTCTTGCAGTTTCATGCGGTTCTCACCAGAACGTTATAATCGCGAGTGCCCCGCTCAGGAATAGGATCAATGACGATTCTTATATCTCCGGAATTATGGTTACAGCTTCGTCAACGGCCTCAGGGTCGGGTTTTAAGCGGGGCCCGTTATCCAGAAATTACCCTGCATGGCAGGTCTACGCTAAAGTCCGAGCAAGAAAAAAGAGAGGAGTTTGCCCACGTGCCGCACACCCGGAGCTAGTCAGCATTGGTCTTCGAGAGATGCTCGCTGGACTCGGTTCATCTACTCGATCTAATTTTATGGCCGCCAACCATCAAAACGTATGACCTAA",
		"TCTCATCCGTAGATTTAGTCCGGAGTGTTGAACAGCCCTCGGAGGTGCTACTAGCAATCACGAGATGCTAACGAGGAATATTTGGGATAGACGGTTCCTTCATGTTGTTCTGGGTACGCACTGCCGGCGAGTACCCCAGTGCCGAAACCGGTAAGAGTAAGTTCCTTAGGTTACGAGATTCCAGGCTTTTTGGGTAAGCGAGACCTACCCACTTGTTGCATCTACCCGTGTCTGTCAATCGCTGACTAGAACTGGTATCACGAGAAGAGAAACTTTCGATCTGTGCCCCATCAGTACCGAAGTTTGGTATAAATCGATGTGATATCCAAGACATGGAATAGCTTTCGCTCTTACGAGAGCATATGAAGGTTGCAACTAATTACCTATCTGATGTACGAAATTCAAGCTAAAGGGGGGTCAATCTCGTCCGAGTGCGACGGGGCAATAGCCCGGTACGATCTCCCATTTTCCCTTCCGGTACTCTACTGCTTTGGCGGGGTCGAGTTATCCGTGCGAACATTCAACCACCTCTGAGAACGGGGCCATAATGAACTGTGATCTTGATTCTACCTAAACACGCAGGACCAAAGCCTTCGCCGA"
	};

	// The
	private static int[] _motifOffsets = new int[] { 56, 11, 576, 530, 382, 1, 90, 585, 465, 403 };
	
	private int[] findMotifOffsets(int motifLength, List<String> strands, int cutoffScore, boolean useUpdateEnergy)
	{
		final int t = strands.size();
		FactorGraph fg = new FactorGraph();
		DiscreteDomain[] motifDomains = new DiscreteDomain[t];
		Discrete[] motifVariables = new Discrete[t];
		for (int i = 0; i < t; ++i)
		{
			DiscreteDomain domain = DiscreteDomain.range(0, strands.get(i).length() - motifLength);
			motifDomains[i] = domain;
			Discrete var = new Discrete(domain);
			var.setName("motif-" + i);
			motifVariables[i] = var;
		}

		MotifScoreFunction scoreFunction = new MotifScoreFunction(motifLength, strands, useUpdateEnergy);
		fg.addFactor(scoreFunction, motifVariables);

		GibbsSolverGraph gibbs = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		GibbsDiscrete[] gibbsMotifVars = new GibbsDiscrete[t];
		for (int i = 0; i < t; ++i)
		{
			gibbsMotifVars[i] = (GibbsDiscrete)gibbs.getSolverVariable(motifVariables[i]);
		}

		// Use seed for repeatable results. This value was chosen because it happens to hit the
		// answer with fewer restarts so the test won't run too long.
		gibbs.setSeed(15);
		
		fg.setOption(GibbsOptions.numRandomRestarts, 2000);
		fg.setOption(GibbsOptions.numSamples, 20);
		fg.setOption(GibbsOptions.enableAnnealing, true);
		
		fg.initialize();
		
		for (int restart = 0; restart < 100; ++restart)
		{
			gibbs.burnIn(restart);
			
			if (restart == 0)
			{
				// Test for infinite energy annealing bug 403.
				// Instead of running many samples to drive energy to infinity, just start with a really low
				// temperature.
				double temperature = gibbs.getTemperature();
				gibbs.setTemperature(1e-300);
				gibbs.sample(20);
				gibbs.setTemperature(temperature);
			}
			else
			{
				gibbs.sample(20);
			}
			
			double sampleScore1 = gibbs.getSampleScore();
			
			ValueDataLayer dataLayer = gibbs.getSampleLayer();
			TestDataLayer.assertInvariants(dataLayer);
			for (Variable var : fg.getVariables())
			{
				assertNotNull(dataLayer.get(var));
			}
			
			DataStack dataStack = new DataStack(new PriorDataLayer(fg), dataLayer);
			assertEquals(sampleScore1, dataStack.computeTotalEnergy(), 1e-15);
			
			if (gibbs.getBestSampleScore() <= cutoffScore)
			{
//				System.out.println("required restarts: " + restart);
				break;
			}
		}

		int[] bestMotifs = new int[t];
		
		for (int i = 0; i < t; ++i)
		{
			GibbsDiscrete svar = requireNonNull((GibbsDiscrete) gibbs.getSolverVariable(motifVariables[i]));
			bestMotifs[i] = svar.getBestSampleIndex();
		}
		
		return bestMotifs;
		
	}
	
	private static class MotifScoreFunction extends FactorFunction
	{
		final boolean _useUpdateEnergy;
		private final int _motifLength;
		private final List<String> _strands;
		private final int[][] _countsByNucleotide;
		private int _nIncremented;
		
		private MotifScoreFunction(int motifLength, List<String> strands, boolean useUpdateEnergy)
		{
			_motifLength = motifLength;
			_strands = strands;
			_useUpdateEnergy = useUpdateEnergy;
			_countsByNucleotide = new int[4][];
			for (int i = 0; i < 4; ++i)
			{
				_countsByNucleotide[i] = new int[motifLength];
			}
		}
		
		@Override
		public double evalEnergy(Value[] values)
		{
			resetCounts();
			for (int i = 0, end = values.length; i < end; ++i)
			{
				int offset = values[i].getInt();
				incrementCount(_strands.get(i), offset);
			}
			
			return computeScore();
		}
		
		@Override
		public boolean useUpdateEnergy(Value[] values, int nChangedValues)
		{
			return _useUpdateEnergy;
		}
		
		@Override
		public double updateEnergy(Value[] values, IndexedValue[] oldValues, double oldEnergy)
		{
			for (IndexedValue oldValue : oldValues)
			{
				final int i = oldValue.getIndex();
				final String strand = _strands.get(i);
				final int oldOffset = oldValue.getValue().getIndex();
				final int newOffset = values[i].getIndex();
				decrementCount(strand, oldOffset);
				incrementCount(strand, newOffset);
			}

			return computeScore();
		}
		
		private int charToIndex(char c)
		{
			switch (c)
			{
			case 'A': return 0;
			case 'C': return 1;
			case 'G': return 2;
			case 'T': return 3;
			default: throw new RuntimeException("Bad character " + c);
			}
		}
		
		private void decrementCount(String strand, int start)
		{
			--_nIncremented;
			for (int i = 0; i < _motifLength; ++i)
			{
				--_countsByNucleotide[charToIndex(strand.charAt(start+i))][i];
			}
		}
		private void incrementCount(String strand, int start)
		{
			++_nIncremented;
			for (int i = 0; i < _motifLength; ++i)
			{
				++_countsByNucleotide[charToIndex(strand.charAt(start+i))][i];
			}
		}
		
		private void resetCounts()
		{
			_nIncremented = 0;
			for (int i = 0; i < 4; ++i)
			{
				Arrays.fill(_countsByNucleotide[i], 0);
			}
		}
		
		private int computeScore()
		{
			int score = 0;
			for (int i = 0; i < _motifLength; ++i)
			{
				int max = 0;
				for (int nucleotide = 0; nucleotide < 4; ++nucleotide)
				{
					max = Math.max(max, _countsByNucleotide[nucleotide][i]);
				}
				
				score += _nIncremented - max;
			}
			return score;
		}
	}
}
