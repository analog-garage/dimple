/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs.samplers.generic;

import org.apache.commons.math3.random.RandomGenerator;

import com.analog.lyric.collect.ArrayUtil;
import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.math.Utilities;

public class CDFSampler extends AbstractGenericSampler implements IDiscreteDirectSampler
{
	protected double[] _samplerScratch = ArrayUtil.EMPTY_DOUBLE_ARRAY;
	protected int _lengthRoundedUp = 0;
	protected int _length = 0;

	@Override
	public void initialize(Domain variableDomain)
	{
		int length = ((DiscreteDomain)variableDomain).size();
		_length = length;
	    _lengthRoundedUp = Utilities.nextPow2(length);
	    _samplerScratch = new double[_lengthRoundedUp];
	}
	
	@Override
	public void nextSample(DiscreteValue sampleValue, double[] energy, double minEnergy, IDiscreteSamplerClient samplerClient)
	{
		final RandomGenerator rand = DimpleRandomGenerator.rand;
		final int length = energy.length;
		int sampleIndex;

		// Special-case lengths 2, 3, and 4 for speed
		if (length == 2)
		{
			sampleIndex = (rand.nextDouble() * (1 + Math.exp(energy[1]-energy[0])) > 1) ? 0 : 1;
		}
		else if (length == 3)
		{
			final double cumulative1 = Math.exp(minEnergy-energy[0]);
			final double cumulative2 = cumulative1 + Math.exp(minEnergy-energy[1]);
			final double sum = cumulative2 + Math.exp(minEnergy-energy[2]);
			final double randomValue = sum * rand.nextDouble();
			sampleIndex = (randomValue > cumulative2) ? 2 : (randomValue > cumulative1) ? 1 : 0;
		}
		else if (length == 4)
		{
			final double cumulative1 = Math.exp(minEnergy-energy[0]);
			final double cumulative2 = cumulative1 + Math.exp(minEnergy-energy[1]);
			final double cumulative3 = cumulative2 + Math.exp(minEnergy-energy[2]);
			final double sum = cumulative3 + Math.exp(minEnergy-energy[3]);
			final double randomValue = sum * rand.nextDouble();
			sampleIndex = (randomValue > cumulative2) ? ((randomValue > cumulative3) ? 3 : 2) : ((randomValue > cumulative1) ? 1 : 0);
		}
		else	// For all other lengths
		{
			// Calculate cumulative conditional probability (unnormalized)
			double sum = 0;
			final double[] samplerScratch = _samplerScratch;
			samplerScratch[0] = 0;
			for (int m = 1; m < length; m++)
			{
				sum += expApprox(minEnergy-energy[m-1]);
				samplerScratch[m] = sum;
			}
			sum += expApprox(minEnergy-energy[length-1]);
			for (int m = length; m < _lengthRoundedUp; m++)
				samplerScratch[m] = Double.POSITIVE_INFINITY;

			final int half = _lengthRoundedUp >> 1;
			while (true)
			{
				// Sample from the distribution using a binary search.
				final double randomValue = sum * rand.nextDouble();
				sampleIndex = 0;
				for (int bitValue = half; bitValue > 0; bitValue >>= 1)
				{
					final int testIndex = sampleIndex | bitValue;
					if (randomValue > samplerScratch[testIndex]) sampleIndex = testIndex;
				}

				// Rejection sampling, since the approximation of the exponential function is so coarse
				final double logp = minEnergy-energy[sampleIndex];
				if (Double.isNaN(logp))
					throw new DimpleException("The energy for all values of this variable is infinite. This may indicate a state inconsistent with the model.");
				if (rand.nextDouble()*expApprox(logp) <= Math.exp(logp)) break;
			}
		}

		samplerClient.setNextSampleIndex(sampleIndex);
	}
	
	
	// This is an approximation to the exponential function; inputs must be non-positive
	// To facilitate subsequent rejection sampling, the error versus the correct exponential function needs to be always positive
	// This is true except for very large negative inputs, for values just as the output approaches zero
	// To ensure rejection is never in an infinite loop, this must reach 0 for large negative inputs before the Math.exp function does
	public final static double expApprox(double value)
	{
		// Convert input to base2 log, then convert integer part into IEEE754 exponent
		final long expValue = (long)((int)(1512775.395195186 * value) + 0x3FF00000) << 32;	// 1512775.395195186 = 2^20/log(2)
		return Double.longBitsToDouble(expValue & ~(expValue >> 63));	// Clip result if negative and convert to a double
	}
}
