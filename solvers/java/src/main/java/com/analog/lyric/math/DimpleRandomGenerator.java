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

package com.analog.lyric.math;

import org.apache.commons.math3.random.RandomGenerator;

import cern.jet.random.engine.RandomEngine;

public class DimpleRandomGenerator
{
	public static RandomGenerator rand = new org.apache.commons.math3.random.MersenneTwister();
	
	// Other random number generators not supported by the Apache framework
	private static RandomEngine randEngine = new cern.jet.random.engine.MersenneTwister(rand.nextInt());
	private static cern.jet.random.Gamma randGamma = new cern.jet.random.Gamma(1, 1, randEngine);
	private static cern.jet.random.Beta randBeta = new cern.jet.random.Beta(1, 1, randEngine);
	private static cern.jet.random.Binomial randBinomial = new cern.jet.random.Binomial(1, 0.5, randEngine);
	
	
	public static void setSeed(long seed)
	{
		rand.setSeed(seed);
		
		// WARNING: setting the seed creates new objects; which will not be used if reference to original object is cached
		randEngine = new cern.jet.random.engine.MersenneTwister((int)seed);
		randGamma = new cern.jet.random.Gamma(1, 1, randEngine);
		randBeta = new cern.jet.random.Beta(1, 1, randEngine);
		randBinomial = new cern.jet.random.Binomial(1, 0.5, randEngine);
	}
	
	public static double nextBeta(double alpha, double beta)
	{
		return randBeta.nextDouble(alpha, beta);
		//return DimpleEnvironment.activeRandom().nextBeta(alpha, beta);
	}

	public static boolean nextBoolean()
	{
		return rand.nextBoolean();
	}
	
	public static double nextDouble()
	{
		return rand.nextDouble();
	}
	
	public static double nextGamma(double alpha, double beta)
	{
		return randGamma.nextDouble(alpha, beta);
	}
	
	public static double nextGaussian()
	{
		return rand.nextGaussian();
	}
	
	public static int nextInt(int n)
	{
		return rand.nextInt(n);
	}
	
	public static long nextLong()
	{
		return rand.nextLong();
	}
	
	// randBinomial doesn't accept zero N value or 1 or 0 p value
	public static final int randomBinomial(int N, double p)
	{
		if (N <= 0)
			return 0;
		else if (p <= 0)
			return 0;
		else if (p >= 1)
			return N;
		else
			return randBinomial.nextInt(N, p);
	}

}
