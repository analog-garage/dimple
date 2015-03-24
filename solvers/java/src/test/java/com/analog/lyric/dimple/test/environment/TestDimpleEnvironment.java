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

import java.security.SecureRandom;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.environment.ExtendedLevel;
import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.EventPrinter;
import com.analog.lyric.dimple.events.IDimpleEventHandler;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.events.IModelEventSource;
import com.analog.lyric.dimple.factorfunctions.Normal;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.core.Ids;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.options.DimpleOptionHolder;
import com.analog.lyric.dimple.solvers.core.proposalKernels.NormalProposalKernel;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.samplers.generic.SliceSampler;
import com.analog.lyric.dimple.test.DimpleTestBase;
import com.analog.lyric.util.test.TestLogger;

/**
 * Unit tests for {@link DimpleEnvironment} class.
 * @since 0.07
 * @author Christopher Barber
 */
public class TestDimpleEnvironment extends DimpleTestBase
{
	@SuppressWarnings("unused")
	@Test
	public void testInstances()
	{
		assertNull(DimpleEnvironment.withId(new SecureRandom().nextLong()));
		
		final DimpleEnvironment global1 = DimpleEnvironment.defaultEnvironment();
		final DimpleEnvironment local1 = DimpleEnvironment.active();
		assertSame(global1, local1);
		assertSame(global1, DimpleEnvironment.withId(global1.getEnvId()));
		
		UUID uid = global1.getUUID();
		assertEquals(2, uid.variant());
		assertEquals(Ids.DIMPLE_UUID_VERSION, uid.version());
		assertEquals(0, uid.getLeastSignificantBits() & (1L<<62) - 1);
		
		Thread thread1 = new Thread() {
			@Override
			public void run()
			{
				assertSame(global1, DimpleEnvironment.active());
				DimpleEnvironment local2 = new DimpleEnvironment();
				assertNotEquals(local2.getEnvId(), global1.getEnvId());
				assertSame(local2, DimpleEnvironment.withId(local2.getEnvId()));
				DimpleEnvironment.setActive(local2);
				assertSame(local2, DimpleEnvironment.active());
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
		
		assertSame(local1, DimpleEnvironment.active());
		
		final DimpleEnvironment global2 = new DimpleEnvironment();
		DimpleEnvironment.setDefaultEnvironment(global2);
		assertSame(global2, DimpleEnvironment.defaultEnvironment());
		assertSame(local1, DimpleEnvironment.active());
		assertNotEquals(uid, global2.getUUID());
		
		Thread thread2 = new Thread() {
			@Override
			public void run()
			{
				assertSame(global2, DimpleEnvironment.active());
				DimpleEnvironment.setDefaultEnvironment(global1);
				assertSame(global1, DimpleEnvironment.defaultEnvironment());
				assertSame(global2, DimpleEnvironment.active());
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
		
		try
		{
			new DimpleEnvironment(Ids.ENV_ID_MAX + 1);
		}
		catch (IllegalArgumentException ex)
		{
		}
		catch (Throwable ex)
		{
			fail("expected IllegalArgumentException");
		}
		
		try
		{
			new DimpleEnvironment(Ids.ENV_ID_MIN - 1);
		}
		catch (IllegalArgumentException ex)
		{
		}
		catch (Throwable ex)
		{
			fail("expected IllegalArgumentException");
		}
		
		try
		{
			new DimpleEnvironment(DimpleEnvironment.active().getEnvId());
		}
		catch (IllegalStateException ex)
		{
		}
		catch (Throwable ex)
		{
			fail("expected IllegalStateException");
		}

	}
	
	@Test
	public void testEventSourceMethods()
	{
		DimpleEnvironment global = DimpleEnvironment.defaultEnvironment();
		assertNull(global.getContainingGraph());
		assertNull(global.getEventParent());
		assertNull(global.getModelEventSource());
		global.notifyListenerChanged(); // does nothing

		assertEquals("DimpleEnvironment", global.getEventSourceName());
		
		class TestSource extends DimpleOptionHolder
		{
			int notifyCount = 0;
			
			@Override
			public @Nullable FactorGraph getContainingGraph()
			{
				return null;
			}

			@Override
			public @Nullable IDimpleEventSource getEventParent()
			{
				return null;
			}

			@Override
			public String getEventSourceName()
			{
				return "test";
			}

			@Override
			public @Nullable IModelEventSource getModelEventSource()
			{
				return null;
			}

			@Override
			public void notifyListenerChanged()
			{
				++notifyCount;
			}
		}
		
		TestSource source1 = new TestSource();
		TestSource source2 = new TestSource();
		IDimpleEventHandler<DimpleEvent> handler = new EventPrinter();
		
		assertNull(global.getEventListener());
		DimpleEventListener listener = global.createEventListener();
		assertNotNull(listener);
		assertSame(listener, global.createEventListener());
		assertSame(listener, global.getEventListener());
		
		
		listener.register(handler, DimpleEvent.class, source1);
		
		global.setEventListener(null);
		global.setEventListener(null);
		assertNull(global.getEventListener());
		assertEquals(1, source1.notifyCount);
		
		global.setEventListener(listener);
		global.setEventListener(listener);
		assertSame(listener, global.getEventListener());
		assertEquals(2, source1.notifyCount);
		
		DimpleEventListener listener2 = new DimpleEventListener();
		listener2.register(handler, DimpleEvent.class, source2);
		global.setEventListener(listener2);
		global.setEventListener(listener2);
		assertSame(listener2, global.getEventListener());
		assertEquals(3, source1.notifyCount);
		assertEquals(1, source2.notifyCount);
	}
	
	@Test
	public void testLogging()
	{
		assertFalse(DimpleEnvironment.loadedFromMATLAB());
		
		DimpleEnvironment env = DimpleEnvironment.active();
		
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
		assertEquals(ExtendedLevel.ERROR, record.getLevel());
		assertEquals("'whoops'", record.getMessage());
		assertTrue(testLogger.loggedRecords().isEmpty());
	}
	
	@Test
	public void testRegistries()
	{
		DimpleEnvironment env = DimpleEnvironment.active();
		
		assertEquals(Normal.class, env.factorFunctions().getClass("Normal"));
		assertSame(env.factorFunctions(), env.factorFunctions());
		
		assertNull(env.factorGraphs().getGraphWithId(0));
		assertSame(env.factorGraphs(), env.factorGraphs());
		
		assertEquals(SliceSampler.class, env.genericSamplers().getClass("SliceSampler"));
		assertSame(env.genericSamplers(), env.genericSamplers());
		
		assertEquals(BPOptions.iterations, env.optionRegistry().get("BPOptions.iterations"));
		assertSame(env.optionRegistry(), env.optionRegistry());
		
		assertEquals(NormalProposalKernel.class, env.proposalKernels().getClass("NormalProposalKernel"));
		assertSame(env.proposalKernels(), env.proposalKernels());
		
		assertEquals(GibbsSolver.class, env.solvers().getClass("Gibbs"));
		assertSame(env.solvers(), env.solvers());
	}
}
