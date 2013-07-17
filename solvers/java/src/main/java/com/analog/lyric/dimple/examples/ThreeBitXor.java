package com.analog.lyric.dimple.examples;

import com.analog.lyric.dimple.FactorFunctions.core.FactorFunction;

public class ThreeBitXor extends FactorFunction 
{
	@Override
	public double eval(Object ... args)
	{
		int arg0 = (Integer)args[0];
		int arg1 = (Integer)args[1];
		int arg2 = (Integer)args[2];
		
		return (arg0 ^ arg1 ^ arg2) == 0 ? 1 : 0;
	}

}
