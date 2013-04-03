package com.analog.lyric.dimple.solvers.core.proposalKernels;

import com.analog.lyric.dimple.solvers.gibbs.GibbsSolverRandomGenerator;

public class CircularNormalProposalKernel implements IProposalKernel
{
	protected double _standardDeviation = 1;
	protected double _min = -Math.PI;
	protected double _max = Math.PI;
	protected double _range = _max-_min;

	public Object next(Object currentValue)
	{
		double value = (Double)currentValue + _standardDeviation * GibbsSolverRandomGenerator.rand.nextGaussian();
		value = ((((value - _min) % _range) + _range) % _range) + _min;		// Wrap from -pi to pi
		return value;
	}
	
	public void setParameters(Object... parameters)
	{
		_standardDeviation = (Double)parameters[0];
		if (parameters.length > 1)
			_min = (Double)parameters[1];
		if (parameters.length > 2)
			_max = (Double)parameters[2];
		_range = _max-_min;
	}
	
	public Object[] getParameters()
	{
		Object[] parameters = new Object[3];
		parameters[0] = _standardDeviation;
		parameters[1] = _min;
		parameters[2] = _max;
		return parameters;
	}
}

