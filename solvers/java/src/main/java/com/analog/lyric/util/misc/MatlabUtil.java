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

package com.analog.lyric.util.misc;

import org.eclipse.jdt.annotation.Nullable;

/**
 * 
 * @since 0.08
 * @author Christopher Barber
 */
@Matlab
public abstract class MatlabUtil
{
	private MatlabUtil() {}

	/**
	 * Returns name of Matlab wrapper function/class for given object, if any.
	 * <p>
	 * This looks through the superclass chain starting with {@code obj}'s class and
	 * returns the first non-null {@link Matlab#wrapper() wrapper} attribute from {@link Matlab}
	 * annotations on those classes. Returns null if no {@code Matlab} annotation is found or if the wrapper
	 * attribute has not been set.
	 * <p>
	 * This is used by the MATLAB {@code wrapProxyObject} function.
	 * <p>
	 * @since 0.08
	 */
	public static @Nullable String wrapper(Object obj)
	{
		Class<?> cl = obj.getClass();
		
		while (cl != null)
		{
			Matlab matlabAnnotation = cl.getAnnotation(Matlab.class);
			
			if (matlabAnnotation != null)
			{
				String wrapper = matlabAnnotation.wrapper();
				if (!wrapper.isEmpty())
					return wrapper;
			}
			
			cl = cl.getSuperclass();
		}
		
		return null;
	}
}