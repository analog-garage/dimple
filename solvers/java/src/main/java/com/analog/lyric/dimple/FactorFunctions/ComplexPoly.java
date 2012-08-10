package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class ComplexPoly extends FactorFunction
{
	protected double _beta;
	public ComplexPoly() {this(1);}
	public ComplexPoly(double smoothing) {super("ComplexPoly"); _beta = 1/smoothing;}
    public double eval(Object ... input)
    {
    	double outR = (Double)input[0];
    	double outI = (Double)input[1];
    	double inR = (Double)input[2];
    	double inI = (Double)input[3];
    	double b1R = (Double)input[4];	// 1st order coefficient
    	double b1I = (Double)input[5];
    	double b3R = (Double)input[6];	// 3rd order coefficient
    	double b3I = (Double)input[7];
    	double b5R = (Double)input[8];	// 5th order coefficient
    	double b5I = (Double)input[9];
    	
    	double inMag = Math.sqrt(inR*inR + inI*inI);
    	double inMag2 = inMag*inMag;
    	double inMag4 = inMag2*inMag2;
    	
    	Complex term0 = complexProduct(b1R, b1I, inR, inI);
    	Complex term3 = complexProduct(b3R, b3I, inR * inMag2, inI * inMag2);
    	Complex term5 = complexProduct(b5R, b5I, inR * inMag4, inI * inMag4);
    	double resultR = term0.R + term3.R + term5.R;
    	double resultI = term0.I + term3.I + term5.I;
    	
    	double diffR = outR - resultR;
    	double diffI = outI - resultI;
    	double potential = diffR*diffR + diffI*diffI;
    	
    	return Math.exp(-potential*_beta);
    }
    
    protected Complex complexProduct(double aR, double aI, double bR, double bI)
    {
    	Complex out = new Complex();
    	out.R = aR*bR - aI*bI;
    	out.I = aR*bI + aI*bR;
    	return out;
    }
    
    protected static class Complex
    {
    	public double R;
    	public double I;
    }
}
