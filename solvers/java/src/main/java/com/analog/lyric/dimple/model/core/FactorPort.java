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

package com.analog.lyric.dimple.model.core;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
@Immutable
public class FactorPort extends Port
{
	/*---------------
	 * Construction
	 */
	
	FactorPort(EdgeState edgeState, FactorGraph graph)
	{
		super(edgeState, graph);
	}
	
	/**
	 * @category internal
	 */
	@Internal
	public FactorPort(Factor factor, int siblingNumber)
	{
		this(factor.getSiblingEdgeState(siblingNumber), factor.requireParentGraph());
	}
	
	/*----------------
	 * IGetId methods
	 */
	
	@Override
	public int getLocalId()
	{
		return Ids.localIdFromParts(Ids.FACTOR_PORT_TYPE, _edgeState.factorEdgeIndex());
	}
	
	/*--------------
	 * Port methods
	 */
	
	@Override
	public Factor getNode()
	{
		return _edgeState.getFactor(_graph);
	}
	
	@Override
	public Variable getSiblingNode()
	{
		return _edgeState.getVariable(_graph);
	}
	
	@Override
	public final int getSiblingNumber()
	{
		return _edgeState.getFactorToVariableEdgeNumber();
	}
	
	@Override
	public VariablePort getSiblingPort()
	{
		return new VariablePort(_edgeState, _edgeState.getVariableParent(_graph));
	}
}
