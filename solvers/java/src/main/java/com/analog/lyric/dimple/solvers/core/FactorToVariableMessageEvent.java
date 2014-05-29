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

package com.analog.lyric.dimple.solvers.core;

import java.io.PrintStream;

import com.analog.lyric.dimple.events.SolverFactorEvent;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class FactorToVariableMessageEvent extends SolverFactorEvent implements IMessageUpdateEvent
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private final int _edge;
	private final ISolverVariable _variable;
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
	FactorToVariableMessageEvent(
		ISolverFactor factor,
		int edge,
		IParameterizedMessage oldMessage,
		IParameterizedMessage newMessage)
	{
		super(factor);
		assert(newMessage != null);
		_edge = edge;
		_variable = factor.getSibling(edge);
		_oldMessage = oldMessage;
		_newMessage = newMessage;
	}
	
	/*---------------------
	 * DimpleEvent methods
	 */
	
	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		out.format("update from '%s' to '%s\n", getFactor().getEventSourceName(), getVariable().getEventSourceName());
		out.print("  new message: ");
		getNewMessage().print(out, verbosity);
		if (verbosity > 0)
		{
			out.print("\n  old message: ");
			getOldMessage().print(out, verbosity);
			if (verbosity > 1)
			{
				out.format("  KL divergence is %g", computeKLDivergence());
			}
		}
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
	
	@Override
	public int getEdge()
	{
		return _edge;
	}
	
	@Override
	public ISolverFactor getFactor()
	{
		return getSolverObject();
	}
	
	@Override
	public final IParameterizedMessage getNewMessage()
	{
		return _newMessage;
	}
	
	@Override
	public final IParameterizedMessage getOldMessage()
	{
		return _oldMessage;
	}

	@Override
	public ISolverVariable getVariable()
	{
		return _variable;
	}

	@Override
	public final boolean isToFactor()
	{
		return false;
	}
}
