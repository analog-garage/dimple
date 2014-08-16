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

package com.analog.lyric.util.test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.jcip.annotations.ThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link java.util.logging.Logger} implementation that simply saves all its log records
 * for test verification purposes.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@ThreadSafe
public class TestLogger extends Logger
{
	private ConcurrentLinkedQueue<LogRecord> _logRecords = new ConcurrentLinkedQueue<>();
	
	@NonNullByDefault(false)
	private class TestHandler extends java.util.logging.Handler
	{
		@Override
		public void close() throws SecurityException
		{
		}

		@Override
		public void flush()
		{
		}

		@Override
		public void publish(LogRecord record)
		{
			_logRecords.add(record);
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	/**
	 * Construct logger with
	 * <p>
	 * The constructor is configured to log all records by default.
	 * <p>
	 * @param name
	 * @since 0.07
	 */
	public TestLogger(String name)
	{
		super(name, null);
		setLevel(Level.ALL);
		addHandler(new TestHandler());
	}
	
	/*--------------------
	 * TestLogger methods
	 */

	/**
	 * View of records logged so far.
	 * <p>
	 * @since 0.07
	 */
	public Queue<LogRecord> loggedRecords()
	{
		return _logRecords;
	}
}
