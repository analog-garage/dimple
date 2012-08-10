package com.analog.lyric.dimple.test;

import com.analog.lyric.dimple.solvers.gaussian.GaussianFactorFunction;

public class GaussianAddFactorFunction extends GaussianFactorFunction
{

	public GaussianAddFactorFunction() 
	{
		super("GaussianAdd");
	}

	@Override
	public double acceptanceRatio(int portIndex, Object... inputs) 
	{
		return 1;
	}

	@Override
	public Object generateSample(int portIndex, Object... inputs) 
	{
		if (portIndex == 0)
		{
			double sum = 0;
			for (int i = 0; i < inputs.length; i++)
			{
				sum += (Double)inputs[i];
			}
			return sum;
		}
		else
		{
			double sum = (Double)inputs[0];
			for (int i = 1; i < inputs.length; i++)
			{
				sum -= (Double)inputs[i];
			}
			return sum;
		}
	}

}
