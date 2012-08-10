package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class RealSum extends FactorFunction
{
	protected double _beta;
	public RealSum() {this(1);}
	public RealSum(double smoothing) {super("RealSum"); _beta = 1/smoothing;}
    public double eval(Object ... input)
    {
    	int length = input.length;
    	double out = (Double)input[0];

    	double sum = 0;
    	for (int i = 1; i < length; i++)
    		sum += (Double)input[i];
    	
    	double diff = sum - out;
    	double potential = diff*diff;
    	
    	return Math.exp(-potential*_beta);
    }
}
