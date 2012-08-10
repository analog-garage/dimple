package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class RealFixedPower extends FactorFunction
{
	protected double _power;
	protected double _beta;
	public RealFixedPower(double power) {this(power, 1);}
	public RealFixedPower(double power, double smoothing) {super("RealFixedPower"); _power = power; _beta = 1/smoothing;}
    public double eval(Object ... arguments)
    {
    	Double result = (Double)arguments[0];
    	Double base = (Double)arguments[1];
    	
    	double computedResult = Math.pow(base, _power);
    	
    	double diff = computedResult - result;
    	double potential = diff*diff;
    	
    	return Math.exp(-potential*_beta);
    }
}
