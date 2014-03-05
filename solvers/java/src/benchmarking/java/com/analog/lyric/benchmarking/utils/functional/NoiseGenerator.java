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

package com.analog.lyric.benchmarking.utils.functional;

import java.util.Random;

/**
 * Implements a generator that produces Gaussian noise with a specified mean and
 * standard deviation.
 */
public class NoiseGenerator implements GeneratorFunction
{
	private final double _mean;
	private final Random _random = new Random();
	private final double _sigma;

	/**
	 * Constructs a NoiseGenerator.
	 * 
	 * @param mean
	 *            The desired mean.
	 * @param sigma
	 *            The desired standard deviation.
	 */
	public NoiseGenerator(double mean, double sigma)
	{
		_mean = mean;
		_sigma = sigma;
	}

	@Override
	public double apply(int... coordinates)
	{
		return _random.nextGaussian() * _sigma + _mean;
	}
}
