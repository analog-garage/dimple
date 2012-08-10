package com.analog.lyric.dimple.matlabproxy;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;

public class PFactorFunction 
{
	private FactorFunction _factorFunction;
	
	public PFactorFunction(FactorFunction factorFunc)
	{
		_factorFunction = factorFunc;
	}
	
	public FactorFunction getModelerObject()
	{
		return _factorFunction;
	}
}
