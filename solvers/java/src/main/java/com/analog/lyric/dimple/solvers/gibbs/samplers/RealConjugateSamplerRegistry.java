package com.analog.lyric.dimple.solvers.gibbs.samplers;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;

public class RealConjugateSamplerRegistry
{
	// LIST	ALL RealConjugateSamplerFactory CLASSES HERE
	// TODO: Is there a way to do this with reflection?
	public static final IRealConjugateSamplerFactory[] availableConjugateSamplers =
	{
		NormalSampler.factory,
		GammaSampler.factory,
		NegativeExpGammaSampler.factory,
	};
	

	// Find a sampler compatible with the specified factor function as an input
	public static IRealConjugateSampler findCompatibleSampler(FactorFunction ff)
	{

		for (IRealConjugateSamplerFactory sampler : availableConjugateSamplers)
			if (sampler.isCompatible(ff))
				return sampler.create();	// Create and return the sampler
		return null;
	}

	
	// Get a sampler by name; assumes it is located in this package
	public static IRealConjugateSampler get(String samplerName)
	{
		String fullQualifiedName = RealConjugateSamplerRegistry.class.getPackage().getName() + "." + samplerName;
		try
		{
			IRealConjugateSampler sampler = (IRealConjugateSampler)(Class.forName(fullQualifiedName).getConstructor().newInstance());
			return sampler;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
