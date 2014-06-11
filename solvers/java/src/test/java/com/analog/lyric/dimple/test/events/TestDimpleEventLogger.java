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

package com.analog.lyric.dimple.test.events;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.DimpleEventLogger;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.model.core.FactorAddEvent;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.util.misc.Nullable;
import com.google.common.io.Files;

/**
 * Unit test for {@link DimpleEventLogger}
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestDimpleEventLogger
{
	@Test
	public void test() throws IOException
	{
		DimpleEventLogger logger = new DimpleEventLogger();
		assertTrue(logger.isClear());
		assertSame(System.err, logger.out());
		assertEquals(0, logger.verbosity());
		assertNull(logger.file());
		assertTrue(logger.isOpen());
		
		logger.close();
		assertFalse(logger.isOpen());
		assertNull(logger.out());
		
		logger.clear();
		
		logger.open(System.out);
		assertTrue(logger.isOpen());
		assertSame(System.out, logger.out());
		
		File tmpFile = null;
		try
		{
			tmpFile = File.createTempFile("TestDimpleEventLogger", ".txt");
			tmpFile.deleteOnExit();
			logger.open(tmpFile);
			assertTrue(logger.isOpen());
			assertSame(tmpFile, logger.file());
			logger.out().println("test");
			assertFile(tmpFile, "test");
			
			logger.close();
			assertFalse(logger.isOpen());
			logger.open(tmpFile);
			logger.out().println("foo");
			assertFile(tmpFile, "test", "foo");
			
			logger.open(tmpFile, false);
			logger.out().println("barf");
			assertFile(tmpFile, "barf");
		}
		finally
		{
			logger.close();
			if (tmpFile != null)
			{
				assertTrue(tmpFile.delete());
			}
		}
		
		// Force line separator character used by PrintStream to be \n to avoid problems on Windows.
		System.setProperty("line.separator",  "\n");
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bos);
		logger.open(out);
		assertTrue(logger.isOpen());
		assertSame(out, logger.out());
		assertNull(logger.file());
		assertTrue(logger.logEntries().isEmpty());
		
		FactorGraph fg = new FactorGraph();
		assertNull(fg.getEventListener());
		Discrete d = new Discrete(DiscreteDomain.bit());
		fg.addVariables(d);
		
		assertTrue(logger.isClear());
		logger.log(TestEvent.class); // no effect
		assertTrue(logger.isClear());
		logger.log(TestEvent.class, fg);
		assertFalse(logger.isClear());
		DimpleEventListener listener = fg.getEventListener();
		assertTrue(listener.isDefault());
		listener.raiseEvent(new TestEvent(fg));

		List<DimpleEventLogger.LogEntry> entries = logger.logEntries();
		assertEquals(1, entries.size());
		assertEquals(fg, entries.get(0).eventSource());
		assertEquals(TestEvent.class, entries.get(0).eventClass());
		
		assertEquals("verbosity=0\n", bos.toString());
		
		bos.reset();
		logger.verbosity(1);
		assertEquals(1, logger.verbosity());
		listener.raiseEvent(new TestEvent(fg));
		assertEquals("TestEvent: verbosity=1\n", bos.toString());
		bos.reset();
		
		logger.close();
		assertTrue(fg.getEventListener().isListeningFor(TestEvent.class, fg));
		listener.raiseEvent(new TestEvent(fg));
		assertEquals("", bos.toString());
		bos.reset();
		
		logger.clear();
		assertTrue(logger.logEntries().isEmpty());
		assertTrue(logger.isClear());
		assertNull(fg.getEventListener());
		assertFalse(listener.isListeningFor(TestEvent.class, fg));
		
		// Make sure that logger will not replace an existing listener with the default listener
		listener = new DimpleEventListener();
		listener.block(FactorAddEvent.class, true, fg);
		fg.setEventListener(listener);
		logger.log(TestEvent.class, fg);
		assertTrue(listener.isListeningFor(TestEvent.class, fg));
		assertSame(listener, fg.getEventListener());
		assertFalse(listener.isDefault());
		entries = logger.logEntries();
		assertEquals(1, entries.size());
		assertSame(TestEvent.class, entries.get(0).eventClass());
		
		logger.open(out = new PrintStream(bos));
		listener.raiseEvent(new TestEvent(fg));
		assertEquals("TestEvent: verbosity=1\n", bos.toString());
		bos.reset();
		
		assertEquals(0, logger.unlog(TestEvent.class, d));
		assertEquals(0, logger.unlog(DimpleEvent.class, fg));
		assertEquals(1, logger.unlog(TestEvent.class, fg));
		assertEquals(0, logger.logEntries().size());
		logger.log(TestEvent.class, d);
		logger.log(TestEvent.class, fg);
		assertEquals(2, logger.logEntries().size());
		logger.clear();
		assertEquals(0, logger.logEntries().size());
		assertSame(listener, fg.getEventListener());

		logger.log(TestEvent.class, d);
		fg.setEventListener(null);
		// No entry removed because listener has changed.
		assertEquals(0, logger.unlog(TestEvent.class,  d));
		assertEquals(1, logger.logEntries().size());
		assertTrue(listener.isListeningFor(TestEvent.class, d));
		logger.clear();
		assertFalse(listener.isListeningFor(TestEvent.class, d));
		
		FactorGraph fg2 = new FactorGraph();
		fg2.setEventListener(listener);
		
		logger.log(TestEvent.class, fg);
		fg.getEventListener().block(FactorAddEvent.class, false, fg);
		logger.log(TestEvent.class, fg2);
		logger.clear();
		assertTrue(fg.getEventListener().isDefault());
		assertFalse(fg2.getEventListener().isDefault());
		assertSame(listener, fg2.getEventListener());
	}
	
	private static class TestEvent extends DimpleEvent
	{
		private static final long serialVersionUID = 1L;

		TestEvent(IDimpleEventSource source)
		{
			super(source);
		}

		@Override
		protected void printDetails(PrintStream out, int verbosity)
		{
			out.format("verbosity=%d", verbosity);
		}

		@Override
		public @Nullable IModelEventSource getModelObject()
		{
			return getSource().getModelEventSource();
		}
	}
	
	private void assertFile(File file, String ... expectedLines) throws IOException
	{
		List<String> actualLines = Files.readLines(file, Charset.defaultCharset());
		assertArrayEquals(expectedLines, actualLines.toArray());
	}
}
