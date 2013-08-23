package com.analog.lyric.dimple.solvers.gibbs.customFactors;

import java.util.Collection;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.samplers.IRealConjugateSamplerFactory;


public abstract class SRealConjugateFactor extends SRealFactor
{
	public SRealConjugateFactor(Factor factor)
	{
		super(factor);
	}

	public Collection<IRealConjugateSamplerFactory> getAvailableSamplers(int portNumber)
	{
		return null;
	}
}
