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

package com.analog.lyric.dimple.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.RealDomain;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;

public class GibbsTestParameterizedNormal
{
	protected static boolean debugPrint = false;
	protected static boolean repeatable = true;

	@Test
	public void test1()
	{
		if (debugPrint) System.out.println("== test1 ==");

		int numNormalVariables = 1000;
		int numSamples = 1000;
		int updatesPerSample = 10;
		int burnInUpdates = 1000;

		FactorGraph graph = new FactorGraph();
		graph.setSolverFactory(new com.analog.lyric.dimple.solvers.gibbs.Solver());
		SFactorGraph solver = (SFactorGraph)graph.getSolver();
		solver.setNumSamples(numSamples);
		solver.setUpdatesPerSample(updatesPerSample);
		solver.setBurnInUpdates(burnInUpdates);
		
		// Generate data
		int seed = 1;
		Random r;
		if (repeatable)
			r = new Random(seed);
		else
			r = new Random();
		double modelMean = 27;
		double modelSigma = 14;
		double modelInverseVariance = 1/(modelSigma*modelSigma);
		double[] normalValues = new double[numNormalVariables];
		for (int i =0; i < numNormalVariables; i++)
			normalValues[i] = modelSigma*r.nextGaussian() + modelMean;
		if (debugPrint) System.out.println("ModelMean: " + (Double)modelMean);
		if (debugPrint) System.out.println("ModelInverseVariance: " + (Double)modelInverseVariance);

		
		Real vModelMean = new Real();
		Real vModelInverseVariance = new Real(new RealDomain(0, Double.POSITIVE_INFINITY));
		vModelMean.setName("Mean");
		vModelInverseVariance.setName("InverseVariance");

		Object[] vars = new Object[numNormalVariables + 2];
		int index = 0;
		vars[index++] = vModelMean;
		vars[index++] = vModelInverseVariance;
		for (int i = 0; i < numNormalVariables; i++)
			vars[index++] = normalValues[i];
			
		graph.addFactor(new Normal(), vars);


		SRealVariable svModelMean = (SRealVariable)vModelMean.getSolver();
		SRealVariable svModelInverseVariance = (SRealVariable)vModelInverseVariance.getSolver();



		if (repeatable) solver.setSeed(1);					// Make this repeatable
		graph.solve();


		// Best should be the same as the mean in this case
		if (debugPrint) System.out.println("vModelMeanBest: " + (Double)svModelMean.getBestSample());
		if (debugPrint) System.out.println("vModelInverseVarianceBest: " + (Double)svModelInverseVariance.getBestSample());

		assertTrue(nearlyEquals(svModelMean.getBestSample(),27.029282787511672));
		assertTrue(nearlyEquals(svModelInverseVariance.getBestSample(),0.005320791975254845));
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
