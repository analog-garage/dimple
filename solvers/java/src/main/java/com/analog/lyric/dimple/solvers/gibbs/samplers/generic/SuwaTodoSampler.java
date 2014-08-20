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
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.math.DimpleRandomGenerator;
import com.analog.lyric.math.Utilities;

public class SuwaTodoSampler extends AbstractGenericSampler implements IDiscreteDirectSampler
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
		RandomGenerator rand = DimpleRandomGenerator.rand;
		final int length = energy.length;
		int sampleIndex;

		// Special-case length 2 for speed
		// This case is equivalent to MH
		if (length == 2)
		{
			final int previousIndex = sampleValue.getIndex();
			final double pdf0 = Math.exp(minEnergy - energy[0]);
			final double pdf1 = Math.exp(minEnergy - energy[1]);
			if (previousIndex == 0)
			{
				double rejectProb = pdf0 - pdf1;
				if (rejectProb < 0)
					sampleIndex = 1;	// Flip
				else if (rand.nextDouble() < rejectProb)
					sampleIndex = 0;
				else
					sampleIndex = 1;	// Flip
			}
			else
			{
				double rejectProb = pdf1 - pdf0;
				if (rejectProb < 0)
					sampleIndex = 0;	// Flip
				if (rand.nextDouble() < rejectProb)
					sampleIndex = 1;
				else
					sampleIndex = 0;	// Flip
			}

		}
		else	// For all other lengths
		{
			// Calculate cumulative conditional probability (unnormalized)
			double sum = 0;
			final double[] samplerScratch = _samplerScratch;
			final int previousIndex = sampleValue.getIndex();
			double previousIntervalValue = 0;
			samplerScratch[0] = 0;
			for (int m = 1; m < length; m++)
			{
				final int mm1 = m - 1;
				final double unnormalizedValue = Math.exp(minEnergy-energy[mm1]);
				if (mm1 == previousIndex) previousIntervalValue = unnormalizedValue;
				sum += unnormalizedValue;
				samplerScratch[m] = sum;
			}
			final int lm1 = length - 1;
			final double unnormalizedValue = Math.exp(minEnergy-energy[lm1]);
			if (previousIndex == lm1) previousIntervalValue = unnormalizedValue;
			sum += unnormalizedValue;
			for (int m = length; m < _lengthRoundedUp; m++)
				samplerScratch[m] = Double.POSITIVE_INFINITY;

			// Sample from a range circularly shifted by the largest interval with size of the previous value interval
			// In this scale, the largest interval is always 1
			double randomValue = samplerScratch[previousIndex] + 1 + previousIntervalValue * rand.nextDouble();
			randomValue = randomValue % sum;		// Circularly wrap
			
			// Sample from the CDF using a binary search
			final int half = _lengthRoundedUp >> 1;
			sampleIndex = 0;
			for (int bitValue = half; bitValue > 0; bitValue >>= 1)
			{
				final int testIndex = sampleIndex | bitValue;
				if (randomValue > samplerScratch[testIndex]) sampleIndex = testIndex;
			}
		}

		samplerClient.setNextSampleIndex(sampleIndex);
	}
}
