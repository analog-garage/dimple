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

package com.analog.lyric.dimple.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.analog.lyric.dimple.FactorFunctions.MixedNormal;
import com.analog.lyric.dimple.FactorFunctions.SimpleNormal;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.solvers.gibbs.SDiscreteVariable;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;


public class RealVariableGibbsTest
{
	protected static boolean debugPrint = false;
	protected static boolean repeatable = true;
	
	@Test
	public void basicTest1() 
	{
		if (debugPrint) System.out.println("== basicTest1 ==");

		int numSamples = 100000;
		int updatesPerSample = 10;
		int burnInUpdates = 1000;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumSamples(numSamples);
		solver.setUpdatesPerSample(updatesPerSample);
		solver.setBurnInUpdates(burnInUpdates);
		
		double aPriorMean = 1;
		double aPriorSigma = 0.5;
		double aPriorR = 1/(aPriorSigma*aPriorSigma);
		double bPriorMean = -1;
		double bPriorSigma = 2.;
		double bPriorR = 1/(bPriorSigma*bPriorSigma);
		Real a = new Real(new SimpleNormal(aPriorMean,aPriorSigma));
		Real b = new Real();
		b.setInputObject(new SimpleNormal(bPriorMean, bPriorSigma));	// Try setting the input differently, just to test a different path
		a.setName("a");
		b.setName("b");
		
		double abMean = 0;
		double abSigma = 1;
		double abR = 1/(abSigma*abSigma);
		graph.addFactor(new SimpleNormal(abMean,abSigma), a, b);
		
		SRealVariable sa = (SRealVariable)a.getSolver();
		SRealVariable sb = (SRealVariable)b.getSolver();
		
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
		
		assertTrue(nearlyEquals(aMean,0.8048546099515951));
		assertTrue(nearlyEquals(bMean,-0.21079379615278554));
		assertTrue(nearlyEquals((Double)sa.getBestSample(),0.7982352395977991));
		assertTrue(nearlyEquals((Double)sb.getBestSample(),-0.20088764481161117));
	}
	
	
	@Test
	public void basicTest2() 
	{
		if (debugPrint) System.out.println("== basicTest2 ==");
		
		int numSamples = 10000;
		int updatesPerSample = 10;
		int burnInUpdates = 1000;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumSamples(numSamples);
		solver.setUpdatesPerSample(updatesPerSample);
		solver.setBurnInUpdates(burnInUpdates);
		
		double aPriorMean = 0;
		double aPriorSigma = 5;
		double aPriorR = 1/(aPriorSigma*aPriorSigma);
		double bProb1 = 0.6;
		double bProb0 = 1 - bProb1;
		Real a = new Real(new SimpleNormal(aPriorMean,aPriorSigma));
//		Real a = new Real();
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
		graph.addFactor(new MixedNormal(fMean0, fSigma0, fMean1, fSigma1), a, b);
		
		SRealVariable sa = (SRealVariable)a.getSolver();
		SDiscreteVariable sb = (SDiscreteVariable)b.getSolver();
		
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
			for (Object s : aSamples) System.out.print((Double)s + " ");
			System.out.print("];\n");
		}

		
		double aExpectedMean = bProb0*(aPriorMean*aPriorR + fMean0*fR0)/(aPriorR + fR0) + bProb1*(aPriorMean*aPriorR + fMean1*fR1)/(aPriorR + fR1);
		if (debugPrint) System.out.println("aExpectedMean: " + aExpectedMean);
		if (debugPrint) System.out.println("bExpectedMean: " + bProb1);
		
		if (debugPrint) System.out.println("aBest: " + (Double)sa.getBestSample());
		if (debugPrint) System.out.println("bBest: " + (Integer)sb.getBestSample());
		
		assertTrue(nearlyEquals(aMean,0.18416769265598606));
		assertTrue(nearlyEquals(bMean,0.598));
		assertTrue(nearlyEquals((Double)sa.getBestSample(),0.9780485820519245));
		assertTrue((Integer)sb.getBestSample() == 1);
	}	
	
	
	private static double TOLLERANCE = 1e-12;
	private boolean nearlyEquals(double a, double b)
	{
		double diff = a - b;
		if (diff > TOLLERANCE) return false;
		if (diff < -TOLLERANCE) return false;
		return true;
	}

}
