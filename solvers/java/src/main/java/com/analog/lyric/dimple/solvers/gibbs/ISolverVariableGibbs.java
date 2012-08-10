package com.analog.lyric.dimple.solvers.gibbs;

public interface ISolverVariableGibbs
{
	public void saveAllSamples();
	public void saveCurrentSample();
    public void saveBestSample();
	public void updateBelief();
	public void setBeta(double beta);
	public double getPotential();
}
