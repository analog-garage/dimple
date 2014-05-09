/*******************************************************************************
p*   Copyright 2014 Analog Devices, Inc.
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

import com.analog.lyric.dimple.events.SolverVariableEvent;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class VariableToFactorMessageEvent extends SolverVariableEvent implements IMessageUpdateEvent
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private final ISolverFactor _factor;
	private final IParameterizedMessage _oldMessage;
	private final IParameterizedMessage _newMessage;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param variable is the whose distribution is described by the messages.
	 * @param factor is the factor to which the message is passed.
	 * @param oldMessage is the value prior to update, which may be null.
	 * @param newMessage is the updated value and must not be null.
	 * @since 0.06
	 */
	VariableToFactorMessageEvent(
		ISolverVariable variable,
		ISolverFactor factor,
		IParameterizedMessage oldMessage,
		IParameterizedMessage newMessage)
	{
		super(variable);
		assert(newMessage != null);
		_factor = factor;
		_oldMessage = oldMessage;
		_newMessage = newMessage;
	}
	
	/*---------------
	 * Local methods
	 */
	
	/*
	 * 
	 */
	@Override
	public double computeKLDivergence()
	{
		if (_oldMessage == null)
		{
			return Double.POSITIVE_INFINITY;
		}
		
		return _oldMessage.computeKLDivergence(_newMessage);
	}
	
	/*
	 * 
	 */
	@Override
	public final IParameterizedMessage getOldMessage()
	{
		return _oldMessage;
	}

	/*
	 * 
	 */
	@Override
	public final IParameterizedMessage getNewMessage()
	{
		return _newMessage;
	}
	
	/*
	 * 
	 */
	@Override
	public ISolverFactor getFactor()
	{
		return _factor;
	}
	
	/*
	 * 
	 */
	@Override
	public ISolverVariable getVariable()
	{
		return getSolverObject();
	}
}
