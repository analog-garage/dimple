package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class SimpleNormal extends FactorFunction
{
	protected double _mean = 0;
	protected double _invSigmaSquared = 1;
	public SimpleNormal(double mean, double sigma) {super("SimpleNormal"); _mean = mean; _invSigmaSquared = 1/(sigma*sigma);}
    public double eval(Object ... input)
    {
    	int length = input.length;
    	double potential = 0;
    	for (int i = 0; i < length; i++)
    	{
    		double relInput = (Double)input[i] - _mean;
    		potential += relInput*relInput*_invSigmaSquared;
    	}
    	return Math.exp(-potential);
    }
}
