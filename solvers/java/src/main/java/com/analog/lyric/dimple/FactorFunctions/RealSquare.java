package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class RealSquare extends FactorFunction
{
	protected double _beta;
	public RealSquare() {this(1);}
	public RealSquare(double smoothing) {super("RealFixedPower"); _beta = 1/smoothing;}
    public double eval(Object ... arguments)
    {
    	Double result = (Double)arguments[0];
    	Double input = (Double)arguments[1];
    	
    	double computedResult = input*input;
    	
    	double diff = computedResult - result;
    	double potential = diff*diff;
    	
    	return Math.exp(-potential*_beta);
    }
}
