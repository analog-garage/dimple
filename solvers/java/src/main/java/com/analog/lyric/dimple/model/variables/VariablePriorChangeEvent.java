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

package com.analog.lyric.dimple.model.variables;

import java.io.PrintStream;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.data.IDatum;

import net.jcip.annotations.Immutable;

/**
 * Event triggered by change to a Variable's prior value.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@Immutable
public class VariablePriorChangeEvent extends VariableChangeEvent
{
	private static final long serialVersionUID = 1L;

	public static enum Type
	{
		ADDED,
		CHANGED,
		REMOVED;
	}
	
	/*-------
	 * State
	 */
	
	private final @Nullable IDatum _oldPrior;
	private final @Nullable IDatum _newPrior;
	
	/*--------------
	 * Construction
	 */
	
	VariablePriorChangeEvent(Variable variable, @Nullable IDatum oldPrior, @Nullable IDatum newPrior)
	{
		super(variable);
		_oldPrior = oldPrior;
		_newPrior = newPrior;
	}
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		switch (getType())
		{
		case ADDED:
			out.format("prior on '%s' set to '%s'", getSourceName(), getNewPrior());
			break;
		case CHANGED:
			out.format("prior on '%s' set to '%s'", getSourceName(), getNewPrior());
			if (verbosity > 0)
			{
				out.format(" (was %s)", getOldPrior());
			}
			break;
		case REMOVED:
			out.format("prior removed from '%s'", getSourceName());
			if (verbosity > 0)
			{
				out.format(" (was %s)", getOldPrior());
			}
			break;
		}
	}

	/*---------------
	 * Local methods
	 */
	
	public @Nullable IDatum getOldPrior()
	{
		return _oldPrior;
	}
	
	public @Nullable IDatum getNewPrior()
	{
		return _newPrior;
	}
	
	public Type getType()
	{
		if (_oldPrior == null)
		{
			return Type.ADDED;
		}
		else if (_newPrior == null)
		{
			return Type.REMOVED;
		}
		else
		{
			return Type.CHANGED;
		}
	}

}
