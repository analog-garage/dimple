package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;


public class XorDelta extends FactorFunction 
{
	public XorDelta()
	{
		super("xorDelta");
	}
    public double eval(Object ... input)
    {
    	double total = 0;
    	
    	for(int i = 0; i < input.length; ++i)
    	{
    		total += (Double)input[i];
    	}

    	double ret = total % 2 == 0 ? 
    				1.0 : 
    				0.0;
     
    	return ret;
    }
}
