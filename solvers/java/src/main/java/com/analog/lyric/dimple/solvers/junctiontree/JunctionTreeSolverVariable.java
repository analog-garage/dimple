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

package com.analog.lyric.dimple.solvers.junctiontree;

import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.core.proxy.ProxySolverFactorGraph;
import com.analog.lyric.dimple.solvers.core.proxy.ProxySolverVariable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * @since 0.05
 * @author Christopher Barber
 *
 */
public class JunctionTreeSolverVariable extends ProxySolverVariable<ISolverVariable>
{
	/*-------
	 * State
	 */
	
	private final JunctionTreeSolverGraphBase<?> _root;
	
	/*--------------
	 * Construction
	 */

	/**
	 * @param modelVariable
	 */
	protected JunctionTreeSolverVariable(Variable modelVariable, JunctionTreeSolverGraphBase<?> root)
	{
		super(modelVariable);
		_root = root;
	}

	/*---------------------
	 * ISolverNode methods
	 */
	
	@Override
	public ProxySolverFactorGraph<?> getRootGraph()
	{
		return _root;
	}

	/*-------------------------
	 * ProxySolverNode methods
	 */
	
	@Override
	public @Nullable ISolverVariable getDelegate()
	{
		return _root.getDelegateSolverVariable(this);
	}
}
