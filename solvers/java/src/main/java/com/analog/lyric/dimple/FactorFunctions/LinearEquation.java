package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class LinearEquation extends FactorFunction
{
	protected double _beta;
	protected double[] _constant;
	public LinearEquation(double[] constant) {this(constant,1);}
	public LinearEquation(double[] constant, double smoothing) {super("LinearEquation"); _beta = 1/smoothing; _constant=constant;}
    public double eval(Object ... input)
    {
    	int length = input.length;
    	double out = (Double)input[0];
    	
    	double sum= 1;
    	for (int i = 1; i < length; i++)
    		sum += _constant[i-1]*(Double)input[i];


    	double diff = sum - out;
    	double potential = diff*diff;
    	
    	return Math.exp(-potential*_beta);
    }
}
