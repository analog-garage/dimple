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
import java.util.EventObject;

import net.jcip.annotations.ThreadSafe;

/**
 * Abstract base class for all Dimple events.
 * <p>
 * Concrete subclasses should be immutable and should support serialization.
 * Subclasses must not attempt to serialize the model or solver graphs themselves!
 * <p>
 * @since 0.06
 * @author Christopher Barber
 */
@ThreadSafe
public abstract class DimpleEvent extends EventObject
{
	private static final long serialVersionUID = 1L;

	private volatile transient boolean _consumed = false;
	
	private final String _eventSourceName;
	
	/*--------------
	 * Construction
	 */
	
	DimpleEvent(IDimpleEventSource source)
	{
		super(source);
		_eventSourceName = null;
	}
	
	/*---------------
	 * Serialization
	 */
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeObject(getSourceName()); // Make sure _eventSourceName is non-null.
	}

	/*---------------------
	 * EventObject methods
	 */
	
	/**
	 * The object that generated this event.
	 * <p>
	 * This should be non-null for a freshly generated event, but will not be preserved
	 * by serialization (i.e. it will be null on a deserialized instance).
	 * <p>
	 * @see #getSourceName()
	 */
	@Override
	public IDimpleEventSource getSource()
	{
		return (IDimpleEventSource)source;
	}
	
	/*---------------------
	 * DimpleEvent methods
	 */
	
	/**
	 * Indicate whether event has been "consumed" by a handler and will therefore not be
	 * passed on for additional handling.
	 * <p>
	 * This attribute will not be serialized (i.e. will be false when deserialized).
	 * <p>
	 * @since 0.06
	 */
	public final boolean consumed()
	{
		return _consumed;
	}
	
	/**
	 * Mark whether event has been {@link #consumed()}.
	 *
	 * @since 0.06
	 */
	public final void consumed(boolean consume)
	{
		_consumed = consume;
	}
	
	/**
	 * Model object associated with the source of the event, if any (could be null).
	 * <p>
	 * Like {@link #getSource()}, this is not preserved by serialization.
	 *
	 * @since 0.06
	 */
	public abstract IModelEventSource getModelObject();
	
	/**
	 * The name of the event source.
	 * <p>
	 * Returns value of {@link IDimpleEventSource#getEventSourceName()} for
	 * source returned by {@link #getSource()}, but unlike the source itself
	 * this value is preserved by serialization.
	 * <p>
	 * @since 0.06
	 */
	public String getSourceName()
	{
		String name = _eventSourceName;
		if (name == null)
		{
			name = getSource().getEventSourceName();
		}
		return name;
	}
}
