package com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;

public class RealJointConjugateSamplerRegistry
{
	// LIST	ALL RealConjugateSamplerFactory CLASSES HERE
	// TODO: Is there a way to do this with reflection?
	public static final IRealJointConjugateSamplerFactory[] availableConjugateSamplers =
	{
		DirichletSampler.factory,
	};
	

	// Find a sampler compatible with the specified factor function as an input
	public static IRealJointConjugateSampler findCompatibleSampler(FactorFunction ff)
	{

		for (IRealJointConjugateSamplerFactory sampler : availableConjugateSamplers)
			if (sampler.isCompatible(ff))
				return sampler.create();	// Create and return the sampler
		return null;
	}

	
	// Get a sampler by name; assumes it is located in this package
	public static IRealJointConjugateSampler get(String samplerName)
	{
		String fullQualifiedName = RealJointConjugateSamplerRegistry.class.getPackage().getName() + "." + samplerName;
		try
		{
			IRealJointConjugateSampler sampler = (IRealJointConjugateSampler)(Class.forName(fullQualifiedName).getConstructor().newInstance());
			return sampler;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
