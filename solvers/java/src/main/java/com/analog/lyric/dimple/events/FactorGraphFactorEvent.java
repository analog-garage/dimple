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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Base class for events that originate on a {@link FactorGraph} and involve
 * a single {@link Factor}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class FactorGraphFactorEvent extends FactorGraphEvent
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private transient final Factor _factor;
	private final @Nullable String _factorName;
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * @param source of the event
	 * @param factor involved in the event
	 * @since 0.06
	 */
	protected FactorGraphFactorEvent(FactorGraph source, Factor factor)
	{
		super(source);
		_factor = factor;
		_factorName = null;
	}

	/*---------------
	 * Serialization
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		// Write out _factorName field with non-null value.
		out.writeObject(getFactorName());
	}
	
	// The default readObject method should work...
	
	/*--------------------------
	 * FactorGraphEvent methods
	 */
	
	@Override
	public @Nullable Node getNode()
	{
		return _factor;
	}
	
	@Override
	public String getNodeName()
	{
		return getFactorName();
	}
	
	@Override
	protected final NodeType nodeType()
	{
		return NodeType.FACTOR;
	}
	
	/*--------------------------------
	 * FactorGraphFactorEvent methods
	 */
	
	/**
	 * The factor involved in the event.
	 * <p>
	 * Will be null if event was obtained through deserialization.
	 * @since 0.06
	 * @see #getFactorName()
	 */
	public @Nullable Factor getFactor()
	{
		return _factor;
	}
	
	/**
	 * The name of the factor involved in the event.
	 * <p>
	 * The value of {@link Factor#getEventSourceName()} on the
	 * factor. Unlike {@link #getFactor}, this is preserved by
	 * serialization.
	 * @since 0.06
	 */
	public String getFactorName()
	{
		final String name = _factorName;
		return name == null ? _factor.getEventSourceName() : name;
	}
}
