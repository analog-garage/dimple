package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class AdditiveNoise extends FactorFunction
{
	protected double _invSigmaSquared = 1;
	public AdditiveNoise(double sigma) {super("AdditiveNoise"); _invSigmaSquared = 1/(sigma*sigma);}
    public double eval(Object ... input)
    {
    	double var1 = (Double)input[0];
    	double var2 = (Double)input[1];
    	
    	double diff = var2 - var1;
    	double potential = diff*diff*_invSigmaSquared;
    	
    	return Math.exp(-potential);
    }
}
