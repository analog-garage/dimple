package com.analog.lyric.dimple.model;

import java.util.Arrays;
import java.util.HashMap;

public class ArrayDiscreteDomain<T> extends TypedDiscreteDomain<T>
{
	/*-------
	 * State
	 */
	
	private static final long serialVersionUID = 1L;
	
	private final T[] _elements;
	private final HashMap<Object,Integer> _elementToIndex;
	
	/*--------------
	 * Construction
	 */
	
	ArrayDiscreteDomain(T ... elements)
	{
		super(computeHashCode(elements));
		
		_elements = elements;

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
	
	private static int computeHashCode(Object[] elements)
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
	public T getElement(int i)
	{
		return _elements[i];
	}

	@Override
	public final T[] getElements()
	{
		return _elements;
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
