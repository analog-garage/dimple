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

import java.util.Random;

public class Utilities
{

	/*
	 * This method expects a PMF and a random number generator and returns a sample from that
	 * distribution.  The PMF does not actually have to be normalized for this to work.
	 */
	public static final int sampleFromMultinomial(double[] unnormalizedPMF, Random rand)
	{
		int M = unnormalizedPMF.length;
        		
	    // Calculate cumulative conditional probability (unnormalized)
	    int M2 = nextPow2(M);	// Round up array size to next power of two for subsequent binary search
	    double[] cumulative = new double[M2];
	    double sum = 0;
	    cumulative[0] = 0;
	    for (int m = 1; m < M; m++)
	    {
	    	sum += unnormalizedPMF[m-1];
	    	cumulative[m] = sum;
	    }
    	sum += unnormalizedPMF[M-1];
	    for (int m = M; m < M2; m++)
	    	cumulative[m] = Double.POSITIVE_INFINITY;
	 
	    // Sample from the distribution using a binary search.
	    double randomValue = sum * rand.nextDouble();
	    int sampleIndex = 0;
	    int half = M2 >> 1;
		for (int bitValue = half; bitValue > 0; bitValue >>= 1)
		{
			int testIndex = sampleIndex | bitValue;
			if (randomValue > cumulative[testIndex]) sampleIndex = testIndex;
		}
		
		return sampleIndex;
	}
	
	
    // Round up to the next power of 2
    public static final int nextPow2(int x)
    {
        --x;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return ++x;
    }
    
    // Find the position of the MSB
    public static final int findMSB(int value)
    {
    	// This might be faster depending on the JVM:
    	// return 32 - Integer.numberOfLeadingZeros(x);
    	// Also, this currently returns 1-based index. Perhaps 0-based would be better w/ -1 for zero case.

    	int count = 0;
    	while (value != 0)
    	{
    		value >>= 1;
    		count++;
    	}
    	return count;
    }
    
    // Log base 2
    private static final double LOG2 = Math.log(2);
	public static final double log2(double x)
	{
		return Math.log(x)/LOG2;
	}


	public static double energyToWeight(double energy)
	{
		return Math.exp(-energy);
	}


	public static double weightToEnergy(double weight)
	{
		return -Math.log(weight);
	}

}
