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

package com.analog.lyric.collect;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.RandomAccess;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A random access list with unique elements and fast lookup.
 * <p>
 * This maintains an array of values and a map of each value to its
 * index in the array.
 * <p>
 * @since 0.08
 * @author Christopher Barber
 */
@NonNullByDefault(false)
public class IndexedArrayList<T> extends AbstractList<T> implements RandomAccess, Serializable
{
	private static final long serialVersionUID = 1L;
	private final ArrayList<T> _array;
	private final HashMap<T,Integer> _valueToIndex;
	
	/*--------------
	 * Construction
	 */
	
	public IndexedArrayList(int capacity)
	{
		_array = new ArrayList<>(capacity);
		_valueToIndex = new HashMap<>(capacity);
	}
	
	public IndexedArrayList()
	{
		this(16);
	}
	
	public IndexedArrayList(Collection<T> collection)
	{
		this(collection.size());
		addAll(collection);
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	@Override
	public boolean add(T var)
	{
		if (_valueToIndex.containsKey(var))
		{
			return false;
		}
		
		_valueToIndex.put(var, _array.size());
		_array.add(var);
		
		return true;
	}
	
	@Override
	public void clear()
	{
		_array.clear();
		_valueToIndex.clear();
	}
	
	@Override
	public boolean contains(Object obj)
	{
		return _valueToIndex.containsKey(obj);
	}
	
	@Override
	public boolean remove(Object obj)
	{
		Integer removedIndex = _valueToIndex.remove(obj);
		if (removedIndex == null)
		{
			return false;
		}
		
		int i = removedIndex;
		_array.remove(i);
		recomputeValueToIndex(removedIndex);
		
		return true;
	}
	
	@Override
	public T remove(int index)
	{
		T var = _array.remove(index);
		_valueToIndex.remove(var);
		
		recomputeValueToIndex(index);
		
		return var;
	}
	
	@Override
	public boolean removeAll(Collection<?> collection)
	{
		if (_array.removeAll(collection))
		{
			_valueToIndex.clear();
			recomputeValueToIndex(0);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean retainAll(Collection<?> collection)
	{
		if (_array.retainAll(collection))
		{
			_valueToIndex.clear();
			recomputeValueToIndex(0);
			return true;
		}
		
		return false;
	}

	@Override
	public int size()
	{
		return _array.size();
	}
	
	/*--------------
	 * List methods
	 */
	
	@Override
	public T get(int index)
	{
		return _array.get(index);
	}
	
	@Override
	public int indexOf(Object obj)
	{
		Integer index = _valueToIndex.get(obj);
		return index != null ? index : -1;
	}
	
	@Override
	public int lastIndexOf(Object obj)
	{
		return indexOf(obj);
	}
	
	@Override
	public T set(int index, T newValue)
	{
		T oldValue = _array.set(index, newValue);
	
		if (!oldValue.equals(newValue))
		{
			_valueToIndex.remove(oldValue);
			Integer oldIndex = _valueToIndex.put(newValue, index);
			if (oldIndex != null)
			{
				// Value was already in array in a different position.
				int i = oldIndex;
				remove(i);
			}
		}
		
		return oldValue;
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private void recomputeValueToIndex(int minIndex)
	{
		for (int i = minIndex, size = _array.size(); i < size; ++i)
		{
			_valueToIndex.put(_array.get(i), i);
		}
	}
	
}
