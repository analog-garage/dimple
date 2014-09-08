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

package com.analog.lyric.dimple.model.variables;

import java.io.PrintStream;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;


/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@Immutable
public class VariableInputChangeEvent extends VariableChangeEvent
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
	
	private final @Nullable Object _oldInput;
	private final @Nullable Object _newInput;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @since 0.06
	 */
	VariableInputChangeEvent(Variable variable, @Nullable Object oldInput, @Nullable Object newInput)
	{
		super(variable);
		_oldInput = oldInput;
		_newInput = newInput;
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
			out.format("input on '%s' set to '%s'", getSourceName(), getNewInput());
			break;
		case CHANGED:
			out.format("input on '%s' set to '%s'", getSourceName(), getNewInput());
			if (verbosity > 0)
			{
				out.format(" (was %s)", getOldInput());
			}
			break;
		case REMOVED:
			out.format("input removed from '%s'", getSourceName());
			if (verbosity > 0)
			{
				out.format(" (was %s)", getOldInput());
			}
			break;
		}
	}
	
	/*----------------------------------
	 * VariableInputChangeEvent methods
	 */
	
	public @Nullable Object getNewInput()
	{
		return _newInput;
	}
	
	public @Nullable Object getOldInput()
	{
		return _oldInput;
	}
	
	public Type getType()
	{
		if (_oldInput == null)
		{
			return Type.ADDED;
		}
		else if (_newInput == null)
		{
			return Type.REMOVED;
		}
		else
		{
			return Type.CHANGED;
		}
	}
}
