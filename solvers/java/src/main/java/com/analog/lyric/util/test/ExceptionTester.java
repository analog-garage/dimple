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

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Pattern;

import com.analog.lyric.collect.Supers;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility functions for testing exception behavior.
 * 
 * @since 0.05
 */
public class ExceptionTester
{
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
	@SafeVarargs
	public static <T> void expectThrow(Class<? extends Throwable> expectedException, @Nullable String messagePattern,
		Object object, Class<?> declaringClass, String methodName, @Nullable T ... args)
	{
		boolean caughtSomething = false;
		
		try
		{
			Supers.invokeMethod(object, declaringClass, methodName, args);
		}
		catch (InvocationTargetException wrappedEx)
		{
			Throwable ex = wrappedEx.getTargetException();
			caughtSomething = true;
			
			if (!expectedException.isInstance(ex))
			{
				fail(String.format("Expected '%s' but caught '%s'", expectedException.getSimpleName(),
					ex.getClass().getSimpleName()));
			}
			
			if (messagePattern != null && !Pattern.matches(messagePattern, ex.getMessage()))
			{
				fail(String.format("Expected message matching '%s' but got '%s'", messagePattern, ex.getMessage()));
			}
		}
		catch (Exception ex)
		{
			fail(ex.toString());
		}
		
		if (!caughtSomething)
		{
			fail(String.format("Expected '%s' but no exception thrown", expectedException.getSimpleName()));
		}
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
