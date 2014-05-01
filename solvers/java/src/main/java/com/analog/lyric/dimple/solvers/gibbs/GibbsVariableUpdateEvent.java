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

package com.analog.lyric.dimple.solvers.gibbs;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.values.Value;

/**
 * Event raised when variable sample is changed by Gibbs solver.
 * <p>
 * Note that changes to the registration of these events do not go into affect until
 * then next time initialize is invoked on the variable.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public class GibbsVariableUpdateEvent extends GibbsSolverVariableEvent
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final Value _oldValue;
	private final Value _newValue;
	
	/*--------------
	 * Construction
	 */
	
	GibbsVariableUpdateEvent(ISolverVariableGibbs source, Value oldValue, Value newValue)
	{
		super(source);
		_oldValue = oldValue.clone();
		_newValue = newValue.clone();
	}
	
	/*----------------------------------
	 * GibbsVariableUpdateEvent methods
	 */
	
	public Value getOldValue()
	{
		return _oldValue;
	}
	
	public Value getNewValue()
	{
		return _newValue;
	}
}
