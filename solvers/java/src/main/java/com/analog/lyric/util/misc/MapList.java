/*******************************************************************************
*   Copyright 2012 Analog Devices, Inc.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import cern.colt.map.OpenIntObjectHashMap;

/*
 * MapList is a collection that stores data in both a HashMap and
 * an ArrayList.  This preserves order and also allows objects to be indexed by Id.
 */
@NotThreadSafe
public class MapList<T extends IGetId>  implements IMapList<T>
{
	private final OpenIntObjectHashMap _hashMap;
	private final ArrayList<T> _arrayList;
		
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
	
	@Override
	public boolean add(T node)
	{
		if (!_hashMap.containsKey(node.getId()))
		{
			_hashMap.put(node.getId(), node);
			_arrayList.add(node);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> arg0)
	{
		ensureCapacity(size() + arg0.size());
		
		boolean changed = false;
		for (T t : arg0)
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
	public boolean contains(Object arg0)
	{
		return (arg0 instanceof IGetId) ? contains((IGetId)arg0) : false ;
	}

	@Override
	public boolean containsAll(Collection<?> objects)
	{
		for (Object object : objects)
		{
			if (!contains(object))
			{
				return false;
			}
		}
		
		return true;
	}

	@Override
	public Iterator<T> iterator()
	{
		return _arrayList.iterator();
	}
	
	@Override
	public boolean isEmpty()
	{
		return _arrayList.isEmpty();
	}

	@Override
	public boolean remove(Object node)
	{
		boolean removed = false;
		
		if (node instanceof IGetId)
		{
			removed = _hashMap.removeKey(((IGetId)node).getId());
			if (removed)
			{
				_arrayList.remove(node);
			}
		}
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		boolean changed = false;
		for (Object o : arg0)
		{
			if(remove(o))
				changed = true;
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> keep)
	{
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
	public <T2> T2[] toArray(T2[] array)
	{
		return _arrayList.toArray(array);
	}

	/*------------------
	 * IMapList methods
	 */
	
	@Override
	public void addAll(T[] nodes)
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
	public T getByKey(int id)
	{
		return (T) _hashMap.get(id);
	}
	
	@Override
	public T getByIndex(int index)
	{
		return _arrayList.get(index);
	}
	
	@Override
	public List<T> values()
	{
		return _arrayList;
	}
	
}
