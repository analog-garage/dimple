package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class ConstantProduct extends FactorFunction
{
	protected double _beta;
	protected double _constant;
	public ConstantProduct() {this(1,1);}
	public ConstantProduct(double constant) {this(constant,1);}
	public ConstantProduct(double constant, double smoothing) {super("ConstantProduct"); _beta = 1/smoothing; _constant=constant;}
    public double eval(Object ... input)
    {
    	
    	double out = (Double)input[0];

    	double product= _constant * (Double) input[1];
    	double diff = product - out;
    	double potential = diff*diff;
    	
    	return Math.exp(-potential*_beta);
    }
}
