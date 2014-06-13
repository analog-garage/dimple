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

package com.analog.lyric.collect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utility methods for cloning.
 * 
 * @since 0.05
 * @author Christopher Barber
 */
public abstract class Clones
{
	/**
	 * Private constructor prevents subclassing (abstract final not allowed).
	 */
	private Clones() {}

	/**
	 * Clones {@code object} by looking up its {@code clone()} method using reflection.
	 * This implementation does not check to see if object implements {@link Cloneable} interface.
	 */
	public static <T> T cloneReflectively(T object)
		throws CloneNotSupportedException, IllegalAccessException, InvocationTargetException
	{
		Class<?> objectClass = object.getClass();
		Method cloneMethod = null;
		try
		{
			cloneMethod = objectClass.getMethod("clone");
		}
		catch (NoSuchMethodException ex)
		{
			throw new CloneNotSupportedException(objectClass.getName());
		}
		
		if ((cloneMethod.getModifiers() & Modifier.PUBLIC) == 0)
		{
			throw new CloneNotSupportedException(objectClass.getName());
		}
		
		@SuppressWarnings("unchecked")
		T clone = (T) cloneMethod.invoke(object);
		return clone;
	}
}
