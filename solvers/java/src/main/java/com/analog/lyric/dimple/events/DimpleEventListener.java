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

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import net.jcip.annotations.NotThreadSafe;
import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.collect.Supers;
import com.analog.lyric.util.misc.Internal;


/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@ThreadSafe
public class DimpleEventListener implements IDimpleEventListener
{
	/*-------
	 * State
	 */
	
	private final ConcurrentMap<IDimpleEventSource, Entry<?>[]> _handlersForSource;
	
	// Reusable iterator for visiting event sources in hierarchical order.
	private final AtomicReference<EventSourceIterator> _eventSourceIterator =
		new AtomicReference<EventSourceIterator>();
	
	/*--------------
	 * Construction
	 */
	
	DimpleEventListener()
	{
		// We use ConcurrentMap so that readers don't have to lock it, but force updates to be single
		// threaded with a lock.
		_handlersForSource = new ConcurrentHashMap<IDimpleEventSource, Entry<?>[]>(16, .75f, 1);
	}
	
	/*------------------------------
	 * IDimpleEventListener methods
	 */
	
	/*
	 * 
	 */
	@Override
	public boolean isListeningFor(Class<? extends DimpleEvent> eventClass, IDimpleEventSource on)
	{
		final ReleasableIterator<IDimpleEventSource> sources = eventSources(on);
		
		while (sources.hasNext())
		{
			final IDimpleEventSource source = sources.next();

			Entry<?>[] entries = _handlersForSource.get(source);
			if (entries != null)
			{
				for (Entry<?> entry : entries)
				{
					if (entry.canHandleEvent(eventClass))
					{
						return !entry._handler.isBlocker();
					}
				}
			}
		}
		
		sources.release();
		
		return false;
	}

	/*
	 * 
	 */
	@Override
	public void raiseEvent(DimpleEvent event)
	{
		final ReleasableIterator<IDimpleEventSource> sources = eventSources(event.getSource());
		
		while (sources.hasNext())
		{
			final IDimpleEventSource source = sources.next();

			Entry<?>[] entries = _handlersForSource.get(source);
			if (entries != null)
			{
				for (Entry<?> entry : entries)
				{
					entry.handleEvent(event);
					if (event.consumed())
					{
						return;
					}
				}
			}
		}
		
		sources.release();
	}
	
	/*-----------------------------
	 * DimpleEventListener methods
	 */
	
	/**
	 * Iterates over sources of {@code event} in hierarchical order from child to parent.
	 * <p>
	 * Iterator is not thread-safe. Expected to be used for a single function call.
	 */
	@Internal
	public ReleasableIterator<IDimpleEventSource> eventSources(IDimpleEventSource source)
	{
		EventSourceIterator iterator = _eventSourceIterator.getAndSet(null);
		if (iterator == null)
		{
			iterator = new EventSourceIterator();
		}
		iterator.init(source);
		return iterator;
	}
	
	public <Event extends DimpleEvent> void registerEventHandler(
		IDimpleEventHandler<? super Event> handler,
		Class<Event> eventClass,
		boolean handleSubclasses,
		IDimpleEventSource target)
	{
		Entry<Event> entry = new Entry<Event>(eventClass, handleSubclasses, handler);
			
		synchronized(_handlersForSource)
		{
			Entry<?>[] entries = _handlersForSource.get(target);
			
			makeNewEntries:
			if (entries == null)
			{
				entries = new Entry[] { entry };
			}
			else
			{
				final int size = entries.length;
				for (int i = 0; i < size; ++i)
				{
					if (entries[i]._eventClass == eventClass)
					{
						// An entry already exists for this class, so replace it.
						entries[i] = entry;
						break makeNewEntries;
					}
				}
				
				entries = Arrays.copyOf(entries, size + 1);
				entries[size] = entry;
				// This makes adding handlers for the same source O(n^2 log n), but we don't expect the
				// number of distinct handlers n to be very high.
				Arrays.sort(entries, EventClassDepth.COMPARATOR);
			}
			
			_handlersForSource.put(target, entries);
		}
	}
	
	/*-----------------------
	 * Private inner classes
	 */

	/**
	 * Entry containing handler registered for given event class for a given target.
	 */
	private static class Entry<Event extends DimpleEvent>
	{
		private final Class<Event> _eventClass;
		private final boolean _handleSubclasses;
		private final IDimpleEventHandler<DimpleEvent> _handler;
		private final int _eventClassDepth;
		
		@SuppressWarnings("unchecked")
		private Entry(Class<Event> eventClass, boolean handleSubclasses, IDimpleEventHandler<? super Event> handler)
		{
			_eventClass = eventClass;
			_handleSubclasses = handleSubclasses;
			_handler = (IDimpleEventHandler<DimpleEvent>) handler;
			_eventClassDepth = Supers.numberOfSuperClasses(eventClass);
		}
		
		private boolean canHandleEvent(Class<? extends DimpleEvent> eventClass)
		{
			return _handleSubclasses ? _eventClass.isAssignableFrom(eventClass) : _eventClass.equals(eventClass);
		}

		private void handleEvent(DimpleEvent event)
		{
			if (canHandleEvent(event.getClass()))
			{
				_handler.handleEvent(event);
			}
		}
	}
	
	/**
	 * Produces an ordering based on the depth of the event class in the class hierarchy with
	 * superclasses coming before subclasses.
	 */
	private static enum EventClassDepth implements Comparator<Entry<?>>
	{
		COMPARATOR;
		
		@Override
		public int compare(Entry<?> entry1, Entry<?> entry2)
		{
			return entry1._eventClassDepth - entry2._eventClassDepth;
		}
	}
	
	@NotThreadSafe
	private class EventSourceIterator implements ReleasableIterator<IDimpleEventSource>
	{
		private IDimpleEventSource _next;
		private IDimpleEventSource _prev;
		
		/*--------------
		 * Construction
		 */
		
		private EventSourceIterator()
		{
		}
		
		private void init(IDimpleEventSource source)
		{
			_next = source;
			_prev = null;
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
		public IDimpleEventSource next()
		{
			IDimpleEventSource source = _next;
			
			if (source != null)
			{
				// Find next source.
				
				if (_prev != null)
				{
					_next = _prev.getEventParent();
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

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		/*----------------------------
		 * ReleasableIterator methods
		 */
		
		@Override
		public void release()
		{
			_eventSourceIterator.set(this);
		}
	}
	
}
