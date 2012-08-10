package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;

public class PriorFactor extends FactorFunction
{
	
	public PriorFactor() 
	{
		super("Prior");
		// TODO Auto-generated constructor stub
	}

	@Override
	public double eval(Object... input) 
	{
		double [] prior = (double[])input[1];
		int index = (int)(double)(Double)input[0];
		return prior[index];
	
	}

}
