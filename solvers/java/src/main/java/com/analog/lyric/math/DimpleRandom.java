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

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.analog.lyric.dimple.environment.DimpleEnvironment;

import cern.jet.random.engine.RandomEngine;
import net.jcip.annotations.NotThreadSafe;

/**
 * Extended implementation of {@link Random} for use in Dimple
 * <p>
 * Most users will want to use {@link DimpleEnvironment#activeRandom()} rather than constructing
 * a new instance.
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

	final RandomGenerator _randGenerator;
	
	private BetaDistribution _randBeta;
	private BinomialDistribution _randBinomial;

	// For now, continue to use the CERN Gamma implementation. It is somewhere between 40-100% faster
	// and the Apache implementation causes tests to fail in a way that suggest that it might not be as
	// numerically accurate or stable. We should look at the next Apache release (3.6) to see if it does
	// anything about this...
	private RandomEngine _randEngine;
	private cern.jet.random.Gamma _randGamma;
	
	private long _seed;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct using specified underlying random generator and seed.
	 * <p>
	 * @since 0.08
	 */
	public DimpleRandom(RandomGenerator randomGenerator, long seed)
	{
		super(randomGenerator);
		_randGenerator = randomGenerator;
		_seed = seed;
		_randEngine = new cern.jet.random.engine.MersenneTwister((int)seed);
		_randGamma = new cern.jet.random.Gamma(1, 1, _randEngine);
		_randBeta = new BetaDistribution(_randGenerator, 1, 1);
		_randBinomial = new BinomialDistribution(_randGenerator, 1, 0.5);
	}
	
	/**
	 * Construct using specified underlying random generator and a randomly generated seed.
	 * <p>
	 * The random value used to seed the generator can be obtained via {@link #getSeed()}.
	 * <p>
	 * @since 0.08
	 */
	public DimpleRandom(RandomGenerator randomGenerator)
	{
		this(randomGenerator, randomGenerator.nextLong());
	}

	/**
	 * Construct with specified seed.
	 * <p>
	 * @since 0.08
	 */
	public DimpleRandom(long seed)
	{
		this(new MersenneTwister(seed), seed);
	}
	
	/**
	 * Construct with randomly generated seed.
	 * <p>
	 * The random value used to seed the generator can be obtained via {@link #getSeed()}.
	 * @since 0.08
	 */
	public DimpleRandom()
	{
		this(new MersenneTwister());
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
	 * The underlying Apache random generator.
	 * <p>
	 * May be used with Apache's distribution classes.
	 * <p>
	 * @since 0.08
	 */
	public RandomGenerator getGenerator()
	{
		return _randGenerator;
	}
	
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
		return nextBeta(1.0, 1.0);
	}
	
	/**
	 * Returns sample from beta distribution with specified alpha and beta parameters.
	 * @since 0.08
	 */
	public double nextBeta(double alpha, double beta)
	{
		BetaDistribution randBeta = _randBeta;
		
		if (randBeta.getAlpha() != alpha || randBeta.getBeta() != beta)
		{
			randBeta = new BetaDistribution(_randGenerator, alpha, beta);
			_randBeta = randBeta;
		}
		
		return randBeta.sample();
	}
	
	/**
	 * Returns sample from bernoulli distribution with parameter p set to .5.
	 * @since 0.08
	 * @see #nextBernoulli(double)
	 */
	public int nextBernoulli()
	{
		return nextBernoulli(.5);
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
		
		BinomialDistribution randBinomial = _randBinomial;
		
		if (randBinomial.getNumberOfTrials() != n || randBinomial.getProbabilityOfSuccess() != p)
		{
			_randBinomial = randBinomial = new BinomialDistribution(_randGenerator, n, p);
		}

		return randBinomial.sample();
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
		return nextGamma(1.0, 1.0);
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
