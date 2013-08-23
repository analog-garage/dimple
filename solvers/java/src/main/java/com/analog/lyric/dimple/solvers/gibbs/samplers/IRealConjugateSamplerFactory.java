package com.analog.lyric.dimple.solvers.gibbs.samplers;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.RealDomain;

public interface IRealConjugateSamplerFactory
{
	public IRealConjugateSampler create();
	public boolean isCompatible(FactorFunction factorFunction);
	public boolean isCompatible(RealDomain domain);
}
