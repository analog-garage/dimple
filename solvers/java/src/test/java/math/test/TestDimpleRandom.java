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

package math.test;

import static org.junit.Assert.*;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import com.analog.lyric.math.DimpleRandom;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public class TestDimpleRandom
{
	@Test
	public void testSeed()
	{
		DimpleRandom r1 = new DimpleRandom();
		DimpleRandom r2 = new DimpleRandom(r1.getSeed());

		// Trivial test to make sure that setting seed replicates results.
		assertEquals(r1.getSeed(), r2.getSeed());
		assertEquals(r1.nextDouble(), r2.nextDouble(), 0.0);
		
		r1.setSeed(42);
		r2.setSeed(42);
		assertEquals(r1.getSeed(), r2.getSeed());
		assertEquals(r1.nextDouble(), r2.nextDouble(), 0.0);
		
		for (int i = 0; i < 100; ++i)
		{
			assertEquals(r1.nextGamma(), r2.nextGamma(), 0.0);
			assertEquals(r1.nextGamma(.5,.7), r2.nextGamma(.5,.7), 0.0);
		}
	}

	/**
	 * Measures speed of Apache vs Colt generators
	 */
	public static void main(String[] args)
	{
		RandomGenerator apacheGenerator = new MersenneTwister(42);
		cern.jet.random.engine.RandomEngine cernGenerator = new cern.jet.random.engine.MersenneTwister();
		
		final int N = 100000;
		long start, end;
			
		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			apacheGenerator.nextDouble();
		}
		end = System.nanoTime();
		long apacheTime = end - start;
		
		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			cernGenerator.nextDouble();
		}
		end = System.nanoTime();
		
		long cernTime = end - start;
		
		System.out.format("MersenneTwister.nextDouble() x %d apache/cern %f\n", N, (double)apacheTime / cernTime);

		
		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			BetaDistribution apacheBeta = new BetaDistribution(apacheGenerator, 1.0, 1.0);
			apacheBeta.sample();
		}
		end = System.nanoTime();
		apacheTime = end - start;
		
		cern.jet.random.Beta cernBeta = new cern.jet.random.Beta(1,1,cernGenerator);

		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			cernBeta.nextDouble();
		}
		end = System.nanoTime();
		apacheTime = end - start;

		System.out.format("Beta x %d apache/cern %f\n", N, (double)apacheTime / cernTime);
		
		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			GammaDistribution apacheGamma = new GammaDistribution(apacheGenerator, 1.0, 1.0);
			apacheGamma.sample();
		}
		end = System.nanoTime();
		apacheTime = end - start;
		
		cern.jet.random.Gamma cernGamma = new cern.jet.random.Gamma(1,1,cernGenerator);

		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			cernGamma.nextDouble();
		}
		end = System.nanoTime();
		apacheTime = end - start;

		System.out.format("Gamma x %d apache/cern %f\n", N, (double)apacheTime / cernTime);
		
		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			BinomialDistribution apacheBinomial = new BinomialDistribution(apacheGenerator, 1, .5);
			apacheBinomial.sample();
		}
		end = System.nanoTime();
		apacheTime = end - start;
		
		cern.jet.random.Binomial cernBinomial = new cern.jet.random.Binomial(1,.5,cernGenerator);

		start = System.nanoTime();
		for (int i = N; --i>=0;)
		{
			cernBinomial.nextInt();
		}
		end = System.nanoTime();
		apacheTime = end - start;

		System.out.format("Binomial x %d apache/cern %f\n", N, (double)apacheTime / cernTime);
	}
}
