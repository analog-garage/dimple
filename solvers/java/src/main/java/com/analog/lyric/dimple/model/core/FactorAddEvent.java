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
 * Event raised for newly added {@link Factor}s.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public final class FactorAddEvent extends FactorGraphFactorEvent
{
	private static final long serialVersionUID = 1L;

	private final boolean _absorbedFromSubgraph;
	
	/**
	 * @param source is the graph to which the factor was added
	 * @param factor is the newly added factor.
	 * @since 0.06
	 */
	FactorAddEvent(FactorGraph source, Factor factor, boolean absorbedFromSubgraph)
	{
		super(source, factor);
		_absorbedFromSubgraph = absorbedFromSubgraph;
	}
	
	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	protected AddRemoveType addRemoveType()
	{
		return AddRemoveType.ADD;
	}
	
	/*---------------
	 * Local methods
	 */
	
	/**
	 * True if factor was added as a result of absorbing the contents of a subgraph
	 * through {@link FactorGraph#absorbSubgraph(FactorGraph)}.
	 * 
	 * @since 0.06
	 */
	public boolean wasAbsorbedFromSubgraph()
	{
		return _absorbedFromSubgraph;
	}
}
