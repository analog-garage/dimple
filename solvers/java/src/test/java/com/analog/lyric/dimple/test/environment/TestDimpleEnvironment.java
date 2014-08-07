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

package com.analog.lyric.dimple.test.environment;

import static org.junit.Assert.*;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.util.test.TestLogger;

/**
 * Unit tests for {@link DimpleEnvironment} class.
 * @since 0.07
 * @author Christopher Barber
 */
public class TestDimpleEnvironment
{
	@Test
	public void testInstances()
	{
		final DimpleEnvironment global1 = DimpleEnvironment.global();
		final DimpleEnvironment local1 = DimpleEnvironment.local();
		assertSame(global1, local1);
		
		Thread thread1 = new Thread() {
			@Override
			public void run()
			{
				assertSame(global1, DimpleEnvironment.local());
				DimpleEnvironment local2 = new DimpleEnvironment();
				DimpleEnvironment.setLocal(local2);
				assertSame(local2, DimpleEnvironment.local());
			}
		};
		thread1.start();
		try
		{
			thread1.join();
		}
		catch (InterruptedException ex)
		{
			fail(ex.toString());
		}
		
		assertSame(local1, DimpleEnvironment.local());
		
		final DimpleEnvironment global2 = new DimpleEnvironment();
		DimpleEnvironment.setGlobal(global2);
		assertSame(global2, DimpleEnvironment.global());
		assertSame(local1, DimpleEnvironment.local());
		
		Thread thread2 = new Thread() {
			@Override
			public void run()
			{
				assertSame(global2, DimpleEnvironment.local());
				DimpleEnvironment.setGlobal(global1);
				assertSame(global1, DimpleEnvironment.global());
				assertSame(global2, DimpleEnvironment.local());
			}
		};
		thread2.start();
		try
		{
			thread2.join();
		}
		catch (InterruptedException ex)
		{
			fail(ex.toString());
		}
	}
	
	@Test
	public void testEventSourceMethods()
	{
		DimpleEnvironment global = DimpleEnvironment.global();
		assertNull(global.getContainingGraph());
		assertNull(global.getEventParent());
		assertNull(global.getModelEventSource());
		global.notifyListenerChanged(); // does nothing

		assertEquals("DimpleEnvironment", global.getEventSourceName());
		
		assertNull(global.getEventListener());
	}
	
	@Test
	public void testLogging()
	{
		assertFalse(DimpleEnvironment.loadedFromMATLAB());
		
		DimpleEnvironment env = DimpleEnvironment.local();
		
		Logger defaultLogger = env.logger();
		assertSame(defaultLogger, Logger.getLogger("com.analog.lyric.dimple"));
		assertSame(defaultLogger, DimpleEnvironment.getDefaultLogger());
		
		TestLogger testLogger = new TestLogger("com.analog.lyric.dimple");
		env.setLogger(testLogger);
		assertSame(testLogger, env.logger());
		
		DimpleEnvironment.logWarning("'%s'","hi there");
		LogRecord record = testLogger.loggedRecords().remove();
		assertEquals(Level.WARNING, record.getLevel());
		assertEquals("'hi there'", record.getMessage());
		assertTrue(testLogger.loggedRecords().isEmpty());

		DimpleEnvironment.logError("'%s'","whoops");
		record = testLogger.loggedRecords().remove();
		assertEquals(Level.SEVERE, record.getLevel());
		assertEquals("'whoops'", record.getMessage());
		assertTrue(testLogger.loggedRecords().isEmpty());
	}
	
}
