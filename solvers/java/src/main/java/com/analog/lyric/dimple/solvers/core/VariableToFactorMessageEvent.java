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

import static java.util.Objects.*;

import java.io.PrintStream;

import com.analog.lyric.dimple.events.SolverVariableEvent;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.Nullable;

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
	
	private final int _edge;
	private final transient @Nullable ISolverFactor _factor;
	private final String _factorName;
	private final @Nullable IParameterizedMessage _oldMessage;
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
		int edge,
		@Nullable IParameterizedMessage oldMessage,
		IParameterizedMessage newMessage)
	{
		super(variable);
		assert(newMessage != null);
		_edge = edge;
		ISolverFactor factor = _factor = requireNonNull(variable.getSibling(edge));
		_factorName = factor.getEventSourceName();
		_oldMessage = oldMessage;
		_newMessage = newMessage;
	}
	
	/*---------------------
	 * DimpleEvent methods
	 */
	
	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		out.format("update from '%s' to '%s\n", getVariableName(), getFactorName());
		out.print("  new message: ");
		getNewMessage().print(out, verbosity);
		if (verbosity > 0)
		{
			IParameterizedMessage oldMsg = getOldMessage();
			if (oldMsg != null)
			{
				out.print("\n  old message: ");
				oldMsg.print(out, verbosity);
				if (verbosity > 1)
				{
					out.format("\n  KL divergence is %g", computeKLDivergence());
				}
			}
		}
	}
	
	/*-----------------------------
	 * IMessageUpdateEvent methods
	 */
	
	@Override
	public double computeKLDivergence()
	{
		final IParameterizedMessage oldMsg = _oldMessage;
		if (oldMsg== null)
		{
			return Double.POSITIVE_INFINITY;
		}
		
		return oldMsg.computeKLDivergence(_newMessage);
	}
	
	@Override
	public int getEdge()
	{
		return _edge;
	}
	
	@Override
	public @Nullable ISolverFactor getFactor()
	{
		return _factor;
	}
	
	@Override
	public String getFactorName()
	{
		return _factorName;
	}
	
	@Override
	public final IParameterizedMessage getNewMessage()
	{
		return _newMessage;
	}
	
	@Override
	public final @Nullable IParameterizedMessage getOldMessage()
	{
		return _oldMessage;
	}

	@Override
	public @Nullable ISolverVariable getVariable()
	{
		return getSolverObject();
	}

	@Override
	public String getVariableName()
	{
		return getSourceName();
	}
	
	@Override
	public final boolean isToFactor()
	{
		return true;
	}
}
