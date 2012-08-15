package com.analog.lyric.dimple.FactorFunctions;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;

/*
 * Function for testing kbestminsum works
 */
public class FactorFunctionForTesting extends FactorFunction 
{

	public FactorFunctionForTesting() {
		super("FactorFunctionForTesting");
	}

	@Override
	public double eval(Object... input) 
	{
		double sum = 0;
		for (int i = 0; i < input.length; i++)
		{
			sum += (Double)input[i];
		}
		return sum;
	}
}
