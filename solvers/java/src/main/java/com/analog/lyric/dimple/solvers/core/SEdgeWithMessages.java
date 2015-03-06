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

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.solvers.interfaces.ISolverEdge;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class SEdgeWithMessages<VarToFactorMsg, FactorToVarMsg> implements ISolverEdge
{
	public VarToFactorMsg varToFactorMsg;
	public FactorToVarMsg factorToVarMsg;
	
	protected SEdgeWithMessages(VarToFactorMsg varToFactorMsg, FactorToVarMsg factorToVarMsg)
	{
		this.varToFactorMsg = varToFactorMsg;
		this.factorToVarMsg = factorToVarMsg;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return String.format("[%s var2factor=%s, factor2var=%s]",
			getClass().getSimpleName(), varToFactorMsg, factorToVarMsg);
	}
	
	/*---------------------
	 * ISolverEdge methods
	 */
	
	@Override
	public final FactorToVarMsg getFactorToVarMsg()
	{
		return factorToVarMsg;
	}
	
	@Override
	public final VarToFactorMsg getVarToFactorMsg()
	{
		return varToFactorMsg;
	}
}
