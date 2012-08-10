package com.analog.lyric.dimple.solvers.core.swedish;

import java.util.Random;

import com.analog.lyric.dimple.model.Port;

public abstract class SwedishSampler 
{
	protected Random _random;
	protected Port _p;
	
	public SwedishSampler(Port p,Random random)
	{
		_p = p;
		_random = random;
	}
	/*
	public void setRandom(Random random)
	{
		_random = random;
	}
	*/
	
	/*
	public void attach(Port p,Random random)
	{
		_p = p;
		_random = random;
	}
	*/
	
	public abstract void initialize() ;
	
	public abstract Object generateSample();
}
