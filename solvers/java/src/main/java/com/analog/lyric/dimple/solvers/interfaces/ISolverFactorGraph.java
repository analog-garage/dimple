/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.solvers.interfaces;

import com.analog.lyric.dimple.FactorFunctions.core.FactorTable;
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
	public void startSolver(boolean initialize);
	public void setNumIterations(int numIterations);
	public int getNumIterations();
	public double getScore();
	public double getBetheFreeEnergy();
	public double getInternalEnergy();
	public double getBetheEntropy();
	public void estimateParameters(FactorTable [] tables,int numRestarts,int numSteps, double stepScaleFactor);
	public void baumWelch(FactorTable [] tables,int numRestarts,int numSteps);
	
}
