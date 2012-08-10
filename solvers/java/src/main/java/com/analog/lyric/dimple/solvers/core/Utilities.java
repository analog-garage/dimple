package com.analog.lyric.dimple.solvers.core;

import java.util.Random;

public class Utilities 
{

	/*
	 * This class expects a PMF and a random number generator and returns a sample from that
	 * distribution.  The PMF does not actually have to be normalized for this to work. 
	 */
	public static int sampleFromMultinomial(double [] unnormalizedPMF,Random rand)
	{
		int M = unnormalizedPMF.length;
		
		double sum = 0;
        for (int m = 0; m < M; m++)
        {
        	sum += unnormalizedPMF[m];
        }
        		
	    //calculate cumulative conditional probability
	    int M2 = nextPow2(M);	// Round up array size to next power of two for subsequent binary search
	    double[] cumulativeProbability = new double[M2];
	    cumulativeProbability[0] = 0;
	    for (int m = 1; m < M; m++)
	    	cumulativeProbability[m] = cumulativeProbability[m-1] + unnormalizedPMF[m-1]/sum;
	    for (int m = M; m < M2; m++)
	    	cumulativeProbability[m] = 1.0;
	    
	 
	    
	    // Sample from the cumulative distribution using a binary search.
	    double randomValue = rand.nextDouble();
	    int sampleIndex = 0;
	    int half = M2 >> 1;
		for (int bitValue = half; bitValue > 0; bitValue >>= 1)
		{
			int testIndex = sampleIndex | bitValue;
			if (randomValue > cumulativeProbability[testIndex]) sampleIndex = testIndex;
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
    	int count = 0;
    	while (value != 0)
    	{
    		value >>= 1;
    		count++;
    	}
    	return count;
    }
    
    
    // Log base 2
	public static final double log2(double x)
	{
		return Math.log(x)/Math.log(2);
	}

}
