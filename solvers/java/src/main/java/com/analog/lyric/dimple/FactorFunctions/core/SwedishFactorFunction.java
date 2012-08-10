package com.analog.lyric.dimple.FactorFunctions.core;

import java.util.Random;

import com.analog.lyric.dimple.model.DimpleException;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishDistributionGenerator;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishSampler;

public abstract class SwedishFactorFunction extends FactorFunction
{

	protected Random _random;
	
	public SwedishFactorFunction(String name) 
	{
		super(name);
	}
	
	public void attachRandom(Random random)
	{
		_random = random;
	}

	@Override
	public double eval(Object... input) 
	{
		throw new DimpleException("not implemented");
	}

	public abstract double acceptanceRatio(int outPortIndex, Object ... inputs);
	public abstract Object generateSample(int outPortIndex, Object ... inputs);
	public abstract SwedishSampler generateSampler(Port p);
	public abstract SwedishDistributionGenerator generateDistributionGenerator(Port p);
	
	public String runSwedishFish()
	{
		return "mmmmm";
	}
}
