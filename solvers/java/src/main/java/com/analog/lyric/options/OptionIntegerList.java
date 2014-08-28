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

package com.analog.lyric.options;

import java.lang.reflect.Array;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.primitives.Ints;



/**
 * Represents list of integers for use as option value.
 * @since 0.07
 * @author Christopher Barber
 */
public class OptionIntegerList extends AbstractOptionValueList<Integer>
{
	private static final long serialVersionUID = 1L;

	final public static OptionIntegerList EMPTY = new OptionIntegerList();
	
	/**
	 * @param elements
	 * @since 0.07
	 */
	public OptionIntegerList(Integer[] elements)
	{
		super(Integer.class, elements);
	}

	public OptionIntegerList(int ... elements)
	{
		super(Ints.asList(elements).toArray(new Integer[elements.length]));
	}
	
	public static OptionIntegerList fromObject(@Nullable Object object)
	{
		if (object == null)
		{
			return EMPTY;
		}
		
		Class<?> objectClass = object.getClass();
		
		if (object instanceof OptionIntegerList)
		{
			return (OptionIntegerList)object;
		}
		if (objectClass == Integer[].class)
		{
			return new OptionIntegerList((Integer[])object);
		}
		if (objectClass == double[].class)
		{
			return new OptionIntegerList((int[])object);
		}
		if (objectClass.isArray())
		{
			final int size = Array.getLength(object);
			Integer[] ints = new Integer[size];
			for (int i = 0; i < size; ++i)
			{
				Object element = Array.get(object, i);
				if (element instanceof Number)
				{
					ints[i] = ((Number)element).intValue();
				}
				else
				{
					throw new OptionValidationException("Cannot convert '%s' to double", element);
				}
			}
			return new OptionIntegerList(ints);
		}
		
		throw new OptionValidationException("Cannot convert %s to ints", object);
	}
	
	@Override
	public int[] toPrimitiveArray()
	{
		return Ints.toArray(this);
	}
}
