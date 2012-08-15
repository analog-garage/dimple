package com.analog.lyric.dimple.solvers.core;

public class KBestFactorTableEngine extends KBestFactorEngine 
{

	public KBestFactorTableEngine(IKBestFactor f) 
	{
		super(f);
		// TODO Auto-generated constructor stub
	}

	protected double getFactorFunctionValueForIndices(int [] inputIndices, Object [][] domains)
	{
		int index = getIKBestFactor().getFactorTable().getWeightIndexFromTableIndices(inputIndices);
		if (index < 0)
			return Double.POSITIVE_INFINITY;
		else
			return getIKBestFactor().getFactorTableValue(index);
	}
}
