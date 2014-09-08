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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.eclipse.jdt.annotation.Nullable;


/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class VariableFixedValueChangeEvent extends VariableChangeEvent
{
	private static final long serialVersionUID = 1L;

	/**
	 * Indicates type of change.
	 * 
	 * @since 0.06
	 * @author Christopher Barber
	 */
	public static enum Type
	{
		/**
		 * A fixed value was set on a variable that previously did not have one.
		 */
		ADDED,
		/**
		 * The value of a fixed value on a variable was changed from its previous value.
		 */
		CHANGED,
		/**
		 * A fixed value was removed from a variable.
		 */
		REMOVED;
	}
	
	/*-------
	 * State
	 */
	
	private final @Nullable Object _oldValue;
	private final @Nullable Object _newValue;
	
	/**
	 * @param variable
	 * @since 0.06
	 */
	VariableFixedValueChangeEvent(Variable variable, @Nullable Object oldValue, @Nullable Object newValue)
	{
		super(variable);
		_oldValue = oldValue;
		_newValue = newValue;
	}
	
	/*---------------
	 * Serialization
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		if (_oldValue != null && _oldValue instanceof Serializable)
		{
			out.writeObject(out);
		}
		else
		{
		}
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
	
	/*---------------------
	 * DimpleEvent methods
	 */

	@Override
	protected void printDetails(PrintStream out, int verbosity)
	{
		switch (getType())
		{
		case ADDED:
			out.format("fixed value on '%s' set to '%s'", getSourceName(), getNewValue());
			break;
		case CHANGED:
			out.format("fixed value on '%s' set to '%s'", getSourceName(), getNewValue());
			if (verbosity > 0)
			{
				out.format(" (was %s)", getOldValue());
			}
			break;
		case REMOVED:
			out.format("fixed value removed from '%s'", getSourceName());
			if (verbosity > 0)
			{
				out.format(" (was %s)", getOldValue());
			}
			break;
		}
	}

	/*-------------------------------
	 * VariableFixedValueChangeEvent
	 */
	
	/**
	 * The previous fixed value.
	 * <p>
	 * Will be null if {@link #getType()} is {@link Type#ADDED} or the event has
	 * been deserialized and the value was not serializable.
	 * <p>
	 * @since 0.06
	 */
	public @Nullable Object getNewValue()
	{
		return _newValue;
	}
	
	/**
	 * The new fixed value.
	 * <p>
	 * Will be null if {@link #getType()} is {@link Type#REMOVED} or the event has
	 * been deserialized and the value was not serializable.
	 * <p>
	 * @since 0.06
	 */
	public @Nullable Object getOldValue()
	{
		return _oldValue;
	}

	/**
	 * Indicates the type of change.
	 * 
	 * @since 0.06
	 */
	public Type getType()
	{
		if (_oldValue == null)
		{
			return Type.ADDED;
		}
		else if (_newValue == null)
		{
			return Type.REMOVED;
		}
		else
		{
			return Type.CHANGED;
		}
	}
}

