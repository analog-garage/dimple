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

package com.analog.lyric.dimple.events;

import java.io.IOException;
import java.io.ObjectOutputStream;

import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Node;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Variable;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base class for events that originate on a {@link FactorGraph} and involve
 * a single {@link Factor}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class FactorGraphVariableEvent extends FactorGraphEvent
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private transient final Variable _variable;
	private final @Nullable String _variableName;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param source of the event
	 * @param variable involved in the event
	 * @since 0.06
	 */
	protected FactorGraphVariableEvent(FactorGraph source, Variable variable)
	{
		super(source);
		_variable = variable;
		_variableName = null;
	}

	/*---------------
	 * Serialization
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		// Write out _variableName field with non-null value.
		out.writeObject(getVariableName());
	}
	
	// The default readObject method should work...
	
	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	public Node getNode()
	{
		return _variable;
	}
	
	@Override
	public String getNodeName()
	{
		return getVariableName();
	}
	
	@Override
	protected NodeType nodeType()
	{
		return NodeType.VARIABLE;
	}
	
	/*----------------------------------
	 * FactorGraphVariableEvent methods
	 */
	
	/**
	 * The variable involved in the event.
	 * <p>
	 * Will be null if event was obtained through deserialization.
	 * @since 0.06
	 * @see #getVariableName()
	 */
	public @Nullable Variable getVariable()
	{
		return _variable;
	}
	
	/**
	 * The name of the factor involved in the event.
	 * <p>
	 * The value of {@link Factor#getEventSourceName()} on the
	 * factor. Unlike {@link #getVariable}, this is preserved by
	 * serialization.
	 * @since 0.06
	 */
	public String getVariableName()
	{
		final String name = _variableName;
		return name == null ? _variable.getEventSourceName() : name;
	}
}
