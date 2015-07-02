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

package com.analog.lyric.dimple.test.solvers.sumproduct;

import static com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.*;
import static java.util.Objects.*;
import static org.junit.Assert.*;

import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.random.CorrelatedRandomVectorGenerator;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.stat.correlation.StorelessCovariance;
import org.junit.Test;

import com.analog.lyric.dimple.factorfunctions.MultivariateNormal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.sugar.ModelSyntacticSugar.CurrentModel;
import com.analog.lyric.dimple.model.variables.Complex;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.MultivariateNormalParameters;
import com.analog.lyric.dimple.solvers.gibbs.GibbsOptions;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolverGraph;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.google.common.primitives.Doubles;

/**
 * Tests for sampled factors in SumProduct solver
 */
public class TestSampledFactors extends DimpleTestBase
{
	/**
	 * Adapted from MATLAB test4 in tests/algoGaussian/testSampledFactors.m
	 */
	@Test
	public void sampledComplexProduct()
	{
		// NOTE: test may fail if seed is changed! We keep the number of samples down so that the test doesn't
		// take too long. Increasing the samples produces better results.
		
		testRand.setSeed(42);
		
		try (CurrentModel cur = using(new FactorGraph()))
		{
			final Complex a = complex("a");
			final Complex b = complex("b");
			final Complex c = product(a,b);
			
			double[] aMean = new double[] { 10, 10 };
			RealMatrix aCovariance = randCovariance(2);
			a.setPrior(new MultivariateNormal(aMean, aCovariance.getData()));
			
			double[] bMean = new double[] { -20, 20 };
			RealMatrix bCovariance = randCovariance(2);
			b.setPrior(new MultivariateNormalParameters(bMean, bCovariance.getData()));

			GaussianRandomGenerator normalGenerator = new GaussianRandomGenerator(testRand);
			CorrelatedRandomVectorGenerator aGenerator =
				new CorrelatedRandomVectorGenerator(aMean, aCovariance, 1e-12, normalGenerator);
			CorrelatedRandomVectorGenerator bGenerator =
				new CorrelatedRandomVectorGenerator(bMean, bCovariance, 1e-12, normalGenerator);
			
			StorelessCovariance expectedCov = new StorelessCovariance(2);
			
			final int nSamples = 10000;
			
			RealVector expectedMean = MatrixUtils.createRealVector(new double[2]);
			double [] cSample = new double[2];
			
			for (int i = 0; i < nSamples; ++i)
			{
				double[] aSample = aGenerator.nextVector();
				double[] bSample = bGenerator.nextVector();

				// Compute complex product
				cSample[0] = aSample[0] * bSample[0] - aSample[1] * bSample[1];
				cSample[1] = aSample[0] * bSample[1] + aSample[1] * bSample[0];
				
				expectedMean.addToEntry(0, cSample[0]);
				expectedMean.addToEntry(1, cSample[1]);
				
				expectedCov.increment(cSample);
			}

			expectedMean.mapDivideToSelf(nSamples); // normalize
			
			SumProductSolverGraph sfg = requireNonNull(cur.graph.setSolverFactory(new SumProductSolver()));
			sfg.setOption(GibbsOptions.numSamples, nSamples);
			
			sfg.solve();

			MultivariateNormalParameters cBelief = requireNonNull(c.getBelief());
			
			RealVector observedMean = MatrixUtils.createRealVector(cBelief.getMean());
			double scaledMeanDistance = expectedMean.getDistance(observedMean) / expectedMean.getNorm();
			
//			System.out.format("expectedMean = %s\n", expectedMean);
//			System.out.format("observedMean = %s\n", observedMean);
//			System.out.println(scaledMeanDistance);

			assertEquals(0.0, scaledMeanDistance, .02);
			
			RealMatrix expectedCovariance = expectedCov.getCovarianceMatrix();
			RealMatrix observedCovariance = MatrixUtils.createRealMatrix(cBelief.getCovariance());
			RealMatrix diffCovariance = expectedCovariance.subtract(observedCovariance);
			
			double scaledCovarianceDistance = diffCovariance.getNorm() / expectedCovariance.getNorm();
				
//			System.out.println(expectedCovariance);
//			System.out.println(expectedCovariance.getNorm());
//			System.out.println(diffCovariance);
//			System.out.println(diffCovariance.getNorm());
//			System.out.println(diffCovariance.getNorm() / expectedCovariance.getNorm());
			
			assertEquals(0.0, scaledCovarianceDistance, .2);
		}
	}
	
	/**
	 * Generates a random covariance matrix with given dimension.
	 */
	RealMatrix randCovariance(int n)
	{
		RealMatrix A = MatrixUtils.createRealMatrix(n, n);
		
		// Randomize
		A.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
			@Override
			public double visit(int row, int column, double value)
			{
				return testRand.nextDouble();
			}
		});

		RealMatrix B = A.add(A.transpose()); // B is symmetric
		double minEig = Doubles.min(new EigenDecomposition(B).getRealEigenvalues());
		double r = testRand.nextGaussian();
		r *= r;
		r += Math.ulp(1.0);
		RealMatrix I = MatrixUtils.createRealIdentityMatrix(n);
		RealMatrix C = B.add(I.scalarMultiply(r - minEig));
		
		return C;
	}
}
