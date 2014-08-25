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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;

import com.analog.lyric.dimple.environment.DimpleEnvironment;
import com.analog.lyric.dimple.environment.DimpleThread;
import com.analog.lyric.dimple.environment.DimpleThreadFactory;
import com.analog.lyric.dimple.test.DimpleTestBase;

/**
 * 
 * @since 0.07
 * @author Christopher Barber
 */
public class TestDimpleThread extends DimpleTestBase
{
	@Test
	public void test()
	{
		final DimpleEnvironment activeEnv = DimpleEnvironment.active();
		final DimpleEnvironment altEnv = new DimpleEnvironment();
		final ThreadGroup defaultGroup = Thread.currentThread().getThreadGroup();
		final ThreadGroup altGroup = new ThreadGroup("alt");
		
		testCase(new DimpleThread() {
			@Override public void run() {
				super.run();
				assertSame(activeEnv, DimpleEnvironment.active());
			}
		}, activeEnv, defaultGroup, null);
		
		testCase(new DimpleThread("bob") {
			@Override public void run() {
				super.run();
				assertSame(activeEnv, DimpleEnvironment.active());
			}
		}, activeEnv, defaultGroup, "bob");

		testCase(new DimpleThread(altGroup, "bob") {
			@Override public void run() {
				super.run();
				assertSame(activeEnv, DimpleEnvironment.active());
			}
		}, activeEnv, altGroup, "bob");
		
		Runnable runnable = new Runnable () {
			@Override
			public void run()
			{
				DimpleThread thread = (DimpleThread)Thread.currentThread();
				assertSame(thread.environment(), DimpleEnvironment.active());
			}
		};
		
		testCase(new DimpleThread(runnable), activeEnv, defaultGroup, null);
		testCase(new DimpleThread(altEnv, runnable), altEnv, defaultGroup, null);
		testCase(new DimpleThread(altGroup, runnable), activeEnv, altGroup, null);
		testCase(new DimpleThread(altEnv, altGroup, runnable), altEnv, altGroup, null);
		testCase(new DimpleThread(runnable, "mary"), activeEnv, defaultGroup, "mary");
		testCase(new DimpleThread(altEnv, runnable, "sam"), altEnv, defaultGroup, "sam");
		testCase(new DimpleThread(altGroup, runnable, "foo"), activeEnv, altGroup, "foo");
		testCase(new DimpleThread(altEnv, altGroup, runnable, "boo"), altEnv, altGroup, "boo");
		testCase(new DimpleThread(altGroup, runnable, "blort", 10000), activeEnv, altGroup, "blort");
		testCase(new DimpleThread(altEnv, altGroup, runnable, "xyzzy", 10000), altEnv, altGroup, "xyzzy");
	}
	
	private void testCase(DimpleThread thread,
		DimpleEnvironment expectedEnvironment,
		ThreadGroup expectedGroup,
		@Nullable String expectedName)
	{
		DimpleEnvironment prevEnv = DimpleEnvironment.defaultEnvironment();
		
		try
		{
			DimpleEnvironment.setDefaultEnvironment(new DimpleEnvironment());
			assertNotSame(prevEnv, DimpleEnvironment.defaultEnvironment());
		
			assertSame(expectedEnvironment, thread.environment());
			assertSame(expectedGroup, thread.getThreadGroup());
			if (expectedName != null)
			{
				assertEquals(expectedName, thread.getName());
			}
			
			final Throwable[] exception = new Throwable[1];
			thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
				@NonNullByDefault(false)
				@Override
				public void uncaughtException(Thread th, Throwable throwable)
				{
					exception[0] = throwable;
				}
			});
			thread.start();
			try
			{
				thread.join(1000);
			}
			catch (InterruptedException ex)
			{
				fail(ex.toString());
			}
			if (exception[0] != null)
			{
				fail(exception[0].toString());
			}
		}
		finally
		{
			DimpleEnvironment.setDefaultEnvironment(prevEnv);
		}
	}
	
	@Test
	public void testFactory()
	{
		final DimpleEnvironment expectedEnv = new DimpleEnvironment();
		DimpleEnvironment.setActive(expectedEnv);
		
		Runnable runnable = new Runnable() {
			@Override
			public void run()
			{
				assertSame(expectedEnv, DimpleEnvironment.active());
			}
		};
		
		DimpleThreadFactory factory = new DimpleThreadFactory();
		assertSame(expectedEnv, factory.environment());
		DimpleThread thread = factory.newThread(runnable);
		assertSame(expectedEnv, thread.environment());
		thread.run();
		
		DimpleEnvironment.setActive(DimpleEnvironment.defaultEnvironment());
		factory = new DimpleThreadFactory(expectedEnv);
		assertSame(expectedEnv, factory.environment());
		thread = factory.newThread(runnable);
		assertSame(expectedEnv, thread.environment());
		thread.run();
	}
}
