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

import static org.junit.Assert.*;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import com.analog.lyric.collect.Supers;

/**
 * Utility functions for testing exception behavior.
 * 
 * @since 0.05
 */
public class ExceptionTester
{
	private static class ReflectionFailure extends Error
	{
		private static final long serialVersionUID = 1L;

		ReflectionFailure(String message)
		{
			super(message);
		}
	}
	
	/**
	 * Test case interface for use with {@link #expectThrow(Class, String, Case)}.
	 * 
	 * @since 0.08
	 */
	public interface Case
	{
		void run() throws Throwable;
	}
	
	/**
	 * Standard error stream that will be used for dumping stacks on failure.
	 * @since 0.08
	 */
	public static PrintStream stderr = System.err;
	
	/**
	 * Test for expected delivery of an exception.
	 * <p>
	 * If test case fails to throw expected exception with expected message, this will
	 * invoke {@link org.junit.Assert#fail}. Additionally, if the wrong exception type
	 * is thrown, this will print the stack on {@link #stderr}.
	 * <p>
	 * Examples:
	 * <blockquote><pre>
	 * import static com.analog.lyric.util.test.ExpectionTester.*;
	 * ...
	 * // Java 8 or later using lambda expression
	 * expectThrow(SomeException.class, "expected message", () -> doSomething());
	 *
	 * // Java 7
	 * expectThrow(SomeException.class, "expected message",
	 *    new ExceptionTester.Case() {
	 *       public void run() throws Throwable {
	 *          doSomething();
	 *       }
	 *    });
	 * 
	 * </pre></blockquote>
	 * <p>
	 * @param expectedEx is the expected exception type. The caught exception must be a subclass of this.
	 * @param msgPattern if not-null specifies a regular expression that must match the exception's
	 * {@linkplain Throwable#getMessage message}
	 * @param testCase has a {@code run} method that is expected to throw the exception.
	 * @since 0.08
	 */
	public static void expectThrow(Class<? extends Throwable> expectedEx, @Nullable String msgPattern, Case testCase)
	{
		boolean caughtSomething = false;
		try
		{
			testCase.run();
		}
		catch (ReflectionFailure ex)
		{
			fail(ex.getMessage());
		}
		catch (Throwable ex)
		{
			caughtSomething = true;
			
			if (!expectedEx.isInstance(ex))
			{
				ex.printStackTrace(stderr);
				fail(String.format("Expected '%s' but caught '%s'", expectedEx.getSimpleName(),
					ex.getClass().getSimpleName()));
			}
			
			if (msgPattern != null && !Pattern.matches(msgPattern, ex.getMessage()))
			{
				fail(String.format("Expected message matching '%s' but got '%s'", msgPattern, ex.getMessage()));
			}
		}
		
		if (!caughtSomething)
		{
			fail(String.format("Expected '%s' but no exception thrown", expectedEx.getSimpleName()));
		}
	}

	/**
	 * Test for expected delivery of an exception.
	 * <p>
	 * @see #expectThrow(Class, String, Case)
	 * @since 0.08
	 */
	public static void expectThrow(Class<? extends Throwable> expectedEx, Case testCase)
	{
		expectThrow(expectedEx, null, testCase);
	}

	/**
	 * Use reflective invocation to test for expected exception.
	 * 
	 * @param expectedException asserts that the invocation will result in an exception of this type (or a subtype).
	 * @param messagePattern if non-null asserts that the exception's message will match this specified regular
	 * expression pattern (see {@link Pattern}).
	 * @param methodName is the method to be invoked
	 * @param object is the object on which the method will be invoked. If this is a class, then a static call
	 * will be performed.
	 * @param args are the parameters of the method call.
	 */
	@SafeVarargs
	public static <T> void expectThrow(Class<? extends Throwable> expectedException, @Nullable String messagePattern,
		Object object, String methodName, @Nullable T ... args)
	{
		Class<?> declaringClass = object instanceof Class ? (Class<?>)object : object.getClass();
		expectThrow(expectedException, messagePattern, object, declaringClass, methodName, args);
		
	}
	
	/**
	 * @deprecated as of release 0.08 it should no longer be necessary to use this method to work around
	 * calling public methods on non-public subclasses.
	 * <p>
	 * Use reflective invocation to test for expected exception.
	 * 
	 * @param expectedException asserts that the invocation will result in an exception of this type (or a subtype).
	 * @param messagePattern if non-null asserts that the exception's message will match this specified regular
	 * expression pattern (see {@link Pattern}).
	 * @param object is the object on which the method will be invoked. If this is a class, then a static call
	 * will be performed.
	 * @param declaringClass is the class in which to look up the method. Use this when runtime type of {@code object}
	 * is private and you need to invoke public method from superclass or interface.
	 * @param methodName is the method to be invoked
	 * @param args are the parameters of the method call.
	 * @since 0.06
	 */
	@Deprecated
	@SafeVarargs
	public static <T> void expectThrow(Class<? extends Throwable> expectedException, @Nullable String messagePattern,
		final Object object, final Class<?> declaringClass, final String methodName, final @Nullable T ... args)
	{
		expectThrow(expectedException, messagePattern,
			new Case() {
				@Override
				public void run() throws Throwable
				{
					try
					{
						Supers.invokeMethod(object, declaringClass, methodName, args);
					}
					catch (InvocationTargetException wrappedEx)
					{
						throw wrappedEx.getTargetException();
					}
					catch (Exception ex)
					{
						throw new ReflectionFailure(ex.toString());
					}
				}
		});
	}
	
	/**
	 * Use reflective invocation to test for expected exception.
	 * 
	 * Same as {@link #expectThrow(Class, String, Object, String, Object...)} but with null
	 * {@code messagePattern}.
	 */
	@SafeVarargs
	public static <T> void expectThrow(Class<? extends Throwable> exceptionClass, Object object, String methodName,
		@Nullable T ... args)
	{
		expectThrow(exceptionClass, null, object, methodName, args);
	}
}
