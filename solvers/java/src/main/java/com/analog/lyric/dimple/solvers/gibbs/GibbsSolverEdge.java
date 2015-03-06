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

package com.analog.lyric.dimple.solvers.gibbs;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdge;

/**
 * Base class for solver edge state for Gibbs.
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class GibbsSolverEdge<Message extends IParameterizedMessage> implements ISolverEdge
{
	public Message factorToVarMsg;

	protected GibbsSolverEdge(Message factorToVarMsg)
	{
		this.factorToVarMsg = factorToVarMsg;
	}
	
	@Override
	public Message getFactorToVarMsg()
	{
		return factorToVarMsg;
	}
	
	@Override
	public @Nullable Object getVarToFactorMsg()
	{
		return null;
	}
	
	@Override
	public void reset()
	{
		factorToVarMsg.setUniform();
	}
}
