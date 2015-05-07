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

package com.analog.lyric.math;

import java.util.Random;

import net.jcip.annotations.NotThreadSafe;

import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.jdt.annotation.NonNullByDefault;

import cern.jet.random.engine.RandomEngine;

/**
 * Extended implementation of {@link Random} for use in Dimple
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
public class DimpleRandom extends RandomAdaptor
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	// Other random number generators not supported by the Apache framework
	private RandomEngine _randEngine;
	private cern.jet.random.Beta _randBeta;
	private cern.jet.random.Binomial _randBinomial;
	private cern.jet.random.Gamma _randGamma;
	private long _seed;
	
	/*--------------
	 * Construction
	 */
	
	public DimpleRandom(RandomGenerator randomGenerator, long seed)
	{
		super(randomGenerator);
		_seed = seed;
		_randEngine = new cern.jet.random.engine.MersenneTwister((int)seed);
		_randGamma = new cern.jet.random.Gamma(1, 1, _randEngine);
		_randBeta = new cern.jet.random.Beta(1, 1, _randEngine);
		_randBinomial = new cern.jet.random.Binomial(1, 0.5, _randEngine);
	}
	
	public DimpleRandom(RandomGenerator randomGenerator)
	{
		this(randomGenerator, randomGenerator.nextLong());
	}

	public DimpleRandom(long seed)
	{
		this(new org.apache.commons.math3.random.MersenneTwister(seed), seed);
	}
	
	/**
	 * Construct with randomly generated seed.
	 * 
	 * @since 0.08
	 */
	public DimpleRandom()
	{
		this(new org.apache.commons.math3.random.MersenneTwister());
		setSeed(nextLong());
	}

	/*----------------
	 * Random methods
	 */
	
	@Override
	public void setSeed(long seed)
	{
		super.setSeed(seed);
		_seed = seed;
		_randEngine = new cern.jet.random.engine.MersenneTwister((int)seed);
		_randGamma = new cern.jet.random.Gamma(1, 1, _randEngine);
		_randBeta = new cern.jet.random.Beta(1, 1, _randEngine);
		_randBinomial = new cern.jet.random.Binomial(1, 0.5, _randEngine);
	}
	
	@Override
	public void setSeed(int seed)
	{
		setSeed(seed | (long)seed << 32L);
	}
	
	@NonNullByDefault(false)
	@Override
	public void setSeed(int[] seed)
	{
		long lseed = 0;
		for (int i : seed)
		{
			lseed *= 13;
			lseed += i;
		}
		setSeed(lseed);
	}
	
	/*----------------------
	 * DimpleRandom methods
	 */
	
	/**
	 * Returns seed used to initialize random generator.
	 * <p>
	 * Returns seed set by one of the {@link #setSeed} methods or randomly generated in constructor.
	 * @since 0.08
	 */
	public long getSeed()
	{
		return _seed;
	}
	
	/**
	 * Returns sample from beta distribution with alpha and beta parameters set to one.
	 * @since 0.08
	 * @see #nextBeta(double, double)
	 */
	public double nextBeta()
	{
		return _randBeta.nextDouble();
	}
	
	/**
	 * Returns sample from beta distribution with specified alpha and beta parameters.
	 * @since 0.08
	 */
	public double nextBeta(double alpha, double beta)
	{
		return _randBeta.nextDouble(alpha, beta);
	}
	
	/**
	 * Returns sample from bernoulli distribution with parameter p set to .5.
	 * @since 0.08
	 * @see #nextBernoulli(double)
	 */
	public int nextBernoulli()
	{
		return _randBinomial.nextInt();
	}
	
	/**
	 * Returns sample from bernoulli distribution with given probability of drawing 1.
	 * @since 0.08
	 */
	public int nextBernoulli(double p)
	{
		return nextBinomial(1, p);
	}
	
	/**
	 * Returns sample from beta distribution with specified alpha and beta parameters.
	 * @since 0.08
	 */
	public int nextBinomial(int n, double p)
	{
		// randBinomial doesn't accept zero N value or 1 or 0 p value
		if (n <= 0)
			return 0;
		else if (p <= 0)
			return 0;
		else if (p >= 1)
			return n;
		else
			return _randBinomial.nextInt(n, p);
	}
	
	/**
	 * Returns boolean with given probability {@code p} of being {@code true}.
	 * @since 0.08
	 */
	public boolean nextBoolean(double p)
	{
		return nextBernoulli(p) == 1;
	}
	
	/**
	 * Returns sample from gamma distribution with alpha and beta parameters set to one.
	 * @since 0.08
	 * @see #nextBeta(double, double)
	 */
	public double nextGamma()
	{
		return _randGamma.nextDouble();
	}
	
	/**
	 * Returns sample from gamma distribution with specified alpha and beta parameters.
	 * @since 0.08
	 */
	public double nextGamma(double alpha, double beta)
	{
		return _randGamma.nextDouble(alpha, beta);
	}
	
	
}
