package com.analog.lyric.dimple.solvers.core.proposalKernels;

public interface IProposalKernel
{
	public Object next(Object currentValue);
	public void setParameters(Object... parameters);
	public Object[] getParameters();
}
