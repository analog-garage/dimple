/*******************************************************************************
*   Copyright 2012 Analog Devices Inc.
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
import java.util.HashMap;
import java.util.Iterator;

/*
 * MapList is a collection that stores data in both a HashMap and
 * an ArrayList.  This preserves order and also allows objects to be indexed by Id.
 */
public class MapList<T extends IGetId>  implements Collection<T>
{
	// FIXME: this class is very similar to the standard LinkedHashMap class.
	// We should consider whether to use that instead. Or else in order to
	// avoid boxing integer keys, we might instead want to implement a
	// integer key version of LinkedHashMap using the GNU Trove library.
	
	private HashMap<Integer,T> _hashMap = new HashMap<Integer, T>();
	private ArrayList<T> _arrayList = new ArrayList<T>();
	
	public T getByKey(int id)
	{
		return _hashMap.get(id);
	}
	
	public T getByIndex(int index)
	{
		return _arrayList.get(index);
	}
	
	@Override
	public int size()
	{
		return _hashMap.size();
	}
	
	public Collection<T> values()
	{
		return _arrayList;
	}
	
	public void add(MapList<T> nodes)
	{
		if (nodes != null) for (T n : nodes) add(n);
	}
	public void add(T[] nodes)
	{
		if (nodes != null) for (T n : nodes) add(n);
	}
	public void add(Collection<T> nodes)
	{
		if (nodes != null) for (T n : nodes) add(n);
	}
	public void add(HashMap<Integer,T> nodes)
	{
		if (nodes != null) for (T n : nodes.values()) add(n);
	}

	public boolean contains(IGetId node)
	{
		return _hashMap.containsKey(node.getId());
	}
	
	@Override
	public Iterator<T> iterator()
	{
		//return _hashMap.values().iterator();
		return _arrayList.iterator();
	}
	
	@Override
	public boolean remove(Object node)
	{
		@SuppressWarnings("unchecked")
		T tnode = (T)node;
		_hashMap.remove(tnode.getId());
		return _arrayList.remove(node);
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0)
	{
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
		if (arg0 instanceof IGetId)
		{
			arg0 = ((IGetId)arg0).getId();
		}
		
		return _hashMap.containsKey(arg0);
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		return _arrayList.containsAll(arg0);
	}

	@Override
	public boolean isEmpty()
	{
		return _arrayList.isEmpty();
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
	public boolean retainAll(Collection<?> arg0)
	{
		boolean changed = false;
		for (Object o : _arrayList)
		{
			if (!arg0.contains(o))
			{
				if (remove(o))
					changed = true;
			}
		}

		return changed;

	}

	@Override
	public Object[] toArray()
	{
		return _arrayList.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] arg0)
	{
		return _arrayList.toArray(arg0);
	}

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

}
