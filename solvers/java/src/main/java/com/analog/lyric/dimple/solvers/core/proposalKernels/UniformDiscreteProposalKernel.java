package com.analog.lyric.dimple.solvers.core.proposalKernels;

import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.domains.Domain;
import com.analog.lyric.dimple.model.values.DiscreteValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.core.SolverRandomGenerator;

public class UniformDiscreteProposalKernel implements IProposalKernel
{

	@Override
	public Proposal next(Value currentValue, Domain variableDomain)
	{
		// Choose uniformly at random from among all values except the current value
		DiscreteDomain domain = (DiscreteDomain)variableDomain;
		int currentIndex = ((DiscreteValue)currentValue).getIndex();
		int nextIndex = SolverRandomGenerator.rand.nextInt(domain.size() - 1);
		if (nextIndex >= currentIndex) nextIndex++;
		return new Proposal(new DiscreteValue(domain, nextIndex));
	}

	@Override
	public void setParameters(Object... parameters)
	{
	}

	@Override
	public Object[] getParameters()
	{
		return null;
	}

}
