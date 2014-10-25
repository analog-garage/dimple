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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.solvers.gibbs.samplers.ISampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.ISamplerClient;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import com.analog.lyric.util.misc.Internal;

public interface ISolverVariableGibbs extends ISolverNodeGibbs, ISolverVariable, ISamplerClient
{
	// External API
	public void saveAllSamples();
	public void disableSavingAllSamples();
	public void saveCurrentSample();
    public void saveBestSample();
    public Value getCurrentSampleValue();
    public void setCurrentSample(Object value);
    public void setCurrentSample(Value value);
	public void setBeta(double beta);
	@Override
	public double getPotential();

	/**
	 * Returns all saved samples in an array of the appropriate type.
	 * @since 0.07
	 */
	public Object getAllSamples();
	
	/**
	 * Sampler instance for generating samples for this variable.
	 * 
	 * @since 0.06
	 */
	public @Nullable ISampler getSampler();
	
	/**
	 * Returns an iterator over the nodes other than this variable node that should be
	 * used to compute the sample score for this variable.
	 * @since 0.05
	 */
	public ReleasableIterator<ISolverNodeGibbs> getSampleScoreNodes();

	/**
	 * True if {@link #getPotential()} does not always return zero.
	 */
	public boolean hasPotential();
	
	/**
	 * Get the rejection rate of the sampler; applies only to certain samplers
	 * @return rejection rate
	 * @since 0.07
	 */
	public double getRejectionRate();
	
	/**
	 * Get the average number of score computations performed per update
	 * @return average number of scores computed per update
	 * @since 0.07
	 */
	public double getNumScoresPerUpdate();
	
	/**
	 * Clear the rejection rate statistics
	 * @since 0.07
	 */
	public void resetRejectionRateStats();
	
	// Internal methods
	@Internal
	public void updateBelief();
	@Internal
	public void randomRestart(int restartCount);
	@Internal
	public void postAddFactor(@Nullable Factor f);
	
	@Internal
	public long getUpdateCount();
	@Internal
	public long getRejectionCount();
}
