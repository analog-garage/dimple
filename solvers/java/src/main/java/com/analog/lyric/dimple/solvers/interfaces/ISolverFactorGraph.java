package com.analog.lyric.dimple.solvers.interfaces;

import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.VariableBase;

public interface ISolverFactorGraph extends ISolverNode
{
	public ISolverVariable createVariable(VariableBase var);
	public ISolverFactor createFactor(Factor factor);
	public boolean customFactorExists(String funcName);
	public void solve(boolean initialize);
	public void iterate(int numIters);
	public boolean isSolverRunning();
	public void interruptSolver();
	public void startSolver();
	public void setNumIterations(int numIterations);
	public int getNumIterations();

}
