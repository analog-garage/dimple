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

import com.analog.lyric.dimple.events.FactorGraphFactorEvent;
import com.analog.lyric.dimple.model.factors.Factor;

/**
 * Event raised when {@link Factor} is removed from a graph.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public final class FactorRemoveEvent extends FactorGraphFactorEvent
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param source is the graph from which the factor was removed.
	 * @param factor is the factor that was removed.
	 * @since 0.06
	 */
	protected FactorRemoveEvent(FactorGraph source, Factor factor)
	{
		super(source, factor);
	}

	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	protected AddRemoveType addRemoveType()
	{
		return AddRemoveType.REMOVE;
	}
}
