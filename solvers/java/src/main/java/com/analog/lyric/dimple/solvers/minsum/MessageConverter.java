package com.analog.lyric.dimple.solvers.minsum;

public class MessageConverter
{
	
	private static final double maxPotential = 1000;	// Clip the potential (-log(P)) to this value
	
    public static double[] fromProb(double[] in)
    {
    	int numValue = in.length;
    	double[] out = new double[numValue];
    	
    	for (int i = 0; i < numValue; i++) 
    		out[i] = Math.min(-Math.log(in[i]),maxPotential);
    	return out;
    }
    
    public static double[] toProb(double[] in)
    {
    	int numValue = in.length;
    	double[] out = new double[numValue];
    	double minPotential = Double.POSITIVE_INFINITY;
        double sum = 0;
        for (int i = 0; i < numValue; i++)
        	if (in[i] < minPotential) minPotential = in[i];
        for (int i = 0; i < numValue; i++)
        {
            out[i] = Math.exp(-(in[i] - minPotential));
            sum += out[i];
        }
        for (int i = 0; i < numValue; i++) out[i] /= sum;		// Normalize to probability values
        return out;
    }
    
    public static double[] initialValue(int numValue)
    {
    	double[] out = new double[numValue];
    	for (int i = 0; i < numValue; i++) out[i] = 0;
    	return out;
    }
    
}
