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
 * Event raised when a subgraph is added to a factor graph.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public final class SubgraphAddEvent extends FactorGraphSubgraphEvent
{
	private static final long serialVersionUID = 1L;

	private final boolean _absorbedFromSubgraph;
	
	/**
	 * @param source is the graph to which the subgraph was added.
	 * @param subgraph is the graph that was added as a subgraph to {@code source}.
	 * @since 0.06
	 */
	SubgraphAddEvent(FactorGraph source, FactorGraph subgraph, boolean absorbedFromSubgraph)
	{
		super(source, subgraph);
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
	
	/**
	 * True if subgraph was added as a result of absorbing the contents of another subgraph
	 * through {@link FactorGraph#absorbSubgraph(FactorGraph)}.
	 * 
	 * @since 0.06
	 */
	public boolean wasAbsorbedFromSubgraph()
	{
		return _absorbedFromSubgraph;
	}
}
