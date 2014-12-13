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

package com.analog.lyric.dimple.model.core;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import net.jcip.annotations.NotThreadSafe;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.Iterators;


/**
 * Maintains owned nodes of a single type for a FactorGraph
 * 
 * @since 0.08
 * @author Christopher Barber
 */
@NotThreadSafe
abstract class OwnedArray<T extends Node> extends AbstractCollection<T>
{
	/*-------
	 * State
	 */
	
	private T[] _nodes;
	private int _size;
	private int _end;
	
	/*--------------
	 * Construction
	 */
	
	OwnedArray(T[] nodes)
	{
		_nodes = nodes;
	}

	/*--------------------
	 * Collection methods
	 */

	@NonNullByDefault(false)
	@Override
	public boolean add(T node)
	{
		if (contains(node))
		{
			return false;
		}
		
		int index = allocate();
		_nodes[index] = node;
		node.setId(index|idTypeMask());
		return true;
	}
	
	@NonNullByDefault(false)
	@Override
	public boolean addAll(Collection<? extends T> nodes)
	{
		ensureCapacity(capacity() + nodes.size());
		
		final int prevSize = _size;
		final int typeMask = idTypeMask();
		
		for (T node : nodes)
		{
			if (!contains(node))
			{
				int index = _end;
				_end = index + 1;
				++_size;
				_nodes[index] = node;
				node.setId(index|typeMask);
			}
		}
		
		return _size != prevSize;
	}
	
	@Override
	public void clear()
	{
		Arrays.fill(_nodes, null);
		_size = _end = 0;
	}
	
	@Override
	public boolean contains(@Nullable Object obj)
	{
		if (obj instanceof Node)
		{
			return contains((Node)obj);
		}
		
		return false;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		if (_size == 0)
		{
			return Iterators.emptyIterator();
		}

		return new Iterator<T>() {

			private int _next = 0;
			private int _prev = -1;
				
			@Override
			public boolean hasNext()
			{
				for (; _next < _end; ++_next)
				{
					if (_nodes[_next] != null)
					{
						return true;
					}
				}
				
				return false;
			}

			@Override
			public @Nullable T next()
			{
				for (; _next < _end; ++_next)
				{
					final T node = _nodes[_next];
					if (node != null)
					{
						++_next;
						_prev = _next;
						return node;
					}
				}
				
				return null;
			}

			@Override
			public void remove()
			{
				if (_prev >= 0)
				{
					_nodes[_prev] = null;
					--_size;
					_prev = -1;
				}
			}
		};
	}
	
	@Override
	public boolean remove(@Nullable Object obj)
	{
		if (obj instanceof Node)
		{
			return remove((Node)obj);
		}
		
		return false;
	}
	
	@Override
	public int size()
	{
		return _size;
	}
	
	/*--------------------
	 * OwnedArray methods
	 */
	
	int capacity()
	{
		return _nodes.length;
	}
	
	void capacity(int capacity)
	{
		if (capacity >= _end)
		{
			_nodes = resize(_nodes, capacity);
		}
	}
	
	public boolean contains(Node node)
	{
		return node == getByKey(node.getLocalId());
	}
	
	void ensureCapacity(int capacity)
	{
	}
	
	@Nullable T get(int index)
	{
		return _nodes[index];
	}
	
	/**
	 * Return's nth node in array (skipping null entries).
	 */
	T getByIndex(int n)
	{
		if (n >= 0 && n < _size)
		{
			int nNulls = _end - _size;
			if (nNulls == 0)
			{
				return _nodes[n];
			}

			for (T node : _nodes)
			{
				if (node != null && --n < 0)
				{
					return node;
				}
			}
		}
		
		throw new IndexOutOfBoundsException();
	}
	
	@Nullable T getByKey(int localId)
	{
		int index = NodeId.indexFromLocalId(localId);
		return index < _end ? _nodes[index] : null;
	}
	
	boolean remove(Node node)
	{
		final int id = node.getLocalId();
		final int index = NodeId.indexFromLocalId(id);
		if (index < _end && _nodes[index] == node)
		{
			_nodes[index] = null;
			--_size;
			return true;
		}
		return false;
	}
	
	/*------------------
	 * Abstract methods
	 */
	
	abstract int idTypeMask();
	abstract T[] resize(T[] array, int length);
	
	/*-----------------
	 * Private methods
	 */
	
	/**
	 * Allocates a slot for node and returns its index.
	 */
	private int allocate()
	{
		++_size;
		int index = _end++;
		if (index >= _nodes.length)
		{
			_nodes = resize(_nodes, _nodes.length * 2);
		}
		return index;
	}
}
