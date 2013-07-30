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

import com.analog.lyric.dimple.FactorFunctions.core.IFactorTable;
import com.analog.lyric.dimple.model.Factor;
import com.analog.lyric.dimple.model.FactorGraph;
import com.analog.lyric.dimple.model.VariableBase;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.util.misc.Matlab;

public interface ISolverFactorGraph extends ISolverNode
{
	/**
	 * Returns the factor graph represented by this solver graph.
	 */
	@Override
	public FactorGraph getModelObject();
	
	/**
	 * Create a new solver-specific variable representing the given model variable.
	 */
	public ISolverVariable createVariable(VariableBase var);
	
	/**
	 * @return solver-specific variable representing the given model variable or else null.
	 */
	public ISolverVariable getSolverVariable(VariableBase var);

	public ISolverBlastFromThePastFactor createBlastFromThePast(BlastFromThePastFactor factor);
	public ISolverFactor createFactor(Factor factor);
	public boolean customFactorExists(String funcName);
	public void solve();
	public void solveOneStep();
	public void continueSolve();
	public void startSolveOneStep();
	public void startContinueSolve();
	public void iterate();
	public void iterate(int numIters);
	public boolean isSolverRunning();
	public void interruptSolver();
	public void startSolver();
	public void setNumIterations(int numIterations);
	public int getNumIterations();
	@Override
	public double getScore();
	public double getBetheFreeEnergy();
	@Override
	public double getInternalEnergy();
	@Override
	public double getBetheEntropy();
	public void estimateParameters(IFactorTable [] tables,int numRestarts,int numSteps, double stepScaleFactor);
	public void baumWelch(IFactorTable[] tables,int numRestarts,int numSteps);
	public void moveMessages(ISolverNode other);
	public void postAdvance();
	public void postAddFactor(Factor f);
	public void postSetSolverFactory();
	
	/**
	 * Returns the name of a solver-specific MATLAB wrapper function that should be invoked from
	 * MATLAB to do the solve. The value or existence of the function is allowed to change depending
	 * on the parameters to the solver. The MATLAB function should take two positional arguments:
	 * the MATLAB FactorGraph object and the instance of this interface.
	 * 
	 * @return name of MATLAB function used to do actual solve in MATLAB interface or null if there is none.
	 */
	@Matlab
	public String getMatlabSolveWrapper();
}
