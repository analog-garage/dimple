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

package com.analog.lyric.dimple.factorfunctions.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.analog.lyric.collect.ConstructorRegistry;
import com.analog.lyric.util.misc.Internal;

/**
 * Registry of known {@link FactorFunction} implementations.
 * <p>
 * Supports lookup and instantiation of factor functions by class name. This is primarily for use
 * in supporting dynamic language API's (e.g. MATLAB).
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
public class FactorFunctionRegistry extends ConstructorRegistry<FactorFunction>
{
	/*--------------
	 * Construction
	 */
	
	@Internal
	public FactorFunctionRegistry()
	{
		super(FactorFunction.class, "com.analog.lyric.dimple.factorfunctions");
	}

	/*--------------------------------
	 * FactorFunctionRegistry methods
	 */
	
	/**
	 * Instantiate's factor function with given parameters.
	 * <p>
	 * The function must provide a constructor taking a single argument of type {@code Map<String,Object>}
	 * that provides parameters.
	 * <p>
	 * @since 0.07
	 */
	public FactorFunction instantiateWithParameters(String name, Map<String,Object> parameters)
	{
		for (Constructor<FactorFunction> constructor : super.getAll(name))
		{
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if (parameterTypes.length == 1 && parameterTypes[0].isInstance(parameters))
			{
				try
				{
					return constructor.newInstance(parameters);
				}
				catch (InvocationTargetException ex)
				{
					throw new RuntimeException(ex.getCause());
				}
				catch (ReflectiveOperationException ex)
				{
					// This really should not be possible, since we should already have verified that
					// the constructor should work.
					throw new RuntimeException(ex);
				}
			}
		}
		
		throw noMatchingClass(name);
	}
}
