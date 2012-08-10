package com.analog.lyric.dimple.solvers.gaussian;

import com.analog.lyric.dimple.FactorFunctions.core.SwedishFactorFunction;
import com.analog.lyric.dimple.model.Port;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishDistributionGenerator;
import com.analog.lyric.dimple.solvers.core.swedish.SwedishSampler;

public abstract class GaussianFactorFunction extends SwedishFactorFunction
{

	public GaussianFactorFunction(String name) 
	{
		super(name);
	}

	@Override
	public SwedishSampler generateSampler(Port p) 
	{
		boolean isDiscretePort = ((VariableBase)p.getConnectedNode()).getDomain().isDiscrete();
		
		if (isDiscretePort)
			return new DiscreteSampler(p,_random);
		else
			return new GaussianSampler(p, _random);
		
	}

	@Override
	public SwedishDistributionGenerator generateDistributionGenerator(Port p) 
	{
		boolean isDiscretePort = ((VariableBase)p.getConnectedNode()).getDomain().isDiscrete();
		
		if (isDiscretePort)
			return new DiscreteDistributionGenerator(p);
		else
			return new GaussianDistributionGenerator(p);
	}

}
