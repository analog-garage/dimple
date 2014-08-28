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

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.primitives.Doubles;


/**
 * Represents list of doubles for use as option value.
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public class OptionDoubleList extends AbstractOptionValueList<Double>
{
	private static final long serialVersionUID = 1L;

	final public static OptionDoubleList EMPTY = new OptionDoubleList();
	
	/**
	 * @param elements
	 * @since 0.07
	 */
	public OptionDoubleList(Double[] elements)
	{
		super(Double.class, elements);
	}

	public OptionDoubleList(double ... elements)
	{
		super(Doubles.asList(elements).toArray(new Double[elements.length]));
	}
	
	public static OptionDoubleList fromObject(@Nullable Object object)
	{
		if (object == null)
		{
			return EMPTY;
		}
		
		Class<?> objectClass = object.getClass();
		
		if (object instanceof OptionDoubleList)
		{
			return (OptionDoubleList)object;
		}
		if (objectClass == Double[].class)
		{
			return new OptionDoubleList((Double[])object);
		}
		if (objectClass == double[].class)
		{
			return new OptionDoubleList((double[])object);
		}
		if (objectClass.isArray())
		{
			final int size = Array.getLength(object);
			Double[] doubles = new Double[size];
			for (int i = 0; i < size; ++i)
			{
				Object element = Array.get(object, i);
				if (element instanceof Number)
				{
					doubles[i] = ((Number)element).doubleValue();
				}
				else
				{
					throw new OptionValidationException("Cannot convert '%s' to double", element);
				}
			}
			return new OptionDoubleList(doubles);
		}
		
		throw new OptionValidationException("Cannot convert %s to doubles", object);
	}
	
	@Override
	public double[] toPrimitiveArray()
	{
		return Doubles.toArray(this);
	}
}
