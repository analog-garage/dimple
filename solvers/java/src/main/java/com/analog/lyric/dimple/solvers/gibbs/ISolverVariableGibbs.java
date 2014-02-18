/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public interface ISolverVariableGibbs extends ISolverNodeGibbs, ISolverVariable
{
	// External API
	public void saveAllSamples();
	public void disableSavingAllSamples();
	public void saveCurrentSample();
    public void saveBestSample();
    public void setCurrentSample(Object value);
    public void setCurrentSample(Value value);
	public void setBeta(double beta);
	@Override
	public double getPotential();
	
	/**
	 * Returns an iterator over the nodes other than this variable node that should be
	 * used to compute the sample score for this variable.
	 */
	public ReleasableIterator<ISolverNodeGibbs> getSampleScoreNodes();

	/**
	 * True if {@link #getPotential()} does not always return zero.
	 */
	public boolean hasPotential();
	
	// Internal methods
	public void updateBelief();
	public void randomRestart(int restartCount);
	public void postAddFactor(Factor f);
}
