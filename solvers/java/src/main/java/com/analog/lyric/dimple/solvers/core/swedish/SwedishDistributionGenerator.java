package com.analog.lyric.dimple.solvers.core.swedish;

import java.util.ArrayList;

import com.analog.lyric.dimple.model.Port;

public abstract class SwedishDistributionGenerator 
{
	protected Port _p;
	//protected Random _random;
	
	public SwedishDistributionGenerator(Port p)
	{
		_p = p;
	}
	
	public abstract void initialize() ;	
	public abstract void generateDistributionInPlace(ArrayList<Object> input);
}
