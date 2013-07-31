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
			_elementToIndex.put(elements[i], i);
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
