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

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.UnmodifiableReleasableIterator;

import net.jcip.annotations.NotThreadSafe;

/**
 * Iterates over Dimple event sources, which are also Dimple option holders.
 * <p>
 * This provides the iterator returned by both the
 * {@link com.analog.lyric.dimple.options.DimpleOptionHolder#getOptionDelegates DimpleOptionHolder.getOptionDelegates}
 * and {@link com.analog.lyric.dimple.events.DimpleEventListener#eventSources DimpleEventListener.eventSources}
 * methods. It is used for both option lookup and event dispatching.
 * <p>
 * The iterator visits objects starting from an initial <em>source</em> using the following recursive ordering:
 * <ol>
 * <li>Visit <em>source</em>
 * <li>Visit <em>source</em>'s corresponding {@linkplain IDimpleEventSource#getModelEventSource model event source}
 * if not null and not the same as the <em>source</em> itself. For instance, if <em>source</em> were a Dimple solver
 * variable, this step would visit the corresponding model variable.
 * <li>If the <em>source</em> has a non-null {@linkplain IDimpleEventSource#getEventParent() parent}, then
 * recursively visit's sources starting with the parent.
 * <li>Otherwise, if the <em>source</em> has no parent, but visited a <em>model source</em> in step 2 with
 * a non-null parent, then the algorithm will recursively visit sources from the model's parent.
 * </ol>
 * Or written in pseudocode:
 * <blockquote>
 * <pre>
 * void visitSources(IDimpleEventSource source)
 * {
 *     visit(source);
 * 
 *     IDimpleEventSource modelSource = source.getModelEventSource();
 *     boolean hasModel = modelSource != null &amp;&amp; modelSource != source;
 *     if (hasModel)
 *         visit(modelSource);
 * 
 *     IDimpleEventSource parent = source.getEventParent();
 *     if (parent == null &amp;&amp; hasModel)
 *         parent = model.getEventParent();
 * 
 *     if (parent != null)
 *     {
 *         visitSources(parent);
 *     }
 * }
 * </pre>
 * </blockquote>
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@NotThreadSafe
public class EventSourceIterator extends UnmodifiableReleasableIterator<IDimpleEventSource>
{
	private @Nullable IDimpleEventSource _next;
	private @Nullable IDimpleEventSource _prev;
	
	private static final AtomicReference<EventSourceIterator> _reusableInstance = new AtomicReference<>();
	
	/*--------------
	 * Construction
	 */
	
	private EventSourceIterator()
	{
	}
	
	/**
	 * Create iterator starting with given source.
	 * <p>
	 * Most users should instead use {@link IDimpleEventSource#getOptionDelegates()}.
	 * <p>
	 * @param source if null will create an empty iterator.
	 * @since 0.07
	 */
	public static EventSourceIterator create(@Nullable IDimpleEventSource source)
	{
		EventSourceIterator iter = _reusableInstance.getAndSet(null);
		if (iter == null)
		{
			iter = new EventSourceIterator();
		}
		iter.reset(source);
		return iter;
	}
	
	/*------------------
	 * Iterator methods
	 */
	
	@Override
	public boolean hasNext()
	{
		return _next != null;
	}

	@Override
	public @Nullable IDimpleEventSource next()
	{
		IDimpleEventSource source = _next;
		
		if (source != null)
		{
			// Find next source.
			
			final IDimpleEventSource prev = _prev;
			if (prev != null)
			{
				_next = prev.getEventParent();
				_prev = null;
				if (_next == null)
				{
					// If no more parents, use parent of corresponding model object
					IModelEventSource modelSource = prev.getModelEventSource();
					if (modelSource != prev && modelSource != null)
					{
						_next = modelSource.getEventParent();
					}
				}
			}
			else
			{
				IModelEventSource modelSource = source.getModelEventSource();
				if (modelSource != source)
				{
					_next = modelSource;
					_prev = source;
				}
				else
				{
					_next = source.getEventParent();
				}
			}
		}

		return source;
	}

	/*----------------------------
	 * ReleasableIterator methods
	 */
	
	@Override
	public void release()
	{
		reset(null);
		_reusableInstance.set(this);
	}

	/**
	 * Resets the iterator to start iteration at given {@code source}.
	 * 
	 * @param source is the first source to visit. If null, the iterator will be empty.
	 * @since 0.07
	 */
	public void reset(@Nullable IDimpleEventSource source)
	{
		_next = source;
		_prev = null;
	}
	
}