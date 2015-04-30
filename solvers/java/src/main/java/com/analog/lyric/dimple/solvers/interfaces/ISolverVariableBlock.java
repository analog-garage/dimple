/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

import java.util.List;

import com.analog.lyric.dimple.events.ISolverEventSource;
import com.analog.lyric.dimple.model.variables.VariableBlock;

/**
 * Solver variable block state.
 * <p>
 * Contains solver-specific state for a {@link VariableBlock} in the model.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public interface ISolverVariableBlock extends ISolverFactorGraphChild, ISolverEventSource
{
	/*---------------------------------
	 * ISolverFactorGraphChild methods
	 */

	@Override
	public VariableBlock getModelObject();

	/*----------------------------
	 * IDimpleEventSource methods
	 */
	
	@Override
	public VariableBlock getModelEventSource();
	
	/*------------------------------
	 * ISolverVariableBlock methods
	 */
	
	/**
	 * An immutable list of solver variables in the block.
	 * <p>
	 * The solver variables should be in the same order as the corresponding variables in
	 * the {@linkplain #getModelObject() model block}.
	 * <p>
	 * @since 0.08
	 */
	public List<? extends ISolverVariable> getSolverVariables();
}
