/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

package com.analog.lyric.dimple.exceptions;

/**
 * Base class for new exception types thrown by Dimple library.
 * <p>
 * Note that since it is derived from {@link RuntimeException} these exceptions
 * are not checked and do not have to be listed in throws clauses for methods.
 */
public class DimpleException extends RuntimeException
{

	private static final long serialVersionUID = 1L;

	/*--------------
	 * Construction
	 */
	
	/**
	 * Constructs exception with given {@code message}.
	 */
	public DimpleException(String message)
	{
		super(message);
	}
	
	/**
	 * Constructs exception with message formatted by {@link String#format}.
	 */
	public DimpleException(String format, Object...args)
	{
		super(String.format(format, args));
	}

	/**
	 * Constructs exception wrapping given exception.
	 */
	public DimpleException(Exception e)
	{
		super(e);
	}
	
	public static DimpleException unimplementedMethod(Class<?> cl, String name)
	{
		return new DimpleException(String.format("Method '%s.%s' is not yet implemented", cl.getSimpleName(), name));
	}
	
	public static DimpleException unsupported(String operation)
	{
		return new DimpleException(String.format("Operation '%s' not supported", operation));
	}
	
	/**
	 * Returns an exception indicating a method is not supported
	 * 
	 * @param cl is the class from which the exception was thrown. Usually it is sufficient to
	 * use {@link Object#getClass()}.
	 * @param name is the name of the method.
	 * 
	 * @see #unsupportedMethod(Class, String, String)
	 */
	public static DimpleException unsupportedMethod(Class<?> cl, String name)
	{
		return new DimpleException(String.format("Method '%s.%s' is not supported.", cl.getSimpleName(), name));
	}
	
	/**
	 * Returns an exception indicating a method is not supported
	 * 
	 * @param cl is the class from which the exception was thrown. Usually it is sufficient to
	 * use {@link Object#getClass()}.
	 * @param name is the name of the method.
	 * @param reason states the reason the method is unsupported in sentence form
	 * 
	 * @see #unsupportedMethod(Class, String)
	 */
	public static DimpleException unsupportedMethod(Class<?> cl, String name, String reason)
	{
		return new DimpleException(String.format("Method '%s.%s' is not supported: %s", cl.getSimpleName(), name, reason));
	}

	/**
	 * Creates an exception stating that specified operation is not supported by a particular solver.
	 */
	public static DimpleException unsupportedBySolver(String solver, String operation)
	{
		return new DimpleException("Operation '%s' not supported by '%s' solver", operation, solver);
	}
}
