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

package com.analog.lyric.dimple.events;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public abstract class SolverVariableEvent extends SolverEvent
{
	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	protected SolverVariableEvent(ISolverVariable source)
	{
		super(source);
	}
	
	/*---------------------
	 * EventObject methods
	 */
	
	@Override
	public ISolverVariable getSource()
	{
		return (ISolverVariable)source;
	}

	/*---------------------
	 * DimpleEvent methods
	 */
	
	@Override
	public @Nullable Variable getModelObject()
	{
		return getSource().getModelObject();
	}
	
	/*---------------------
	 * SolverEvent methods
	 */

	@Override
	public ISolverVariable getSolverObject()
	{
		return (ISolverVariable)source;
	}
}
