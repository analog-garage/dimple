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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Iterators;

import cern.colt.map.OpenLongObjectHashMap;
import net.jcip.annotations.NotThreadSafe;

/*
 * MapList is a collection that stores data in both a HashMap and
 * an ArrayList. This preserves order and also allows objects to be indexed by Id.
 */
@NotThreadSafe
public class MapList<T extends IGetId>  implements IMapList<T>
{
	private @Nullable Iterable<T> _iterable;
	private @Nullable OpenLongObjectHashMap _hashMap;
	private @Nullable ArrayList<T> _arrayList;
		
	/*---------------
	 * Construction
	 */
	
	public MapList()
	{
		this(16);
	}
	
	public MapList(int initialCapacity)
	{
		_hashMap = new OpenLongObjectHashMap(initialCapacity);
		_arrayList = new ArrayList<T>(initialCapacity);
	}
	
	/**
	 * Construct lazily from an iterable.
	 * <p>
	 * The contents will be read from the {@code iterable} the first time a method other
	 * than {@link #iterator} is invoked. This is to avoid copying many objects in the common
	 * situation in which the list is only used to iterate over its contents.
	 * <p>
	 * @param iterable
	 * @since 0.08
	 */
	public MapList(Iterable<T> iterable)
	{
		_iterable = iterable;
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
		hashMap().put(node.getGlobalId(), node);
		arrayList().add(node);
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
		_iterable = null;
		final ArrayList<T> arrayList = _arrayList;
		if (arrayList != null)
		{
			arrayList.clear();
		}
		final OpenLongObjectHashMap hashMap = _hashMap;
		if (hashMap != null)
		{
			hashMap.clear();
		}
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
	 * <p>
	 * If this collection was constructed using the {@link #MapList(Iterable)} constructor
	 * and no other method has been called, this will simply use the iterable to create
	 * an iterator.
	 */
	@Override
	public Iterator<T> iterator()
	{
		Iterable<T> iterable = _iterable;
		if (iterable == null)
		{
			iterable = requireNonNull(_arrayList);
		}
		return Iterators.unmodifiableIterator(iterable.iterator());
	}
	
	@Override
	public boolean isEmpty()
	{
		return arrayList().isEmpty();
	}

	/**
	 * Removes all instances of this object from the collection.
	 * @return false if no instances were found.
	 * @see #removeByIndex(int)
	 */
	@Override
	public boolean remove(@Nullable Object obj)
	{
		boolean removed = false;
		
		if (obj instanceof IGetId)
		{
			final IGetId node = (IGetId)obj;
			OpenLongObjectHashMap hashMap = hashMap();
			
			removed = hashMap.removeKey(node.getGlobalId());
			if (removed)
			{
				final ArrayList<T> arrayList = arrayList();
				int nLeft = arrayList.size() - hashMap.size();
				if (nLeft <= 1)
				{
					// There can only be one instance, so a simple remove call is sufficient.
					arrayList.remove(node);
				}
				else
				{
					Iterator<T> arrayIter = arrayList.iterator();
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
		return arrayList().size();
	}
	
	@Override
	public Object[] toArray()
	{
		return arrayList().toArray();
	}

	@Override
	@NonNullByDefault(false)
	public <T2> T2[] toArray(T2[] array)
	{
		return arrayList().toArray(array);
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
		return hashMap().containsKey(node.getGlobalId());
	}
	
	@Override
	public void ensureCapacity(int minCapacity)
	{
		hashMap().ensureCapacity(minCapacity);
		arrayList().ensureCapacity(minCapacity);
	}

	@Override
	public @Nullable T getByKey(long id)
	{
		@SuppressWarnings("unchecked")
		T value = (T) hashMap().get(id);
		return value;
	}
	
	/**
	 * @return element at given {@code index} which depends on the order in which the element
	 * was added to the list.
	 */
	@NonNull // FIXME - workaround for Eclipse JDT bug (467610?)
	@Override
	public T getByIndex(int index)
	{
		return arrayList().get(index);
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
		final ArrayList<T> arrayList = arrayList();
		final T elt = arrayList.remove(index);
		final OpenLongObjectHashMap hashMap = hashMap();
		if (hashMap.size() < arrayList.size() || !arrayList.contains(elt))
		{
			// If map is smaller than the array, then there can't have been more than
			// one instance of each element so we can skip the contains test.
			hashMap.removeKey(elt.getGlobalId());
		}
		return elt;
	}
	
	@Override
	public List<T> values()
	{
		return arrayList();
	}
	
	/*-----------------
	 * Private methods
	 */
	
	private ArrayList<T> arrayList()
	{
		if (_arrayList == null)
		{
			load();
		}
		return requireNonNull(_arrayList);
	}
	
	private OpenLongObjectHashMap hashMap()
	{
		if (_hashMap == null)
		{
			load();
		}
		return requireNonNull(_hashMap);
	}
	
	private void load()
	{
		final ArrayList<T> arrayList = _arrayList = new ArrayList<>();
		final OpenLongObjectHashMap hashMap = _hashMap = new OpenLongObjectHashMap();
		Iterable<T> iterable = _iterable;
		if (iterable != null)
		{
			for (T node : iterable)
			{
				arrayList.add(node);
				hashMap.put(node.getGlobalId(), node);
			}
			_iterable = null;
		}
	}
}
