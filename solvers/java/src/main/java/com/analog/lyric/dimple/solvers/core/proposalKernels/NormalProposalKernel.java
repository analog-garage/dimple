package com.analog.lyric.dimple.solvers.core.proposalKernels;

import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverRandomGenerator;

public class NormalProposalKernel implements IProposalKernel
{
	protected double _standardDeviation = 1;
	
	public Object next(Object currentValue)
	{
		return (Double)currentValue + _standardDeviation * GibbsSolverRandomGenerator.rand.nextGaussian();
	}
	
	public void setParameters(Object... parameters)
	{
		_standardDeviation = (Double)parameters[0];
	}
	
	public Object[] getParameters()
	{
		Object[] parameters = new Object[1];
		parameters[0] = _standardDeviation;
		return parameters;
	}
}
