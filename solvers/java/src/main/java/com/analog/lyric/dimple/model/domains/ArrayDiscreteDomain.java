package com.analog.lyric.dimple.model.domains;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;

import com.analog.lyric.collect.Supers;

public class ArrayDiscreteDomain<Element> extends TypedDiscreteDomain<Element>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final Element[] _elements;
	private final HashMap<Object,Integer> _elementToIndex;
	
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
			elements[i+1-offset] = moreElements[i];
		}
		_elements = (Element[])elements;

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
	public boolean inDomain(Object value)
	{
		return _elementToIndex.containsKey(value);
	}
	
	/*------------------------
	 * DiscreteDomain methods
	 */

	@Override
	public final Class<? extends Element> getElementClass()
	{
		return (Class<? extends Element>) _elements.getClass().getComponentType();
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
	public int getIndex(Object value)
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
