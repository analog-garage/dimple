/*******************************************************************************
 * Copyright 2014 Analog Devices, Inc. Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable
 * law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 * for the specific language governing permissions and limitations under the License.
 ********************************************************************************/

package com.analog.lyric.dimple.solvers.core;

import com.analog.lyric.dimple.solvers.core.parameterizedMessages.IParameterizedMessage;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public interface IMessageUpdateEvent
{

	public abstract double computeKLDivergence();

	public int getEdge();
	
	public abstract @Nullable ISolverFactor getFactor();
	public String getFactorName();

	/**
	 * The new value of the message after update.
	 * @since 0.06
	 */
	public abstract IParameterizedMessage getNewMessage();

	/**
	 * The previous value of the message, prior to update. May be null.
	 * @since 0.06
	 */
	public abstract @Nullable IParameterizedMessage getOldMessage();

	public abstract @Nullable ISolverVariable getVariable();

	public String getVariableName();
	
	/**
	 * True if this is a {@link VariableToFactorMessageEvent} and false if it is
	 * a {@link FactorToVariableMessageEvent}.
	 * 
	 * @since 0.06
	 */
	public boolean isToFactor();
}