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

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverEdgeState;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
public abstract class SEdgeWithSymetricParameterizedMessages<Message extends IParameterizedMessage>
	extends SEdgeWithSymetricMessages<Message>
{

	/**
	 * @param varToFactorMsg
	 * @param factorToVarMsg
	 * @since 0.08
	 */
	protected SEdgeWithSymetricParameterizedMessages(Message varToFactorMsg, Message factorToVarMsg)
	{
		super(varToFactorMsg, factorToVarMsg);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The default implementation invokes {@link IParameterizedMessage#setUniform()} on each message.
	 */
	@Override
	public void reset()
	{
		varToFactorMsg.setUniform();
		factorToVarMsg.setUniform();
	}
	
	@Override
	public void setFrom(ISolverEdgeState other)
	{
		SEdgeWithSymetricParameterizedMessages<?> that = (SEdgeWithSymetricParameterizedMessages<?>)other;
		varToFactorMsg.setFrom(that.varToFactorMsg);
		factorToVarMsg.setFrom(that.factorToVarMsg);
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * @param msg must be a {@link IParameterizedMessage}.
	 */
	@Override
	public void setFactorToVarMsg(@Nullable Object msg)
	{
		if (msg != null)
		{
			factorToVarMsg.setFrom((IParameterizedMessage)msg);
		}
		else
		{
			factorToVarMsg.setNull();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * @param msg must be a {@link IParameterizedMessage}.
	 */
	@Override
	public void setVarToFactorMsg(@Nullable Object msg)
	{
		if (msg != null)
		{
			varToFactorMsg.setFrom((IParameterizedMessage)msg);
		}
		else
		{
			varToFactorMsg.setNull();
		}
	}
}
