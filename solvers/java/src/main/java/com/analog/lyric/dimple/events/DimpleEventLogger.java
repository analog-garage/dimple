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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jcip.annotations.ThreadSafe;

import com.analog.lyric.dimple.model.core.FactorGraph;

/**
 * Provides basic logging of dimple events.
 * <p>
 * This class provides a simpler interface for than {@link DimpleEventListener} when
 * you only want to print the text of events to an output stream.
 * <p>
 * For instance, to log all dimple events to stderr for a graph {@code fg}, you only need to write:
 * <pre>
 *     DimpleEventLogger logger = new DimpleEventLogger();
 *     logger.log(DimpleEvent.class, fg);
 * </pre>
 * and to write to a specified file instead, you just need to open the logger with the
 * given file, as in:
 * <pre>
 *     logger.open(new File("events.log"));
 * </pre>
 * 
 * The event logger will use the existing {@link DimpleEventListener} that is associated
 * with the root graph of each event source, if it has been set. Otherwise, it will automatically
 * set the root graph to use the listener {@link DimpleEventListener#getDefault()}.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
@ThreadSafe
public class DimpleEventLogger implements Closeable
{
	/*-------
	 * State
	 */
	
	/**
	 * Events will be printed on this stream, if non-null.
	 */
	private volatile PrintStream _out;
	
	/**
	 * The file used for {@link #_out}, if opened by {@link #open(File)}.
	 */
	private volatile File _file = null;
	
	private volatile int _verbosity;
	
	private final Set<DimpleEventListener> _listeners = new HashSet<DimpleEventListener>();
	private final Set<FactorGraph> _graphs = new HashSet<FactorGraph>();

	private final DimpleEventHandler<DimpleEvent> _handler = new EventPrinter();
	
	private class EventPrinter extends DimpleEventHandler<DimpleEvent>
	{
		@Override
		public void handleEvent(DimpleEvent event)
		{
			PrintStream out = _out;
			if (out != null)
			{
				event.println(out, _verbosity);
				out.flush();
			}
		}
	}
	
	public static class LogEntry
	{
		private final Class<? extends DimpleEvent> _eventClass;
		private final IDimpleEventSource _eventSource;
		
		private LogEntry(DimpleEventListener.IHandlerEntry entry)
		{
			_eventClass = entry.eventClass();
			_eventSource = entry.eventSource();
		}
		
		public Class<? extends DimpleEvent> eventClass()
		{
			return _eventClass;
		}
		
		public IDimpleEventSource eventSource()
		{
			return _eventSource;
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs logger with output going to {@link System#err} and
	 * {@link #verbosity()} set to zero.
	 * 
	 * @since 0.06
	 */
	public DimpleEventLogger()
	{
		_out = System.err;
		_verbosity = 0;
	}
	
	/*-------------------
	 * Closeable methods
	 */
	
	/**
	 * Closes underlying stream.
	 * <p>
	 * If {@link #out()} is non-null, closes it if not one of {@link System#out} or {@link System#err},
	 * and then sets to null.
	 */
	@Override
	public synchronized void close()
	{
		if (_out != null)
		{
			if (_out != System.out && _out != System.err)
			{
				_out.close();
			}
			_out = null;
		}
	}
	
	/*---------
	 * Methods
	 */

	/**
	 * File last used by {@link #open(File)} or null if last opened by {@link #open(PrintStream)}.
	 * 
	 * @since 0.06
	 */
	public File file()
	{
		return _file;
	}
	
	/**
	 * True if object is known to not have any active log entries, no calls have been made
	 * to {@link #log(Class, IDimpleEventSource...)} since object was created or last call
	 * to {@link #clear()}.
	 * 
	 * @since 0.06
	 */
	public boolean isClear()
	{
		return _listeners.isEmpty();
	}
	
	/**
	 * True if {@link #out()} is non-null.
	 * 
	 * @since 0.06
	 */
	public boolean isOpen()
	{
		return _out != null;
	}
	
	/**
	 * Enable logging of given event type on specified targets.
	 * <p>
	 * Enables logging by registering an event handler with the {@link FactorGraph#getEventListener()}
	 * for the root graph containing each source. If a root graph does not currently have a listener,
	 * its listener will automatically be set to {@link DimpleEventListener#getDefault()}.
	 * <p>
	 * @param eventType is the superclass of the type of events that will be logged. If {@code eventType}
	 * is abstract then all subtypes will be logged, otherwise only that specific type will be logged.
	 * @param sources lists the objects that should log events. This will affect both those objects and their
	 * children unless blocked.
	 * @since 0.06
	 */
	public synchronized void log(Class<? extends DimpleEvent> eventType, IDimpleEventSource ... sources)
	{
		for (IDimpleEventSource source : sources)
		{
			DimpleEventListener listener = listenerForSource(source);
			listener.register(_handler, eventType, source);
		}
	}
	
	/**
	 * Returns newly created list of current log entries for this object in no particular order.
	 * 
	 * @since 0.06
	 */
	public synchronized List<LogEntry> logEntries()
	{
		ArrayList<LogEntry> entries = new ArrayList<LogEntry>();
		for (DimpleEventListener listener : _listeners)
		{
			for (DimpleEventListener.IHandlersForSource handlersForSource : listener.allHandlerPerSource())
			{
				for (DimpleEventListener.IHandlerEntry entry : handlersForSource.handlerEntries())
				{
					if (entry.eventHandler() == _handler)
					{
						entries.add(new LogEntry(entry));
					}
				}
			}
		}
		
		return entries;
	}
	
	/**
	 * Removes log configuration of given event type on specified targets.
	 * <p>
	 * Note that this will only remove logging set up for the same {@code eventType} and source combination.
	 * It will not block logging when {@link #log(Class, IDimpleEventSource...)} was called on a parent
	 * object of the source.
	 * <p>
	 * @param eventType is type used in a previous call to {@link #log}.
	 * @param sources are one or more sources previously used with {@code eventType} in a previous call
	 * to {@link #log}.
	 * @return the number of log entries that were removed by this call.
	 * @since 0.06
	 */
	public synchronized int unlog(Class<? extends DimpleEvent> eventType, IDimpleEventSource ... sources)
	{
		int nRemoved = 0;
		for (IDimpleEventSource source : sources)
		{
			FactorGraph rootGraph = source.getContainingGraph().getRootGraph();
			DimpleEventListener listener = rootGraph.getEventListener();
			if (listener!= null)
			{
				if (listener.unregister(_handler, eventType, source))
				{
					++nRemoved;
				}
			}
		}
		return nRemoved;
	}
	
	/**
	 * Directs log output to append to file.
	 * <p>
	 * Invokes {@link #close()} before opening new file.
	 * <p>
	 * @param file is non-null file that will be opened in append mode.
	 * @throws FileNotFoundException
	 * @since 0.06
	 * @see #open(File, boolean)
	 */
	public void open(File file) throws FileNotFoundException
	{
		open(file, true);
	}
	
	/**
	 * Directs log output to a file.
	 * <p>
	 * Invokes {@link #close()} before opening new file.
	 * <p>
	 * @param file is non-null file that will be opened in append mode.
	 * @param append indicates whether to append to the file or overwrite the existing contents.
	 * @throws FileNotFoundException
	 * @since 0.06
	 * @see #open(File)
	 * @see #open(PrintStream)
	 */
	public synchronized void open(File file, boolean append) throws FileNotFoundException
	{
		close();
		_out = new PrintStream(new FileOutputStream(file, append));
		_file = file;
	}
	
	/**
	 * Directs log output to given stream.
	 * 
	 * @param out
	 * @since 0.06
	 * @see #open(File)
	 * @see #open(File, boolean)
	 */
	public synchronized void open(PrintStream out)
	{
		close();
		_out = out;
		_file = null;
	}
	
	/**
	 * Clears all logging handlers controlled by this object.
	 * 
	 * @since 0.06
	 */
	public synchronized void clear()
	{
		DimpleEventListener defaultListener = null;
		for (DimpleEventListener listener : _listeners)
		{
			listener.unregisterAll(_handler);
			if (listener.isDefault())
			{
				defaultListener = listener;
			}
		}
		_listeners.clear();
		if (defaultListener != null && defaultListener.isEmpty())
		{
			for (FactorGraph graph : _graphs)
			{
				if (graph.getEventListener() == defaultListener)
				{
					graph.setEventListener(null);
				}
			}
		}
		_graphs.clear();
	}
	
	/**
	 * The current stream used for logging. May be null.
	 * @since 0.06
	 */
	public PrintStream out()
	{
		return _out;
	}
	
	/**
	 * The verbosity of logged events.
	 * <p>
	 * This is the value that will be passed to {@link DimpleEvent#println(PrintStream, int)}
	 * when output an event.
	 * 
	 * @since 0.06
	 */
	public int verbosity()
	{
		return _verbosity;
	}
	
	/**
	 * Sets the value of {@link #verbosity()} to the specified value.
	 * @since 0.06
	 */
	public void verbosity(int verbosity)
	{
		_verbosity = verbosity;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private DimpleEventListener listenerForSource(IDimpleEventSource source)
	{
		final FactorGraph rootGraph = source.getContainingGraph().getRootGraph();

		DimpleEventListener listener = rootGraph.getEventListener();
		if (listener == null)
		{
			listener = DimpleEventListener.getDefault();
			rootGraph.setEventListener(listener);
		}

		_graphs.add(rootGraph);
		_listeners.add(listener);

		return listener;
	}
	
}
