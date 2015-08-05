/*******************************************************************************
*   Copyright 2015 Analog Devices, Inc.
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

package com.analog.lyric.util.test.tests;

import static com.analog.lyric.util.test.ExceptionTester.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.regex.Pattern;

import org.junit.Test;

import com.analog.lyric.util.test.ExceptionTester;

/**
 * Tests of {@link ExceptionTester} class
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
public class TestExceptionTester
{
	@Test
	public void test()
	{
		// Catch stack dump spew here
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ExceptionTester.stderr = new PrintStream(bos);
		
		// no exception
		boolean caught = false;
		try
		{
			expectThrow(Exception.class, new ExceptionTester.Case() {
				@Override
				public void run() throws Throwable
				{
				}
			});
		}
		catch (AssertionError err)
		{
			caught = true;
			assertEquals("Expected 'Exception' but no exception thrown", err.getMessage());
		}
		if (!caught)
		{
			fail("Expected AssertionError");
		}
		assertEquals(0, bos.size());
	
		// wrong exception
		caught = false;
		try
		{
			expectThrow(IllegalArgumentException.class, new ExceptionTester.Case() {
				@Override
				public void run() throws Throwable
				{
					throw new Error();
				}
			});
		}
		catch (AssertionError err)
		{
			caught = true;
			assertEquals("Expected 'IllegalArgumentException' but caught 'Error'", err.getMessage());
		}
		if (!caught)
		{
			fail("Expected AssertionError");
		}
		assertTrue(Pattern.matches("(?s)java\\.lang\\.Error\\s+at com\\.analog\\.lyric.*", bos.toString()));
		bos.reset();
		
		// wrong message
		caught = false;
		try
		{
			expectThrow(IllegalArgumentException.class, "barf", new ExceptionTester.Case() {
				@Override
				public void run() throws Throwable
				{
					throw new IllegalArgumentException("gag");
				}
			});
		}
		catch (AssertionError err)
		{
			caught = true;
			assertEquals("Expected message matching 'barf' but got 'gag'", err.getMessage());
		}
		if (!caught)
		{
			fail("Expected AssertionError");
		}
		assertEquals(0, bos.size());
		
		// ok
		expectThrow(IllegalArgumentException.class, new ExceptionTester.Case() {
			@Override
			public void run() throws Throwable
			{
				throw new IllegalArgumentException("barf");
			}
		});
		expectThrow(IllegalArgumentException.class, "barf", new ExceptionTester.Case() {
			@Override
			public void run() throws Throwable
			{
				throw new IllegalArgumentException("barf");
			}
		});
		assertEquals(0, bos.size());
		
		//
		// Reflection cases
		//
		
		expectThrow(IllegalArgumentException.class, getClass(), "throwSomething", IllegalArgumentException.class, "gag");
		expectThrow(RuntimeException.class, this, "throwSomething2", IllegalArgumentException.class, "gag");
		
		bos.reset();
		caught = false;
		try
		{
			expectThrow(IllegalArgumentException.class, getClass(), "throwSomething", Error.class, "gag");
		}
		catch (AssertionError err)
		{
			caught = true;
			assertEquals("Expected 'IllegalArgumentException' but caught 'Error'", err.getMessage());
		}
		if (!caught)
		{
			fail("Expected AssertionError");
		}
		assertTrue(Pattern.matches("(?s)java\\.lang\\.Error: gag\\s+at.*", bos.toString()));
		bos.reset();
		
		caught = false;
		try
		{
			expectThrow(IllegalArgumentException.class, this, "noSuchMethod");
		}
		catch (AssertionError err)
		{
			caught = true;
		}
		if (!caught)
		{
			fail("Expected AssertionError");
		}
	}
	
	/**
	 * Test function that throws an exception of specified class and message
	 * @since 0.08
	 */
	public static void throwSomething(Class<? extends Throwable> exType, String msgFormat, Object ... msgArgs) throws Throwable
	{
		Constructor<? extends Throwable> constructor = exType.getConstructor(String.class);
		Throwable ex = constructor.newInstance(String.format(msgFormat,  msgArgs));
		throw ex;
	}
	
	public void throwSomething2(Class<? extends Throwable> exType, String msgFormat, Object ... msgArgs) throws Throwable
	{
		Constructor<? extends Throwable> constructor = exType.getConstructor(String.class);
		Throwable ex = constructor.newInstance(String.format(msgFormat,  msgArgs));
		throw ex;
	}
}
