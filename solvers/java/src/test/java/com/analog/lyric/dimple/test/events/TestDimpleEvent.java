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

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.IDimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.util.test.SerializationTester;

/**
 * Test for {@link DimpleEvent}
 * @author Christopher Barber
 */
public class TestDimpleEvent
{
	@Test
	public void test()
	{
		expectThrow(IllegalArgumentException.class, getClass(), "newEvent", (IDimpleEventSource)null);
		
		FactorGraph fg = new FactorGraph();
		fg.setName("bob");
		
		TestEvent event = new TestEvent(fg);
		
		assertSame(fg, event.getSource());
		assertSame(fg, event.getModelObject());
		assertEquals(fg.getEventSourceName(), event.getSourceName());
		assertEquals(fg.getId(), event.getModelId());
		
		assertFalse(event.consumed());
		event.consumed(true);
		assertTrue(event.consumed());
		event.consumed(false);
		assertFalse(event.consumed());
		
		TestEvent event2 = SerializationTester.clone(event);
		assertNotSame(event, event2);
		assertEquals(event.getSourceName(), event2.getSourceName());
		assertEquals(event.getModelId(), event2.getModelId());
		assertNull(event2.getSource());
		assertNull(event2.getModelObject());
		
		// Test print
		System.setProperty("line.separator", "\n"); // force unix line endings
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bos);
		event.print(out, 0);
		assertEquals("verbosity=0", bos.toString());
		bos.reset();
		event.print(out, 1);
		assertEquals("TestEvent: verbosity=1", bos.toString());
		bos.reset();
		event.println(out, 1);
		assertEquals("TestEvent: verbosity=1\n", bos.toString());
		assertEquals("TestEvent: verbosity=2", event.toString(2));
		assertEquals("TestEvent: verbosity=1", event.toString());
		assertEquals("", event.toString(-1));
		bos.reset();
		event.print(out, -2);
		assertEquals("", bos.toString());
		
	
		IDimpleEventSource source = new TestSource("bob");
		event = newEvent(source);
		assertEquals("bob", event.getSourceName());
		assertNull(event.getModelObject());
		assertEquals(-1, event.getModelId());
	}
	
	public static TestEvent newEvent(IDimpleEventSource source)
	{
		return new TestEvent(source);
	}
	
	private static class TestEvent extends DimpleEvent
	{
		private static final long serialVersionUID = 1L;

		private TestEvent(IDimpleEventSource source)
		{
			super(source);
		}

		@Override
		protected void printDetails(PrintStream out, int verbosity)
		{
			out.format("verbosity=%d", verbosity);
		}

		@Override
		public IModelEventSource getModelObject()
		{
			IDimpleEventSource source = getSource();
			return source != null ? source.getModelEventSource() : null;
		}
	}
	
	private static class TestSource implements IDimpleEventSource
	{
		private final String _name;
		
		private TestSource(String name)
		{
			_name = name;
		}
		
		@Override
		public FactorGraph getContainingGraph()
		{
			return null;
		}

		@Override
		public IDimpleEventListener getEventListener()
		{
			return null;
		}

		@Override
		public IDimpleEventSource getEventParent()
		{
			return null;
		}

		@Override
		public String getEventSourceName()
		{
			return _name;
		}

		@Override
		public IModelEventSource getModelEventSource()
		{
			return null;
		}

		@Override
		public void notifyListenerChanged()
		{
		}
	}
}
