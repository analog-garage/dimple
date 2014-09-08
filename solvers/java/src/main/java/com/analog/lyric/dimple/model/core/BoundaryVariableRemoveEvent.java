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

// BUG 84: there is no way to remove boundary variables so this event is never raised.

/**
 * Event raised when a boundary variable is removed from a graph.
 * <p>
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public final class BoundaryVariableRemoveEvent extends FactorGraphVariableEvent
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param source is the graph from which the boundary variable was removed.
	 * @param variable is the boundary variable that was removed.
	 * @since 0.06
	 */
	BoundaryVariableRemoveEvent(FactorGraph source, Variable variable)
	{
		super(source, variable);
	}

	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	protected AddRemoveType addRemoveType()
	{
		return AddRemoveType.REMOVE;
	}
	
	@Override
	protected final NodeType nodeType()
	{
		return NodeType.BOUNDARY;
	}
	
}
