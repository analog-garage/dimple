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

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.UnmodifiableReleasableIterator;
import com.analog.lyric.util.misc.Nullable;

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
		_next = null;
		_prev = null;
		_reusableInstance.set(this);
	}

	public void reset(@Nullable IDimpleEventSource source)
	{
		_next = source;
		_prev = null;
	}
	
}