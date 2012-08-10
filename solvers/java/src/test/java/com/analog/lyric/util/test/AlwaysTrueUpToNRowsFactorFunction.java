package com.analog.lyric.util.test;

import java.util.Random;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;

public class AlwaysTrueUpToNRowsFactorFunction extends FactorFunction
{
	private int _maxRows;
	private int _currRows;
	private boolean _randomWeights;
	static private Random _r = new Random();

	public AlwaysTrueUpToNRowsFactorFunction(int maxRows, boolean randomWeights)
	{
		super(String.format("AlwaysTrueUpTo%dRows", maxRows));
		_maxRows = maxRows;
		_currRows = 0;
		_randomWeights = randomWeights;
	}

	@Override
	public double eval(Object... input)
	{
		double value = _randomWeights ? 
							_r.nextDouble() :
							1.0;
		if(_currRows < _maxRows)
		{
			_currRows++;
		}
		else
		{
			value = 0.0;			
		}
		return value;
	}
}
