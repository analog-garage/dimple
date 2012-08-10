package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class RealProduct extends FactorFunction
{
	protected double _beta;
	public RealProduct() {this(1);}
	public RealProduct(double smoothing) {super("RealProduct"); _beta = 1/smoothing;}
    public double eval(Object ... input)
    {
    	int length = input.length;
    	double out = (Double)input[0];

    	double product = 1;
    	for (int i = 1; i < length; i++)
    	{
    		product *= (Double)input[i];
    	}
    	
    	double diff = product - out;
    	double potential = diff*diff;
    	
    	
    	return Math.exp(-potential*_beta);
    }
}
