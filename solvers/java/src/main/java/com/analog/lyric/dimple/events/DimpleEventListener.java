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

import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.ReleasableIterator;
import com.analog.lyric.collect.Supers;
import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.util.misc.Internal;
import com.google.common.collect.MapMaker;

import net.jcip.annotations.ThreadSafe;


/**
 * Manages dispatching of {@link DimpleEvent}s.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@ThreadSafe
public class DimpleEventListener implements IDimpleEventListener
{
	/*---------------------
	 * Public nested types
	 */
	
	/**
	 * Describes a particular event handler setting.
	 * <p>
	 * @since 0.06
	 * @see IHandlersForSource
	 * @see DimpleEventListener#register(IDimpleEventHandler, Class, boolean, IDimpleEventSource)
	 */
	public interface IHandlerEntry
	{
		/**
		 * The base class of the events registered for handling by {@link #eventHandler()}.
		 * 
		 * @since 0.06
		 */
		public Class<? extends DimpleEvent> eventClass();
		
		/**
		 * The handler instance registered for events of specified {@link #eventClass}.
		 * 
		 * @since 0.06
		 */
		public IDimpleEventHandler<?> eventHandler();
		
		/**
		 * The event source whose events will be handled by {@link #eventHandler()}.
		 * 
		 * @since 0.06
		 */
		public IDimpleEventSource eventSource();
		
		/**
		 * Indicates whether the handler is registered to handle all subclass events
		 * or just ones with the exact specified {@link #eventClass}.
		 * 
		 * @since 0.06
		 */
		public boolean handleSubclasses();
	}
	
	/**
	 * Describes association of registered event handlers with a particular event source.
	 *
	 * @since 0.06
	 * @see DimpleEventListener#allHandlerPerSource()
	 */
	public interface IHandlersForSource
	{
		/**
		 * Event source associated with {@link #handlerEntries()}.
		 * 
		 * @since 0.06
		 */
		public IDimpleEventSource eventSource();
		
		/**
		 * Immutable list of handler entries associated with {@link #eventSource()}.
		 * 
		 * @since 0.06
		 */
		public List<IHandlerEntry> handlerEntries();
	}
	
	/*-------
	 * State
	 */
	
	private final ConcurrentMap<IDimpleEventSource, Entry[]> _handlersForSource;
	
	@Deprecated
	private static final AtomicReference<DimpleEventListener> _defaultListener =
		new AtomicReference<DimpleEventListener>();
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs a new empty event listener.
	 */
	public DimpleEventListener()
	{
		// We use ConcurrentMap so that readers don't have to lock it, but force updates to be single
		// threaded with a lock this so the concurrency level is set to one to minimize overhead.
		// Use weak keys to prevent listener from keeping event sources in memory if no one else is using them.
		_handlersForSource = new MapMaker().concurrencyLevel(1).weakKeys().makeMap();
	}

	/**
	 * @deprecated As of release 0.07 the default listener is no longer used and will be removed
	 * in a future release.
	 */
	@Deprecated
	public static DimpleEventListener getDefault()
	{
		DimpleEventListener listener;
		while ((listener = _defaultListener.get()) == null)
		{
			_defaultListener.compareAndSet(null, new DimpleEventListener());
		}
		return listener;
	}

	/**
	 * @deprecated As of release 0.07 the default listener is no longer used and will be removed
	 * in a future release.
	 */
	@Deprecated
	public boolean isDefault()
	{
		return this == _defaultListener.get();
	}
	
	/**
	 * @deprecated As of release 0.07 the default listener is no longer used and will be removed
	 * in a future release.
	 */
	@Deprecated
	public static @Nullable DimpleEventListener setDefault(@Nullable DimpleEventListener listener)
	{
		return _defaultListener.getAndSet(listener);
	}
	
	/*------------------------------
	 * IDimpleEventListener methods
	 */
	
	@Override
	public boolean isListeningFor(Class<? extends DimpleEvent> eventClass, IDimpleEventSource on)
	{
		boolean isListening = false;
		
		final ReleasableIterator<IDimpleEventSource> sources = eventSources(on);
		
		outer:
		while (sources.hasNext())
		{
			final IDimpleEventSource source = sources.next();

			Entry[] entries = _handlersForSource.get(source);
			if (entries != null)
			{
				for (Entry entry : entries)
				{
					if (entry.canHandleEvent(eventClass))
					{
						isListening = !entry._handler.isBlocker();
						break outer;
					}
				}
			}
		}
		
		sources.release();
		
		return isListening;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This method will visit each handler in order listed by the {@link #getHandlersFor} method
	 * for the event's concrete class and its source attribute and invoke it's {@code handleEvent}
	 * method. If the event is marked as consumed then no further handlers will be tried.
	 * <p>
	 * @since 0.06
	 * @see DimpleEvent#consumed()
	 * @see DimpleEvent#getSource()
	 * @see IDimpleEventHandler#handleEvent(DimpleEvent)
	 */
	@Override
	public boolean raiseEvent(DimpleEvent event)
	{
		final ReleasableIterator<IDimpleEventSource> sources = eventSources(event.getSource());
		boolean handlerFound = false;
		
		outer:
		while (sources.hasNext())
		{
			final IDimpleEventSource source = sources.next();

			Entry[] entries = _handlersForSource.get(source);
			if (entries != null)
			{
				for (Entry entry : entries)
				{
					handlerFound |= entry.handleEvent(event);
					if (event.consumed())
					{
						break outer;
					}
				}
			}
		}
		
		sources.release();
		
		return handlerFound;
	}
	
	/*------------------------------------
	 * Static DimpleEventListener methods
	 */
	
	/**
	 * Determine if event source has listener active for given event type.
	 * 
	 * @param source non-null source of event.
	 * @param eventClass non-null type of event listened for.
	 * @return true if {@code source} has an event listener that is listening for
	 * the given {@code eventClass}.
	 * @since 0.06
	 */
	public static boolean sourceHasListenerFor(IDimpleEventSource source, Class<? extends DimpleEvent> eventClass)
	{
		IDimpleEventListener listener = source.getEventListener();
		return listener != null && listener.isListeningFor(eventClass, source);
	}
	
	/*-----------------------------
	 * DimpleEventListener methods
	 */
	
	/**
	 * Lists all handlers currently registered with this listener for each source.
	 * <p>
	 * The underlying iterators do not support removal.
	 * <p>
	 * @since 0.06
	 */
	public Iterable<IHandlersForSource> allHandlerPerSource()
	{
		return new HandlersForSourceIterable();
	}
	
	/**
	 * Block specified events from percolating past specified source.
	 * <p>
	 * Simply invokes {@link #register(IDimpleEventHandler, Class, boolean, IDimpleEventSource)} with
	 * {@link DimpleEventBlocker} as the handler.
	 * @since 0.06
	 */
	public void block(Class<? extends DimpleEvent> eventClass, boolean blockSubclasses, IDimpleEventSource target)
	{
		register(DimpleEventBlocker.INSTANCE, eventClass, blockSubclasses, target);
	}
	
	/**
	 * Returns list of handler entries registered to handle the given event.
	 * <p>
	 * The entries are given in the order in which they would be invoked when calling {@link #raiseEvent(DimpleEvent)}
	 * when passed an event with given {@code eventClass} and {@code eventSource}.
	 * This may include more entries than would be invoked by {@code raiseEvent} since that method stops processing
	 * if the event is consumed by a handler.
	 * <p>
	 * The list is constructed by checking very source in {@link #eventSources(IDimpleEventSource)} starting
	 * with given {@code eventSource} in order and adding entries matching given {@code eventClass} in topological
	 * order from subclass to superclass. The list is terminated if a blocking handler is found.
	 * 
	 * @return newly allocated mutable list
	 * @see IDimpleEventHandler#isBlocker()
	 * @since 0.06
	 */
	public List<IHandlerEntry> getHandlersFor(Class<? extends DimpleEvent> eventClass, IDimpleEventSource eventSource)
	{
		final List<IHandlerEntry> handlers = new ArrayList<IHandlerEntry>();
		
		final ReleasableIterator<IDimpleEventSource> sources = eventSources(eventSource);
		
		outer:
		while (sources.hasNext())
		{
			final IDimpleEventSource source = sources.next();

			Entry[] entries = _handlersForSource.get(source);
			if (entries != null)
			{
				for (Entry entry : entries)
				{
					if (entry.canHandleEvent(eventClass))
					{
						handlers.add(entry);
						if (entry.eventHandler().isBlocker())
						{
							break outer;
						}
					}
				}
			}
		}
		
		sources.release();
		
		return handlers;
	}
	
	/**
	 * Iterates over sources of {@code event} in hierarchical order from child to parent.
	 * <p>
	 * Specifically, this is equivalent to the ordering that would be produced by:
	 * <p>
	 * <pre>
	 * void addSources(List&lt;IDimpleEventSource&gt; sources, IDimpleEventSource source)
	 * {
	 *     source.add(source);
	 *     if (source.getModelEventSource() != source)
	 *     {
	 *         source.add(source.getModelEventSource());
	 *     }
	 *     if (source.getEventParent() != null)
	 *     {
	 *         addSources(source.getEventParent());
	 *     }
	 * }
	 * </pre>
	 * <p>
	 * Iterator is not thread-safe. Expected to be used for a single function call.
	 * 
	 * @since 0.06
	 */
	public EventSourceIterator eventSources(@Nullable IDimpleEventSource source)
	{
		return EventSourceIterator.create(source);
	}
	
	/**
	 * True if no handlers are registered with this listener.
	 * 
	 * @since 0.06
	 */
	public boolean isEmpty()
	{
		return _handlersForSource.isEmpty();
	}
	
	/**
	 * Notifies all registered sources that listener has changed.
	 * <p>
	 * Invokes {@link IDimpleEventSource#notifyListenerChanged()} on every source with a registered
	 * handler. This is used by {@link DimpleEnvironment} when changing the environment's listener.
	 * <p>
	 * @since 0.07
	 */
	@Internal
	public synchronized void notifyListenerChanged()
	{
		for (IDimpleEventSource source : _handlersForSource.keySet())
		{
			source.notifyListenerChanged();
		}
	}
	
	/**
	 * Registers an event handler for given event class on given event source.
	 * <p>
	 * @param handler non-null handler
	 * @param eventClass is the class of events to be handled
	 * @param handleSubclasses specifies whether the handler should be advertised as listening for instances
	 * of subclasses of the {@code eventClass}.
	 * @param target is the object to be monitored for events. Note that children of this object may also
	 * @since 0.06
	 * @see #register(IDimpleEventHandler, Class, IDimpleEventSource)
	 * @see #raiseEvent(DimpleEvent)
	 */
	public <Event extends DimpleEvent> void register(
		IDimpleEventHandler<? super Event> handler,
		Class<Event> eventClass,
		boolean handleSubclasses,
		IDimpleEventSource target)
	{
		Objects.requireNonNull(handler);
		Objects.requireNonNull(eventClass);
		Objects.requireNonNull(target);
		
		final Entry entry = new Entry(handler, eventClass, handleSubclasses, target);
			
		synchronized(this)
		{
			Entry[] entries = _handlersForSource.get(target);
			
			if (entries == null)
			{
				entries = new Entry[] { entry };
			}
			else
			{
				final int size = entries.length;
				int i = Arrays.binarySearch(entries, entry, EntryEventClassOrder.COMPARATOR);
				
				if (i >= 0)
				{
					// A match is found. Search linearly for an exact match.
					for (; i < size && !entry.equals(entries[i]); ++i) {}
				}
				
				if (0 <= i && i < size)
				{
					// An entry already exists for this class/handler combination; so replace it.
					entries = entries.clone();
					entries[i] = entry;
				}
				else
				{
					entries = Arrays.copyOf(entries, size + 1);
					if (i < 0)
					{
						// Insert new entry in middle.
						i = -(i+1);
						System.arraycopy(entries, i, entries, i + 1, size - i);
					}
					entries[i] = entry;
				}
			}
			
			_handlersForSource.put(target, entries);
		}
	}
	
	/**
	 * Registers an event handler for given event class on given event source.
	 * <p>
	 * This simply invokes {@link #register(IDimpleEventHandler, Class, boolean, IDimpleEventSource)}
	 * passing true for the {@code handleSubclasses} argument if {@code eventClass} is abstract.
	 * <p>
	 * @param handler non-null handler
	 * @param eventClass is the class of events to be handled
	 * @param target is the object to be monitored for events. Note that children of this object may also
	 * @since 0.06
	 * @see #raiseEvent(DimpleEvent)
	 */
	public <Event extends DimpleEvent> void register(
		IDimpleEventHandler<? super Event> handler,
		Class<Event> eventClass,
		IDimpleEventSource target)
	{
		register(handler, eventClass, Modifier.isAbstract(eventClass.getModifiers()), target);
	}
	
	/**
	 * Unblocks specified events from percolating past specified source.
	 * <p>
	 * Simply invokes {@link #unregister(IDimpleEventHandler, Class, IDimpleEventSource)} with
	 * {@link DimpleEventBlocker} as the handler. Note that the {@code eventClass} and {@code target}
	 * must exactly match that used with previous {@link #block(Class, boolean, IDimpleEventSource)} call.
	 * 
	 * @return false if no matching blocker was found.
	 * @since 0.06
	 */
	public boolean unblock(Class<? extends DimpleEvent> eventClass, IDimpleEventSource target)
	{
		return unregister(DimpleEventBlocker.INSTANCE, eventClass, target);
	}
	
	/**
	 * Removes previously registered handler for given event class and event source.
	 * 
	 * @return false if no handler matching the arguments was found.
	 * @since 0.06
	 */
	public boolean unregister(
		IDimpleEventHandler<?> handler,
		Class<? extends DimpleEvent> eventClass,
		IDimpleEventSource target)
	{
		boolean found = false;
		
		synchronized(this)
		{
			Entry[] entries = _handlersForSource.get(target);
			
			if (entries != null)
			{
				final Entry entry = new Entry(handler, eventClass, false, target);
				
				int i = Arrays.binarySearch(entries, entry, EntryEventClassOrder.COMPARATOR);
				if (i >= 0)
				{
					for (int size = entries.length; i < size; ++i)
					{
						if (entry.equals(entries[i]))
						{
							if (--size == 0)
							{
								_handlersForSource.remove(target);
							}
							else
							{
								final Entry[] newEntries = new Entry[size];
								System.arraycopy(entries, 0, newEntries, 0, i);
								System.arraycopy(entries, i + 1, newEntries, i, size - i);
								_handlersForSource.put(target, newEntries);
							}
							found = true;
							break;
						}
					}
				}
			}
		}
		
		return found;
	}
	
	/**
	 * Unregister all event handlers for given source.
	 * <p>
	 * This does not remove handlers registered with the source's children.
	 * 
	 * @return true if any handlers were unregistered.
	 * 
	 * @since 0.06
	 */
	public boolean unregisterSource(IDimpleEventSource source)
	{
		boolean found = false;
		
		synchronized(this)
		{
			found = null != _handlersForSource.remove(source);
		}
		
		return found;
	}
	
	/**
	 * Unregisters all handlers.
	 * <p>
	 * After invoking this {@link #allHandlerPerSource()} will be empty.
	 * 
	 * @since 0.06
	 */
	public void unregisterAll()
	{
		synchronized(this)
		{
			_handlersForSource.clear();
		}
	}
	
	/**
	 * Unregisters all instances of given handler.
	 * <p>
	 * @param handler is the handler instance that will be removed.
	 * @return the number of entries that were removed from the listener.
	 * @since 0.06
	 */
	public int unregisterAll(IDimpleEventHandler<?> handler)
	{
		int nRemoved = 0;
		
		synchronized(this)
		{
			final List<IHandlerEntry> entriesToRemove = new ArrayList<IHandlerEntry>();
			
			for (IHandlersForSource handlersForSource : allHandlerPerSource())
			{
				for (IHandlerEntry entry : handlersForSource.handlerEntries())
				{
					if (entry.eventHandler() == handler)
					{
						entriesToRemove.add(entry);
					}
				}
			}
			
			for (IHandlerEntry entry : entriesToRemove)
			{
				unregister(handler, entry.eventClass(), entry.eventSource());
				++nRemoved;
			}
		}
		
		return nRemoved;
	}
	
	/*-----------------------
	 * Private inner classes
	 */

	/**
	 * Entry containing handler registered for given event class for a given target.
	 */
	private static class Entry implements IHandlerEntry
	{
		/*-------
		 * State
		 */
		
		private final IDimpleEventSource _eventSource;
		private final Class<? extends DimpleEvent> _eventClass;
		private final boolean _handleSubclasses;
		private final IDimpleEventHandler<DimpleEvent> _handler;
		private final int _eventClassDepth;
		
		/*--------------
		 * Construction
		 */
		
		@SuppressWarnings("unchecked")
		private Entry(IDimpleEventHandler<?> handler, Class<? extends DimpleEvent> eventClass, boolean handleSubclasses,
			IDimpleEventSource eventSource)
		{
			_eventSource = eventSource;
			_eventClass = eventClass;
			_handleSubclasses = handleSubclasses;
			_handler = (IDimpleEventHandler<DimpleEvent>) handler;
			_eventClassDepth = Supers.numberOfSuperClasses(eventClass);
		}
		
		/*----------------
		 * Object methods
		 */
		
		/**
		 * True if other entry has same {@link #eventClass}, {@link #eventSource}, and {@link #eventHandler}.
		 */
		@Override
		public boolean equals(@Nullable Object other)
		{
			if (this == other)
			{
				return true;
			}
			
			if (!(other instanceof Entry))
			{
				return false;
			}
			
			Entry that = (Entry)other;
			
			return this._eventSource == that._eventSource &&
				this._eventClass == that._eventClass &&
				this._handler == that._handler;
		}
		
		@Override
		public int hashCode()
		{
			return _eventSource.hashCode() * 3 + _eventClass.hashCode() * 5 + _handler.hashCode();
		}
		
		/*----------------
		 * IHandlerEntry
		 */
		
		@Override
		public final Class<? extends DimpleEvent> eventClass()
		{
			return _eventClass;
		}

		@Override
		public final IDimpleEventHandler<?> eventHandler()
		{
			return _handler;
		}

		@Override
		public final IDimpleEventSource eventSource()
		{
			return _eventSource;
		}
		
		@Override
		public final boolean handleSubclasses()
		{
			return _handleSubclasses;
		}

		/*---------
		 * Private
		 */
		
		private boolean canHandleEvent(Class<? extends DimpleEvent> eventClass)
		{
			return _handleSubclasses ? _eventClass.isAssignableFrom(eventClass) : _eventClass.equals(eventClass);
		}

		private boolean handleEvent(DimpleEvent event)
		{
			final boolean handle = canHandleEvent(event.getClass());
			
			if (handle)
			{
				_handler.handleEvent(event);
			}
			
			return handle;
		}
	}
	
	/**
	 * Produces an ordering based on the depth of the event class in the class hierarchy with
	 * subclasses coming before superclasses. Ties are broken using the event class names,
	 * which ensures that multiple entries for the same class will be next to each other.
	 */
	@NonNullByDefault(false)
	private static enum EntryEventClassOrder implements Comparator<Entry>
	{
		COMPARATOR;
		
		@Override
		public int compare(Entry entry1, Entry entry2)
		{
			int diff = entry2._eventClassDepth - entry1._eventClassDepth;
			if (diff == 0)
			{
				diff = entry1._eventClass.getName().compareTo(entry2._eventClass.getName());
			}
			return diff;
		}
	}
	
	private static final class HandlerEntryList extends AbstractList<IHandlerEntry>
	{
		private final IHandlerEntry[] _entries;
		
		private HandlerEntryList(IHandlerEntry[] entries)
		{
			_entries = entries;
		}
		
		@Override
		public IHandlerEntry get(int index)
		{
			return _entries[index];
		}

		@Override
		public int size()
		{
			return _entries.length;
		}
		
	}
	
	private static final class HandlersForSource implements IHandlersForSource
	{
		private final IDimpleEventSource _eventSource;
		private final List<IHandlerEntry> _handlerEntries;
		
		private HandlersForSource(Map.Entry<IDimpleEventSource, Entry[]> entry)
		{
			_eventSource = entry.getKey();
			_handlerEntries = new HandlerEntryList(entry.getValue());
		}
		
		@Override
		public IDimpleEventSource eventSource()
		{
			return _eventSource;
		}
		
		@Override
		public List<IHandlerEntry> handlerEntries()
		{
			return _handlerEntries;
		}
	}
	
	private final class HandlersForSourceIterator implements Iterator<IHandlersForSource>
	{
		private Iterator<Map.Entry<IDimpleEventSource, Entry[]>> _iterator =
			_handlersForSource.entrySet().iterator();
		
		@Override
		public boolean hasNext()
		{
			return _iterator.hasNext();
		}

		@Override
		public HandlersForSource next()
		{
			return new HandlersForSource(_iterator.next());
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Iterator.remove");
		}
	}
	
	private final class HandlersForSourceIterable implements Iterable<IHandlersForSource>
	{
		@Override
		public Iterator<IHandlersForSource> iterator()
		{
			return new HandlersForSourceIterator();
		}
	}
}
