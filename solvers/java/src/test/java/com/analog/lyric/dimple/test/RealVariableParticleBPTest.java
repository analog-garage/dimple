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
import com.analog.lyric.dimple.FactorFunctions.Normal;
import com.analog.lyric.dimple.FactorFunctions.Sum;
import com.analog.lyric.dimple.model.Discrete;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.Real;
import com.analog.lyric.dimple.model.RealDomain;
import com.analog.lyric.dimple.solvers.particleBP.SFactorGraph;
import com.analog.lyric.dimple.solvers.particleBP.SRealVariable;


public class RealVariableParticleBPTest
{
	protected static boolean debugPrint = false;
	protected static boolean repeatable = true;
	
	@Test
	public void basicTest1() 
	{
		if (debugPrint) System.out.println("== basicTest1 ==");

		int numIterations = 10;
		int numParticlesPerRealVariable = 200;
		int numResamplingUpdatesPerParticle = 50;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.particleBP.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumIterations(numIterations);
		solver.setNumParticles(numParticlesPerRealVariable);
		solver.setResamplingUpdatesPerParticle(numResamplingUpdatesPerParticle);

		
		double aPriorMean = 1;
		double aPriorSigma = 0.5;
		double aPriorR = 1/(aPriorSigma*aPriorSigma);
		double bPriorMean = -1;
		double bPriorSigma = 2.;
		double bPriorR = 1/(bPriorSigma*bPriorSigma);
		Real a = new Real(new Normal(aPriorMean,aPriorSigma));
		Real b = new Real();
		b.setInputObject(new Normal(bPriorMean, bPriorSigma));	// Try setting the input differently, just to test a different path
		a.setName("a");
		b.setName("b");
		
		double abMean = 0;
		double abSigma = 1;
		double abR = 1/(abSigma*abSigma);
		graph.addFactor(new Normal(0,1), a, b);
		
		SRealVariable sa = (SRealVariable)a.getSolver();
		SRealVariable sb = (SRealVariable)b.getSolver();
		
		sa.setProposalStandardDeviation(0.5);
		sb.setProposalStandardDeviation(0.5);
		

		if (repeatable) solver.setSeed(1);					// Make this repeatable
		graph.solve();


		double[] aBelief = (double[])a.getBeliefObject();
		double[] bBelief = (double[])b.getBeliefObject();
		double[] aParticles = sa.getParticleValues();
		double[] bParticles = sb.getParticleValues();
		if (debugPrint)
		{
			System.out.print("aBelief = [");
			for (int i = 0; i < aBelief.length; i++) System.out.print(aBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bBelief = [");
			for (int i = 0; i < bBelief.length; i++) System.out.print(bBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("aParticles = [");
			for (int i = 0; i < aParticles.length; i++) System.out.print(aParticles[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bParticles = [");
			for (int i = 0; i < bParticles.length; i++) System.out.print(bParticles[i] + " ");
			System.out.print("];\n");
		}
		
		
		int aNumPoints = 500;
		double aLower = -3;
		double aUpper = 3;
		double[] aUniformPointSet = new double[aNumPoints];
		for (int i = 0; i < aNumPoints; i++)
			aUniformPointSet[i] = aLower + i*(aUpper-aLower)/aNumPoints;
		double[] aUniformBelief = (double[])sa.getBelief(aUniformPointSet);

		int bNumPoints = 500;
		double bLower = -3;
		double bUpper = 3;
		double[] bUniformPointSet = new double[bNumPoints];
		for (int i = 0; i < bNumPoints; i++)
			bUniformPointSet[i] = bLower + i*(bUpper-bLower)/bNumPoints;
		double[] bUniformBelief = (double[])sb.getBelief(bUniformPointSet);

		if (debugPrint)
		{
			System.out.print("aUniformBelief = [");
			for (int i = 0; i < aUniformBelief.length; i++) System.out.print(aUniformBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("aUniformPointSet = [");
			for (int i = 0; i < aUniformPointSet.length; i++) System.out.print(aUniformPointSet[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bUniformBelief = [");
			for (int i = 0; i < bUniformBelief.length; i++) System.out.print(bUniformBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bUniformPointSet = [");
			for (int i = 0; i < bUniformPointSet.length; i++) System.out.print(bUniformPointSet[i] + " ");
			System.out.print("];\n");
		}

		double aSolverMean = 0;
		for (int i = 0; i < aUniformPointSet.length; i++) aSolverMean += aUniformPointSet[i] * aUniformBelief[i];
		double bSolverMean = 0;
		for (int i = 0; i < bUniformPointSet.length; i++) bSolverMean += bUniformPointSet[i] * bUniformBelief[i];
		if (debugPrint) System.out.println("aSolverMean: " + aSolverMean);
		if (debugPrint) System.out.println("bSolverMean: " + bSolverMean);


		
		double aExpectedMean = (aPriorMean*aPriorR + abMean*abR)/(aPriorR + abR);
		double bExpectedMean = (bPriorMean*bPriorR + abMean*abR)/(bPriorR + abR);
		if (debugPrint) System.out.println("aExpectedMean: " + aExpectedMean);
		if (debugPrint) System.out.println("bExpectedMean: " + bExpectedMean);
		
		
		assertTrue(nearlyEquals(aSolverMean,0.7999989412684679));
		assertTrue(nearlyEquals(bSolverMean,-0.19800348473801446));
	}
	
	
	@Test
	public void basicTest2() 
	{
		// Test a combination of real and discrete variables connected to a single factor
		if (debugPrint) System.out.println("== basicTest2 ==");
		
		int numIterations = 10;
		int numParticlesPerRealVariable = 200;
		int numResamplingUpdatesPerParticle = 50;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.particleBP.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumIterations(numIterations);
		solver.setNumParticles(numParticlesPerRealVariable);
		solver.setResamplingUpdatesPerParticle(numResamplingUpdatesPerParticle);


		double aPriorMean = 0;
		double aPriorSigma = 5;
		double aPriorR = 1/(aPriorSigma*aPriorSigma);
		double bProb1 = 0.6;
		double bProb0 = 1 - bProb1;
		Real a = new Real(new Normal(aPriorMean,aPriorSigma));
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
		//SVariable sb = (SVariable)b.getSolver();
		
		sa.setProposalStandardDeviation(1.0);

		
		if (repeatable) solver.setSeed(1);					// Make this repeatable
		graph.solve();

		int aNumPoints = 100;
		double aLower = -3;
		double aUpper = 3;
		double[] aUniformPointSet = new double[aNumPoints];
		for (int i = 0; i < aNumPoints; i++)
			aUniformPointSet[i] = aLower + i*(aUpper-aLower)/aNumPoints;
		double[] aUniformBelief = (double[])sa.getBelief(aUniformPointSet);
		double[] aBelief = (double[])a.getBeliefObject();
		double[] bBelief = (double[])b.getBeliefObject();
		double[] aParticles = sa.getParticleValues();
		Object[] bDomain = b.getDiscreteDomain().getElements();
		if (debugPrint)
		{
			System.out.print("aUniformBelief = [");
			for (int i = 0; i < aUniformBelief.length; i++) System.out.print(aUniformBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("aUniformPointSet = [");
			for (int i = 0; i < aUniformPointSet.length; i++) System.out.print(aUniformPointSet[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("aBelief = [");
			for (int i = 0; i < aBelief.length; i++) System.out.print(aBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("aParticles = [");
			for (int i = 0; i < aParticles.length; i++) System.out.print(aParticles[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bBelief = [");
			for (int i = 0; i < bBelief.length; i++) System.out.print(bBelief[i] + " ");
			System.out.print("];\n");
		}
		double aSolverMean = 0;
		for (int i = 0; i < aUniformPointSet.length; i++) aSolverMean += aUniformPointSet[i] * aUniformBelief[i];
		double bSolverMean = 0;
		for (int i = 0; i < bDomain.length; i++) bSolverMean += (Integer)(bDomain[i]) * bBelief[i];
		if (debugPrint) System.out.println("aSolverMean: " + aSolverMean);
		if (debugPrint) System.out.println("bSolverMean: " + bSolverMean);


		
		double aExpectedMean = bProb0*(aPriorMean*aPriorR + fMean0*fR0)/(aPriorR + fR0) + bProb1*(aPriorMean*aPriorR + fMean1*fR1)/(aPriorR + fR1);
		if (debugPrint) System.out.println("aExpectedMean: " + aExpectedMean);
		if (debugPrint) System.out.println("bExpectedMean: " + bProb1);
		
		
		assertTrue(nearlyEquals(aSolverMean,0.1929829696757485));
		assertTrue(nearlyEquals(bSolverMean,0.5833220375555341));
	}
	
	
	@Test
	public void basicTest3() 
	{
		// Test particle initialization based on the domain and setInitialParticleRange, which should override the domain
		if (debugPrint) System.out.println("== basicTest3 ==");
		
		int numIterations = 1;
		int numParticlesPerRealVariable = 100;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.particleBP.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumIterations(numIterations);
		solver.setNumParticles(numParticlesPerRealVariable);

		
		Real a = new Real();
		Real b = new Real(new RealDomain(-10,10));
		Real c = new Real(new RealDomain(-20,20));
		Real d = new Real();
		a.setName("a");
		b.setName("b");
		c.setName("c");
		d.setName("d");
		graph.addFactor(new Normal(0,1), a, b, c, d);
		
		SRealVariable sa = (SRealVariable)a.getSolver();
		SRealVariable sb = (SRealVariable)b.getSolver();
		SRealVariable sc = (SRealVariable)c.getSolver();
		SRealVariable sd = (SRealVariable)d.getSolver();
		sa.setInitialParticleRange(-11, 14);
		sb.setInitialParticleRange(0, 7);
		
		
		graph.resetMessages();
				
		double[] aParticles = sa.getParticleValues();
		double[] bParticles = sb.getParticleValues();
		double[] cParticles = sc.getParticleValues();
		double[] dParticles = sd.getParticleValues();
		if (debugPrint)
		{
			System.out.print("aParticles = [");
			for (int i = 0; i < aParticles.length; i++) System.out.print(aParticles[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bParticles = [");
			for (int i = 0; i < bParticles.length; i++) System.out.print(bParticles[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("cParticles = [");
			for (int i = 0; i < cParticles.length; i++) System.out.print(cParticles[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("dParticles = [");
			for (int i = 0; i < dParticles.length; i++) System.out.print(dParticles[i] + " ");
			System.out.print("];\n");
		}
		assertTrue(nearlyEquals(aParticles[0],-11));
		assertTrue(nearlyEquals(aParticles[numParticlesPerRealVariable-1],14));
		assertTrue(nearlyEquals(bParticles[0],0));
		assertTrue(nearlyEquals(bParticles[numParticlesPerRealVariable-1],7));
		assertTrue(nearlyEquals(cParticles[0],-20));
		assertTrue(nearlyEquals(cParticles[numParticlesPerRealVariable-1],20));
		assertTrue(nearlyEquals(dParticles[0],0));
		assertTrue(nearlyEquals(dParticles[numParticlesPerRealVariable-1],0));
	}	
	
	
	@Test
	public void basicTest4() 
	{
		// Test tempering
		if (debugPrint) System.out.println("== basicTest4 ==");

		int numIterations = 50;
		int numParticlesPerRealVariable = 20;
		int numResamplingUpdatesPerParticle = 10;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.particleBP.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumIterations(numIterations);

		double aPriorMean = 1;
		double aPriorSigma = 0.1;
		double bPriorMean = 2;
		double bPriorSigma = 0.1;
		Real a = new Real(new Normal(aPriorMean,aPriorSigma));
		Real b = new Real(new Normal(bPriorMean,bPriorSigma));
		Real c = new Real();
		a.setName("a");
		b.setName("b");
		c.setName("c");
		
		graph.addFactor(new Sum(1.0),c,b,a);
				
		SRealVariable sa = (SRealVariable)a.getSolver();
		SRealVariable sb = (SRealVariable)b.getSolver();
		SRealVariable sc = (SRealVariable)c.getSolver();
		
		sa.setProposalStandardDeviation(0.1);
		sb.setProposalStandardDeviation(0.1);
		sc.setProposalStandardDeviation(0.1);

		sa.setInitialParticleRange(0, 2);
		sb.setInitialParticleRange(1, 3);
		sc.setInitialParticleRange(2, 4);

		// Test setting this after the variables have already been created
		solver.setNumParticles(numParticlesPerRealVariable);
		solver.setResamplingUpdatesPerParticle(numResamplingUpdatesPerParticle);
		

		
		// Enable tempering
		double initialTemperature = 1.0;
		double temperingHalfLifeInIterations = 5;
		solver.setInitialTemperature(initialTemperature);
		solver.setTemperingHalfLifeInIterations(temperingHalfLifeInIterations);
		assertTrue(solver.isTemperingEnabled());			// Make sure this automatically enabled tempering
		
		if (repeatable) solver.setSeed(1);					// Make this repeatable
		graph.solve();


		double[] aBelief = (double[])a.getBeliefObject();
		double[] bBelief = (double[])b.getBeliefObject();
		double[] cBelief = (double[])c.getBeliefObject();
		double[] aParticles = sa.getParticleValues();
		double[] bParticles = sb.getParticleValues();
		double[] cParticles = sc.getParticleValues();
		if (debugPrint)
		{
			System.out.print("aBelief = [");
			for (int i = 0; i < aBelief.length; i++) System.out.print(aBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bBelief = [");
			for (int i = 0; i < bBelief.length; i++) System.out.print(bBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("cBelief = [");
			for (int i = 0; i < cBelief.length; i++) System.out.print(cBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("aParticles = [");
			for (int i = 0; i < aParticles.length; i++) System.out.print(aParticles[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bParticles = [");
			for (int i = 0; i < bParticles.length; i++) System.out.print(bParticles[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("cParticles = [");
			for (int i = 0; i < cParticles.length; i++) System.out.print(cParticles[i] + " ");
			System.out.print("];\n");
		}

		
		int aNumPoints = 500;
		double aLower = -4;
		double aUpper = 4;
		double[] aUniformPointSet = new double[aNumPoints];
		for (int i = 0; i < aNumPoints; i++)
			aUniformPointSet[i] = aLower + i*(aUpper-aLower)/aNumPoints;
		double[] aUniformBelief = (double[])sa.getBelief(aUniformPointSet);

		int bNumPoints = 500;
		double bLower = -4;
		double bUpper = 4;
		double[] bUniformPointSet = new double[bNumPoints];
		for (int i = 0; i < bNumPoints; i++)
			bUniformPointSet[i] = bLower + i*(bUpper-bLower)/bNumPoints;
		double[] bUniformBelief = (double[])sb.getBelief(bUniformPointSet);

		int cNumPoints = 500;
		double cLower = -4;
		double cUpper = 4;
		double[] cUniformPointSet = new double[cNumPoints];
		for (int i = 0; i < cNumPoints; i++)
			cUniformPointSet[i] = cLower + i*(cUpper-cLower)/cNumPoints;
		double[] cUniformBelief = (double[])sc.getBelief(cUniformPointSet);

		
		if (debugPrint)
		{
			System.out.print("aUniformBelief = [");
			for (int i = 0; i < aUniformBelief.length; i++) System.out.print(aUniformBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("aUniformPointSet = [");
			for (int i = 0; i < aUniformPointSet.length; i++) System.out.print(aUniformPointSet[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bUniformBelief = [");
			for (int i = 0; i < bUniformBelief.length; i++) System.out.print(bUniformBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("bUniformPointSet = [");
			for (int i = 0; i < bUniformPointSet.length; i++) System.out.print(bUniformPointSet[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("cUniformBelief = [");
			for (int i = 0; i < cUniformBelief.length; i++) System.out.print(cUniformBelief[i] + " ");
			System.out.print("];\n");
		}
		if (debugPrint)
		{
			System.out.print("cUniformPointSet = [");
			for (int i = 0; i < cUniformPointSet.length; i++) System.out.print(cUniformPointSet[i] + " ");
			System.out.print("];\n");
		}


		double aSolverMean = 0;
		for (int i = 0; i < aUniformPointSet.length; i++) aSolverMean += aUniformPointSet[i] * aUniformBelief[i];
		double bSolverMean = 0;
		for (int i = 0; i < bUniformPointSet.length; i++) bSolverMean += bUniformPointSet[i] * bUniformBelief[i];
		double cSolverMean = 0;
		for (int i = 0; i < cUniformPointSet.length; i++) cSolverMean += cUniformPointSet[i] * cUniformBelief[i];
		if (debugPrint) System.out.println("aSolverMean: " + aSolverMean);
		if (debugPrint) System.out.println("bSolverMean: " + bSolverMean);
		if (debugPrint) System.out.println("cSolverMean: " + cSolverMean);


		double aSolverVariance = 0;
		for (int i = 0; i < aUniformPointSet.length; i++)
		{
			double pointDifference = aUniformPointSet[i] - aSolverMean;
			aSolverVariance += pointDifference * pointDifference * aUniformBelief[i];
		}
		double aSolverStdMeasured = Math.sqrt(aSolverVariance);
		double bSolverVariance = 0;
		for (int i = 0; i < bUniformPointSet.length; i++)
		{
			double pointDifference = bUniformPointSet[i] - bSolverMean;
			bSolverVariance += pointDifference * pointDifference * bUniformBelief[i];
		}
		double bSolverStdMeasured = Math.sqrt(bSolverVariance);
		double cSolverVariance = 0;
		for (int i = 0; i < cUniformPointSet.length; i++)
		{
			double pointDifference = cUniformPointSet[i] - cSolverMean;
			cSolverVariance += pointDifference * pointDifference * cUniformBelief[i];
		}
		double cSolverStdMeasured = Math.sqrt(cSolverVariance);
		if (debugPrint) System.out.println("aSolverStdMeasured: " + aSolverStdMeasured);
		if (debugPrint) System.out.println("bSolverStdMeasured: " + bSolverStdMeasured);
		if (debugPrint) System.out.println("cSolverStdMeasured: " + cSolverStdMeasured);
		
		
		double cExpectedMean = aPriorMean + bPriorMean;
		double cExpectedStd = Math.sqrt(aPriorSigma*aPriorSigma + bPriorSigma*bPriorSigma);
		if (debugPrint) System.out.println("cExpectedMean: " + cExpectedMean);
		if (debugPrint) System.out.println("cExpectedStd: " + cExpectedStd);
		
		assertTrue(Math.abs(cSolverMean - 3.0) < 0.1);
		assertTrue(cSolverStdMeasured <= cExpectedStd + 0.01);
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
