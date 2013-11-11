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

package com.analog.lyric.dimple.solvers.gibbs;

import com.analog.lyric.dimple.model.factors.Factor;

public interface ISolverVariableGibbs
{
	// External API
	public void saveAllSamples();
	public void saveCurrentSample();
    public void saveBestSample();
    public void setCurrentSample(Object value);
	public void setBeta(double beta);
	public double getPotential();
	
	// Internal methods
	public double getConditionalPotential(int portIndex);
	public void updateBelief();
	public void randomRestart();
	public void postAddFactor(Factor f);
}
