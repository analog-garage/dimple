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

import java.io.PrintStream;
import java.util.EventObject;

import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.dimple.model.core.INode;
import com.analog.lyric.util.misc.IPrintable;
import org.eclipse.jdt.annotation.Nullable;

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
public abstract class DimpleEvent extends EventObject implements IPrintable
{
	private static final long serialVersionUID = 1L;

	/*-------
	 * State
	 */
	
	private volatile transient boolean _consumed = false;
	
	private final int _modelId;
	private final String _eventSourceName;
	
	/*--------------
	 * Construction
	 */
	
	protected DimpleEvent(IDimpleEventSource source)
	{
		super(source);
		_eventSourceName = source.getEventSourceName();
		IModelEventSource obj = source.getModelEventSource();
		_modelId = obj != null ? obj.getId() : -1;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		return toString(1);
	}
	
	/*--------------------
	 * IPrintable methods
	 */
	
	@Override
	public final void print(PrintStream out, int verbosity)
	{
		if (verbosity >= 0)
		{
			if (verbosity > 0)
			{
				out.format("%s: ", getClass().getSimpleName());
			}
			printDetails(out, verbosity);
		}
	}
	
	protected abstract void printDetails(PrintStream out, int verbosity);
	
	@Override
	public final void println(PrintStream out, int verbosity)
	{
		IPrintable.Methods.println(this, out, verbosity);
	}
	
	@Override
	public String toString(int verbosity)
	{
		return IPrintable.Methods.toString(this, verbosity);
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
	public @Nullable IDimpleEventSource getSource()
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
	public abstract @Nullable IModelEventSource getModelObject();
	
	/**
	 * The id of the model object associated with the source of the event, if applicable.
	 * <p>
	 * This is the same as the the value of {@link INode#getId()} on {@link #getModelObject()}
	 * when the latter is non-null. This is preserved by serialization.
	 * 
	 * @since 0.06
	 */
	public int getModelId()
	{
		return _modelId;
	}
	
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
		return _eventSourceName;
	}

}
