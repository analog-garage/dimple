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

package com.analog.lyric.dimple.solvers.gibbs;

import java.io.PrintStream;

import net.jcip.annotations.Immutable;

import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.RealJoint;

/**
 * Event raised when variable sample is changed by Gibbs solver.
 * <p>
 * Note that changes to the registration of these events do not go into affect until
 * then next time initialize is invoked on the variable.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public class GibbsVariableUpdateEvent extends GibbsSolverVariableEvent
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;

	private final Value _oldValue;
	private final Value _newValue;
	private final int _rejectCount;
	
	/*--------------
	 * Construction
	 */
	
	GibbsVariableUpdateEvent(ISolverVariableGibbs source, Value oldValue, Value newValue, int rejectCount)
	{
		super(source);
		_oldValue = oldValue.clone();
		_newValue = newValue.clone();
		_rejectCount = rejectCount;
	}
	
	/*---------------------
	 * DimpleEvent methods
	 */
	
	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		out.format("sample for '%s' set to '%s'", getSourceName(), getNewValue());
		if (verbosity > 0)
		{
			out.format(" (was %s)", getOldValue());
			if (verbosity > 1)
			{
				out.format(" [reject count %d]", getRejectCount());
			}
		}
	}
	
	/*----------------------------------
	 * GibbsVariableUpdateEvent methods
	 */
	
	/**
	 * A copy of the value of the variable prior to Gibbs update.
	 * @since 0.06
	 */
	public Value getOldValue()
	{
		return _oldValue;
	}
	
	/**
	 * A copy of the value of the variable after Gibbs update.
	 * @since 0.06
	 */
	public Value getNewValue()
	{
		return _newValue;
	}
	
	/**
	 * The number of components of the sample for this variable that were rejected
	 * during the generation of the sample. For single-dimension variables
	 * this will always be either zero or one. And some samplers will never reject. For
	 * {@link RealJoint} variables, this will be a value from zero up to the size of the
	 * joint domain.
	 * <p>
	 * @since 0.06
	 */
	public int getRejectCount()
	{
		return _rejectCount;
	}
}
