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
	
	
    /**
     *  Round up to the next power of 2
     * @param x is a non-negative value
     */
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

	/**
	 * Normalizes weights to sum to one.
	 * <p>
	 * Modifies the weights in the array by dividing by their sum.
	 * Does nothing if the sum of the weights is zero.
	 * <p>
	 * @param weights a non-empty array of positive weights.
	 * @return {@code weights} array that was passed in
	 * @since 0.08
	 */
	public static double[] normalize(double[] weights)
	{
		final int n = weights.length;
		double sum = 0.0;
		
		for (int i = n; --i>=0;)
			sum += weights[i];
		if (sum != 0.0)
		{
			for (int i = n; --i>=0;)
				weights[i] /= sum;
		}
		
		return weights;
	}
	
	/**
	 * Convert value from energy domain to weight/probability domain.
	 * <p>
	 * This simply returns:
	 * <blockquote>
	 * <big>
	 * e<sup>-energy</sup>
	 * </big>
	 * </blockquote>
	 * <p>
	 * @param energy is any value. Positive infinity corresponds to a zero weight,
	 * and zero corresponds to a weight of one.
	 * @see #weightToEnergy(double)
	 */
	public static double energyToWeight(double energy)
	{
		return Math.exp(-energy);
	}

	/**
	 * Convert value from weight/probability domain to energy domain.
	 * <p>
	 * This is simply the negative natural log of the weight:
	 * <blockquote>
	 * -ln(weight)
	 * </blockquote>
	 * <p>
	 * @param weight is a non-negative value.
	 * @since 0.08
	 */
	public static double weightToEnergy(double weight)
	{
		return -Math.log(weight);
	}

}
