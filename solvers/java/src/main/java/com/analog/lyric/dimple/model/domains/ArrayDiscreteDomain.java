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

package com.analog.lyric.dimple.model.domains;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import com.analog.lyric.collect.Supers;
import org.eclipse.jdt.annotation.Nullable;

public class ArrayDiscreteDomain<Element> extends TypedDiscreteDomain<Element>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final Element[] _elements;
	private final HashMap<Object,Integer> _elementToIndex;
	private final boolean _hasIntCompatibleValues;
	
	/*--------------
	 * Construction
	 */
	
	@SuppressWarnings("unchecked")
	ArrayDiscreteDomain(Element firstElement, int offset, Object ... moreElements)
	{
		super(computeHashCode(firstElement, moreElements));
		
		Class<?> eltClass = Supers.nearestCommonSuperClass(firstElement, moreElements);
		Object[] elements = (Object[]) Array.newInstance(eltClass,  moreElements.length + 1 - offset);
		elements[0] = firstElement;
		for (int i = moreElements.length; --i>=offset;)
		{
			elements[i+1-offset] = Objects.requireNonNull(moreElements[i]);
		}
		_elements = (Element[])elements;
		
		boolean hasIntCompatibleValues = isIntCompatibleClass(eltClass);

		_elementToIndex = new HashMap<Object,Integer>(elements.length);
		for (int i = 0, end = elements.length; i < end; ++i)
		{
			Object element = elements[i];
			_elementToIndex.put(element, i);
			
			// HACK: In Java, although 0.0 == -0.0, this is not true when wrapped in a
			// Double or Float object!
			if (element instanceof Double)
			{
				Double d = (Double)element;
				if (d == 0.0)
				{
					_elementToIndex.put(Double.valueOf(-d), i);
				}
			}
			else if (element instanceof Float)
			{
				Float f = (Float)element;
				if (f == 0.0)
				{
					_elementToIndex.put(Float.valueOf(-f), i);
				}
			}
		}
		
		if (!hasIntCompatibleValues && Number.class.isAssignableFrom(eltClass))
		{
			Number[] numbers = (Number[])elements;
			hasIntCompatibleValues = true;
			for (Number number : numbers)
			{
				if (!isIntCompatibleValue(number))
				{
					hasIntCompatibleValues = false;
					break;
				}
			}
		}
		
		_hasIntCompatibleValues = hasIntCompatibleValues;
	}
	
	private static int computeHashCode(Object firstElement, Object[] elements)
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(elements);
		return result;
	}

	/*----------------
	 * Domain methods
	 */
	
	@Override
	public final boolean hasIntCompatibleValues()
	{
		return _hasIntCompatibleValues;
	}
	
	@Override
	public boolean inDomain(@Nullable Object value)
	{
		return _elementToIndex.containsKey(value);
	}
	
	@Override
	public boolean isNumber()
	{
		return isNumeric();
	}
	
	@Override
	public boolean isNumeric()
	{
		return Number.class.isAssignableFrom(getElementClass());
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */

	@Override
	public final Class<? extends Element> getElementClass()
	{
		@SuppressWarnings("unchecked")
		Class<? extends Element> elementClass = (Class<? extends Element>) _elements.getClass().getComponentType();
		return elementClass;
	}
	
	@Override
	public Element getElement(int i)
	{
		return _elements[i];
	}

	@Override
	public final Element[] getElements()
	{
		return _elements.clone();
	}

	// Find the list of elements corresponding to the value; return -1 if not a valid value
	@Override
	public int getIndex(@Nullable Object value)
	{
		Integer index = _elementToIndex.get(value);
		return index != null ? index : -1;
	}

	@Override
	public final int size()
	{
		return _elements.length;
	}

}
