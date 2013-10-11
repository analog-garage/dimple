package com.analog.lyric.dimple.matlabproxy.testutil;

import com.analog.lyric.dimple.model.core.FactorGraph;

public class RepeatedSolve 
{
	public static void solveRepeated(FactorGraph fg,int numTimes)
	{
		for (int i = 0; i < numTimes; i++)
		{
			fg.solve();
		}
	}
}
