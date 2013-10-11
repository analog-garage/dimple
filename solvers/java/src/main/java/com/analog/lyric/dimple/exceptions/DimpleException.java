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
 * Base class for all exceptions thrown by Dimple library.
 * <p>
 * Note that since it is derived from {@link RuntimeException} these exceptions
 * are not checked and do not have to be listed in throws clauses for methods.
 */
public class DimpleException extends RuntimeException implements IDimpleException
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
	
	public static DimpleException unsupported(String operation)
	{
		return new DimpleException("Operation '%s' not supported", operation);
	}
	
	public static DimpleException unsupportedMethod(Class<?> cl, String name)
	{
		return new DimpleException("Method '%s' not supported on '%s'", cl, name);
	}
	
	/**
	 * Creates an exception stating that specified operation is not supported by a particular solver.
	 */
	public static DimpleException unsupportedBySolver(String solver, String operation)
	{
		return new DimpleException("Operation '%s' not supported by '%s' solver", operation, solver);
	}
	
	@Override
	public Exception getException()
	{
		return this;
	}
	
	
}
