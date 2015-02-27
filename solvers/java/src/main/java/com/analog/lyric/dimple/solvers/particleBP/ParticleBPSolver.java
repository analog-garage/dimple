/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
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

package com.analog.lyric.dimple.solvers.particleBP;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.solvers.core.SolverBase;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;

public class ParticleBPSolver extends SolverBase<ParticleBPSolverGraph>
{
	/*----------------
	 * Object methods
	 */
	
	@Override
	@NonNullByDefault(false)
	public boolean equals(Object obj)
	{
		return obj instanceof ParticleBPSolver;
	}
	
	@Override
	public int hashCode()
	{
		return ParticleBPSolver.class.hashCode();
	}
	
	/*------------------------------
	 *  IFactorGraphFactory methods
	 */
	
	@SuppressWarnings("deprecation")
	@Override
	public final SFactorGraph createFactorGraph(FactorGraph factorGraph, @Nullable ISolverFactorGraph parent)
	{
		return new SFactorGraph(factorGraph, parent);
	}
}
