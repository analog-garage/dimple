package com.analog.lyric.dimple.solvers.gibbs;


public interface ISolverRealFactorGibbs
{
	public double getConditionalPotential(double value, int outPortIndex);
}
