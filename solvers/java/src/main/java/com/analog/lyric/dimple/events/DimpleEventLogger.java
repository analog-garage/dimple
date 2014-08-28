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

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.environment.IDimpleEnvironmentHolder;

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
public class DimpleEventLogger implements Closeable, IDimpleEnvironmentHolder
{
	/*-------
	 * State
	 */
	
	private final DimpleEnvironment _env;
	
	/**
	 * Events will be printed on this stream, if non-null.
	 */
	@GuardedBy("this")
	private volatile @Nullable PrintStream _out;
	
	/**
	 * The file used for {@link #_out}, if opened by {@link #open(File)}.
	 */
	@GuardedBy("this")
	private volatile @Nullable File _file = null;
	
	private volatile int _verbosity;
	
	private final DimpleEventHandler<DimpleEvent> _handler = new EventPrinter();

	@GuardedBy("this")
	private final Set<LogEntry> _logEntries = new HashSet<LogEntry>();
	
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
	
	/**
	 * A simple pairing of event source and event type.
	 * <p>
	 * @see DimpleEventLogger#logEntries()
	 * @since 0.06
	 */
	public static class LogEntry
	{
		private final Class<? extends DimpleEvent> _eventClass;
		private final IDimpleEventSource _eventSource;
		
		private LogEntry(Class<? extends DimpleEvent> eventType, IDimpleEventSource eventSource)
		{
			_eventClass = eventType;
			_eventSource = eventSource;
		}
		
		/*----------------
		 * Object methods
		 */
		
		@Override
		public boolean equals(@Nullable Object other)
		{
			if (this == other)
			{
				return true;
			}
			
			if (other instanceof LogEntry)
			{
				LogEntry that = (LogEntry)other;
				return this._eventClass == that._eventClass && this._eventSource == that._eventSource;
			}
			
			return false;
		}
		
		@Override
		public int hashCode()
		{
			return _eventClass.hashCode() * 11 + _eventSource.hashCode();
		}
		
		/*------------------
		 * LogEntry methods
		 */
		
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
	 * <p>
	 * The {@linkplain #getEnvironment environment} will be set to the
	 * {@linkplain DimpleEnvironment#active active environment}.
	 * <p>
	 * @since 0.06
	 */
	public DimpleEventLogger()
	{
		_env = DimpleEnvironment.active();
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
		final PrintStream out = _out;
		if (out != null)
		{
			if (out != System.out && out != System.err)
			{
				out.close();
			}
			_out = null;
		}
	}
	
	/*----------------------------------
	 * IDimpleEnvironmentHolder methods
	 */
	
	/**
	 * {@inheritDoc}
	 * @see #DimpleEventLogger()
	 * @since 0.07
	 */
	@Override
	public DimpleEnvironment getEnvironment()
	{
		return _env;
	}
	
	/*---------
	 * Methods
	 */

	/**
	 * File last used by {@link #open(File)} or null if last opened by {@link #open(PrintStream)}.
	 * 
	 * @since 0.06
	 */
	public @Nullable File file()
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
	public synchronized boolean isClear()
	{
		return _logEntries.isEmpty();
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
	 * Enables logging by registering an event handler with the event listener of the
	 * {@linkplain #getEnvironment() environment} creating a new one if necessary. It
	 * is assumed that all of the listed {@code sources} have the same environment.
	 * <p>
	 * @param eventType is the superclass of the type of events that will be logged. If {@code eventType}
	 * is abstract then all subtypes will be logged, otherwise only that specific type will be logged.
	 * @param sources lists the objects that should log events. This will affect both those objects and their
	 * children unless blocked.
	 * @since 0.06
	 */
	public synchronized void log(Class<? extends DimpleEvent> eventType, IDimpleEventSource ... sources)
	{
		final DimpleEventListener listener = _env.createEventListener();
		for (IDimpleEventSource source : sources)
		{
			listener.register(_handler, eventType, source);
			_logEntries.add(new LogEntry(eventType, source));
		}
	}
	
	/**
	 * Returns newly created list of current log entries for this object in no particular order.
	 * 
	 * @since 0.06
	 */
	public synchronized List<LogEntry> logEntries()
	{
		return new ArrayList<LogEntry>(_logEntries);
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
		
		DimpleEventListener listener = _env.getEventListener();
		
		if (listener != null)
		{
			for (IDimpleEventSource source : sources)
			{
				_logEntries.remove(new LogEntry(eventType, source));
				if (listener.unregister(_handler, eventType, source))
				{
					++nRemoved;
				}
			}
			
			if (listener.isEmpty())
			{
				// Remove the listener from the environment if it contains no more entries.
				_env.setEventListener(null);
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
		DimpleEventListener listener = _env.getEventListener();
		
		if (listener != null)
		{
			for (LogEntry entry : _logEntries)
			{
				listener.unregister(_handler, entry.eventClass(), entry.eventSource());
			}
			
			if (listener.isEmpty())
			{
				_env.setEventListener(null);
			}
		}
		
		_logEntries.clear();
	}
	
	/**
	 * The current stream used for logging. May be null.
	 * @since 0.06
	 */
	public @Nullable PrintStream out()
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
	
}
