/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.apache.commons.math3.stat.StatUtils;
import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.factorfunctions.AdditiveNoise;
import com.analog.lyric.dimple.factorfunctions.MixedNormal;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.repeated.FactorFunctionDataSource;
import com.analog.lyric.dimple.model.repeated.FactorGraphStream;
import com.analog.lyric.dimple.model.repeated.RealStream;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.options.DimpleOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsDiscrete;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsReal;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverGraph;
import com.analog.lyric.dimple.solvers.gibbs.ISolverFactorGibbs;
import com.analog.lyric.dimple.test.DimpleTestBase;

public class RealVariableGibbsTest extends DimpleTestBase
{
	protected static boolean debugPrint = false;
	protected static boolean repeatable = true;
	
	@SuppressWarnings({ "deprecation", "null" })
	@Test
	public void basicTest1()
	{
		if (debugPrint) System.out.println("== basicTest1 ==");

		int numSamples = 10000;
		int updatesPerSample = 10;
		int burnInUpdates = 1000;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		GibbsSolverGraph solver = (GibbsSolverGraph)graph.getSolver();
		solver.setNumSamples(numSamples);
		solver.setUpdatesPerSample(updatesPerSample);
		solver.setBurnInUpdates(burnInUpdates);
		
		double aPriorMean = 1;
		double aPriorSigma = 0.5;
		double aPriorR = 1/(aPriorSigma*aPriorSigma);
		double bPriorMean = -1;
		double bPriorSigma = 2.;
		double bPriorR = 1/(bPriorSigma*bPriorSigma);
		Real a = new Real();
		Real b = new Real();
		a.setInputObject(new Normal(aPriorMean,aPriorR));
		b.setInputObject(new Normal(bPriorMean, bPriorR));
		a.setName("a");
		b.setName("b");
		
		double abMean = 0;
		double abSigma = 1;
		double abR = 1/(abSigma*abSigma);
		graph.addFactor(new Normal(abMean,abR), a, b);
		
		GibbsReal sa = (GibbsReal)a.getSolver();
		GibbsReal sb = (GibbsReal)b.getSolver();
		
		sa.setProposalStandardDeviation(0.1);
		sb.setProposalStandardDeviation(0.1);

		
		if (repeatable) solver.setSeed(1);					// Make this repeatable
		solver.saveAllSamples();
		graph.solve();


		double[] aSamples = sa.getAllSamples();
		double[] bSamples = sb.getAllSamples();
		double aSum = 0;
		for (Object s : aSamples) aSum += (Double)s;
		double aMean = aSum/aSamples.length;
		if (debugPrint) System.out.println("aSampleMean: " + aMean);
		double bSum = 0;
		for (Object s : bSamples) bSum += (Double)s;
		double bMean = bSum/bSamples.length;
		if (debugPrint) System.out.println("bSampleMean: " + bMean);
		
		double aExpectedMean = (aPriorMean*aPriorR + abMean*abR)/(aPriorR + abR);
		double bExpectedMean = (bPriorMean*bPriorR + abMean*abR)/(bPriorR + abR);
		if (debugPrint) System.out.println("aExpectedMean: " + aExpectedMean);
		if (debugPrint) System.out.println("bExpectedMean: " + bExpectedMean);
		
		// Best should be the same as the mean in this case
		if (debugPrint) System.out.println("aBest: " + (Double)sa.getBestSample());
		if (debugPrint) System.out.println("bBest: " + (Double)sb.getBestSample());
		
		assertEquals(aMean,0.8050875226168582,1e-12);
		assertEquals(bMean,-0.1921312702232493,1e-12);
		assertEquals(sa.getBestSample(),0.8043550661413381,1e-12);
		assertEquals(sb.getBestSample(),-0.20700427734616236,1e-12);
	}
	
	
	@SuppressWarnings("deprecation")
	@Test
	public void basicTest2()
	{
		if (debugPrint) System.out.println("== basicTest2 ==");
		
		int numSamples = 10000;
		int updatesPerSample = 10;
		int burnInUpdates = 1000;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		GibbsSolverGraph solver = requireNonNull((GibbsSolverGraph)graph.getSolver());
		solver.setNumSamples(numSamples);
		solver.setUpdatesPerSample(updatesPerSample);
		solver.setBurnInUpdates(burnInUpdates);
		
		double aPriorMean = 0;
		double aPriorSigma = 5;
		double aPriorR = 1/(aPriorSigma*aPriorSigma);
		double bProb1 = 0.6;
		double bProb0 = 1 - bProb1;
		Real a = new Real();
		a.setInputObject(new Normal(aPriorMean,aPriorR));
		Discrete b = new Discrete(0,1);
		b.setInput(bProb0, bProb1);
		a.setName("a");
		b.setName("b");
		
		double fMean0 = -1;
		double fSigma0 = 0.75;
		double fMean1 = 1;
		double fSigma1 = 0.75;
		double fR0 = 1/(fSigma0*fSigma0);
		double fR1 = 1/(fSigma1*fSigma1);
		graph.addFactor(new MixedNormal(fMean0, fR0, fMean1, fR1), a, b);
		
		GibbsReal sa = requireNonNull((GibbsReal)a.getSolver());
		GibbsDiscrete sb = requireNonNull((GibbsDiscrete)b.getSolver());
		
		sa.setProposalStandardDeviation(1.0);

		
		if (repeatable) solver.setSeed(1);					// Make this repeatable
		solver.saveAllSamples();
		graph.solve();


		double[] aSamples = sa.getAllSamples();
		Object[] bSamples = sb.getAllSamples();
		double aSum = 0;
		for (Object s : aSamples) aSum += (Double)s;
		double aMean = aSum/aSamples.length;
		if (debugPrint) System.out.println("aSampleMean: " + aMean);
		double bSum = 0;
		for (Object s : bSamples) bSum += (Integer)s;
		double bMean = bSum/bSamples.length;
		if (debugPrint) System.out.println("bSampleMean: " + bMean);
		
		if (debugPrint)
		{
			System.out.print("a = [");
			for (Object s : aSamples) System.out.print(s + " ");
			System.out.print("];\n");
		}

		
		double aExpectedMean = bProb0*(aPriorMean*aPriorR + fMean0*fR0)/(aPriorR + fR0) + bProb1*(aPriorMean*aPriorR + fMean1*fR1)/(aPriorR + fR1);
		if (debugPrint) System.out.println("aExpectedMean: " + aExpectedMean);
		if (debugPrint) System.out.println("bExpectedMean: " + bProb1);
		
		if (debugPrint) System.out.println("aBest: " + (Double)sa.getBestSample());
		if (debugPrint) System.out.println("bBest: " + sb.getBestSample());
		
		assertEquals(aMean,0.20867216566185906, 1e-12);
		assertEquals(bMean,0.6055,1e-12);
		assertEquals(sa.getBestSample(),0.977986266650138,1e-12);
		assertTrue((Integer)sb.getBestSample() == 1);
	}
	
	@Test
	public void testBeliefMoments()
	{
		// Java version of MATLAB testBeliefMoments/test1
		
		
		// Construct model
		final FactorGraph fg = new FactorGraph();
		
		Real a, b, x, y;
		
		try (CurrentModel current = using(fg))
		{
			a = name("a", normal(0, 1));
			b = name("b", gamma(1, 1));
			x = name("x", square(sum(a, b)));
			y = name("y", sum(x, square(log(lognormal(2,7)))));
		}
			
		// Set data
		y.setFixedValue(5);
		
		// Configure Gibbs options
		// TODO: test is sensitive to choice of seed! Perhaps we should increase numSamples or something...
		fg.setOption(DimpleOptions.randomSeed, 1L);
		fg.setOption(GibbsOptions.numSamples, 100);
		fg.setOption(GibbsOptions.burnInScans, 10);
		
		// Run the solver without saving samples.
		GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		fg.setOption(GibbsOptions.saveAllSamples, false);
		fg.solve();
		
		GibbsReal sa = sfg.getReal(a);
		GibbsReal sb = sfg.getReal(b);
		GibbsReal sx = sfg.getReal(x);
		GibbsReal sy = sfg.getReal(y);
		
		double aMean = sa.getSampleMean();
		double aVariance = sa.getSampleVariance();
		double bMean = sb.getSampleMean();
		double bVariance = sb.getSampleVariance();
		double xMean = sx.getSampleMean();
		double xVariance = sx.getSampleVariance();
		double yMean = sy.getSampleMean();
		double yVariance = sy.getSampleVariance();
		
		// Run the solver again, this time saving all samples
		fg.setOption(GibbsOptions.saveAllSamples, true);
		fg.solve();
	
		double[] aSamples = sa.getAllSamples();
		double[] bSamples = sb.getAllSamples();
		double[] xSamples = sx.getAllSamples();
		double[] ySamples = sy.getAllSamples();

		assertEquals(aMean, StatUtils.mean(aSamples), 1e-13);
		assertEquals(bMean, StatUtils.mean(bSamples), 1e-13);
		assertEquals(xMean, StatUtils.mean(xSamples), 1e-13);
		assertEquals(yMean, StatUtils.mean(ySamples), 1e-13);
		assertEquals(aVariance, StatUtils.variance(aSamples), 1e-13);
		assertEquals(bVariance, StatUtils.variance(bSamples), 1e-13);
		assertEquals(xVariance, StatUtils.variance(xSamples), 1e-13);
		assertEquals(yVariance, StatUtils.variance(ySamples), 1e-13);
		
		assertEquals(y.getFixedValue(), yMean, 0.0);
		assertEquals(0.0, yVariance, 0.0);
		
		// Make sure moments are the same the next time
		assertEquals(aMean, sa.getSampleMean(), 0.0);
		assertEquals(aVariance, sa.getSampleVariance(), 0.0);
	}
	
	@Test
	public void testRealRolledUp()
	{
		// Java version of MATLAB testRealRolledUp.m
		
		// FIXME - test is highly dependent on value of seed!
		
		// Graph parameters
		final boolean useSeed = true;
		final long seed = 45L;
		final int hmmLength = 20;
		final int bufferSize = 10;
		
		// Gibbs parameters
		DimpleEnvironment env = DimpleEnvironment.active();
		env.setOption(GibbsOptions.numSamples, 10000);
		env.setOption(GibbsOptions.burnInScans, 100);
		env.setOption(GibbsOptions.numRandomRestarts, 0);
		
		if (useSeed)
		{
			testRand.setSeed(seed);
			env.setOption(DimpleOptions.randomSeed, seed);
		}
		System.out.println(testRand.getSeed());
		
		// Model parameters
		final double initialMean = 0.0;
		final double initialSigma = 20.0;
		final double transitionMean = 0.0;
		final double transitionSigma = 0.1;
		final double obsMean = 0.0;
		final double obsSigma = 1.0;
		
		// Sample from system to be estimated
		final double[] x = new double[hmmLength];
		x[0] = testRand.nextGaussian() * initialSigma + initialMean;
		for (int i = 1; i < hmmLength; ++i)
		{
			x[i] = x[i-1] + testRand.nextGaussian() * transitionSigma + transitionMean;
		}
		
		final double[] obsNoise = new double[hmmLength];
		final double[] o = x.clone();
		for (int i = 0; i < hmmLength; ++i)
		{
			o[i] += obsNoise[i] = testRand.nextGaussian() * obsSigma + obsMean;
		}
		
		// Solve using Gibbs
		
		final FactorGraph sg = new FactorGraph();
		Real Xo, Xi, Ob;
		try (CurrentModel cur = using(sg))
		{
			Xo = boundary(real("Xo"));
			Xi = boundary(real("Xi"));
			Ob = boundary(real("Ob"));
			name("transitionNoise", addFactor(new AdditiveNoise(transitionSigma), Xo, Xi));
			name("observationNoise", addFactor(new AdditiveNoise(obsSigma), Ob, Xi));
		}
		
		FactorGraph fg = name("fg", new FactorGraph());
		RealStream X = new RealStream("X"), O = new RealStream("O");
		FactorGraphStream f = fg.addRepeatedFactor(sg, X, X.getSlice(1), O);
		f.setBufferSize(bufferSize);
		
		// Solve
		final GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));
		int inputIndex = 0, outputIndex = 0;
		final double[] output = new double[hmmLength];
		
		for (int j = 0, end = O.size(); j < end; ++j)
		{
			O.get(j).setFixedValue(o[inputIndex++]);
		}
		
		fg.initialize();
		fg.setNumSteps(0);
		
		final GibbsReal X0 = sfg.getReal(X.get(0));
		final ISolverFactorGibbs X0first = X0.getSibling(0);
		final ISolverFactorGibbs X0next = X0.getSibling(1);
		final GibbsReal Olast = sfg.getReal(O.get(O.size()-1));

		final int ln = hmmLength - bufferSize;
		for (int i = 0; i < ln; ++i)
		{
			fg.solveOneStep();
			output[outputIndex++] = X0.getBestSample();
			
			if (!fg.hasNext())
			{
				break;
			}
			
			final double tmp = X0first.getPotential();
			fg.advance();
			assertEquals(tmp, X0next.getPotential(), 0.0);
			Olast.setAndHoldSampleValue(o[inputIndex++]);
		}
		
		final double[] actualdiff = new double[hmmLength];
		final double[] obsdiff = new double[hmmLength];
		double actualnorm = 0.0, obsnorm = 0.0;
		for (int i = 0; i < ln; ++i)
		{
			double diff = actualdiff[i] = x[i] - output[i];
			actualnorm += diff * diff;
			diff = obsdiff[i] = x[i] - o[i];
			obsnorm += diff * diff;
		}
		
		actualnorm = Math.sqrt(actualnorm);
		obsnorm = Math.sqrt(obsnorm);
		
		assertTrue(actualnorm < 1.0);
		assertTrue(obsnorm > 3.0);
	}
	
	@Test
	public void testRolledUpBeliefMoments()
	{
		// Java version of MATLAB testBeliefMoments/test3

		// Construct model
		final int numDataPoints = 10;
		final double dataPrecision = 1e4;
		final double transitionPrecision = 10;

		final FactorGraph fg = new FactorGraph();
		fg.setName("root");
		
		final GibbsSolverGraph sfg = requireNonNull(fg.setSolverFactory(new GibbsSolver()));

		final FactorGraph nfg = new FactorGraph();
		nfg.setName("nested");
		
		try (CurrentModel current = using(nfg))
		{
			Real x = boundary(real("x"));
			/*Real y = */ boundary(name("y",normal(name("x11",product(x, 1.1)), transitionPrecision)));
		}

		final RealStream vars = new RealStream("r");
		fg.addRepeatedFactor(nfg, vars, vars.getSlice(1));

		FactorFunctionDataSource dataSource = new FactorFunctionDataSource();
		for (int i = 0; i < numDataPoints; ++i)
		{
			dataSource.add(new Normal(1.0, dataPrecision));
		}
		vars.setDataSource(dataSource);
		
		// Configure Gibbs
		DimpleEnvironment env = DimpleEnvironment.active();
		env.setOption(DimpleOptions.randomSeed, 2L);
		env.setOption(GibbsOptions.numSamples,  3000);
		env.setOption(GibbsOptions.burnInScans, 10);
		
		fg.initialize();
		
		// Construct second model
		final FactorGraph fg2 = new FactorGraph();
		Real[] r = new Real[numDataPoints];
		
		try (CurrentModel current = using(fg2))
		{
			r[0] = real("r0");
			for (int i = 1; i < numDataPoints; ++i)
			{
				r[i] = name("r"+i, normal(name("r"+i+"x11", product(r[i-1], 1.1)), transitionPrecision));
			}
		}
		
		// Configure Gibbs
		final GibbsSolverGraph sfg2 = requireNonNull(fg2.setSolverFactory(new GibbsSolver()));
		// Options inherited from environment
		
		// Run
		fg.setNumSteps(0);
		for (int i = 0; fg.hasNext(); fg.advance(), ++i)
		{
			r[i].setInput(new Normal(1, dataPrecision));
			r[i+1].setInput(new Normal(1, dataPrecision));

			fg.solveOneStep();
			fg2.solve();
			
			GibbsReal a = sfg.getReal(vars.get(0));
			GibbsReal b = sfg.getReal(vars.get(1));
			GibbsReal a2 = sfg2.getReal(r[i]);
			GibbsReal b2 = sfg2.getReal(r[i+1]);

			double a2mean = a2.getSampleMean();
			double amean = a.getSampleMean();
			double b2mean = b2.getSampleMean();
			double bmean = b.getSampleMean();
			
			double a2variance = a2.getSampleVariance();
			double avariance = a.getSampleVariance();
			double b2variance = b2.getSampleVariance();
			double bvariance = b.getSampleVariance();
			
//			System.out.format("%d: a2 %f/%f\n", i, a2mean, a2variance);
//			System.out.format("%d:  a %f/%f\n", i, amean, avariance);
//
//			System.out.format("%d: b2 %f/%f\n", i, b2mean, b2variance);
//			System.out.format("%d:  b %f/%f\n", i, bmean, bvariance);
			
			assertEquals(0.0, a2mean - amean, 0.01);
			assertEquals(0.0, 1.0 - a2variance / avariance, 0.1);
			assertEquals(0.0, b2mean - bmean, 0.01);
			assertEquals(0.0, 1.0 - b2variance / bvariance, 0.1);
		}
	}
}
