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

package com.analog.lyric.dimple.model.core;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.events.FactorGraphVariableEvent;
import com.analog.lyric.dimple.model.variables.Variable;

/**
* Event raised for newly added boundary variables.
  * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public final class BoundaryVariableAddEvent extends FactorGraphVariableEvent
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param source is the graph to which the boundary variable was added.
	 * @param variable is the variable that was added.
	 * @since 0.06
	 */
	BoundaryVariableAddEvent(FactorGraph source, Variable variable)
	{
		super(source, variable);
	}

	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	protected AddRemoveType addRemoveType()
	{
		return AddRemoveType.ADD;
	}
	
	@Override
	protected final NodeType nodeType()
	{
		return NodeType.BOUNDARY;
	}
	

}
