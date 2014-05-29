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

import com.analog.lyric.dimple.events.FactorGraphSubgraphEvent;

/**
 * Event raised when a subgraph is removed from a factor graph.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public final class SubgraphRemoveEvent extends FactorGraphSubgraphEvent
{
	private static final long serialVersionUID = 1L;

	private final boolean _absorbedByParent;
	
	/**
	 * @param source is the graph from which the subgraph was removed.
	 * @param subgraph is the subgraph that was removed.
	 * @since 0.06
	 */
	protected SubgraphRemoveEvent(FactorGraph source, FactorGraph subgraph, boolean absorbedByParent)
	{
		super(source, subgraph);
		_absorbedByParent = absorbedByParent;
	}
	
	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	protected AddRemoveType addRemoveType()
	{
		return AddRemoveType.REMOVE;
	}
	
	/**
	 * True if subgraph was removed because its contents were absorbed by its parent graph
	 * as a result of a call to {@link FactorGraph#absorbSubgraph(FactorGraph)}.
	 * @since 0.06
	 */
	public boolean wasAbsorbedByParent()
	{
		return _absorbedByParent;
	}
}
