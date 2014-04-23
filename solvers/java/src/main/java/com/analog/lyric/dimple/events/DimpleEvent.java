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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.EventObject;

import net.jcip.annotations.ThreadSafe;

/**
 * Abstract base class for all Dimple events.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@ThreadSafe
public abstract class DimpleEvent extends EventObject
{
	private static final long serialVersionUID = 1L;

	private volatile transient boolean _consumed = false;
	
	private String _eventSourceName;
	
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
		if (_eventSourceName == null)
		{
			IDimpleEventSource source = getSource();
			_eventSourceName = source.getEventSourceName();
		}
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
	}
	
	/*---------------------
	 * EventObject methods
	 */
	
	/**
	 * The object that generated this event.
	 * <p>
	 * This should be non-null for a freshly generated event, but will not be preserved
	 * by serialization (i.e. it will be null on a deserialized instance).
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
