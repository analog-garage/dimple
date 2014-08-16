/*******************************************************************************
*   Copyright 2012-2013 Analog Devices, Inc.
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

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import cern.colt.map.OpenIntObjectHashMap;

import com.google.common.collect.Iterators;

/*
 * MapList is a collection that stores data in both a HashMap and
 * an ArrayList. This preserves order and also allows objects to be indexed by Id.
 */
@NotThreadSafe
public class MapList<T extends IGetId>  implements IMapList<T>
{
	final OpenIntObjectHashMap _hashMap;
	final ArrayList<T> _arrayList;
		
	/*---------------
	 * Construction
	 */
	
	public MapList()
	{
		this(16);
	}
	
	public MapList(int initialCapacity)
	{
		_hashMap = new OpenIntObjectHashMap(initialCapacity);
		_arrayList = new ArrayList<T>(initialCapacity);
	}
	
	public MapList(Iterable<T> vars)
	{
		this();
		for (T v : vars)
			add(v);
	}
	
	/*--------------------
	 * Collection methods
	 */
	
	/**
	 * Adds element to the collection. The same element may be added multiple times and will
	 * appear multiple times when using list-related operations such as {@link #iterator()}
	 * and {@link #getByIndex(int)}.
	 * 
	 * @return true
	 */
	@Override
	@NonNullByDefault(false)
	public boolean add(T node)
	{
		_hashMap.put(node.getId(), node);
		_arrayList.add(node);
		return true;
	}
	
	@Override
	@NonNullByDefault(false)
	public boolean addAll(Collection<? extends T> collection)
	{
		if (collection == this)
		{
			return false;
		}
		
		ensureCapacity(size() + collection.size());
		
		boolean changed = false;
		for (T t : collection)
		{
			if (add(t))
				changed = true;
		}
		return changed;
	}

	@Override
	public void clear()
	{
		_arrayList.clear();
		_hashMap.clear();
	}

	@Override
	public boolean contains(@Nullable Object arg0)
	{
		return (arg0 instanceof IGetId) ? contains((IGetId)arg0) : false ;
	}

	@Override
	public boolean containsAll(@Nullable Collection<?> objects)
	{
		for (Object object : requireNonNull(objects))
		{
			if (!contains(object))
			{
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Visits entries in index order. {@link Iterator#remove()} is not supported.
	 */
	@Override
	public Iterator<T> iterator()
	{
		return Iterators.unmodifiableIterator(_arrayList.iterator());
	}
	
	@Override
	public boolean isEmpty()
	{
		return _arrayList.isEmpty();
	}

	/**
	 * Removes all instances of this object from the collection.
	 * @return false if no instances were found.
	 * @see #removeByIndex(int)
	 */
	@Override
	public boolean remove(@Nullable Object node)
	{
		boolean removed = false;
		
		if (node instanceof IGetId)
		{
			removed = _hashMap.removeKey(((IGetId)node).getId());
			if (removed)
			{
				int nLeft = _arrayList.size() - _hashMap.size();
				if (nLeft <= 1)
				{
					// There can only be one instance, so a simple remove call is sufficient.
					_arrayList.remove(node);
				}
				else
				{
					Iterator<T> arrayIter = _arrayList.iterator();
					while (arrayIter.hasNext())
					{
						if (arrayIter.next() == node)
						{
							arrayIter.remove();
						}
					}
				}
			}
		}
		return removed;
	}

	@Override
	public boolean removeAll(@Nullable Collection<?> elements)
	{
		boolean changed = false;
		for (Object o : requireNonNull(elements))
		{
			if(remove(o))
				changed = true;
		}
		return changed;
	}

	@Override
	public boolean retainAll(@Nullable Collection<?> keep)
	{
		requireNonNull(keep);
		boolean changed = false;
		for (int i = size(); --i >= 0;)
		{
			T value = getByIndex(i);
			if (!keep.contains(value))
			{
				remove(value);
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public int size()
	{
		return _arrayList.size();
		//return _hashMap.size();
	}
	
	@Override
	public Object[] toArray()
	{
		return _arrayList.toArray();
	}

	@Override
	@NonNullByDefault(false)
	public <T2> T2[] toArray(T2[] array)
	{
		return _arrayList.toArray(array);
	}

	/*------------------
	 * IMapList methods
	 */
	
	@Override
	public void addAll(@Nullable T[] nodes)
	{
		if (nodes != null)
		{
			ensureCapacity(size() + nodes.length);
			for (T n : nodes) add(n);
		}
	}

	@Override
	public boolean contains(IGetId node)
	{
		return _hashMap.containsKey(node.getId());
	}
	
	@Override
	public void ensureCapacity(int minCapacity)
	{
		_hashMap.ensureCapacity(minCapacity);
		_arrayList.ensureCapacity(minCapacity);
	}

	@Override
	public @Nullable T getByKey(int id)
	{
		@SuppressWarnings("unchecked")
		T value = (T) _hashMap.get(id);
		return value;
	}
	
	/**
	 * @return element at given {@code index} which depends on the order in which the element
	 * was added to the list.
	 */
	@Override
	public T getByIndex(int index)
	{
		return _arrayList.get(index);
	}
	
	/**
	 * Removes and returns element at given {@code index}.
	 * <p>
	 * This only removes the specified instance. If the element may have been added more
	 * than once, and you want to remove all instances instead use {@link #remove(Object)}.
	 */
	@Override
	public @Nullable T removeByIndex(int index)
	{
		T elt = _arrayList.remove(index);
		if (_hashMap.size() < _arrayList.size() || !_arrayList.contains(elt))
		{
			// If map is smaller than the array, then there can't have been more than
			// one instance of each element so we can skip the contains test.
			_hashMap.removeKey(elt.getId());
		}
		return elt;
	}
	
	@Override
	public List<T> values()
	{
		return _arrayList;
	}
}
