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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.RandomAccess;

import net.jcip.annotations.Immutable;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base implementation for list option values.
 * <p>
 * @since 0.07
 * @author Christopher Barber
 */
@Immutable
public abstract class AbstractOptionValueList<T extends Serializable>
	extends AbstractList<T>
	implements IOptionValue, RandomAccess
{
	private static final long serialVersionUID = 1L;

	private final T[] _array;

	/*--------------
	 * Construction
	 */

	/**
	 * Constructs list with given elements.
	 * <p>
	 * @param elementType is the class of the type of the elements (i.e. the concrete instantiation of T)
	 * @param elements are the elements, which will be copied.
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	protected AbstractOptionValueList(Class<T> elementType, T ... elements)
	{
		_array = (T[]) Array.newInstance(elementType, elements.length);
		System.arraycopy(elements, 0, _array, 0, elements.length);
	}
	
	/**
	 * Construct list with given raw element array.
	 * <p>
	 * This constructor does not clone {@code elements}, so the caller should
	 * ensure that it has the correct component type and is not exposed to users.
	 * <p>
	 * @param elements is the exact array that will be used by this class.
	 * @since 0.07
	 */
	protected AbstractOptionValueList(T[] elements)
	{
		_array = elements;
	}
	
	/*----------------
	 * Object methods
	 */
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		final int size = _array.length;
		if (size > 0)
		{
			sb.append(_array[0].toString());
			for (int i = 1; i < size; ++i)
			{
				sb.append(',');
				sb.append(_array[i].toString());
			}
		}
		sb.append('}');
		
		return sb.toString();
	}

	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public T get(int index)
	{
		return _array[index];
	}

	@Override
	public int size()
	{
		return _array.length;
	}
	
	@Override
	public Object[] toArray()
	{
		final int size = _array.length;
		Object[] array = new Object[size];
		System.arraycopy(_array,  0, array, 0 ,size);
		return array;
	}
	
	@SuppressWarnings("unchecked")
	@NonNullByDefault(false)
	@Override
	public <E> E[] toArray(E[] array)
	{
		final int size = _array.length;
		if (array.length != size)
		{
			if (array.length < size)
			{
				array = (E[])Array.newInstance(array.getClass().getComponentType(), size);
			}
			else
			{
				array[size] = null;
			}
		}
		System.arraycopy(_array,  0, array, 0 ,size);
		return array;
	}
	
	/*---------------------------------
	 * AbstractOptionValueList methods
	 */

	/**
	 * The base type of the elements in the list.
	 * @since 0.07
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends T> elementType()
	{
		return (Class<? extends T>) _array.getClass().getComponentType();
	}
	
	/**
	 * Returns array containing values, using primitive element type if possible.
	 * <p>
	 * If {@link #elementType()} is a primitive wrapper type (e.g. {@link Double}),
	 * this will return an array of the corresponding primitive. Otherwise this is the
	 * same as {@link #toArray()}.
	 * @since 0.07
	 */
	public Object toPrimitiveArray()
	{
		return toArray();
	}
}
